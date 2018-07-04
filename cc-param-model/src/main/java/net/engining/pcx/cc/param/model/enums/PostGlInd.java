package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 总账入账方式
 */
@EnumInfo({
	"Normal|正常",
	"Suspend|挂账",
	"Writeoff|核销",
	"Unknow|未明暂不记账",
	"Delete|确定不记账",
	"OddSuspend|零头余额暂挂"
})
public enum PostGlInd{
	/**
	 * 正常
	 */
	Normal,
	/**
	 * 挂账
	 */
	Suspend,
	/**
	 * 核销
	 */
	Writeoff,
	/**
	 * 未明暂不记账
	 */
	Unknow,
	/**
	 * 确定不记账
	 */
	Delete,
	/**
	 * 零头余额暂挂
	 */
	OddSuspend
}