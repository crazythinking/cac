package net.engining.pcx.cc.batch.cc5402;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlVolDtl;
import net.engining.pcx.cc.infrastructure.shared.model.ApInternalGltxn;
import net.engining.pcx.cc.infrastructure.shared.model.ApInternalSubjectSum;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.param.model.enums.DbCrInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pcx.cc.process.service.common.AcctingRecord;
import net.engining.pcx.cc.process.service.common.GlCalculator;
import net.engining.pg.parameter.OrganizationContextHolder;
import net.engining.pg.parameter.ParameterFacility;

public class Cc5402W implements ItemWriter<ApInternalGltxn> {

	@Autowired
	private ParameterFacility parameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private GlCalculator glCalculator;

	@Override
	public void write(List<? extends ApInternalGltxn> items) throws Exception {

		//用来记录发生额的表格。这里使用ApGlBal是为了复用glCalculator
		//    branch  subject
		Table<String, String, ApGlBal> table = HashBasedTable.create();
		OrganizationContextHolder.setCurrentOrganizationId(ParameterFacility.GLOBAL_ORGANIZATION_ID);
		
		for (ApInternalGltxn txn : items)
		{
			List<AcctingRecord> acctingRecords = new ArrayList<AcctingRecord>();
			ApGlVolDtl agvd = new ApGlVolDtl();
			agvd.setCurrCd(txn.getCurrCd());
			if(txn.getDbsubjectCd()!=null&&txn.getCrsubjectCd()!=null){
				agvd.setDbsubjectCd(txn.getDbsubjectCd());
				agvd.setCrsubjectCd(txn.getCrsubjectCd());
			}
			else if(txn.getDbsubjectCd()!=null)
				agvd.setDbsubjectCd(txn.getDbsubjectCd());
			else if(txn.getCrsubjectCd()!=null)
				agvd.setCrsubjectCd(txn.getCrsubjectCd());
				
			agvd.setVolDt(txn.getTxnDate());
			agvd.setRedBlueInd(txn.getRedBlueInd());
			if(txn.getRedBlueInd()==RedBlueInd.R)//红字
				agvd.setSubjAmount(txn.getPostAmount().negate());
			else
				agvd.setSubjAmount(txn.getPostAmount());
			agvd.setVolDesc(txn.getTxnDesc());
			agvd.setBranch(txn.getBranchNo());
			agvd.setTxnDetailSeq(txn.getTxnDetailSeq());
			agvd.setTxnDetailType(txn.getTxnDetailType());
			
			em.persist(agvd);
			
			if(txn.getDbsubjectCd()!=null)
				acctingRecords.add(new AcctingRecord(parameterFacility.getParameter(Subject.class, txn.getDbsubjectCd()), DbCrInd.D, txn.getRedBlueInd()));
			if(txn.getCrsubjectCd()!=null)
				acctingRecords.add(new AcctingRecord(parameterFacility.getParameter(Subject.class, txn.getCrsubjectCd()), DbCrInd.C, txn.getRedBlueInd()));
			
			for (AcctingRecord r : acctingRecords) {
				
				if(r.getSubject()==null)
					continue;
				
				ApGlBal apGlBal = table.get(txn.getBranchNo(), r.getSubject().subjectCd);
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
					table.put(txn.getBranchNo(), r.getSubject().subjectCd, apGlBal);
				}				
				
				switch (r.getRedBlueInd()) {
				case R: // 红字(撤销交易)
					glCalculator.writeOff(apGlBal, r, txn.getPostAmount());
					break;
				case N: // （正常交易）
					glCalculator.writeOn(apGlBal, r, txn.getPostAmount());
					break;
				case B: // 蓝字（正常交易）
					glCalculator.writeOn(apGlBal, r, txn.getPostAmount());
					break;
				default:
					throw new RuntimeException("枚举未定义");
				}
			}
		}
		
		//最后把汇总信息进summary表
		for (String branchId : table.rowKeySet())
		{
			for (String subjectCd : table.row(branchId).keySet())
			{
				ApGlBal agb = table.get(branchId, subjectCd);
				ApInternalSubjectSum summary = new ApInternalSubjectSum();
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
	
}
