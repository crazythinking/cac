package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pg.support.cstruct.CChar;

/**
 * 约定还款失败回盘文件接口DD_RESPONSE_INTERFACE 正文, 参考《D-C01-CPS01-BRD-贷记卡核心系统账务模块数据格式-V0.7.0.xlsx》，“约定还款接口格式”
 * 
 * @author heyu.wang
 *
 */
public class DdResponseInterfaceItem {
	
	@CChar( value = 12, order = 100 )
	public String org;

	@CChar( value = 80, order = 200 )
	public String custName;
	
	@CChar( value = 9, zeroPadding = true, order = 300 )
	public Integer acctNo;

	/**
	 * 业务类型
	 */
	@CChar( value = 2, order = 400 )
	public BusinessType businessType;
	
	/**
	 * 币种
	 */
	@CChar( value = 3, order = 450 )
	public String CurrCd;

	@CChar( value = 19, order = 500 )
	public String defaltCardNo;
	
	/**
	 * 实际扣款金额
	 */
	@CChar( value = 15, precision = 2, order = 600 )
	public BigDecimal txnAmt;
	
	/**
	 * 约定还款回盘日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 700 )
	public Date txnReturnDate;
}
