package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 额度控制类型
 * H|highest-最高额度控制
 * S|sum-汇总额度控制
 */
@EnumInfo({
	"H|最高额度控制",
	"S|汇总额度控制"
})
public enum LimitControlType{H,S}