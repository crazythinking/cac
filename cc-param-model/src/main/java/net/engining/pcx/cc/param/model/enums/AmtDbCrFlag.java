package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 
 * @author heyu.wang
 */
@EnumInfo({
	"D|只允许借方发生额",
	"C|只允许贷方发生额",
	"T|双向发生额"
})
public enum AmtDbCrFlag {
	/**
	 * D - 只允许借方余额
	 */
	D,
	/**
	 * C - 只允许贷方余额
	 */
	C,
	/**
	 * T - 双向发生额
	 */
	T
}