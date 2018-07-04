package net.engining.pcx.cc.process.service.support;

import java.math.BigDecimal;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationEvent;

/**
 * 结罚息事件
 * @author binarier
 *
 */
public class PenaltySettleEvent extends ApplicationEvent
{
	public PenaltySettleEvent(Object source)
	{
		super(source);
	}

	private static final long serialVersionUID = 1L;
	
	/**
	 * 子账户
	 */
	private int subAcctId;
	
	/**
	 * 结罚息金额，不带符号
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
}
