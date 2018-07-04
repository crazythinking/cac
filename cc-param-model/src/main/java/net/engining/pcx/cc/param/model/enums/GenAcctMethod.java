package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 建账规则
 */
@EnumInfo({
	"A|签约建账",
	"D|贷记交易建账",
	"N|借记交易建账"
})
public enum GenAcctMethod{
	/**
	 * 签约建账
	 */
	A,
	/**
	 * 贷记交易建账
	 */
	D,
	/**
	 * 借记交易建账
	 */
	N
}
