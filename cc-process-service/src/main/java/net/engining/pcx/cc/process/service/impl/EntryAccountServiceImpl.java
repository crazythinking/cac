package net.engining.pcx.cc.process.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlVolDtl;
import net.engining.pcx.cc.infrastructure.shared.model.ApInternalGltxn;
import net.engining.pcx.cc.infrastructure.shared.model.QApGlBal;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.param.model.enums.DbCrInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pcx.cc.process.service.common.AccountingBean;
import net.engining.pcx.cc.process.service.common.AcctingRecord;
import net.engining.pcx.cc.process.service.common.GlCalculator;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.OrganizationContextHolder;
import net.engining.pg.parameter.ParameterFacility;

@Service
public class EntryAccountServiceImpl {
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@Autowired
	private ParameterFacility parameterCacheFacility;
	@Autowired
	private Provider7x24 provider7x24;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private GlCalculator glCalculator;
	
	/**
	 * 记会计分录
	 * @param txnBalance
	 * @param currency
	 * @param dbsubject
	 * @param crsubject
	 * @param account
	 * @param txndesc
	 * @param branch
	 * @param stDbRedFlag
	 * @param txnDetailSeq
	 * @param txnDetailType
	 * @param postDate
	 */
	public void recAccounting(BigDecimal txnBalance, String currency, String dbsubject, String crsubject, String txndesc, String branch, RedBlueInd stDbRedFlag, String txnDetailSeq, TxnDetailType txnDetailType, Date postDate){

		ApInternalGltxn apInternalGltxn = new ApInternalGltxn();
		apInternalGltxn.setPostAmount(txnBalance);
		apInternalGltxn.setCurrCd(currency);
		apInternalGltxn.setDbsubjectCd(dbsubject);
		apInternalGltxn.setCrsubjectCd(crsubject);
		apInternalGltxn.setTxnDesc(txndesc);
		apInternalGltxn.setBranchNo(branch);
		apInternalGltxn.setRedBlueInd(stDbRedFlag);
		apInternalGltxn.setTxnDate(postDate);
		apInternalGltxn.setTxnDetailSeq(txnDetailSeq);
		apInternalGltxn.setTxnDetailType(txnDetailType);
		apInternalGltxn.setBizDate(provider7x24.getCurrentDate().toDate());
		apInternalGltxn.fillDefaultValues();
		em.persist(apInternalGltxn);
		
	}
	
	/**
	 * 会计科目不应该被实时处理，
	 * 尤其是7*24处理（日终批量日切后到SystemStatusType.B改为SystemStatusType.N这段时间内），
	 * 如果此时调用这个方法，会把T+1的会计科目流水算到T日。
	 * 应该用recAccounting方法替代 。
	 */
	@Deprecated
	public void accountingProcess(List<AccountingBean> list ){
		
		
		//遍历list
		for(AccountingBean bean:list){
			List<AcctingRecord> acctingRecords = new ArrayList<AcctingRecord>();
			ApGlVolDtl agvd = new ApGlVolDtl();
			agvd.setCurrCd(bean.getCurrency());
			if(bean.getDbsubject()!=null&&bean.getCrsubject()!=null){
				agvd.setDbsubjectCd(bean.getDbsubject());
				agvd.setCrsubjectCd(bean.getCrsubject());
			}
			else if(bean.getDbsubject()!=null)
				agvd.setDbsubjectCd(bean.getDbsubject());
			else if(bean.getCrsubject()!=null)
				agvd.setCrsubjectCd(bean.getCrsubject());
				
			agvd.setVolDt(bean.getTxnDate());
			agvd.setRedBlueInd(bean.getStDbRedFlag());
			if(bean.getStDbRedFlag()==RedBlueInd.R)//红字
				agvd.setSubjAmount(bean.getAmount().negate());
			else
				agvd.setSubjAmount(bean.getAmount());
			agvd.setVolDesc(bean.getTxndesc());
			agvd.setBranch(bean.getBranch());
			agvd.setBizDate(provider7x24.getCurrentDate().toDate());
			agvd.fillDefaultValues();
			em.persist(agvd);
			
			if(bean.getDbsubject()!=null)
				acctingRecords.add(new AcctingRecord(parameterCacheFacility.getParameter(Subject.class, bean.getDbsubject()), DbCrInd.D, bean.getStDbRedFlag()));
			if(bean.getCrsubject()!=null)
				acctingRecords.add(new AcctingRecord(parameterCacheFacility.getParameter(Subject.class, bean.getCrsubject()), DbCrInd.C, bean.getStDbRedFlag()));
			
			for (AcctingRecord r : acctingRecords) {
				
				if(r.getSubject()==null)
					continue;
				
				//FIXME 用em.find
				QApGlBal qApGlBal = QApGlBal.apGlBal ;
				ApGlBal apGlBal = new JPAQueryFactory(em)
						.select(qApGlBal)
						.from(qApGlBal)
						.where(
								qApGlBal.branchNo.eq(OrganizationContextHolder.getCurrentOrganizationId())
								.and(qApGlBal.subjectCd.eq(r.getSubject().subjectCd)))
						.fetchOne();
				
				if (apGlBal == null)
					throw new RuntimeException("科目初始化错误, " + "branchNo:" + OrganizationContextHolder.getCurrentOrganizationId() +  ", titleCd:" + r.getSubject().subjectCd);
				
				switch (r.getRedBlueInd()) {
				case R: // 红字(撤销交易)
					glCalculator.writeOff(apGlBal, r, bean.getAmount());
					break;
				case N: // （正常交易）
					glCalculator.writeOn(apGlBal, r, bean.getAmount());
					break;
				case B: // 蓝字（正常交易）
					glCalculator.writeOn(apGlBal, r, bean.getAmount());
					break;
				default:
					throw new RuntimeException("枚举未定义");
				}
			}
			
		}
		
	}

}
