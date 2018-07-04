package net.engining.pcx.cc.process.service.support;

import org.springframework.context.ApplicationEvent;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;

public class LoanTransformEvent extends ApplicationEvent
{
	public LoanTransformEvent(Object source)
	{
		super(source);
	}

	private static final long serialVersionUID = 1L;
	
	/**
	 * 发生形态变换的子账户
	 */
	private int subAcctId;
	
	/**
	 * 来源交易流水号
	 */
	private String txnDetailSeq;
	
	private AgeGroupCd orginalAgeGroupCd;
	
	private AgeGroupCd newAgeGroupCd;
	
	private String orginalAgeCd;
	
	private String newAgeCd;
	
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

	public AgeGroupCd getNewAgeGroupCd()
	{
		return newAgeGroupCd;
	}

	public void setNewAgeGroupCd(AgeGroupCd newAgeGroupCd)
	{
		this.newAgeGroupCd = newAgeGroupCd;
	}

	public String getOrginalAgeCd() {
		return orginalAgeCd;
	}

	public void setOrginalAgeCd(String orginalAgeCd) {
		this.orginalAgeCd = orginalAgeCd;
	}

	public String getNewAgeCd() {
		return newAgeCd;
	}

	public void setNewAgeCd(String newAgeCd) {
		this.newAgeCd = newAgeCd;
	}

	public AgeGroupCd getOrginalAgeGroupCd() {
		return orginalAgeGroupCd;
	}

	public void setOrginalAgeGroupCd(AgeGroupCd orginalAgeGroupCd) {
		this.orginalAgeGroupCd = orginalAgeGroupCd;
	}

}
