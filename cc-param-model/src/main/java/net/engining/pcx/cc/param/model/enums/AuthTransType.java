package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"Normal|正常",
	"Reversal|冲正",
	"Revocation|撤销",
	"RevocationReversal|撤销的冲正", 
	"Advice|通知",
	"Confirm|确认",
	"Query|查询"
})

/**
 * 交易类型
 */
public enum AuthTransType{
	/**
	 * 正常
	 */
	Normal, 
	/**
	 * 冲正
	 */
	Reversal,
	/**
	 * 撤销
	 */
	Revocation,
	/**
	 * 撤销的冲正
	 */
	RevocationReversal,
	/**
	 * 通知
	 */
	Advice,
	/**
	 * 确认
	 */
	Confirm,
	/**
	 * 查询
	 */
	Query
}