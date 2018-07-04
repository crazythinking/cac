package net.engining.pcx.cc.process.service.support.refactor;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationEvent;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

/**
 * 根据LoanTransformEvent重构，针对独立记账
 * @author luxue
 *
 */
public class LoanTransformEvent4Re extends ApplicationEvent
{
	public LoanTransformEvent4Re(Object source)
	{
		super(source);
	}

	private static final long serialVersionUID = 1L;
	
	/**
	 * CactAccont 主键
	 */
	private Integer acctSeq;
	
	/**
	 * 子账户表实例
	 */
	private CactSubAcct cactSubAcct;
	
	/**
	 * 来源交易流水类型
	 */
	private TxnDetailType txnDetailType;
	
	/**
	 * 来源交易流水号
	 */
	private String txnDetailSeq;
	
	/**
	 * 清算日期
	 */
	private LocalDate clearDate;
	
	/**
	 * 交易日期
	 */
	private LocalDate transDate;
	
	/**
	 * 交易前账龄组
	 */
	private AgeGroupCd orginalAgeGroupCd;
	
	/**
	 * 交易后账龄组
	 */
	private AgeGroupCd newAgeGroupCd;
	
	/**
	 * 交易前账龄
	 */
	private String orginalAgeCd;
	
	/**
	 * 交易后账龄
	 */
	private String newAgeCd;
	
	public LocalDate getClearDate() {
		return clearDate;
	}

	public void setClearDate(LocalDate clearDate) {
		this.clearDate = clearDate;
	}

	public LocalDate getTransDate() {
		return transDate;
	}

	public void setTransDate(LocalDate transDate) {
		this.transDate = transDate;
	}

	public Integer getAcctSeq() {
		return acctSeq;
	}

	public void setAcctSeq(Integer acctSeq) {
		this.acctSeq = acctSeq;
	}

	public CactSubAcct getCactSubAcct() {
		return cactSubAcct;
	}

	public void setCactSubAcct(CactSubAcct cactSubAcct) {
		this.cactSubAcct = cactSubAcct;
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
