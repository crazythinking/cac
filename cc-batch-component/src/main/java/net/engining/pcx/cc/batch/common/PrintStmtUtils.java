package net.engining.pcx.cc.batch.common;

import net.engining.pcx.cc.file.model.StmtInterfaceItem;
import net.engining.pcx.cc.file.model.StmttxnInterfaceItem;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnUnstmt;

import org.springframework.stereotype.Service;


/**
 * 账单工具类
 * 
 * @author Heyu.wang
 */
@Service
public class PrintStmtUtils {

	/**
	 * 创建账单汇总信息接口文件项
	 * 
	 * @param stmtHst
	 * @param account
	 * @return
	 */
	public StmtInterfaceItem createStmtItem(CactStmtHst stmtHst) {
		if (stmtHst == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmtInterfaceItem item = new StmtInterfaceItem();
		item.acctNo = stmtHst.getAcctSeq();
		item.ageCd = stmtHst.getAgeCd();
		item.creditLimit = stmtHst.getAcctLimit();
		item.ctdAmtDb = stmtHst.getCtdAmtDb();	
		item.ctdPaymentAmt = stmtHst.getCtdPaymentAmt();
		item.currCd = stmtHst.getCurrCd();
		item.email = stmtHst.getEmail();
		item.gender = stmtHst.getGender();
		item.mobileNo = stmtHst.getMobileNo();
		item.name = stmtHst.getName();
		item.org = stmtHst.getOrg();
		item.pmtDueDate = stmtHst.getPmtDueDate();
		item.qualGraceBal = stmtHst.getQualGraceBal();
		item.stmtBegBal = stmtHst.getStmtBegBal();
		item.stmtCurrBal = stmtHst.getStmtCurrBal();
		item.stmtDate = stmtHst.getStmtDate();
		item.stmtMediaType = stmtHst.getStmtMediaType();
		item.tempLimit = stmtHst.getTempLimit();
		item.tempLimitBeginDate = stmtHst.getTempLimitBeginDate();
		item.tempLimitEndDate = stmtHst.getTempLimitEndDate();
		item.totDueAmt = stmtHst.getTotDueAmt();

		return item;
	}
	
	/**
	 * 补打账单信息生成
	 * 获取账单历史统计信息后,替换账单地址信息
	 * @param stmtHst
	 * @param account
	 * @return
	 */
	public StmtInterfaceItem createStmtItem(CactStmtHst stmtHst, CactAccount account) {
		if (stmtHst == null || account == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmtInterfaceItem item = createStmtItem(stmtHst);
//		item.email = account.getEmail();
		item.gender = stmtHst.getGender();
		item.mobileNo = stmtHst.getMobileNo();
//		item.stmtMediaType = account.getStmtMediaType();

		return item;
	}

	/**
	 * 创建账单交易明细接口文件项
	 * 
	 * @param txnHst
	 * @return
	 */
	public StmttxnInterfaceItem createStmttxnItem(CactTxnHst txnHst) {
		if (txnHst == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmttxnInterfaceItem item = new StmttxnInterfaceItem();
		item.acctNo = txnHst.getAcctSeq();
		item.businessType = txnHst.getBusinessType();
		item.acqBranchId = txnHst.getAcqBranchId();
		item.acqAcceptorId = txnHst.getAcqAcceptorId();
		item.acqTerminalId = txnHst.getAcqTerminalId();
		item.authCode = txnHst.getAuthCode();
		item.cardNo = txnHst.getCardNo();
		item.dbCrInd = txnHst.getDbCrInd();
		item.mcc = txnHst.getMcc();
		item.org = txnHst.getOrg();
		item.postAmt = txnHst.getPostAmt();
		item.postCurrCd = txnHst.getPostCurrCd();
		item.postDate = txnHst.getPostDate();
		item.postingFlag = txnHst.getPostingFlag();
		item.prePostingFlag = txnHst.getPrePostingFlag();
		item.postTxnType = txnHst.getPostTxnType();
		item.refNbr = txnHst.getRefNbr();
		item.stmtDate = txnHst.getStmtDate();
		item.txnSeq = txnHst.getTxnSeq();
		item.txnAmt = txnHst.getTxnAmt();
		item.txnCode = txnHst.getPostCode();
		item.txnCurrCd = txnHst.getTxnCurrCd();
		item.txnDate = txnHst.getTxnDate();
		item.txnDesc = txnHst.getTxnDesc();
		item.txnTime = txnHst.getTxnTime();

		return item;
	}

	/**
	 * 创建账单交易明细接口文件项
	 * 
	 * @param txnHst
	 * @return
	 */
	public StmttxnInterfaceItem createStmttxnItem(CactTxnUnstmt txnUnstmt) {
		if (txnUnstmt == null) {
			throw new IllegalArgumentException("输入null");
		}

		StmttxnInterfaceItem item = new StmttxnInterfaceItem();
		item.acctNo = txnUnstmt.getAcctSeq();
		item.businessType = txnUnstmt.getBusinessType();
		item.acqBranchId = txnUnstmt.getAcqBranchId();
		item.acqAcceptorId = txnUnstmt.getAcqAcceptorId();
		item.acqTerminalId = txnUnstmt.getAcqTerminalId();
		item.authCode = txnUnstmt.getAuthCode();
		item.cardNo = txnUnstmt.getCardNo();
		item.dbCrInd = txnUnstmt.getDbCrInd();
		item.mcc = txnUnstmt.getMcc();
		item.org = txnUnstmt.getOrg();
		item.postAmt = txnUnstmt.getPostAmt();
		item.postCurrCd = txnUnstmt.getPostCurrCd();
		item.postDate = txnUnstmt.getPostDate();
		item.postingFlag = txnUnstmt.getPostingFlag();
		item.prePostingFlag = txnUnstmt.getPrePostingFlag();
		item.refNbr = txnUnstmt.getRefNbr();
		item.stmtDate = txnUnstmt.getStmtDate();
		item.txnSeq = txnUnstmt.getTxnSeq();
		item.txnAmt = txnUnstmt.getTxnAmt();
		item.txnCode = txnUnstmt.getPostCode();
		item.txnCurrCd = txnUnstmt.getTxnCurrCd();
		item.txnDate = txnUnstmt.getTxnDate();
		item.txnDesc = txnUnstmt.getTxnDesc();
		item.txnTime = txnUnstmt.getTxnTime();
		return item;
	}

}
