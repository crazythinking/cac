package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 拖欠处理容忍度标志：
 */
@EnumInfo({
	"A|按最小还款额未偿还部分金额计算"
})
public enum DelqTolInd{
	/**
	 * 按最小还款额未偿还部分金额计算
	 */
	A}