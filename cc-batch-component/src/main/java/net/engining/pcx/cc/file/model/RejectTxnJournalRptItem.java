package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pg.support.cstruct.CChar;


/**
 * 当日挂账交易流水表
 * @author zhengpy
 * (ORG/ACCT_NO/ACCT_TYPE/账户的POSTING_FLAG/账户的PRE_POSTING_FLAG/账户的BLOCK_CD/
 * CARD_NO/TXN_CODE/TXN_DATE/TXN_TIME/TXN_AMOUNT/PLAN_NBR/REF_NBR/卡片的BLOCK_CD)<br>
 */
public class RejectTxnJournalRptItem {

	/**
	 * 机构号码
	 */
	@CChar( value = 12, order = 100 )
	public String org;

	/**
	 * 账户号码
	 */
	@CChar( value = 20, order = 200 )
	public Integer acctNo;
	
	/**
	 * 业务类型
	 */
	@CChar( value = 1, order = 300 )
	public BusinessType businessType;

	/**
	 * 入账结果标示码
	 */
	@CChar( value = 3, order = 400 )
	public PostingFlag postingFlag;
	
	/**
	 * 往日入账结果标示码
	 */
	@CChar( value = 3, order = 500 )
	public PostingFlag prePostingFlag;

	/**
	 * 账户锁定码
	 */
	@CChar( value = 27, order = 600 )
	public String acctBlockCd;
	
	/**
     * 介质卡号
     */
	@CChar( value = 19, order = 700 )
	public String cardNo;
	
	/**
     * 交易码
     */
	@CChar( value = 4, order = 800 )
	public String txnCode;
	
	/**
     * 交易日期
     */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 900 )
	public Date txnDate;
	
	/**
     * 交易时间
     */
	@CChar( value = 6, datePattern = "hhmmss", order = 1000 )
	public Date txnTime;
	
	/**
	 * 交易入账金额
	 */
	@CChar( value = 15, precision = 2, zeroPadding = true, order = 1100 )
	public BigDecimal glPostAmt;
	
	/**
     * 子账户id
     */
	@CChar( value = 20, order = 1200 )
	public Integer subAcctId;
	
	/**
     * 交易参考号
     */
	@CChar( value = 23, order = 1300 )
	public String refNbr;

	/**
	 * 币种
	 */
	@CChar( value = 3, order = 1400 )
	public String currCd;
	
	/**
	 * 卡产品
	 */
	@CChar( value = 6, order = 1500 )
	public String productCd;
	
	/**
	 * 分行代码
	 */
	@CChar( value = 9, order = 1600 )
	public String acqBranchId;
	
	/**
	 * 交易描述
	 */
	@CChar( value = 40, order = 1700 )
	public String txnShortDesc;
}
