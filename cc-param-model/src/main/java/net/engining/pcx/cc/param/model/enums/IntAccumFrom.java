package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 计息开始日期类型
 */
@EnumInfo({
	"C|账单日",
	"P|入账日"
})
public enum IntAccumFrom{
	/**
	 * 账单日
	 */
	C,
	/**
	 * 入账日
	 */
	P}