/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.service.PaymentPlanService;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewPostService;
import net.engining.pcx.cc.process.service.impl.InternalAccountService;
import net.engining.pcx.cc.process.service.ledger.NewLedgerService;
import net.engining.pcx.cc.process.service.support.LoanTransformEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;


/**
 * 本金、利息宽限日当日处理利息转逾期结转；每日判断是否转非
 *
 */
@Service
@Scope("step")
public class Cc1800P366LoanStmtCarry implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {

	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;
	
	@Autowired
	private NewComputeService newComputeService;

	@Autowired
	private NewAgeService newAgeService;
	
	@Autowired
	private ApplicationContext ctx;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		// 迭代所有账户
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos)
			{
				CactAccount cactAccount = cc1800IAccountInfo.getCactAccount();
				int currLoanPeriod = cc1800IAccountInfo.getCactAccount().getCurrentLoanPeriod();
				Date beginDate = cactAccount.getGraceDate(); //宽限日期
				List<CactSubAcct>   subAccts=  cc1800IAccountInfo.getCactSubAccts() ;
				Account account = newComputeService.retrieveAccount(cactAccount);
				
				if( TransformType.D.equals(account.carryType)
				&& cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.BL
				&& currLoanPeriod > 0
				&& currLoanPeriod <= cc1800IAccountInfo.getCactAccount().getTotalLoanPeriod() 
				&& beginDate != null && batchDate.toDate().compareTo(beginDate) == 0 //宽限日当天判断是否需要利息转逾期
				&& cactAccount.getFirstOverdueDate() != null
				&&  ( newAgeService.calcAgeGroupCd(cactAccount.getAgeCd() ) == AgeGroupCd.Attention  ||
					  newAgeService.calcAgeGroupCd(cactAccount.getAgeCd() ) == AgeGroupCd.Above4M3  )) { //贷款处于逾期状态或者非应计状态
					  
					//对于最新结转出来的余额成分做形态转移-转逾期
					for(CactSubAcct cactSubAcct : subAccts){
						if(cactSubAcct.getStmtHist()!=1 ) continue  ;//当期结转出来的余额成分，账期为1
						if("LOAN".equals(cactSubAcct.getSubAcctId()) ) continue ;  //未到期部分不做形态转移
						
						LoanTransformEvent event = new LoanTransformEvent(this);
						event.setSubAcctId(cactSubAcct.getSubAcctId());
						event.setTxnDetailSeq( cc1800IAccountInfo.getCactAccount().getAcctSeq().toString());
						event.setTxnDetailType(TxnDetailType.A);
						event.setOrginalAgeCd("0");
						event.setNewAgeCd("1");
						event.setOrginalAgeGroupCd(AgeGroupCd.Normality);
						event.setNewAgeGroupCd(AgeGroupCd.S);
						ctx.publishEvent(event);
					}
				}
				
				if( TransformType.D.equals(account.carryType)
						&& cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.BL
						&& currLoanPeriod > 0
						&& cactAccount.getFirstOverdueDate() != null
						&&  ( newAgeService.calcAgeGroupCd(cactAccount.getAgeCd() ) == AgeGroupCd.Attention  ||
							  newAgeService.calcAgeGroupCd(cactAccount.getAgeCd() ) == AgeGroupCd.Above4M3  )) { //贷款处于逾期状态或者非应计状态
						
					//对于最新结转出来的余额成分做形态转移-转非应计
					Integer stmthist= null;
					for(CactSubAcct cactSubAcct : subAccts){
						//满足90天转非应计的数据，取账期
						if( (Days.daysBetween(new LocalDate(cactSubAcct.getSetupDate()), batchDate).getDays() )==90
							&&  (cactSubAcct.getSubAcctType().equals("LBAL")||  cactSubAcct.getSubAcctType().equals("INTE")  )){ //只有本金或者利息成分是到期日结转出来的
							stmthist = cactSubAcct.getStmtHist();
							break;
						}
					}
					if(stmthist!=null){
						//根据上面取得的账期判断是否满足转非应计条件
						for(CactSubAcct cactSubAcct : subAccts ){
							if(cactSubAcct.getStmtHist()== stmthist  
								&& 	cactSubAcct.getEndDayBal().add( cactSubAcct.getPenalizedAmt() ).compareTo(new BigDecimal(0))>0  
								&&  (cactSubAcct.getSubAcctType().equals("PNIT")|| cactSubAcct.getSubAcctType().equals("LBAL")||  cactSubAcct.getSubAcctType().equals("INTE")||  cactSubAcct.getSubAcctType().equals("SFEE") )){
								
								LoanTransformEvent event = new LoanTransformEvent(this);
								event.setSubAcctId(cactSubAcct.getSubAcctId());
								event.setTxnDetailSeq( cc1800IAccountInfo.getCactAccount().getAcctSeq().toString());
								event.setTxnDetailType(TxnDetailType.A);
								event.setOrginalAgeGroupCd(AgeGroupCd.S);
								event.setNewAgeGroupCd(AgeGroupCd.Z);
								ctx.publishEvent(event);
							}
						}
					}
				}
			}
		}
		return item;
	}

	
}
