/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactRepayFeeInfo;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactRepayFeeInfo;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.enums.CalcMethod;
import net.engining.pcx.cc.param.model.enums.LoanFeeMethod;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewPostService;
import net.engining.pcx.cc.process.service.account.PostDetail;
import net.engining.pcx.cc.process.service.impl.InternalAccountService;
import net.engining.pg.parameter.ParameterFacility;


/**
 * 分期手续费
 *
 */
@Service
@StepScope
public class Cc1800P366Loanfee implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	
	@Autowired
	private ParameterFacility parameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;
	
	@Autowired
	private NewPostService newPostService;
	
	@Autowired
	private NewComputeService newComputeService;

	@Autowired
	private InternalAccountService internalAccountService;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		// 迭代所有账户
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos)
			{
				int currLoanPeriod = cc1800IAccountInfo.getCactAccount().getCurrentLoanPeriod();
				
				//首期手续费在联机消费时已入账，此处收取非首期手续费
				if(cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.BL
						&& currLoanPeriod > 0
						&& currLoanPeriod <= cc1800IAccountInfo.getCactAccount().getTotalLoanPeriod()){
					
					//在期初日期收取分期手续费，第一次收取是在建账日，后面每次收取在结息日
					//手续费收取在每一期的宽限日收取
//					Date beginDate = cc1800IAccountInfo.getCactAccount().getLastInterestDate();
					Date beginDate = cc1800IAccountInfo.getCactAccount().getGraceDate();
					if(beginDate != null && batchDate.toDate().compareTo(beginDate) == 0){
						
						//分期手续费用
						BigDecimal amt = BigDecimal.ZERO;
						Account acctParam = newComputeService.retrieveAccount(cc1800IAccountInfo.getCactAccount());
						if(acctParam.loanFeeMethod == null)
							continue;
						
						if(acctParam.loanFeeCalcMethod == CalcMethod.A){
							amt = acctParam.feeAmount;
						}
						else if(acctParam.loanFeeCalcMethod == CalcMethod.R){
							amt = cc1800IAccountInfo.getCactAccount().getTotalLoanPrincipalAmt().multiply(acctParam.feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
						}
						if (amt.compareTo(BigDecimal.ZERO) != 0){

							//如果是分期收取，将在宽限日收取
							if(acctParam.loanFeeMethod == LoanFeeMethod.E 
									&& !alreadyPostFee(cc1800IAccountInfo.getCactSubAccts(),cc1800IAccountInfo.getCactAccount())
									&& !alreadyFinish(cc1800IAccountInfo.getCactSubAccts())
									&& !alreadyFinishInterest(cc1800IAccountInfo.getCactAccount()))
							{
								AcctModel model = cc1800IAccountInfo.getAcctModel();
								Map<SysTxnCd, String> txnCdMap = acctParam.sysTxnCdMapping;
								PostCode postcd = parameterFacility.loadParameter(PostCode.class, txnCdMap.get(SysTxnCd.S35));
								PostDetail cactTxnPost = new PostDetail();
								cactTxnPost.setTxnDate(new Date());
								cactTxnPost.setTxnTime(new Date());
								cactTxnPost.setPostTxnType(PostTxnTypeDef.M);
								cactTxnPost.setPostCode(postcd.postCode);
								cactTxnPost.setTxnAmt(amt);
								cactTxnPost.setPostAmt(amt);
								cactTxnPost.setTxnCurrCd(model.getCactAccount().getCurrCd());
								cactTxnPost.setPostCurrCd(model.getCactAccount().getCurrCd());
								//按联机收取 FIXME 这里-1是什么鬼
								cactTxnPost.setTxnDetailSeq("-1");
								cactTxnPost.setTxnDetailType(TxnDetailType.O);
								
								//手续费为当期的手续费，所以账期设为1
								newPostService.postToAccount(model, batchDate, cactTxnPost, true, 1);
								
								CactAccount cactAccount = cc1800IAccountInfo.getCactAccount();
								//手续费内部账户入账
								//因为手续费是在宽限日才会入账所以只有逾期或者非应计是手续费才会入账
								if (acctParam.internalAcctPostMapping != null)
								{
									SysInternalAcctActionCd sysInternalAcctActionCd = null;
									if(newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()) == AgeGroupCd.Attention){
										sysInternalAcctActionCd = SysInternalAcctActionCd.S025;
									}
									if(newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()) == AgeGroupCd.Above4M3){
										sysInternalAcctActionCd = SysInternalAcctActionCd.S026;
									}
									if(sysInternalAcctActionCd != null){
										List<String> internalAcctPostCodes = acctParam.internalAcctPostMapping.get(sysInternalAcctActionCd);
										if (internalAcctPostCodes != null)
										{
											for(String postCode2 :internalAcctPostCodes)
											{
												internalAccountService.postByCode(postCode2, amt.abs(), cactAccount.getCurrCd(), cactAccount.getAcctSeq().toString(), TxnDetailType.A, batchDate.toDate());
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return item;
	}

	private boolean alreadyPostFee(List<CactSubAcct> cactSubAccts,CactAccount cactAccount){
		
		//手续费是否在提前还款时已入账
//		for(CactSubAcct cactSubAcct : cactSubAccts){
//			if(cactSubAcct.getSubAcctType().equals("SFEE") && cactSubAcct.getStmtHist() == -1){
//				alreadyPostFee = true;
//				break;
//			}
//		}
		//查询已还款的期数，判断是否已经提前还款
		QCactRepayFeeInfo qRepayFeeInfo = QCactRepayFeeInfo.cactRepayFeeInfo;
		List<CactRepayFeeInfo> ibRepayFeeInfoList = new JPAQueryFactory(em)
				.select(qRepayFeeInfo)
				.from(qRepayFeeInfo)
				.where(qRepayFeeInfo.acctSeq.eq(cactAccount.getAcctSeq()))
				.orderBy(qRepayFeeInfo.seqId.desc())
				.fetch();
		
		if(ibRepayFeeInfoList.size() == 0 || ibRepayFeeInfoList.get(0).getRepayPeriod() < cactAccount.getCurrentLoanPeriod()){
			//没有提前还款
			return false;
		}
		return true;
	}
	
	private boolean alreadyFinish(List<CactSubAcct> cactSubAccts){
		
		//判断贷款是否还清,全额还款则不计费
		BigDecimal subSum = BigDecimal.ZERO;
		for(CactSubAcct cactSubAcct : cactSubAccts){
			subSum = subSum.add(cactSubAcct.getCurrBal());
		}
		
		if(subSum.compareTo(BigDecimal.ZERO) != 0){
			return false;
		}
		
		return true;
	}
	
	private boolean alreadyFinishInterest(CactAccount cactAccount){
		//根据日期判断计费次数是否已达到分期次数
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(cactAccount.getSetupDate());
//		int startMonth = calendar.get(Calendar.MONTH);
//		calendar.setTime(batchDate.toDate());
//		int endMonth = calendar.get(Calendar.MONTH);
//		return (endMonth - startMonth) > cactAccount.getTotalLoanPeriod();
		Account acctParam = newComputeService.retrieveAccount(cactAccount);
		
		return DateUtils.addDays(batchDate.toDate(), -acctParam.pmtGracePrd).compareTo(DateUtils.addMonths(cactAccount.getSetupDate(), cactAccount.getTotalLoanPeriod())) > 0;
	}
	
}
