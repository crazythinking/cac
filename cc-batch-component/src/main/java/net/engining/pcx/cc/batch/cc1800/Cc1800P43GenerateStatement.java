package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.process.service.account.PaymentDateCalculationService;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;

/**
 * 账单汇总信息处理
 * 
 * @author linwk
 * 
 */
@Service
@StepScope
public class Cc1800P43GenerateStatement implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 入账通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute;

	/**
	 * 锁定码组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Autowired
	private PaymentDateCalculationService paymentDateCalculationService;
	
	/**
	 * 最小还款额及账龄计算业务组件
	 */
	@Autowired
	private UComputeDueAndAgeCode cc1800UComputeDueAndAgeCode;

	/**
	 * 实体管理类
	 */
	@PersistenceContext
	private EntityManager em;

	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {

		for (List<Cc1800IAccountInfo> infos : item.getAccountList().values())
		{
			for (Cc1800IAccountInfo info : infos)
			{
				if (logger.isDebugEnabled()) {
					logger.debug("账单汇总信息处理：Org["+info.getCactAccount().getOrg()
							+"],AcctNo["+info.getCactAccount().getAcctNo()
							+"],BusinessType["+info.getCactAccount().getBusinessType()
							+"],CurrCd["+info.getCactAccount().getCurrCd()
							+"],BatchDate["+batchDate
							+"],NextStmtDate["+info.getCactAccount().getInterestDate()
							+"]");
				}
				Date nextStmtDate = info.getCactAccount().getInterestDate();
		        //这里只处理贷款类的账户，存款类账户的结息日更新在结息步骤里处理。
				// 判断是否账单日
				if (DateUtils.isSameDay(batchDate, nextStmtDate) 
						&& (info.getCactAccount().getBusinessType() == BusinessType.CC
						|| info.getCactAccount().getBusinessType() == BusinessType.BL
//						|| info.getCactAccount().getBusinessType() == BusinessType.CL
						)) {
					// 批量日期是下一账单日
					logger.debug("开始账单处理");
					
					//账户日终余额
					BigDecimal bal = BigDecimal.ZERO;
			        for (CactSubAcct cactSubAcct : info.getCactSubAccts()) {
			        	bal = bal.add(cactSubAcct.getEndDayBal());
			        }
		
					// 更新账户相关信息
					updateAccountAtStmtDay(info, bal);
					
					// 更新信用计划相关信息
					updateSubAcctAtStmtDay(info);
					
					// 更新账户相关信息完成后，再新建账单历史统计信息。
					//因为当前账期的应还款金额和最后还款日都是在更新账户相关信息完成后才产生
					CactStmtHst stmtHst = createStmtHst(info.getCactAccount(), info.getCactSubAccts(), bal);
					em.persist(stmtHst);
					
					//	更新账龄历史记录
//					logger.debug("更新账龄历史记录");
//					cc1800UComputeDueAndAgeCode.recordAgeCdHst(info.getCactAccount());
		
				} else {
					// 批量日期不是下一账单日
					logger.debug("不是账单日，跳过账单处理过程");
				}
			}
		}
		return item;
	}

	/**
	 * 账单日更新帐户状态
	 * 
	 * @param accountInfo
	 */
	private void updateAccountAtStmtDay(Cc1800IAccountInfo accountInfo, BigDecimal bal) {
		// 修改注意事项：
		// 下一账单日字段必须最后更新，否则还款日等相关字段计算会错误

		CactAccount account = accountInfo.getCactAccount();	
		
		Account accountParam = commonCompute.retrieveAccount(account);
		// 设置期初余额 = 当前余额
	    
        account.setBeginBal(bal);

		// 设置当期取现金额为0
//		account.setCtdCashAmt(BigDecimal.ZERO);

		// 设置当期取现笔数为0
//		account.setCtdCashCnt(0);

		// 设置当期贷记调整金额为0
//		account.setCtdCrAdjAmt(BigDecimal.ZERO);

		// 设置当期贷记调整笔数为0
//		account.setCtdCrAdjCnt(0);
		
		// 设置当期借记调整金额为0
//		account.setCtdDbAdjAmt(BigDecimal.ZERO);

		// 设置当期借记调整笔数为0
//		account.setCtdDbAdjCnt(0);

		// 设置当期费用金额为0
//		account.setCtdFeeAmt(BigDecimal.ZERO);

		// 设置当期费用笔数为0
//		account.setCtdFeeCnt(0);

		// 设置当期最高超限金额为0
//		account.setCtdHiOvrlmtAmt(BigDecimal.ZERO);

		// 设置当期入账利息金额为0
//		account.setCtdInterestAmt(BigDecimal.ZERO);
//		account.setCtdInterestCnt(0);
		
		// 设置当期还款金额为0
		account.setCtdPaymentAmt(BigDecimal.ZERO);

		// 设置当期还款笔数为0
//		account.setCtdPaymentCnt(0);

		// 设置当期退货金额为0
//		account.setCtdRefundAmt(BigDecimal.ZERO);

		// 设置当期退货笔数为0
//		account.setCtdRefundCnt(0);

		// 设置当期消费金额为0
//		account.setCtdRetailAmt(BigDecimal.ZERO);

		// 设置当期消费笔数为0
//		account.setCtdRetailCnt(0);

		// 设置约定还款日期，必须在更新下一账单日之前更新
		account.setDdDate(commonCompute.getNextDDDay(account));

		// FIXME 设定下次溢缴款购汇还款日期
//		account.setDlblDate(null);

		

		// 设置全额还款标志
		//account.setGraceDaysFullInd(Boolean.FALSE);

		// 设置上个当期还款日为当前还款日
		account.setLastPmtDueDate(account.getPmtDueDate());

		// 设置上个账单日
		account.setLastInterestDate(account.getInterestDate());

		// 设置下个还款日
		if (account.getCurrentLoanPeriod() < account.getTotalLoanPeriod()
				|| account.getTotalLoanPeriod() == -1) {
			account.setPmtDueDate(paymentDateCalculationService.getNextPaymentDay(account));
			// 宽限日期的设定必须在账单日设定之前
			account.setGraceDate(commonCompute.getNextGraceDay(account));
		}		

		// 设置全部应还款额
		account.setQualGraceBal(commonCompute.calcQualGraceBal(accountInfo.getCactSubAccts()));
		if(account.getQualGraceBal().compareTo(BigDecimal.ZERO) > 0){
			account.setGraceDaysFullInd(false);
		}
		// 设置还款日余额为0
		account.setPmtDueDayBal(account.getQualGraceBal());
		
		cc1800UComputeDueAndAgeCode.computeMinDue(account, accountInfo.getCactSubAccts(), accountInfo.getCactAgeDues(), batchDate);

		if(-1 != accountInfo.getCactAccount().getTotalLoanPeriod()
				/*&& accountInfo.getCactAccount().getCurrentLoanPeriod() != accountInfo.getCactAccount().getTotalLoanPeriod()*/){
			accountInfo.getCactAccount().setCurrentLoanPeriod(accountInfo.getCactAccount().getCurrentLoanPeriod() + 1);
		}
		
		// 设置下个账单日，必须最后更新
		/*account.setInterestDate(commonCompute.getNextInterstDate(DateUtils.addDays(account.getInterestDate(), 1), accountParam, account.getBillingCycle()));*/
		account.setInterestDate(commonCompute.getNextInterstDate(account, account.getInterestDate(), accountParam, account.getBillingCycle()));
	}
	
	/**
	 * 创建账单统计信息 必须在账户状态更新之前进行
	 * 
	 * @return
	 */
	private CactStmtHst createStmtHst(CactAccount cactAccount, List<CactSubAcct> cactSubAccts, BigDecimal bal) {
		// 批量日期
		CactStmtHst cactStmtHst = new CactStmtHst();
		//	TODO 有时间将convertToMap修改为指定赋值
		cactStmtHst.updateFromMap(cactAccount.convertToMap());
		// 设置账单日期
		cactStmtHst.setStmtDate(batchDate);
		// 设置还款日
		cactStmtHst.setPmtDueDate(paymentDateCalculationService.getNextPaymentDay(cactAccount));
		// 设置生成账单类型
		cactStmtHst.setStmtFlag(calcStatementFlag(cactAccount, bal));
		// 设置上期账单期初余额
		cactStmtHst.setStmtBegBal(cactAccount.getBeginBal());
		// 设置账单应还款额
		cactStmtHst.setQualGraceBal(commonCompute.calcQualGraceBal(cactSubAccts));
		// 设置当期借记金额 = 当期取现金额 + 当期消费金额 + 当期借记调整金额
//		cactStmtHst.setCtdAmtDb(cactAccount.getCtdCashAmt().add(cactAccount.getCtdRetailAmt())
//				.add(cactAccount.getCtdDbAdjAmt()));

		// 设置当期借记笔数
//		cactStmtHst.setCtdNbrDb(cactAccount.getCtdCashCnt() + cactAccount.getCtdRetailCnt()
//				+ cactAccount.getCtdDbAdjCnt());

		// 设置当期贷记金额 = 当期贷记调整金额 + 当期退货金额 + 当期还款金额
//		cactStmtHst.setCtdAmtCr(cactAccount.getCtdCrAdjAmt().add(cactAccount.getCtdRefundAmt())
//				.add(cactAccount.getCtdPaymentAmt()));

		// 设置当期贷记笔数
//		cactStmtHst.setCtdNbrCr(cactAccount.getCtdCrAdjCnt() + cactAccount.getCtdRefundCnt()
//				+ cactAccount.getCtdPaymentCnt());

//		Date beginDate = cactAccount.getTempLimitBeginDate();
//		Date endDate = cactAccount.getTempLimitEndDate();
//		// 判定批量日期时临时额度是否有效，无效则将账单历史中的临时额度相关字段清空
//		if (beginDate != null 
//				&& !(batchDate.before(endDate) 
//						&& batchDate.after(beginDate))) {
//			cactStmtHst.setTempLimit(BigDecimal.ZERO);
//			cactStmtHst.setTempLimitBeginDate(null);
//			cactStmtHst.setTempLimitEndDate(null);
//		}
	
		//	设置当前余额
		cactStmtHst.setStmtCurrBal(bal);
		
		//	 账单上的最小还款额需要包括超限部分
		cactStmtHst.setCurrDueAmt(cactAccount.getTotDueAmt());
		//TODO：liyinxia					.add(commonCompute.computeOverLimitAmt(cactAccount);
		
		cactStmtHst.fillDefaultValues();
		return cactStmtHst;
	}
	
	/**
	 * 账单日更新信用计划统计信息
	 * @param item
	 */
	private void updateSubAcctAtStmtDay(Cc1800IAccountInfo item){
		for (CactSubAcct cactSubAcct : item.getCactSubAccts()){		
				cactSubAcct.setStmtHist(cactSubAcct.getStmtHist()+1);
				
				cactSubAcct.setBeginBal(cactSubAcct.getEndDayBal());
		}
	}
	
	/**
	 * 检验生成的账单类型
	 * 
	 * @return
	 */
	private Boolean calcStatementFlag(CactAccount cactAccount, BigDecimal bal) {
		if (cactAccount == null) {
			throw new IllegalArgumentException("输入null");
		}

		// 获取账户参数
		Account account = commonCompute.retrieveAccount(cactAccount);
					
		
			// 客户要求出账单，判断出账单的条件
		Boolean stmtFlag = null;
			// 是否需要出账单文件
			Boolean isGenMedia = Boolean.FALSE;

			// 判断参数只有贷方余额是否小于免出账单最大金额
			if ((!isGenMedia) 
					&& (bal.negate().compareTo(account.crMaxbalNoStmt) >= 0 
					|| cactAccount.getCtdCrAdjAmt().add(cactAccount.getCtdPaymentAmt()).add(cactAccount.getCtdRefundAmt()).compareTo(account.crMaxbalNoStmt) >= 0)) {
				isGenMedia = true;
			}

			// 判断参数出账单最小余额
			if ((!isGenMedia) && bal.add(cactAccount.getCtdCrAdjAmt()).add(cactAccount.getCtdPaymentAmt()).add(cactAccount.getCtdRefundAmt()).compareTo(account.stmtMinBal) >= 0) {
				isGenMedia = true;
			}

			// 判断是否需要出账单
			if (isGenMedia) {
				// 需要出账单，账单类型取账户信息上的账单类型
				stmtFlag = Boolean.TRUE;
			} else {
				// 不需要出账单
				stmtFlag = Boolean.FALSE;
			}
		

		// 判定锁定码,锁定码指示不出账单，则不出账单
		if (stmtFlag && (!blockCodeUtils.getMergedStmtInd(cactAccount.getBlockCode(), account))) {
			stmtFlag = Boolean.FALSE;
		}

		return isGenMedia;
	}
}
