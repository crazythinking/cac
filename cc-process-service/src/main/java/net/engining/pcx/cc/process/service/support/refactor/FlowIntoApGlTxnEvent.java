package net.engining.pcx.cc.process.service.support.refactor;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.context.ApplicationEvent;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.InOutFlagDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.param.model.enums.PostGlInd;

public class FlowIntoApGlTxnEvent extends ApplicationEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FlowIntoApGlTxnEvent(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 * 来源交易流水号
	 */
	private Integer txnSeq;
	
	/**
	 * 账户编号
	 */
	private Integer acctSeq;
	
	/**
	 * 入账代码
	 */
	private String postCode;
	
	/**
	 * 入账描述
	 */
//	private String postDesc;
	
	/**
	 * 借贷标志
	 */
//	private TxnDirection txnDirection;
	
	/**
	 * 入账日期
	 */
	private Date postDate;
	
	/**
	 * 入账金额
	 */
	private BigDecimal postAmount;
	/**
	 * 账龄组代码
	 * @return
	 */
//	private AgeGroupCd ageGroupCd;
	/**
	 * 子账户类型
	 * @return
	 */
	private String subAcctType;
	/**
	 * 期限
	 * @return
	 */
//	private String deadLine;
	/**
	 * 来源交易流水号
	 * @return
	 */
	private String txnDetailSeq;
	/**
	 * 表内外标志
	 * @return
	 */
//	private InOutFlagDef inOutFlag;
	/**
	 * 几张说明
	 * @return
	 */
//	private String accountDesc;
	/**
	 * 清算日期
	 * @return
	 */
	private Date clearDate;
	/**
	 * 交易日期
	 * @return
	 */
	private Date TransDate;

	public String getSubAcctType() {
		return subAcctType;
	}

	public void setSubAcctType(String subAcctType) {
		this.subAcctType = subAcctType;
	}

//	public String getDeadLine() {
//		return deadLine;
//	}
//
//	public void setDeadLine(String deadLine) {
//		this.deadLine = deadLine;
//	}

	public String getTxnDetailSeq() {
		return txnDetailSeq;
	}

	public void setTxnDetailSeq(String txnDetailSeq) {
		this.txnDetailSeq = txnDetailSeq;
	}

	public Date getClearDate() {
		return clearDate;
	}

	public void setClearDate(Date clearDate) {
		this.clearDate = clearDate;
	}

	public Date getTransDate() {
		return TransDate;
	}

	public void setTransDate(Date transDate) {
		TransDate = transDate;
	}

	public Integer getTxnSeq() {
		return txnSeq;
	}

	public void setTxnSeq(Integer txnSeq) {
		this.txnSeq = txnSeq;
	}

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

//	public String getPostDesc() {
//		return postDesc;
//	}
//
//	public void setPostDesc(String postDesc) {
//		this.postDesc = postDesc;
//	}

//	public TxnDirection getTxnDirection() {
//		return txnDirection;
//	}
//
//	public void setTxnDirection(TxnDirection txnDirection) {
//		this.txnDirection = txnDirection;
//	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public BigDecimal getPostAmount() {
		return postAmount;
	}

	public void setPostAmount(BigDecimal postAmount) {
		this.postAmount = postAmount;
	}
	
}
