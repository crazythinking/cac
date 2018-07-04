package net.engining.pcx.cc.process.service.account;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;

/**
 * 入账交易信息
 * @author binarier
 *
 */
public class PostDetail
{
    private Date txnDate;
    private Date txnTime;
    private PostTxnTypeDef postTxnType;
    private String postCode;
    private BigDecimal txnAmt;
    private String txnCurrCd;
    private BigDecimal postAmt;
    private String postCurrCd;
    /**
     * optional
     */
    private String txnDesc;
    /**
     * optional
     */
    private String txnShortDesc;
    /**
     * optional
     */
    private String txnDetailSeq;
    /**
     * optional
     */
    private TxnDetailType txnDetailType;
    /**
     * optional
     */
    private BigDecimal acctBal;//入账前账户余额
    /**
     * optional
     */
    private String txnType;//交易类型，外部系统使用
    /**
     * optional
     */
	private String oppAcct;//交易对手账号，外部系统使用
    
	public Date getTxnDate()
	{
		return txnDate;
	}
	public void setTxnDate(Date txnDate)
	{
		this.txnDate = txnDate;
	}
	public Date getTxnTime()
	{
		return txnTime;
	}
	public void setTxnTime(Date txnTime)
	{
		this.txnTime = txnTime;
	}
	public PostTxnTypeDef getPostTxnType()
	{
		return postTxnType;
	}
	public void setPostTxnType(PostTxnTypeDef postTxnType)
	{
		this.postTxnType = postTxnType;
	}
	public String getPostCode()
	{
		return postCode;
	}
	public void setPostCode(String postCode)
	{
		this.postCode = postCode;
	}
	public BigDecimal getTxnAmt()
	{
		return txnAmt;
	}
	public void setTxnAmt(BigDecimal txnAmt)
	{
		this.txnAmt = txnAmt;
	}
	public BigDecimal getPostAmt()
	{
		return postAmt;
	}
	public void setPostAmt(BigDecimal postAmt)
	{
		this.postAmt = postAmt;
	}
	public String getTxnCurrCd()
	{
		return txnCurrCd;
	}
	public void setTxnCurrCd(String txnCurrCd)
	{
		this.txnCurrCd = txnCurrCd;
	}
	public String getPostCurrCd()
	{
		return postCurrCd;
	}
	public void setPostCurrCd(String postCurrCd)
	{
		this.postCurrCd = postCurrCd;
	}
	public String getTxnDesc()
	{
		return txnDesc;
	}
	public void setTxnDesc(String txnDesc)
	{
		this.txnDesc = txnDesc;
	}
	public String getTxnShortDesc()
	{
		return txnShortDesc;
	}
	public void setTxnShortDesc(String txnShortDesc)
	{
		this.txnShortDesc = txnShortDesc;
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
	public BigDecimal getAcctBal() {
		return acctBal;
	}
	public void setAcctBal(BigDecimal acctBal) {
		this.acctBal = acctBal;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getOppAcct() {
		return oppAcct;
	}
	public void setOppAcct(String oppAcct) {
		this.oppAcct = oppAcct;
	}
	
}
