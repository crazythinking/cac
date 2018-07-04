package net.engining.pcx.cc.process.service.support;

import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.PostDetail;

import org.springframework.context.ApplicationEvent;

/**
 * CactTxnPost当日入账交易表，产生入账前的入账事件定义
 * @author luxue
 *
 */
public class PrePostEvent extends ApplicationEvent
{
	public PrePostEvent(Object source)
	{
		super(source);
	}

	private static final long serialVersionUID = 1L;
	
	private int subAcctId;
	
	private PostDetail detail;
	
	private AcctModel model;
	
	private boolean offset;
	
	private int stmtHist;

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

	public AcctModel getModel()
	{
		return model;
	}

	public void setModel(AcctModel model)
	{
		this.model = model;
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

	
}
