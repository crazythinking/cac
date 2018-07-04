package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.AcctCloseReason;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pg.support.cstruct.CChar;


/**
 * 关闭账户送报表接口
 * 
 * @author yinxia
 *
 */
public class AcctCloseRptItem {

	/**
	 * 机构代码
	 */
	@CChar ( value = 12, zeroPadding = true, order = 100 )
	public String org;

	/**
	 * 账号
	 */
	@CChar ( value = 20, zeroPadding = true, order = 200 )
	public Integer acctNo;

	/**
	 * 业务类型
	 */
	@CChar ( value = 2, order = 300 )
	public BusinessType businessType;

	/**
	 * 销户日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 400 )
	public Date cancelDate;

	/**
	 * 封锁码
	 */
	@CChar ( value = 1, order = 500 )
	public String blockCode;

	/**
	 * 关闭账户日期
	 */
	@CChar ( value = 8, datePattern = "yyyyMMdd", order = 600 )
	public Date closedDate;

	/**
	 * 关闭账户原因
	 */
	@CChar( value = 3, order = 700 )
	public AcctCloseReason reason;

	/**
	 * 余额
	 */
	@CChar ( value = 15, precision = 2, zeroPadding = true, order = 800 )
	public BigDecimal currBal;

	/**
	 * 未匹配借记金额
	 */
	@CChar ( value = 15, precision = 2, zeroPadding = true, order = 900 )
	public BigDecimal unmatchDb;

	/**
	 * 未匹配贷记金额
	 */
	@CChar ( value = 15, precision = 2, zeroPadding = true, order = 1000 )
	public BigDecimal unmatchCr;
	
	
	/**
	 * 默认逻辑卡号
	 */
	@CChar ( value = 19, order = 1100 )
	public String defaultLogicalCardNo;
	
	/**
	 * 币种
	 */
	@CChar ( value = 3, order = 1200 )
	public String currencyCode;

	
}
