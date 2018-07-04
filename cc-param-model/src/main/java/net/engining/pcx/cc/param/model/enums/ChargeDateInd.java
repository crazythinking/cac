package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 费用收取日期：
 * P - 费用发生当天收取（posting date）-- 如超限费为发生超限的当天，取现费为发生取现交易的当天
 * C - 账单日收取（cycle date）
 * D - 还款日（payment due date）
 * G - 还款宽限日（payment grace date）
 */
@EnumInfo({
	"P|当天收取（posting date）-- 如超限费为发生超限的当天，取现费为发生取现交易的当天",
	"C|账单日收取（cycle date）",
	"D|还款日（payment due date）",
	"G|还款宽限日（payment grace date）"
})
public enum ChargeDateInd{
	/**
	 * 当天收取
	 */
	P,
	/**
	 * 账单日收取
	 */
	C,
	/**
	 * 还款日
	 */
	D,
	/**
	 * 还款宽限日
	 */
	G
}