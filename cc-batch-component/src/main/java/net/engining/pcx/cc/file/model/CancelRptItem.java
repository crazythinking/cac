package net.engining.pcx.cc.file.model;

import java.util.Date;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.RequestTypeDef;
import net.engining.pg.support.cstruct.CChar;

/**
 * 销卡销户请求\撤销送报表接口
 * 
 * @author yinxia
 *
 */
public class CancelRptItem {

	/**
	 * 机构代码
	 */
	@CChar( value = 12, order = 100 )
	public String org;

	/**
	 * 账号
	 */
	@CChar( value = 20, zeroPadding = true, order = 200 )
	public Integer acctNo;

	/**
	 * 业务类型
	 */
	@CChar( value = 2, order = 300 )
	public BusinessType businessType;

	/**
	 * 逻辑卡号
	 */
	@CChar( value = 19, order = 400 )
	public String logicCardNo;

	/**
	 * 申请类型
	 */
	@CChar( value = 1, order = 500 )
	public RequestTypeDef requestType;

	/**
	 * 申请日期
	 */
	@CChar( value = 8, datePattern = "yyyyMMdd", order = 600 )
	public Date appDate;

	/**
	 * 姓名
	 */
	@CChar( value = 80, order = 700 )
	public String name;

	/**
	 * 手机
	 */
	@CChar( value = 20, order = 800 )
	public String mobile;


}
