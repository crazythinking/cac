package net.engining.pcx.cc.process.service.support.refactor;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationEvent;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;

/**
 * 针对日终批量记账 入当日总账交易流水
 * 
 * @author xiachuanhu
 *
 */
public class DirectAccountingEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public DirectAccountingEvent(Object source) {
		super(source);
	}

	/*
	 * 账户编号
	 */
	private Integer acctSeq;
	/*
	 * 入账交易码
	 */
	private String postCode;
	/*
	 * 入账金额
	 */
	private BigDecimal postAmount;
	/*
	 * 交易日期
	 */
	private Date trdate;
	/*
	 * 清算日期
	 */
	private Date clearDate;
	/*
	 * 记账日期
	 */
	private Date postDate;
	/*
	 * 来源流水号
	 */
	private String txnDetailSeq;
	/*
	 * 来源流水对应的业务明细类型
	 */
	private TxnDetailType txnDetailType;

	public Integer getAcctSeq() {
		return acctSeq;
	}

	public void setAcctSeq(Integer acctSeq) {
		this.acctSeq = acctSeq;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public BigDecimal getPostAmount() {
		return postAmount;
	}

	public void setPostAmount(BigDecimal postAmount) {
		this.postAmount = postAmount;
	}

	public Date getTrdate() {
		return trdate;
	}

	public void setTrdate(Date trdate) {
		this.trdate = trdate;
	}

	public Date getClearDate() {
		return clearDate;
	}

	public void setClearDate(Date clearDate) {
		this.clearDate = clearDate;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public String getTxnDetailSeq() {
		return txnDetailSeq;
	}

	public void setTxnDetailSeq(String txnDetailSeq) {
		this.txnDetailSeq = txnDetailSeq;
	}

	public TxnDetailType getTxnDetailType() {
		return txnDetailType;
	}

	public void setTxnDetailType(TxnDetailType txnDetailType) {
		this.txnDetailType = txnDetailType;
	}

}
