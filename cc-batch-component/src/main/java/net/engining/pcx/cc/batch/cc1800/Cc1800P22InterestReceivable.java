package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import net.engining.pcx.cc.process.service.account.date.AccrualInterestDateCalculationService;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.RateCalcMethod;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.InterestAccrualType;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.param.model.enums.PrePaySettlementType;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.service.PaymentPlanService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;


/**
 * 利息 － 计息
 * @author linwk
 *
 */
@Service
@StepScope
public class Cc1800P22InterestReceivable implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Autowired
	private NewComputeService newComputeService;

	@Autowired
	private PaymentPlanService paymentPlanService;

	@Autowired
	private AccrualInterestDateCalculationService accrualInterestDateCalculationService;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item)
	{
		// 迭代所有账户
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos)
			{	
				for (CactSubAcct cactSubAcct : cc1800IAccountInfo.getCactSubAccts())
				{
					CactAccount cactAccount = cc1800IAccountInfo.getCactAccount();
					
					// TODO 这里暂时判定智能存款不作计息，待探索更好的办法
					if (cactAccount.getBusinessType() == BusinessType.ID)
					{
						continue;
					}
					
					Account account = newComputeService.retrieveAccount(cactAccount);
					SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);

					//	判断锁定码定义是否需要进行计息
					if (!blockCodeUtils.getMergedIntAccuralInd(cactAccount.getBlockCode(), account))
					{
						continue;
					}

					//先确定起息日
					LocalDate startDate = accrualInterestDateCalculationService.calcStartDate(
							cactSubAcct.getLastComputingInterestDate(), cactSubAcct.getSubAcctType(), cactAccount, subAcct, account);
					//计息截止日
					LocalDate endDate = batchDate.plusDays(1);

					//准备利率表列表
					List<InterestTable> tables = newInterestService.retrieveInterestTable(cactAccount, cactSubAcct, account, subAcct, endDate);
					
					InterestTable receivableTable = null;

					//TODO 结转的逻辑不该在这里
					boolean flag = TransformType.D.equals(account.carryType)
							&&  (
									(PaymentMethod.MRG).equals(account.paymentMethod)
									|| (PaymentMethod.MSF).equals(account.paymentMethod)
								);
					if(flag){
						for (InterestTable interestTable : tables) {
							//直接取利率
							receivableTable=interestTable;
						}
						if (receivableTable != null) {
							updateReceivable(cactSubAcct, cactAccount, subAcct,account, receivableTable, startDate);
						}
					}
					else{
						// 计息使用的利率表
						receivableTable = newInterestService.fitInterestTable(startDate, endDate, tables);
						
						//如果没有可用利率，表明还没到最小的计息周期，不计息
						if (receivableTable != null)
						{
							updateReceivable(cactSubAcct, cactAccount, subAcct,account, receivableTable, startDate);
						}
					}
					

				}
			}
		}
		return item;
	}

	/**
	 * 计息
	 * @param cactSubAcct
	 * @param cactAccount
	 * @param subAcct
	 * @param interestTable
	 * @return
	 */
	private void updateReceivable(CactSubAcct cactSubAcct, CactAccount cactAccount, SubAcct subAcct,Account account,
			InterestTable interestTable, LocalDate startDate)
	{
		// 出于维护考虑，默认使用逐笔法
		InterestAccrualType interestAccrualType = MoreObjects.firstNonNull(subAcct.interestAccrualType, InterestAccrualType.Iterative);
		switch (interestAccrualType)
		{
			case AddUp:
			{
				// 每日更新积数
				cactSubAcct.setAddupAmt(
						MoreObjects.firstNonNull(cactSubAcct.getAddupAmt(), BigDecimal.ZERO)
						.add(cactSubAcct.getEndDayBal()));

				// 如果当前日期等于计息日期，则计息
				if (batchDate.toDate().equals(cactAccount.getInterestDate()))
				{
					//需要计算积数和调用利率查询接口计算利息
					//活期计息，按1天*积数来计
					BigDecimal interest = newInterestService.calcInterest(
							batchDate,
							batchDate.plusDays(1),
							cactSubAcct.getAddupAmt(), 
							ImmutableList.of(interestTable),
							newComputeService.getReceivableScale(),
							InterestCycleRestMethod.NA);

					//计算应收利息
					cactSubAcct.setIntReceivable(MoreObjects.firstNonNull(cactSubAcct.getIntReceivable(), BigDecimal.ZERO).add(interest));
					cactSubAcct.setLastUpdateDate(new Date());
					cactSubAcct.setLastComputingInterestDate(batchDate.toDate());
				}
				break;
			}
			//逐笔法
			case Iterative:
			{
				BigDecimal interest = null;
				BigDecimal interestAmount= BigDecimal.ZERO;

				//等额本金-剩余靠前特殊固定日还款
				if( (PaymentMethod.MRG).equals(account.paymentMethod) ){
					//根据下次结息日来控制应收利息
					if(new LocalDate(batchDate.plusDays(1)).equals(new LocalDate(cactAccount.getInterestDate())) ){
						// 最后剩余的天数按日息来处理
						List<RateCalcMethod> dailyRates = newInterestService.convertRates(Interval.D, interestTable);
						
						//不满整计息周期的剩余天数,乘上日利率得出利息
						interest = interestAmount.add(
								newComputeService.calcTieredAmount(interestTable.tierInd, dailyRates, cactSubAcct.getEndDayBal(), cactSubAcct.getEndDayBal())
									.multiply(BigDecimal.valueOf(Days.daysBetween(startDate, batchDate.plusDays(1)).getDays()))
									.setScale(newComputeService.getReceivableScale(), RoundingMode.HALF_UP)
								);	// endDate表示闭区间，所以直接是这个days的值
					}
				}
				//等额本息-剩余靠前特殊固定日还款
				else if((PaymentMethod.MSF ).equals(account.paymentMethod) ){
					//根据下次结息日来控制应收利息
					if(new LocalDate(batchDate.plusDays(1)).equals( new LocalDate(cactAccount.getInterestDate())) ){
						//首期放款特殊处理
						if( cactAccount.getCurrentLoanPeriod()==0 ){
							//转换成月利率
							List<RateCalcMethod> cycleRates = newInterestService.convertRates(interestTable.cycleBase, interestTable);
							
							interest = interestAmount.add(
									newComputeService.calcTieredAmount(interestTable.tierInd, cycleRates, cactSubAcct.getEndDayBal(), cactSubAcct.getEndDayBal())
										.multiply(BigDecimal.valueOf(interestTable.cycleBaseMult) )
										.setScale(2, RoundingMode.HALF_UP)
									);	// endDate表示闭区间，所以直接是这个days的值
							
							if( startDate.isBefore( new LocalDate(DateUtils.addMonths(cactAccount.getInterestDate(), -1)) )  ){
								//转换成日利率
								List<RateCalcMethod> dailyRates = newInterestService.convertRates(Interval.D, interestTable);
								
								interest=interest.add(
										newComputeService.calcTieredAmount(interestTable.tierInd, dailyRates, cactSubAcct.getEndDayBal(), cactSubAcct.getEndDayBal())
										.multiply(BigDecimal.valueOf(Days.daysBetween(startDate, new LocalDate(DateUtils.addMonths(cactAccount.getInterestDate(), -1))).getDays()))
										.setScale(newComputeService.getReceivableScale(), RoundingMode.HALF_UP)
										);
							}
							
						}
						else{
							//转换成月利率
							List<RateCalcMethod> cycleRates = newInterestService.convertRates(interestTable.cycleBase, interestTable);
							
							interest = interestAmount.add(
									newComputeService.calcTieredAmount(interestTable.tierInd, cycleRates, cactSubAcct.getEndDayBal(), cactSubAcct.getEndDayBal())
										.multiply(BigDecimal.valueOf(interestTable.cycleBaseMult) )
										.setScale(newComputeService.getReceivableScale(), RoundingMode.HALF_UP)
									);	// endDate表示闭区间，所以直接是这个days的值
						}
					}
					
				}
				else{
					//计算利息的金额基数
					BigDecimal inteBaseAmount = cactSubAcct.getEndDayBal();
					//等本等息还款方式任何时候都使用贷款的全额本金计算利息
					boolean flag = (account.paymentMethod == PaymentMethod.PSV || account.paymentMethod == PaymentMethod.PSZ) && inteBaseAmount.compareTo(BigDecimal.ZERO) != 0;
					if(flag) {
						inteBaseAmount = cactAccount.getTotalLoanPrincipalAmt();
					}
					//等本等息还款日当天生成的LBAL子账户，不计息，因为已在LOAN里计息
					if (cactSubAcct.getStmtHist() == 0 &&
							"LBAL".equals(cactSubAcct.getSubAcctType()) &&
							(account.paymentMethod == PaymentMethod.PSV || account.paymentMethod == PaymentMethod.PSZ)) {
						break;
					}
					
					interest = newInterestService.calcInterest(
							startDate,
							batchDate.plusDays(1),
							inteBaseAmount,
							ImmutableList.of(interestTable),
							newComputeService.getReceivableScale(),
							InterestCycleRestMethod.NA);
				}
				
				// 确实进行了计息操作
				if (interest != null)
				{
					// 判断该余额成份是否享受免息期
					if (subAcct.intWaive && Boolean.TRUE.equals(cactAccount.getGraceDaysFullInd()))
					{
						cactSubAcct.setIntPending(cactSubAcct.getIntPending().add(interest));
					}
					else
					{
						// 不可免利息累积后,记入非延迟利息
						if( cactAccount.getLastInterestDate()==null ||cactAccount.getInterestDate().compareTo(cactAccount.getLastInterestDate()) != 0 ){
							//中民项目一次还本按月付息，末期利息特殊处理
							if( account.paymentMethod.equals(PaymentMethod.IFP) && account.intUnit.equals(Interval.M )
									 && interestTable.rateBaseInterval.equals(Interval.Y) && PrePaySettlementType.M.equals(account.advanceType) 
									 && interest.compareTo(new BigDecimal(0))>0 ){
								PaymentPlan paymentPlan = paymentPlanService.searchPaymentPlan(cactAccount.getAcctSeq());
								//末期还款计划日期
								Date paymentDate=paymentPlan.getDetailsMap().get(paymentPlan.getDetailsMap().size()).getPaymentDate();
								//末期利息处理,取最后一期利息结转
								if( paymentDate.compareTo(batchDate.toDate() )==0 ){
									cactSubAcct.setIntReceivable(paymentPlan.getDetailsMap().get(paymentPlan.getDetailsMap().size()).getInterestAmt() );
								}
								else{
									cactSubAcct.setIntReceivable(cactSubAcct.getIntReceivable().add(interest) );
								}
							}
							else if( account.paymentMethod.equals(PaymentMethod.PSV ) && interest.compareTo(new BigDecimal(0))>0 ){
								PaymentPlan paymentPlan = paymentPlanService.searchPaymentPlan(cactAccount.getAcctSeq());
								for(int i=paymentPlan.getDetailsMap().size() ;i>=1 ; i--){ 
									Date paymentDate=paymentPlan.getDetailsMap().get(i).getPaymentDate();
									if( paymentDate.compareTo(batchDate.plusDays(1).toDate() )==0 ){ 
										cactSubAcct.setIntReceivable(paymentPlan.getDetailsMap().get(i).getInterestAmt() );
									}
								}
							}
							else{
								cactSubAcct.setIntReceivable(cactSubAcct.getIntReceivable().add(interest));
							}
						}
					}
					cactSubAcct.setLastUpdateDate(new Date());
					cactSubAcct.setLastComputingInterestDate(batchDate.toDate());
				}
				break;
			}
			default:
			{
				throw new IllegalArgumentException("should not be here");
			}
		}
	}
	
	
	
}
