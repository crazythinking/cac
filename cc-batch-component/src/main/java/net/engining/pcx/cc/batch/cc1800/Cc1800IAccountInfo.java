package net.engining.pcx.cc.batch.cc1800;

import java.util.ArrayList;
import java.util.List;

import net.engining.pcx.cc.file.model.LoanXfrRptItem;
import net.engining.pcx.cc.file.model.RejectTxnJournalRptItem;
import net.engining.pcx.cc.file.model.SubAcctSumItem;
import net.engining.pcx.cc.file.model.TxnJournalRptItem;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.CactCardGroup;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnReject;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnUnstmt;
import net.engining.pcx.cc.process.model.AcctModel;

/**
 * 账户主表(cact_account)<br>
 * 子账户表(cact_sub_acct)<br>
 * 当日入账交易表(cact_TXN_POST)-清算交易表<br>
 * 授权未达账表(auth_UNMATCH_O)-静态数据<br>
 * 授权匹配状态-unmatchStates：M-成功匹配；U-未匹配；E-过期授权<br>
 * 卡组信息表(cact_card_group)
 * <br>
 * 入账交易历史表(cact_TXN_HST)<br>
 * 挂账交易历史表(cact_TXN_REJECT)<br>
 * 未出账单交易历史表(cact_TXN_UNSTMT)<br>
 * 账单交易历史(cact_STMT_HST)<br>
 * <br>
 * 还款成功短信通知接口文件-DSucessMessInterfaceItem<br>
 * 催收接口文件-CollectionItem<br>
 * 账单统计信息文件-StmtInterfaceItem<br>
 * --总账交易流水文件-GlTxnItem<br>
 * <br>
 * --异常账户报表-ExceptionAccountRptItem(org/账号/账户类型/默认卡号/异常原因)<br>
 * 当日交易流水报表-TxnJournalRptItem【包括：当日内部生成交易表】(ORG/ACCT_NO/ACCT_TYPE/POSTING_FLAG/PRE_POSTING_FLAG/CARD_NO/TXN_CODE/TXN_DATE/TXN_TIME/TXN_AMOUNT/PLAN_NBR/REF_NBR/账户的BLOCK_CD/卡片的BLOCK_CD)<br>
 * 当日挂账交易流水报表-RejectTxnJournalRptItem(ORG/ACCT_NO/ACCT_TYPE/POSTING_FLAG/PRE_POSTING_FLAG/CARD_NO/TXN_CODE/TXN_DATE/TXN_TIME/TXN_AMOUNT/PLAN_NBR/REF_NBR/账户的BLOCK_CD/卡片的BLOCK_CD)<br>
 * 授权成功匹配报表-MatchAuthJournalRptItem(ORG/ACCT_NO/ACCT_TYPE/CARD_NO/LOG_OL_TIME/TXN_AMT/AUTH_CODE/TXN_STATUS/TXN_TYPE/status（即匹配结果 M E U）)<br>
 * 授权未匹配报表-UnmatchAuthJournalRptItem(ORG/ACCT_NO/ACCT_TYPE/CARD_NO/LOG_OL_TIME/TXN_AMT/AUTH_CODE/TXN_STATUS/TXN_TYPE/status（即匹配结果 M E U）)<br>
 * 过期授权报表-ExpiredAuthJournalRptItem(ORG/ACCT_NO/ACCT_TYPE/CARD_NO/LOG_OL_TIME/TXN_AMT/AUTH_CODE/TXN_STATUS/TXN_TYPE/status（即匹配结果 M E U）)<br>
 * 超限账户报表-OverLimitAccountRptItem(org/账号/账户类型/账户余额/超限部分金额/超限费/账户有效额度)<br>
 * 内部生成积分交易报表-TxnPointsRptItem(org/账号/账户类型/CARD NO/TXN_DATE/TXN_CODE(指原金融交易的交易码)/TXN_TIME/TXN_AMT/REF_NBR/POINTS)<br>
 * 分期XFR报表-LoanXfrRptItem(org/账号/账户类型/CARD NO/TXN_DATE/TXN_CODE/TXN_AMT/PLAN_NBR/REF_NBR)<br>
 */
public class Cc1800IAccountInfo{

	/**
	 * 账户信息
	 */
	private CactAccount cactAccount;

	/**
	 * 子账户列表
	 */
	private List<CactSubAcct> cactSubAccts = new ArrayList<CactSubAcct>();
	
	/**
	 * 最小还款额历史
	 */
	private List<CactAgeDue> cactAgeDues = new ArrayList<CactAgeDue>();
	
	/**
	 * 卡组信息表
	 */
	private List<CactCardGroup> cactCardGroups = new ArrayList<CactCardGroup>();

	/**
	 * 账户对应的当日入账交易List
	 */
	private List<CactTxnPost> cactTxnPosts = new ArrayList<CactTxnPost>();

	/**
	 * 入账交易历史表(TM_TXN_HST)
	 */
	private List<CactTxnHst> cactTxnHsts = new ArrayList<CactTxnHst>();

	/**
	 * 挂账交易历史表(TM_TXN_REJECT)
	 */
	private List<CactTxnReject> cactTxnRejects = new ArrayList<CactTxnReject>();

	/**
	 * 未出账单交易历史表(TM_TXN_UNSTMT)
	 */
	private List<CactTxnUnstmt> cactTxnUnstmts = new ArrayList<CactTxnUnstmt>();
	
	/**
	 * 当日交易流水报表
	 */
	private List<TxnJournalRptItem> txnJournals = new ArrayList<TxnJournalRptItem>();
	
	/**
	 * 当日挂账交易流水表
	 */
	private List<RejectTxnJournalRptItem> rejectTxnJournals = new ArrayList<RejectTxnJournalRptItem>();
	
	/**
	 * 分期XFR报表
	 */
	private List<LoanXfrRptItem> loanXfrs = new ArrayList<LoanXfrRptItem>();
	
	/**
	 * 分户账汇总信息文件
	 */
	private List<SubAcctSumItem> subAcctSumItems = new ArrayList<SubAcctSumItem>();

	/**
	 * 批量起始时账户状态
	 * 用于处理总账结转交易生成
	 */
	private CactAccount preCactAccount;

	/**
	 * 批量起始时信用计划状态
	 * 用于处理总账结转交易生成
	 */
	private List<CactSubAcct> preCactSubAccts = new ArrayList<CactSubAcct>();
	
	/**
	 * 当前结构体对应的 {@link AcctModel}，用于系统重构的过渡
	 */
	private AcctModel acctModel;

	public CactAccount getCactAccount() {
		return cactAccount;
	}

	public void setCactAccount(CactAccount cactAccount) {
		this.cactAccount = cactAccount;
	}

	public List<CactSubAcct> getCactSubAccts() {
		return cactSubAccts;
	}

	public void setCactSubAccts(List<CactSubAcct> cactSubAccts) {
		this.cactSubAccts = cactSubAccts;
	}

	public List<CactTxnPost> getCactTxnPosts() {
		return cactTxnPosts;
	}

	public void setCactTxnPosts(List<CactTxnPost> cactTxnPosts) {
		this.cactTxnPosts = cactTxnPosts;
	}

	public List<CactTxnHst> getCactTxnHsts() {
		return cactTxnHsts;
	}

	public void setCactTxnHsts(List<CactTxnHst> cactTxnHsts) {
		this.cactTxnHsts = cactTxnHsts;
	}

	public List<CactTxnReject> getCactTxnRejects() {
		return cactTxnRejects;
	}

	public void setCactTxnRejects(List<CactTxnReject> cactTxnRejects) {
		this.cactTxnRejects = cactTxnRejects;
	}

	public List<CactTxnUnstmt> getCactTxnUnstmts() {
		return cactTxnUnstmts;
	}

	public void setCactTxnUnstmts(List<CactTxnUnstmt> cactTxnUnstmts) {
		this.cactTxnUnstmts = cactTxnUnstmts;
	}

	public CactAccount getPreCactAccount() {
		return preCactAccount;
	}

	public void setPreCactAccount(CactAccount preCactAccount) {
		this.preCactAccount = preCactAccount;
	}

	public List<CactSubAcct> getPreCactSubAccts() {
		return preCactSubAccts;
	}

	public void setPreCactSubAccts(List<CactSubAcct> preCactSubAccts) {
		this.preCactSubAccts = preCactSubAccts;
	}
	
	public void setSubAcctSumItems(List<SubAcctSumItem> subAcctSumItems) {
		this.subAcctSumItems = subAcctSumItems;
	}

	public List<TxnJournalRptItem> getTxnJournals() {
		return txnJournals;
	}

	public void setTxnJournals(List<TxnJournalRptItem> txnJournals) {
		this.txnJournals = txnJournals;
	}
	
	public List<RejectTxnJournalRptItem> getRejectTxnJournals() {
		return rejectTxnJournals;
	}

	public void setRejectTxnJournals(List<RejectTxnJournalRptItem> rejectTxnJournals) {
		this.rejectTxnJournals = rejectTxnJournals;
	}
	
	public List<LoanXfrRptItem> getLoanXfrs() {
		return loanXfrs;
	}

	public void setLoanXfrs(List<LoanXfrRptItem> loanXfrs) {
		this.loanXfrs = loanXfrs;
	}
	
	public List<SubAcctSumItem> getSubAcctSumItems() {
		return subAcctSumItems;
	}

	public void setsubAcctSumItems(List<SubAcctSumItem> subAcctSumItems) {
		this.subAcctSumItems = subAcctSumItems;
	}

	public List<CactCardGroup> getCactCardGroups() {
		return cactCardGroups;
	}

	public void setCactCardGroups(List<CactCardGroup> cactCardGroups) {
		this.cactCardGroups = cactCardGroups;
	}

	public List<CactAgeDue> getCactAgeDues() {
		return cactAgeDues;
	}

	public void setCactAgeDues(List<CactAgeDue> cactAgeDues) {
		this.cactAgeDues = cactAgeDues;
	}

	public AcctModel getAcctModel() {
		return acctModel;
	}

	public void setAcctModel(AcctModel acctModel) {
		this.acctModel = acctModel;
	}
}