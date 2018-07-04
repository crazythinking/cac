package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 参数类型
 */
@EnumInfo({
	"Float|浮动参数",
	"Fixed|固定参数"
})
public enum ParamBaseType{
	/**
	 * 浮动参数
	 */
	Float,
	/**
	 * 固定参数
	 */
	Fixed}