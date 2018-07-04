package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pg.support.cstruct.CChar;

/**
 * 分户账汇总信息流水文件
 * @author zhengpy
 *
 */
public class SubAcctSumItem {

	/**
	 * 机构号
	 */
	@CChar( value = 12, order = 100 )
	public String org;
	
	/**
	 * 账号
	 */
	@CChar( value = 20, order = 200 )
	public Integer acctNo;
	
	/**
	 * 业务类型
	 */
	@CChar( value = 1, order = 300 )
	public BusinessType businessType;
	
	/**
	 * 币种
	 */
	@CChar( value = 3, order = 400 )
	public String currCd;

	/**
	 * 账龄
	 */
	@CChar( value = 1, order = 500 )
	public String ageCd;
	
	/**
	 * 交易码
	 */
	@CChar( value = 4, order = 600 )
	public String postCode;
	
	/**
	 * 入账日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 700 )
	public Date postDate;
	
	/**
	 * 入账金额
	 */
	@CChar( value = 15, precision = 2, order = 800 )
    public BigDecimal postAmount;

	/**
	 * 子账户类型
	 */
	@CChar( value = 6, order = 1100 )
	public String subAcctType;
}
