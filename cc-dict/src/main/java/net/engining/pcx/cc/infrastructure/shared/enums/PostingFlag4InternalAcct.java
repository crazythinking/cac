package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"FFF|待入账",
	"F00|成功入账",
	"F01|账户关闭-拒绝入账",
	"F02|账户销户-拒绝入账",
	"F03|入账引起内部账户余额方向错误"
})
//只能定义3位，对应的数据库表字段长度为3
public enum PostingFlag4InternalAcct {
	/**
	 * 待入账
	 */
	FFF,
	
	/**
	 * 成功入账
	 */
	F00,

	/**
	 * 账户关闭-拒绝入账
	 */
	F01,

	/**
	 * 账户销户-拒绝入账
	 */
	F02,

	/**
	 * 入账引起内部账户余额方向错误
	 */
	F03
}
