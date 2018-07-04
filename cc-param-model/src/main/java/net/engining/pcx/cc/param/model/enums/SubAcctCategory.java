package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 子账户类别
 */
@EnumInfo({
	"S|登记簿",
	"D|分户"
})
public enum SubAcctCategory{
	/**
	 * 登记簿
	 */
	S,
	/**
	 * 分户
	 */
	D
}