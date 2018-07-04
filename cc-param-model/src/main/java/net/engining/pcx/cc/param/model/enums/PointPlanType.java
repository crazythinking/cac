package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 积分计划类型
 * @author Ronny
 *
 */
@EnumInfo({
	"S|单笔交易积分",
	"A|累积交易积分",
	"F|非交易积分"
})
public enum PointPlanType {
	S,
	A,
	F
}
