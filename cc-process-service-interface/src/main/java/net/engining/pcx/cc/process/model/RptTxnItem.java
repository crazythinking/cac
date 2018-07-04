package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pg.support.cstruct.CChar;

/**
 * 送报表接口(ETL、金融交易预处理)
 * 
 * @author yinxia
 *
 */
public class RptTxnItem implements Serializable {
	
	private static final long serialVersionUID = -7020893643870941539L;

	/**
	 * 机构号
	 */
	@CChar( value = 12, autoTrim = true, order = 100 )
	public String org;

	/**
	 * tc交易流水号
	 */
	@CChar( value = 10, order = 200 )
	public Integer ccTxnSeq;

	/**
	 * 账号
	 */
	@CChar( value = 20, zeroPadding = true, order = 300 )
	public Integer acctNo;
	
	/**
	 * 账户类型
	 */
	@CChar( value = 2, order = 400 )
	public BusinessType businessType;

	/**
	 * 介质卡号
	 */
	@CChar( value = 19, autoTrim = true, order = 500 )
	public String cardNo;

	/**
	 * 逻辑卡号
	 */
	@CChar( value = 19, autoTrim = true, order = 600 )
	public String cardGroupID;

	/**
	 * 对应主卡逻辑卡号
	 */
	@CChar( value = 19, autoTrim = true, order = 700 )
	public String bscLogiccardNo;
	
	/**
	 * 交易日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 900 )
	public Date txnDate;
	
	/**
	 * 交易时间
	 */
	@CChar( value = 14, datePattern = "yyyyMMddHHmmss", order = 1000 )
	public Date txnTime;
	
	/**
	 * 交易码
	 */
	@CChar( value = 4, autoTrim = true, order = 1200 )
	public String txnCode;
	
	/**
	 * 入账金额
	 */
	@CChar( value = 15, precision = 2, zeroPadding = true, order = 1300 )
	public BigDecimal postAmt;
	
	/**
	 * 入账日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 1400 )
	public Date postDate;
	
	/**
	 * 授权码
	 */
	@CChar( value = 6, autoTrim = true, order = 1500 )
	public String authCode;
	
	/**
	 * 入账币种代码
	 */
	@CChar( value = 3, autoTrim = true, order = 1600 )
	public String postCurrCd;
	
	/**
	 * 子账户号
	 */
	@CChar( value = 20, autoTrim = true, order = 1700 )
	public Integer subAcctId;
	
	/**
	 * 交易参考号
	 */
	@CChar( value = 23, autoTrim = true, order = 1800 )
	public String refNbr;
	
	/**
	 * 入账结果标识码
	 */
	@CChar( value = 3, autoTrim = true, order = 2000 )
	public PostingFlag postingFlag;
	
	/**
	 * 往日入账结果标识码
	 */
	@CChar( value = 3, autoTrim = true, order = 2100 )
	public PostingFlag prePostingFlag;
	
	/**
	 * 账单日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 2200 )
	public Date stmtDate;
	
	
}
