package net.engining.pcx.cc.process.service;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.enums.CalcMethod;
import net.engining.pcx.cc.param.model.enums.LoanFeeMethod;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.process.model.PaymentPlan;

/**
 * 还款计划服务
 * 
 * @author yinxia
 *
 */
public interface PaymentPlanService {

	/**
	 * 生成还款计划
	 * 
	 * @param totalPeriod
	 *            期数
	 * @param interval
	 *            周期类型
	 * @param mult
	 *            周期乘数
	 * @param paymentMethod
	 *            还款方式
	 * @param loanAmount
	 *            贷款总金额
	 * @param it
	 *            利率表
	 * @param loanFeeMethod
	 *            手续费收取方式
	 * @param loanFeeCalcMethod
	 *            手续费计算方式
	 * @param feeAmount
	 *            手续费金额
	 * @param feeRate
	 *            手续费费率
	 * @param postDate
	 *            入账日期
	 * @param pmtDueDays
	 *            到期还款天数
	 * @return
	 */
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod,
			BigDecimal loanAmount, InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod,
			BigDecimal feeAmount, BigDecimal feeRate, Date postDate, int pmtDueDays);
	
	/**
	 * 生成还款计划
	 * 
	 * @param totalPeriod
	 *            期数
	 * @param interval
	 *            周期类型
	 * @param mult
	 *            周期乘数
	 * @param paymentMethod
	 *            还款方式
	 * @param loanAmount
	 *            贷款总金额
	 * @param it
	 *            利率表
	 * @param loanFeeMethod
	 *            手续费收取方式
	 * @param loanFeeCalcMethod
	 *            手续费计算方式
	 * @param feeAmount
	 *            手续费金额
	 * @param feeRate
	 *            手续费费率
	 * @param postDate
	 *            入账日期
	 * @param intFirstPeriodAdj
	 *            首期天数是否调整
	 * @param fixedPmtDay
	 *            固定还款日
	 * @return
	 */
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod,
			BigDecimal loanAmount, InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod,
			BigDecimal feeAmount, BigDecimal feeRate, Date postDate, Boolean intFirstPeriodAdj, int fixedPmtDay);

	/**
	 * 生成还款计划
	 * 
	 * @param totalPeriod
	 *            期数
	 * @param interval
	 *            周期类型
	 * @param mult
	 *            周期乘数
	 * @param paymentMethod
	 *            还款方式
	 * @param loanAmount
	 *            贷款总金额
	 * @param it
	 *            利率表
	 * @param loanFeeMethod
	 *            手续费收取方式
	 * @param loanFeeCalcMethod
	 *            手续费计算方式
	 * @param feeAmount
	 *            手续费金额
	 * @param feeRate
	 *            手续费费率
	 * @param postDate
	 *            入账日期
	 * @param pmtDueDays
	 *            到期还款天数
	 * @param intFirstPeriodAdj
	 *            首期天数是否调整
	 * @param fixedPmtDay
	 *            固定还款日
	 * @return
	 */
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod,
			BigDecimal loanAmount, InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod,
			BigDecimal feeAmount, BigDecimal feeRate, Date postDate, int pmtDueDays, Boolean intFirstPeriodAdj, int fixedPmtDay);
	
	
	/**
	 * 保存还款计划
	 * 
	 * @param acctSeq
	 *            贷款序列号
	 * @param custId
	 *            客户编号
	 * @param acctParamId
	 *            贷款参数编号
	 * @param plan
	 *            还款计划
	 * @param saveDate
	 *            保存日期
	 */
	public void savePaymentPlan(Integer acctSeq, String custId, String acctParamId, PaymentPlan plan, Date saveDate);

	/**
	 * 更新还款计划
	 * 
	 * @param acctSeq
	 *            贷款序列号
	 * @param custId
	 *            客户编号
	 * @param acctParamId
	 *            贷款参数编号
	 * @param plan
	 *            还款计划
	 * @param saveDate
	 *            保存日期
	 */
	public void updatePaymentPlan(Integer acctSeq, String custId, String acctParamId, PaymentPlan plan, Date saveDate);

	/**
	 * 获取还款计划表
	 * 
	 * @param acctSeq
	 *            账户编号
	 * @return
	 */
	public PaymentPlan findLatestPaymentPlan(Integer acctSeq);

	/**
	 * 实时查询还款计划
	 * 
	 * @param acctSeq
	 */
	public PaymentPlan searchPaymentPlan(Integer acctSeq);

//	/**
//	 * 增加入参acctSeq，目的是为了判断第一次放款的贷款还是后面的贷款
//	 * 
//	 * @param totalPeriod
//	 * @param interval
//	 * @param mult
//	 * @param paymentMethod
//	 * @param loanAmount
//	 * @param it
//	 * @param loanFeeMethod
//	 * @param loanFeeCalcMethod
//	 * @param feeAmount
//	 * @param feeRate
//	 * @param postDate
//	 * @param pmtDueDays
//	 * @param acctSeq
//	 * @return
//	 */
//	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
//			InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod, BigDecimal feeAmount, BigDecimal feeRate, Date postDate,
//			int pmtDueDays, Integer acctSeq);

//	/**
//	 * 增加入参intFirstPeriodAdj，目的是为了判断是否第一次调整期数，按日计息的贷款需要重新计算还款日
//	 * 
//	 * @param totalPeriod
//	 * @param interval
//	 * @param mult
//	 * @param paymentMethod
//	 * @param loanAmount
//	 * @param it
//	 * @param loanFeeMethod
//	 * @param loanFeeCalcMethod
//	 * @param feeAmount
//	 * @param feeRate
//	 * @param postDate
//	 * @param pmtDueDays
//	 * @param intFirstPeriodAdj
//	 * @param acctSeq
//	 * @return
//	 */
//	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
//			InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod, BigDecimal feeAmount, BigDecimal feeRate, Date postDate,
//			int pmtDueDays, Boolean intFirstPeriodAdj, Integer acctSeq);

	
//	public BigDecimal getCalculateInte(CactAccount cactAccount);

	/**
	 * 提前还款重算还款计划
	 * 
	 * @param paymentPlan
	 * @param cactAccount
	 * @param acctParam
	 * @return
	 */
	public PaymentPlan reCreatePaymentPlan(PaymentPlan paymentPlan, CactAccount cactAccount, Account acctParam);
}
