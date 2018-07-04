package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.pg.support.cstruct.CChar;

/**
 * 约定还款成功短信接口文件 正文(DD_SUCESS_MESS_INTERFACE),参考《D-C01-CPS01-BRD-贷记卡核心系统账务模块数据格式-V006.xlsx》
 * 
 * @author heyu.wang
 * 
 */
public class DdSucessMessInterfaceItem extends BaseMsgInterfaceItem {
	/**
	 * 约定还款金额
	 */
	@CChar( value = 15, precision = 2, zeroPadding = true, order = 100 )
	public BigDecimal txnAmt;
	
	/**
	 * 约定还款日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 200 )
	public Date txnDate;
}
