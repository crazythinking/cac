package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pg.support.cstruct.CChar;

/**
 * 分期XFR报表
 * @author zhengpy
 * (org/账号/账户类型/CARD NO/TXN_DATE/TXN_CODE/TXN_AMT/REF_NBR/PLAN_NBR)<br>
 */
public class LoanXfrRptItem {

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
     * 介质卡号
     */
	@CChar( value = 19, order = 400 )
	public String cardNo;

	/**
     * 交易日期
     */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 500 )
	public Date txnDate;
	
	/**
     * 交易码
     */
	@CChar( value = 4, order = 600 )
	public String txnCode;
	
	
	/**
	 * 交易入账金额
	 */
	@CChar( value = 15, precision = 2, zeroPadding = true, order = 700 )
	public BigDecimal glPostAmt;
	
	/**
     * 交易参考号
     */
	@CChar( value = 23, order = 800 )
	public String refNbr;

	/**
     * 子账户id
     */
	@CChar( value = 20, order = 900 )
	public Integer subAcctId;
	
	/**
	 * 币种
	 */
	@CChar( value = 3, order = 1000 )
	public String currCd;
}
