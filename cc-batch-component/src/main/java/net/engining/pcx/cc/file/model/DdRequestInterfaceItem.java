package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.DDType;
import net.engining.pg.support.cstruct.CChar;


/**
 * 约定还款扣款文件
 * @author Ronny
 * @deprecated 被交换表代替
 */
@Deprecated
public class DdRequestInterfaceItem {
	/**
	 * 机构号码
	 */
	@CChar( value = 12, order = 100 )
	public String org;

	/**
	 * 客户姓名
	 */
	@CChar( value = 80, order = 200 )
	public String name;

	/**
	 * 账户号码
	 */
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

	/**
	 * 默认卡号
	 */
	@CChar( value = 19, order = 500 )
	public String defaultCardNo;

	/**
	 * 约定还款类型标志
	 */
	@CChar( value = 1, order = 600 )
	public DDType dbIndDef;

	/**
	 * 约定还款开户行名称
	 */
	@CChar( value = 80, order = 700 )
	public String ddBankName;

	/**
	 * 约定还款开户行行号
	 */
	@CChar( value = 9, order = 800 )
	public String ddBankBranch;

	/**
	 * 约定还款借记账号
	 */
	@CChar( value = 20, order = 900 )
	public String ddBankAcctNo;

	/**
	 * 约定还款借记账户户名
	 */
	@CChar( value = 80, order = 1000 )
	public String dbBankAcctName;

	/**
	 * 约定还款扣款金额
	 */
	@CChar( value = 15, precision = 2, order = 1200 )
	public BigDecimal ddAmount;

	/**
	 * 约定还款日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 1300 )
	public Date ddDate;
}
