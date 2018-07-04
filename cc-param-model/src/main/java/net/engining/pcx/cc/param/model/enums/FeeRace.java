package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"F|手续费",
	"P|罚息",
	"I|利息",
	"A|合同期后罚息"
})
public enum FeeRace {
	/**
	 * 手续费
	 */
	F, 
	/**
	 * 罚息
	 */
	P,
	/**
	 * 利息
	 */
	I, 
	/**
	 * 合同期后罚息
	 */
	A
}
