package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 授权决定
 */
@EnumInfo({
	"B|全额还款",
	"D|部分还款",
	"U|还款未达最小还款额",
	"N|未还款",
	"C|无需还款"
})

public enum PaymentStatus {
	/**
	 * 全额还款
	 */
	B,
	/**
	 * 部分还款
	 */
	D,
	/**
	 * 还款未达最小还款额
	 */
	U,
	/**
	 * 未还款
	 */
	N,
	/**
	 * 无需还款
	 */
	C
}
