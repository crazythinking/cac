package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.math.BigDecimal;

import net.engining.gm.infrastructure.enums.BusinessType;

public class BasicAccountInfo implements Serializable{

	private static final long serialVersionUID = 4978374696446886922L;

	/**
	 * 账号
	 */
	private Integer accountNo;
	
	/**
	 * 业务类型
	 */
	private BusinessType businessType;
	
	/**
	 * 货币代码
	 */
	private String currencyCode;

	/**
	 * 信用额度
	 */
	private BigDecimal creditLimit;
	
	/**
	 * 账单日
	 */
	private String billingDay;

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(BigDecimal creditLimit) {
		this.creditLimit = creditLimit;
	}

	public String getBillingDay() {
		return billingDay;
	}

	public void setBillingDay(String billingDay) {
		this.billingDay = billingDay;
	}
	public Integer getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(Integer accountNo) {
		this.accountNo = accountNo;
	}

	public BusinessType getBusinessType() {
		return businessType;
	}

	public void setBusinessType(BusinessType businessType) {
		this.businessType = businessType;
	}
}
