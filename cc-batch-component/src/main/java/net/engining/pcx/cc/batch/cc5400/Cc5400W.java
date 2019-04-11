package net.engining.pcx.cc.batch.cc5400;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.*;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.param.model.TxnSubjectMapping;
import net.engining.pcx.cc.param.model.TxnSubjectParam;
import net.engining.pcx.cc.param.model.enums.DbCrInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pcx.cc.process.service.common.AcctingRecord;
import net.engining.pcx.cc.process.service.common.GlCalculator;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.OrganizationContextHolder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cc5400W implements ItemWriter<ApGlTxn> {

	@Autowired
	private ParameterFacility parameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private GlCalculator glCalculator;

	@Override
	public void write(List<? extends ApGlTxn> items) throws Exception {
		//拆分交易，生成临时会计分录对象列表
		// 根据记录交易码和账龄获取套型，包括如下字段：科目号、借贷方向、科目发生额、账号、所在网点号
		// 根据交易码和账龄获取TxnGlt对象

		//用来记录发生额的表格。这里使用ApGlBal是为了复用glCalculator
		//    branch  subject
		Table<String, String, ApGlBal> table = HashBasedTable.create();
		OrganizationContextHolder.setCurrentOrganizationId(ParameterFacility.GLOBAL_ORGANIZATION_ID);
		
		for (ApGlTxn item : items)
		{
			CactAccount cact = em.find(CactAccount.class, item.getAcctSeq());//获取business type
			BusinessType businesstype = cact.getBusinessType();
			TxnSubjectParam txnSubjectParam = parameterFacility.getParameter(TxnSubjectParam.class, TxnSubjectParam.key(item.getPostCode(), item.getAgeGroupCd(), businesstype));
			
			if (txnSubjectParam == null)
			{
				// 没配参数则不处理
				continue;
			}
			
			List<AcctingRecord> acctingRecords = new ArrayList<AcctingRecord>();
			int volSeq = 0;
			for(TxnSubjectMapping txnSubjectMapping : txnSubjectParam.entryList)
			{
				ApGlVolDtl agvd = new ApGlVolDtl();
				//插入分录流水表
				agvd.setTxnDetailSeq(item.getGltSeq());
				agvd.setTxnDetailType(TxnDetailType.C);
				agvd.setVolDt(item.getPostDate());
				agvd.setBranch(item.getOwingBranch());
				//agvd.setTxnBrcd(item.getAcqBranch());
				agvd.setCurrCd(item.getCurrCd());
				agvd.setSubjAmount(item.getPostAmount());//金额

				if(txnSubjectMapping.ntDbSubjectCd != null || txnSubjectMapping.ntCrSubjectCd != null)
				{
					//借贷
					agvd.setDbsubjectCd(txnSubjectMapping.ntDbSubjectCd);//借方科目
					agvd.setCrsubjectCd(txnSubjectMapping.ntCrSubjectCd);//贷方科目
					if(txnSubjectMapping.ntDbRedFlag == RedBlueInd.R && txnSubjectMapping.ntCrRedFlag == RedBlueInd.R)
					{//红字
						agvd.setSubjAmount(item.getPostAmount().negate());//金额取相反数
					}
					else
					{
						agvd.setSubjAmount(item.getPostAmount());//金额
					}
				}
				else
				{
					//收付
					if(txnSubjectMapping.ntDbSubjectCdOs != null)
					{
						agvd.setDbsubjectCd(txnSubjectMapping.ntDbSubjectCdOs);
						agvd.setCrsubjectCd("");
						if(txnSubjectMapping.ntDbRedFlagOs == RedBlueInd.R)
						{
							//红字
							agvd.setSubjAmount(item.getPostAmount().negate());//金额取相反数
						}
						else
						{
							agvd.setSubjAmount(item.getPostAmount());//金额
						}
					}
					else if(txnSubjectMapping.ntCrSubjectCdOs != null)
					{
						agvd.setCrsubjectCd(txnSubjectMapping.ntCrSubjectCdOs);
						agvd.setDbsubjectCd("");
						if(txnSubjectMapping.ntCrRedFlagOs == RedBlueInd.R)
						{
							//红字
							agvd.setSubjAmount(item.getPostAmount().negate());//金额取相反数
						}
						else
						{
							agvd.setSubjAmount(item.getPostAmount());//金额
						}
					}
					else
					{
						throw new RuntimeException("不正确的参数配置TxnSubjectMapping, 位于Subject参数key:" + TxnSubjectParam.key(item.getPostCode(), item.getAgeGroupCd(), businesstype));
					}
				}
				agvd.setVolSeq(volSeq);
				em.persist(agvd);
				volSeq++;
	 
			/*
			 * 入总账标志 POST_GL_IND CHAR N|normal 正常入账交易S|spend 挂账交易W|write off 核销交易
			 */
				switch (item.getPostGlInd()) {
				case Normal: // normal 正常入账交易
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntDbSubjectCd, DbCrInd.D, txnSubjectMapping.ntDbRedFlag);
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntCrSubjectCd, DbCrInd.C, txnSubjectMapping.ntCrRedFlag);
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntDbSubjectCdOs, DbCrInd.D, txnSubjectMapping.ntDbRedFlagOs);
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntCrSubjectCdOs, DbCrInd.C, txnSubjectMapping.ntCrRedFlagOs);
					break;
				case Suspend: // Suspend 挂账交易
					addAcctingRecord(acctingRecords, txnSubjectMapping.stDbSubjectCd, DbCrInd.D, txnSubjectMapping.stDbRedFlag);
					addAcctingRecord(acctingRecords, txnSubjectMapping.stCrSubjectCd, DbCrInd.C, txnSubjectMapping.stCrRedFlag);
					break;
				case Writeoff: // write off 核销交易
					addAcctingRecord(acctingRecords, txnSubjectMapping.woDbSubjectCd, DbCrInd.D, txnSubjectMapping.woDbRedFlag);
					addAcctingRecord(acctingRecords, txnSubjectMapping.woCrSubjectCd, DbCrInd.C, txnSubjectMapping.woCrRedFlag);
					
					// 正常入账交易
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntDbSubjectCd, DbCrInd.D, txnSubjectMapping.ntDbRedFlag);
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntCrSubjectCd, DbCrInd.C, txnSubjectMapping.ntCrRedFlag);
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntDbSubjectCdOs, DbCrInd.D, txnSubjectMapping.ntDbRedFlagOs);
					addAcctingRecord(acctingRecords, txnSubjectMapping.ntCrSubjectCdOs, DbCrInd.C, txnSubjectMapping.ntCrRedFlagOs);
					
					break;
				default:
					throw new IllegalArgumentException("PostGlIndicator枚举未定义, " + item.getPostGlInd());
				}
			}
			
			for (AcctingRecord r : acctingRecords)
			{
				if(r.getSubject()==null)
					continue;

				ApGlBal apGlBal = table.get(item.getOwingBranch(), r.getSubject().subjectCd);
				if (apGlBal == null)
				{
					//初始化一个发生额对象（仅仅为了复用glCalculator）
					apGlBal = new ApGlBal();
					apGlBal.setCrAmt(BigDecimal.ZERO);
					apGlBal.setCrBal(BigDecimal.ZERO);
					apGlBal.setCrCount(0);
					apGlBal.setDbAmt(BigDecimal.ZERO);
					apGlBal.setDbBal(BigDecimal.ZERO);
					apGlBal.setDbCount(0);
					table.put(item.getOwingBranch(), r.getSubject().subjectCd, apGlBal);
				}				
				
				/*
				 * 分录录入总账 根据红蓝字标志决定是冲销还是正常录入
				 */
				switch (r.getRedBlueInd()) {
				case R: // 红字(撤销交易)
					glCalculator.writeOff(apGlBal, r, item.getPostAmount());
					break;
				case N: // （正常交易）
					glCalculator.writeOn(apGlBal, r, item.getPostAmount());
					break;
				case B: // 蓝字（正常交易）
					glCalculator.writeOn(apGlBal, r, item.getPostAmount());
					break;
				default:
					throw new RuntimeException("枚举未定义：" + r.getRedBlueInd());
				}
				
			}
		}
		
		//最后把汇总信息进summary表
		for (String branchId : table.rowKeySet())
		{
			for (String subjectCd : table.row(branchId).keySet())
			{
				ApGlBal agb = table.get(branchId, subjectCd);
				ApSubjectSummary summary = new ApSubjectSummary();
				summary.setOwingBranch(branchId);
				summary.setSubjectCd(subjectCd);
				summary.setCrAmt(agb.getCrAmt());
				summary.setDbAmt(agb.getDbAmt());
				summary.setCrBal(agb.getCrBal());
				summary.setDbBal(agb.getDbBal());
				summary.setCrCount(agb.getCrCount());
				summary.setDbCount(agb.getDbCount());

				em.persist(summary);
			}
		}
	}
	
	private void addAcctingRecord(List<AcctingRecord> acctingRecords, String subjectCd, DbCrInd dbCrInd, RedBlueInd redBlueInd){
		if (subjectCd != null){
			acctingRecords.add(new AcctingRecord(parameterFacility.getParameter(Subject.class, subjectCd), dbCrInd, redBlueInd));
		}
	}
}
