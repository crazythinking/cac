package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 还款计划明细信息
 * 
 * @author Ronny
 *
 */
public class PaymentPlanDetail implements Serializable {

	private static final long serialVersionUID = -5230975672991369042L;

	/**
	 * 当期期数
	 */
	private Integer loanPeriod;

	/**
	 * 还款业务日
	 */
	private Date paymentDate;
	
	/**
	 * 还款自然日
	 */
	private Date paymentNatureDate;

	/**
	 * 原始应还本金
	 */
	private BigDecimal origPrincipalBal = BigDecimal.ZERO;

	/**
	 * 应还本金
	 */
	private BigDecimal principalBal = BigDecimal.ZERO;

	/**
	 * 原始应还利息
	 */
	private BigDecimal origInterestAmt = BigDecimal.ZERO;

	/**
	 * 应还利息
	 */
	private BigDecimal interestAmt = BigDecimal.ZERO;

	/**
	 * 原始应还费用
	 */
	private BigDecimal origFeeAmt = BigDecimal.ZERO;

	/**
	 * 应还费用
	 */
	private BigDecimal feeAmt = BigDecimal.ZERO;

	/**
	 * 应还罚息
	 */
	private BigDecimal penalizedAmt = BigDecimal.ZERO;

	/**
	 * 应还总金额
	 */
	private BigDecimal totalRepayAmt = BigDecimal.ZERO;

	/**
	 * 首次生产还款计划时，将key = subacctType 和value = 余额成分存入，供还款冲销的时候使用
	 */
	private Map<String, BigDecimal> acctTypeAndAmtMap = new HashMap<String, BigDecimal>();

	/**
	 * @return the paymentNatureDate
	 */
	public Date getPaymentNatureDate() {
		return paymentNatureDate;
	}

	/**
	 * @param paymentNatureDate the paymentNatureDate to set
	 */
	public void setPaymentNatureDate(Date paymentNatureDate) {
		this.paymentNatureDate = paymentNatureDate;
	}

	public BigDecimal getOrigPrincipalBal() {
		return origPrincipalBal;
	}

	public void setOrigPrincipalBal(BigDecimal origPrincipalBal) {
		this.origPrincipalBal = origPrincipalBal;
	}

	public BigDecimal getOrigInterestAmt() {
		return origInterestAmt;
	}

	public void setOrigInterestAmt(BigDecimal origInterestAmt) {
		this.origInterestAmt = origInterestAmt;
	}

	public BigDecimal getOrigFeeAmt() {
		return origFeeAmt;
	}

	public void setOrigFeeAmt(BigDecimal origFeeAmt) {
		this.origFeeAmt = origFeeAmt;
	}

	public BigDecimal getTotalRepayAmt() {
		return totalRepayAmt;
	}

	public void setTotalRepayAmt(BigDecimal totalRepayAmt) {
		this.totalRepayAmt = totalRepayAmt;
	}

	public Map<String, BigDecimal> getAcctTypeAndAmtMap() {
		return acctTypeAndAmtMap;
	}

	public void setAcctTypeAndAmtMap(Map<String, BigDecimal> acctTypeAndAmtMap) {
		this.acctTypeAndAmtMap = acctTypeAndAmtMap;
	}

	public BigDecimal getPenalizedAmt() {
		return penalizedAmt;
	}

	public void setPenalizedAmt(BigDecimal penalizedAmt) {
		this.penalizedAmt = penalizedAmt;
	}

	public Integer getLoanPeriod() {
		return loanPeriod;
	}

	public void setLoanPeriod(Integer loanPeriod) {
		this.loanPeriod = loanPeriod;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getPrincipalBal() {
		return principalBal;
	}

	public void setPrincipalBal(BigDecimal principalBal) {
		this.principalBal = principalBal;
	}

	public BigDecimal getInterestAmt() {
		return interestAmt;
	}

	public void setInterestAmt(BigDecimal interestAmt) {
		this.interestAmt = interestAmt;
	}

	public BigDecimal getFeeAmt() {
		return feeAmt;
	}

	public void setFeeAmt(BigDecimal feeAmt) {
		this.feeAmt = feeAmt;
	}

}
