package net.engining.pcx.cc.process.service.support.refactor;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationEvent;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.InOutFlagDef;
import net.engining.pcx.cc.param.model.enums.PostGlInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;

/**
 * 根据LoanTransformEvent重构，针对批量直接记账 入会计分录拆分交易流水表
 * 
 * @author xiachuanhu
 *
 */
public class DirectAccountSplitEvent extends ApplicationEvent {
	public DirectAccountSplitEvent(Object source) {
		super(source);
	}

	private static final long serialVersionUID = 1L;
	/**
	 * 表内表外标志
	 */
	private InOutFlagDef inoutFlag;
	/**
	 * 借方科目号
	 */
	private String dbSubCd;
	/**
	 * 贷方科目号
	 */
	private String crSubCd;

	/**
	 * 记账金额
	 */
	private BigDecimal accountingAmt;
	/**
	 * 分录序号
	 */
	private int volSeq;
	/**
	 * 借贷标志
	 */
	private TxnDirection txnDirection;
	/**
	 * 交易日期
	 */
	private Date trdate;
	/**
	 * 记账日期
	 */
	private LocalDate postDate;
	/**
	 * 辅助核算项
	 */
	private String assistData;
	/**
	 * 总账入账方式
	 */
	private PostGlInd postGlInd;
	/**
	 * 流水号
	 */
	private String gltSeq;
	/**
	 * 红蓝字标识
	 */
	private RedBlueInd redBlueInd;
	

	public RedBlueInd getRedBlueInd() {
		return redBlueInd;
	}

	public void setRedBlueInd(RedBlueInd redBlueInd) {
		this.redBlueInd = redBlueInd;
	}

	public InOutFlagDef getInoutFlag() {
		return inoutFlag;
	}

	public void setInoutFlag(InOutFlagDef inoutFlag) {
		this.inoutFlag = inoutFlag;
	}

	public String getDbSubCd() {
		return dbSubCd;
	}

	public void setDbSubCd(String dbSubCd) {
		this.dbSubCd = dbSubCd;
	}

	public String getCrSubCd() {
		return crSubCd;
	}

	public void setCrSubCd(String crSubCd) {
		this.crSubCd = crSubCd;
	}

	public BigDecimal getAccountingAmt() {
		return accountingAmt;
	}

	public void setAccountingAmt(BigDecimal accountingAmt) {
		this.accountingAmt = accountingAmt;
	}

	public int getVolSeq() {
		return volSeq;
	}

	public void setVolSeq(int volSeq) {
		this.volSeq = volSeq;
	}

	public TxnDirection getTxnDirection() {
		return txnDirection;
	}

	public void setTxnDirection(TxnDirection txnDirection) {
		this.txnDirection = txnDirection;
	}

	public Date getTrdate() {
		return trdate;
	}

	public void setTrdate(Date trdate) {
		this.trdate = trdate;
	}

	public LocalDate getPostDate() {
		return postDate;
	}

	public void setPostDate(LocalDate postDate) {
		this.postDate = postDate;
	}

	public String getAssistData() {
		return assistData;
	}

	public void setAssistData(String assistData) {
		this.assistData = assistData;
	}

	public PostGlInd getPostGlInd() {
		return postGlInd;
	}

	public void setPostGlInd(PostGlInd postGlInd) {
		this.postGlInd = postGlInd;
	}

	public String getGltSeq() {
		return gltSeq;
	}

	public void setGltSeq(String gltSeq) {
		this.gltSeq = gltSeq;
	}



}
