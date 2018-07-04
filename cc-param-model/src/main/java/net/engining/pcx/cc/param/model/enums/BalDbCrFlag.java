package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 
 * @author Heyu.wang
 * @date 2012-8-16 下午6:49:58
 */
@EnumInfo({
	"D|只允许借方余额",
	"C|只允许贷方余额",
	"B|按轧差金额",
	"T|双向余额"
})
public enum BalDbCrFlag {
	/**
	 * D - 只允许借方余额
	 */
	D,
	/**
	 * C - 只允许贷方余额
	 */
	C,
	/**
	 * B - 按轧差金额
	 */
	B,
	/**
	 * T - 双向余额
	 */
	T
}