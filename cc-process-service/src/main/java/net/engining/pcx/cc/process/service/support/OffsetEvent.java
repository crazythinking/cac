package net.engining.pcx.cc.process.service.support;

import java.math.BigDecimal;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationEvent;

public class OffsetEvent extends ApplicationEvent
{
	public OffsetEvent(Object source)
	{
		super(source);
	}

	private static final long serialVersionUID = 1L;
	
	/**
	 * 目标冲销子账户
	 */
	private int subAcctId;
	
	private int acctSeq;
	
	/**
	 * 冲销金额，不带符号
	 */
	private BigDecimal amount;
	
	private LocalDate postDate;
	
	/**
	 * 来源交易流水号
	 */
	private String txnDetailSeq;
	
	/**
	 * 来源交易流水类型
	 */
	private TxnDetailType txnDetailType;

	public int getSubAcctId()
	{
		return subAcctId;
	}

	public void setSubAcctId(int subAcctId)
	{
		this.subAcctId = subAcctId;
	}

	public String getTxnDetailSeq()
	{
		return txnDetailSeq;
	}

	public void setTxnDetailSeq(String txnDetailSeq)
	{
		this.txnDetailSeq = txnDetailSeq;
	}

	public TxnDetailType getTxnDetailType()
	{
		return txnDetailType;
	}

	public void setTxnDetailType(TxnDetailType txnDetailType)
	{
		this.txnDetailType = txnDetailType;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public LocalDate getPostDate() {
		return postDate;
	}

	public void setPostDate(LocalDate postDate) {
		this.postDate = postDate;
	}

	public int getAcctSeq() {
		return acctSeq;
	}

	public void setAcctSeq(int acctSeq) {
		this.acctSeq = acctSeq;
	}


	
}
