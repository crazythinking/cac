package net.engining.pcx.cc.process.service;

import java.math.BigDecimal;
import java.util.List;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

/**
 * 账户信息查询服务
 * @author Ronny
 *
 */
public interface AccountQueryService {
	
	/**
	 * 获取系统某个业务类型账户的总余额
	 * @return
	 */
	BigDecimal getSystemBal(BusinessType type, String currCd);
	
	/**
	 * 获取客户某个账号的总余额
	 * @param acctNo
	 * @return
	 */
	BigDecimal getCustBal(int acctNo, String currCd);
	
	/**
	 * 获取客户某个账户的余额
	 * @param acctSeq
	 * @return
	 */
	BigDecimal getCustAcctBal(int acctSeq);
	
	/**
	 * 获取客户某个业务类型账号的总余额
	 * @param acctNo
	 * @param type
	 * @param currCd
	 * @return
	 */
	BigDecimal getCustAcctBal(int acctNo, BusinessType type, String currCd);
	
	/**
	 * 获取某个账户下某个子账户类型的总余额 
	 * @param acctSeq
	 * @return
	 */
	BigDecimal getSubAcctBal(int acctSeq, String subAcctType, String currCd);
	
	/**
	 * 用account seq查询贷款剩余本金
	 * @param onlineTxnSeq
	 * @return
	 */
	BigDecimal getLoanAndTopyByAccountSeq(int accountSeq);
	
	/**
	 * 用account seq查询欠款金额(已入账金额)
	 * @param accountSeq
	 * @return
	 */
	BigDecimal getArrearsByAccountSeq(int accountSeq);
	
	/**
	 * 用account seq查询除费用外的欠款金额(已入账金额)
	 * @param accountSeq
	 * @return
	 */
	BigDecimal getArrearsExceptFeeByAccountSeq(int accountSeq);
	
	/**
	 * 用account seq查询欠款金额(已入账金额、未入账利息、未入账罚息)
	 * @param accountSeq
	 * @return
	 */
	BigDecimal getPaymentsByAccountSeq(int accountSeq);
	
	/**
	 * 用account seq查询子账户
	 * @param onlineTxnSeq
	 * @return
	 */
	List<CactSubAcct> getSubAcctsByAccountSeq(int accountSeq);
}
