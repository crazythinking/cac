package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.pg.support.cstruct.CChar;

/**
 * 账单提醒短信
 * 
 * @author Heyu.wang
 */
public class StmtMsgInterfaceItem extends BaseMsgInterfaceItem {
	/**
	 * 账单日
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 100 )
    public Date stmtDate;
	
	/**
	 * 最后还款日
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 200 )
    public Date paymentDate;
	
	/**
	 * 币种
	 */
	@CChar( value = 3, order = 300 )
    public String currencyCd;
	
	/**
	 * 应还款额
	 */
	@CChar( value = 15, precision = 2, order = 400 )
    public BigDecimal graceBalance;
	
	/**
	 * 最小还款额
	 */
	@CChar( value = 15, precision = 2, order = 500 )
    public BigDecimal due;
}
