package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 全额还款宽限计算方式：
 */
@EnumInfo({
	"R|按比例计算",
	"A|按金额计算",
	"B|比例和金额同时考虑"
})
public enum DownpmtTolInd{
	/**
	 * 按比例计算
	 */
	R,
	/**
	 * 按金额计算
	 */
	A,
	/**
	 * 比例和金额同时考虑
	 */
	B}