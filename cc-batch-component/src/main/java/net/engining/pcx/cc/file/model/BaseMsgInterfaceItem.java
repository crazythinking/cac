package net.engining.pcx.cc.file.model;

import net.engining.pcx.cc.infrastructure.shared.enums.GenderDef;
import net.engining.pcx.cc.infrastructure.shared.enums.MessageCategory;
import net.engining.pg.support.cstruct.CChar;

/**
 * 批量信息接口文件，短信邮件接口类型的基础类
 * 
 * @author heyu.wang
 */
public class BaseMsgInterfaceItem {
	/**
	 * 机构号码
	 */
	@CChar( value = 12, order = 10 )
	public String org;
	
	/**
	 * 客户姓名
	 */
	@CChar( value = 80, order = 15 )
	public String custName;
	
	/**
	 * 性别
	 */
	@CChar( value = 1, order = 20 )
	public GenderDef gender;
	
	/**
	 * 信息模板类型
	 */
	@CChar( value = 3, order = 25 )
	public MessageCategory category;
	
	/**
	 * 产品代码
	 */
	@CChar( value = 6, order = 30 )
	public String productCd;
	
	/**
	 * 电话号码
	 */
	@CChar( value = 40, order = 40 )
	public String mobileNo;
	
	/**
	 * EMAIL
	 */
	@CChar( value = 128, order = 45 )
	public String email;
}
