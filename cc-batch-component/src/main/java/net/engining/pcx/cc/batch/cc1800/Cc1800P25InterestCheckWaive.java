/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.service.account.NewComputeService;


/**
 * 全额还款检验
 * @author linwk
 *
 */
@Service
@StepScope
public class Cc1800P25InterestCheckWaive implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 账务处理通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		for (List<Cc1800IAccountInfo> infos : item.getAccountList().values())
		{
			for (Cc1800IAccountInfo info : infos)
			{
				
				if (logger.isDebugEnabled()) {
					logger.debug("全额还款检验：Org["+info.getCactAccount().getOrg()
							+"],AcctType["+info.getCactAccount().getBusinessType()
							+"],AcctNo["+info.getCactAccount().getAcctNo()
							+"],BatchDate["+batchDate
							+"]");
				}

				// 未经过首次账单日的账户，更新全额还款标志为已还款，并返回
				// 如果还没有首次账单日，则直接返回
				if (info.getCactAccount().getFirstStmtDate() == null || info.getCactAccount().getFirstStmtDate().after(batchDate) ){
					if (info.getCactAccount().getGraceDaysFullInd() == false){
						info.getCactAccount().setGraceDaysFullInd(true);
					}
					continue;
				}
				
				// 下一宽限日
				Date graceDay = info.getCactAccount().getGraceDate();
				
				// 判断批量日期在账单日与宽限日之间，则进行全额还款标志计算
				if (info.getCactAccount().getGraceDaysFullInd() == false ||
						(graceDay != null && DateUtils.isSameDay(batchDate, DateUtils.addDays(graceDay, 1))))
				{
					logger.debug("日期时间段符合 批量日期:[" + batchDate.toString() + "] 上一账单日:[" 
													+ info.getCactAccount().getLastInterestDate().toString() + "] 下一还款日:[" 
													+ graceDay.toString() + "]");
					verifyFullPayment(info);
				}
				
				logger.debug("全额还款检验Process finished! AccountNo:[" + info.getCactAccount().getAcctNo() + "] AccountType:["
																+ info.getCactAccount().getBusinessType() + "] ");
			}
		}
		return item;
	}
	
	/**
	 * 判断是否全额还款，更新全额还款标志
	 * @param item 账户信息
	 */
	private void verifyFullPayment(Cc1800IAccountInfo item)	{
		
		CactAccount cactAccount = item.getCactAccount();
		logger.debug("开始判断全额还款 accountNo:[" + cactAccount.getAcctNo() + "] accountType:[" + cactAccount.getBusinessType() + "]");
		
		//获取参数
		Account account = commonCompute.retrieveAccount(cactAccount);				
		logger.debug("容忍度计算方式 downpmtTolInd:[" + account.downpmtTolInd + "]");
		
		//容忍金额
		BigDecimal stmtBalance = BigDecimal.ZERO;
		
		//当期贷方交易金额
//		BigDecimal ctdAmountCr = cactAccount.getCtdPaymentAmt()
//									.add(cactAccount.getCtdCrAdjAmt())
//									.add(cactAccount.getCtdRefundAmt());
		
		//当期贷方交易金额大于账单总金额时，更新全额还款指示
		switch(account.downpmtTolInd){
		//按比例计算容忍度
		case R : stmtBalance = item.getCactAccount().getPmtDueDayBal().multiply(account.downpmtTolPerc);break;
		//按金额计算容忍度
		case A : stmtBalance = account.downpmtTol; break;
		//比例和金额同时考虑,取高的应还款额
		case B : 
			BigDecimal rateBalance = item.getCactAccount().getPmtDueDayBal().multiply(account.downpmtTolPerc);
			BigDecimal amountBalance = account.downpmtTol;
			stmtBalance = rateBalance.compareTo(amountBalance) > 0 ? rateBalance : amountBalance;
			break;
		default : 
			throw new IllegalArgumentException("账户属性中全额还款容忍度标志无法处理" );
		}
		if (item.getCactAccount().getQualGraceBal().compareTo(stmtBalance) <= 0) {
			// 已全额还款
			
			// 设置全额还款标志
			cactAccount.setGraceDaysFullInd(true);
			
			// 免除往期累积延时利息
			waiveInterest(item.getCactSubAccts());
		}
		else {
			cactAccount.setGraceDaysFullInd(false);
		}
	}
	
	/**
	 * 免除往期余额累积利息
	 * @param plans 信用计划列表
	 */
	private void waiveInterest(List<CactSubAcct> cactSubAccts){
		for (CactSubAcct cactSubAcct : cactSubAccts){
			cactSubAcct.setIntPending(BigDecimal.ZERO);
		}
	}
}
