package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 到期还款日类型
 */
@EnumInfo({
	"D|账单日(结息日)后若干天",
	"F|每月固定日期"
})
public enum PaymentDueDay{
	/**
	 * 对于业务类型是循环信用的账户来说，到期还款日类型是账单日后若干天。
	 * 对于业务类型是一次性授信贷款的账户来说，到期还款日类型是结息日后若干天。
	 */
	D,
	/**
	 * 每月固定日期
	 */
	F}