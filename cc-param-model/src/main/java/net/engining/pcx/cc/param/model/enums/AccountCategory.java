package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 账户类别
 */
@EnumInfo({
	"S|汇总类",
	"D|明细类"
})
public enum AccountCategory{
	/**
	 * 汇总类
	 */
	S,
	/**
	 * 明细类
	 */
	D
}