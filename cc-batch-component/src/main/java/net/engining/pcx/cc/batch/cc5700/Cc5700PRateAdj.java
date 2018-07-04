package net.engining.pcx.cc.batch.cc5700;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.service.PaymentPlanService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.param.CcParamComparatorService;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.entity.model.ParameterSeqence;

/**
 * 利率调整
 * @author tuyi
 *
 */
@Service
@Scope("step")
public class Cc5700PRateAdj implements ItemProcessor<CactAccount, CactAccount> {
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@PersistenceContext
	protected EntityManager em;
	
	@Autowired
	private CcParamComparatorService comparatorService;
	
	@Autowired
	private PaymentPlanService paymentPlanService;
	
	@Autowired
	ParameterFacility parameterFacility;
	
	@Override
	public CactAccount process(CactAccount cactAccount) throws Exception {
		//取账户参数
		Account account = newComputeService.retrieveAccount(cactAccount);
		//取未到期本金子账户利率参数
		SubAcct subAcctParam = newComputeService.retrieveSubAcct(account.subAcctParam.get("LOAN"), cactAccount);
		//取当前的利率参数
		InterestTable inttable =  newComputeService.retrieveInterestTable(subAcctParam.intTables.get(0), cactAccount);
		BigDecimal newRate = new BigDecimal(0.085); //变更后的利率
		InterestTable newIntTable = newComputeService.setupIntTable(inttable , newRate);
		newIntTable= comparatorService.jdugeNewInterestTable(newIntTable); //新的利率代码维护
		
		//未到期本金子账户的维护
		SubAcct newSubAcct =newComputeService.setupSubAcct(subAcctParam , newIntTable.interestCode );
		newSubAcct = comparatorService.jdugeNewSubAcct(newSubAcct) ;
		
		
		//子账户维护
		String AcctParamId= newSubAcct.subAcctId;
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> cactSubAcctList = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(cactAccount.getAcctSeq()),
						qCactSubAcct.subAcctType.eq("LOAN")
						)
				.fetch();
		
		int subAcctId= cactSubAcctList.get(0).getSubAcctId();
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, subAcctId);
		cactSubAcct.setSubacctParamId(AcctParamId); //子账户参数更新
		
		ParameterSeqence pgParamSeqence = em.find(ParameterSeqence.class, Account.class.getCanonicalName());
		pgParamSeqence.setParamSeq(pgParamSeqence.getParamSeq() + 1);
		String parameter = "ACC" + StringUtils.leftPad(pgParamSeqence.getParamSeq().toString(), 10, "0");
		Account newone = newComputeService.setupAcct(account, AcctParamId);
		newone.paramId = parameter;
		parameterFacility.addParameter(parameter, newone);

		cactAccount.setAcctParamId(parameter);
		
		
		//还款计划更新,暂时满足中民项目
		PaymentPlan paymentPlan = paymentPlanService.regPaymentPlan(cactAccount.getTotalLoanPeriod(), account.intUnit, account.intUnitMult, account.paymentMethod,
				cactAccount.getTotalLoanPrincipalAmt(),newIntTable , account.loanFeeMethod, account.loanFeeCalcMethod, account.feeAmount, account.feeRate, 
				cactAccount.getSetupDate(), account.pmtDueDays, account.intFirstPeriodAdj );
		paymentPlanService.updatePaymentPlan( cactAccount.getAcctSeq() ,cactAccount.getCustId(),cactAccount.getAcctParamId(),
				paymentPlan,new Date()	);
		
		return cactAccount;
	}
	
	
}