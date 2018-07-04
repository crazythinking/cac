package net.engining.pcx.cc.process.service.support;

import java.math.BigDecimal;

import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.PostDetail;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationEvent;

/**
 * CactTxnPost当日入账交易表，产生入账后的入账事件定义
 * @author luxue
 *
 */
public class PostPostEvent extends ApplicationEvent
{
	public PostPostEvent(Object source)
	{
		super(source);
	}

	private static final long serialVersionUID = 1L;
	
	private int subAcctId;
	
	private String postCode;
	
	private BigDecimal postAmount;
	
	private LocalDate postDate;
	
	private PostDetail detail;
	
	private AcctModel acctModel;
	
	private boolean offset;
	
	private int stmtHist;
	
	private Integer txnPostSeq;

	public int getSubAcctId()
	{
		return subAcctId;
	}

	public void setSubAcctId(int subAcctId)
	{
		this.subAcctId = subAcctId;
	}

	public PostDetail getDetail()
	{
		return detail;
	}

	public void setDetail(PostDetail detail)
	{
		this.detail = detail;
	}

	public boolean isOffset()
	{
		return offset;
	}

	public void setOffset(boolean offset)
	{
		this.offset = offset;
	}

	public int getStmtHist()
	{
		return stmtHist;
	}

	public void setStmtHist(int stmtHist)
	{
		this.stmtHist = stmtHist;
	}

	public BigDecimal getPostAmount() {
		return postAmount;
	}

	public void setPostAmount(BigDecimal postAmount) {
		this.postAmount = postAmount;
	}

	public LocalDate getPostDate() {
		return postDate;
	}

	public void setPostDate(LocalDate postDate) {
		this.postDate = postDate;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public AcctModel getAcctModel() {
		return acctModel;
	}

	public void setAcctModel(AcctModel acctModel) {
		this.acctModel = acctModel;
	}

	public Integer getTxnPostSeq() {
		return txnPostSeq;
	}

	public void setTxnPostSeq(Integer txnPostSeq) {
		this.txnPostSeq = txnPostSeq;
	}

}
