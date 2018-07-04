package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.IdType;
import net.engining.pg.support.cstruct.CChar;

/**
 * 建客建账建卡送报表文件接口
 * 
 * @author heyu.wang
 *
 */
public class ItemApplyResponse {

	/**
	 * 机构代码
	 */
	@CChar( value = 12, order = 100 )
	public String org;

	/**
	 * 申请编号
	 */
	@CChar( value = 20, order = 200 )
	public String appNo;

	/**
	 * 处理日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 300 )
	public Date setupDate;

	/**
	 * 产品代码
	 */
	@CChar( value = 6, order = 400 )
	public String productCd;

	/**
	 * 客户ID
	 */
	@CChar( value = 15, order = 500 )
	public Integer custId;

	/**
	 * 姓名
	 */
	@CChar( value = 80, order = 600 )
	public String name;

	/**
	 * 证件类型
	 */
	@CChar( value = 1, order = 700 )
	public IdType idType;

	/**
	 * 证件号码
	 */
	@CChar( value = 30, order = 800 )
	public String idNo;

	/**
	 * 业务类型
	 */
	@CChar( value = 2, order = 900 )
	public BusinessType businessType;

	/**
	 * 币种
	 */
	@CChar( value = 3, order = 910 )
	public String currencyCode;
	
	/**
	 * 账号
	 */
	@CChar( value = 20, zeroPadding = true, order = 1000 )
	public Integer acctNo;

	/**
	 * 介质卡号
	 */
	@CChar( value = 19, order = 1100 )
	public String cardNo;

	/**
	 * 发卡网点
	 */
	@CChar( value = 9, order = 1200 )
	public String owningBranch;

	/**
	 * 移动电话
	 */
	@CChar( value = 20, order = 1300 )
	public String mobileNo;

	/**
	 * 拒绝原因
	 */
	//@CChar( value = 3, order = 1400 )
	//public AppRejectReason appRejectReason;

	/**
	 * 逻辑卡号
	 */
	@CChar( value = 19, autoTrim = true, order = 1500 )
	public String logicalCardNo;

	/**
	 * 主附卡标识
	 */
	//@CChar( value = 1, order = 1600 )
	//public BscSuppIndicator bscSuppInd;

	/**
	 * 封锁码
	 */
	@CChar( value = 27, autoTrim = true, order = 1700 )
	public String blockCode;

	/**
	 * 客户信用额度
	 */
	@CChar( value = 13, zeroPadding = true, order = 1800 )
	public BigDecimal creditLimitCust;

	/**
	 * 账户信用额度
	 */
	@CChar( value = 13, zeroPadding = true, order = 1900 )
	public BigDecimal creditLimitAcct;

	/**
	 * 卡面代码
	 */
	@CChar( value = 10, autoTrim = true, order = 2000 )
	public String pyhCd;
}
