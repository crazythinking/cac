package net.engining.pcx.cc.process.service.impl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 还款计划明细摘要
 * 如果已经逾期，并超过贷款设定的最大期数，那么继续增加贷款期数，并按照子账户账期修对应期数的应还金额
 * @author liyinxia
 *
 */
public class SimplePaymentPlanDetail
{
    private Integer loanPeriod;
		
	private	Date paymentDate;
		
	private	BigDecimal principalBal = BigDecimal.ZERO;
	  
	private	BigDecimal interestAmt = BigDecimal.ZERO;
	  
	private	BigDecimal feeAmt = BigDecimal.ZERO ;
	  
	private	BigDecimal penalizedAmt = BigDecimal.ZERO;

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

	public BigDecimal getPenalizedAmt() {
		return penalizedAmt;
	}

	public void setPenalizedAmt(BigDecimal penalizedAmt) {
		this.penalizedAmt = penalizedAmt;
	}  

}
