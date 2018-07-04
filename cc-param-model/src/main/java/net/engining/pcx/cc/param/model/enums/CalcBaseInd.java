package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 滞纳金计算基准金额指示：
 * T - 用总的最小还款额剩余部分（total due）
 * L - 用往期最小还款额剩余部分（last due）
 * C - 用当期最小还款额剩余部分（ctd due）
 */
@EnumInfo({
	"T|用总的最小还款额剩余部分（total due）",
	"L|用往期最小还款额剩余部分（last due）",
	"C|用当期最小还款额剩余部分（ctd due）"
})
public enum CalcBaseInd{
	/**
	 * 用总的最小还款额剩余部分计算
	 */
	T,
	/**
	 * 用往期最小还款额剩余部分计算
	 */
	L,
	/**
	 * 用当期最小还款额剩余部分计算
	 */
	C
}
