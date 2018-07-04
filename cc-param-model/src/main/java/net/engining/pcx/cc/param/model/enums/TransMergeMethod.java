package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;


/**
 * 交易合并规则
 */
@EnumInfo({
	"D|当日合并余额",
	"A|全部合并余额",
	"N|不合并余额"
})
public enum TransMergeMethod{
	/**
	 * 当日合并余额
	 */
	D,
	/**
	 * 全部合并余额
	 */
	A,
	/**
	 * 不合并余额
	 */
	N
}
