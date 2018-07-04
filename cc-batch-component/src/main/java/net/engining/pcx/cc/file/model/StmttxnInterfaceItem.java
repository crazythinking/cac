package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pg.support.cstruct.CChar;

/**
 * 实体账单当期交易接口文件正文(STMTTXN_INTERFACE)，接口格式同入账交易接口格式TT_TXN_POST，接口文件中暂缺
 * 
 * @author heyu.wang
 * 
 */
public class StmttxnInterfaceItem {
	
	@CChar( value = 12, order = 100 )
	public String org;

	/**
	 * CC交易流水号
	 */
	@CChar( value = 8, order = 200 )
	public Integer txnSeq;

	@CChar( value = 8, order = 300 )
	public Integer acctNo;

	@CChar( value = 1, order = 400 )
	public BusinessType businessType;
 
	@CChar( value = 19, order = 500 )
	public String cardNo;

	@CChar( value = 19, order = 600 )
	public String bscLogiccardNo;
	
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 800 )
	public Date txnDate;

	@CChar( value = 14, datePattern = "yyyyMMddHHmmss", order = 900 )
	public Date txnTime;

	@CChar( value = 1, order = 1000 )
	public PostTxnTypeDef postTxnType;
	
	@CChar( value = 4, order = 1100 )
	public String txnCode;

	@CChar( value = 1, order = 1200 )
	public TxnDirection dbCrInd;

	@CChar( value = 15, precision = 2, order = 1300 )
	public BigDecimal txnAmt;
	
	@CChar( value = 15, precision = 2, order = 1400 )
	public BigDecimal postAmt;

	@CChar( value = 8, datePattern = "yyyyMMdd", order = 1500 )
	public Date postDate;
	
	@CChar( value = 6, order = 1600 )
	public String authCode;

	@CChar( value = 3, order = 1700 )
	public String txnCurrCd;

	@CChar( value = 3, order = 1800 )
	public String postCurrCd;

	@CChar( value = 23, order = 1900 )
	public String refNbr;

	@CChar( value = 40, order = 2000 )
	public String txnDesc;
	
	@CChar( value = 13, precision = 0, order = 2100 )
	public BigDecimal point;

	/**
	 * 入账结果标示码
	 */
	@CChar( value = 3, order = 2200 )
	public PostingFlag postingFlag;

	/**
	 * 往日入账结果标示码
	 */
	@CChar( value = 3, order = 2300 )
	public PostingFlag prePostingFlag;
	
	/**
	 * 公司卡还款金额
	 */
	@CChar( value = 15, precision = 2, order = 2400 )
	public BigDecimal relPmtAmt;

	/**
	 * 受理分行代码
	 */
	@CChar( value = 9, order = 2500 )
	public String acqBranchId;

	/**
	 * 受理机构终端标识码
	 */
	@CChar( value = 8, order = 2600 )
	public String acqTerminalId;

	/**
	 * 受卡方标识码
	 */
	@CChar( value = 15, order = 2700 )
	public String acqAcceptorId;

	/**
	 * 商户类别代码
	 */
	@CChar( value = 4, order = 2800 )
	public String mcc;

	/**
	 * 原交易货币转换费
	 */
	@CChar( value = 15, precision = 2, order = 2900 )
	public BigDecimal interchangeFee;

	/**
	 * 原交易交易手续费
	 */
	@CChar( value = 15, precision = 2, order = 3000 )
	public BigDecimal inputTxnFee;

	/**
	 * 发卡方应得手续费收入
	 */
	@CChar( value = 15, precision = 2, order = 3100 )
	public BigDecimal feeProfit;

	/**
	 * 分期交易发卡行收益
	 */
	@CChar( value = 15, precision = 2, order = 3200 )
	public BigDecimal loanIssueProfit;

	/**
	 * 账单日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 3300 )
	public Date stmtDate;

	/**
	 * 销售单凭证号
	 */
	@CChar( value = 7, order = 3400 )
	public String voucherNo;
}
