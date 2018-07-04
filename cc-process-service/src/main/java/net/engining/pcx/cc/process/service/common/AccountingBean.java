package net.engining.pcx.cc.process.service.common;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.pcx.cc.param.model.enums.RedBlueInd;

public class AccountingBean {
	
	private BigDecimal amount;//金额
	
	
	private String currency;//币种
	
	private String dbsubject;//借方科目号
	
	private String crsubject;//贷方科目号
	
	private String account;//账号
	
	private String txndesc;//摘要
	
	private Date txnDate;//记账日期
	
	private String branch;//机构
	
	private RedBlueInd stDbRedFlag;//红蓝字
	
	
	public String getDbsubject() {
		return dbsubject;
	}

	public void setDbsubject(String dbsubject) {
		this.dbsubject = dbsubject;
	}

	public String getCrsubject() {
		return crsubject;
	}

	public void setCrsubject(String crsubject) {
		this.crsubject = crsubject;
	}

	public RedBlueInd getStDbRedFlag() {
		return stDbRedFlag;
	}

	public void setStDbRedFlag(RedBlueInd stDbRedFlag) {
		this.stDbRedFlag = stDbRedFlag;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public Date getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(Date txnDate) {
		this.txnDate = txnDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}



	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}



	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getTxndesc() {
		return txndesc;
	}

	public void setTxndesc(String txndesc) {
		this.txndesc = txndesc;
	}
	
	

}
