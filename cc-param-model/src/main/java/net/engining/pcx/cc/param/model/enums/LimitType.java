package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 额度计算类型
 * @author Ronny
 *
 */
@EnumInfo({
	"S|SUM，客户额度取其下账户额度之和",
	"H|HIGHEST，客户额度去其下账户额度最大值"
})
public enum LimitType {

	/**
	 *	SUM，客户额度取其下账户额度之和 
	 */
	S,

	/**
	 *	HIGHEST，客户额度去其下账户额度最大值 
	 */
	H
}
