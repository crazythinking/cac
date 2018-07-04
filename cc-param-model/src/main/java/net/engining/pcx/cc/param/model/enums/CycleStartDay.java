package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * @about 周期起始日
 * @author guopy
 * @date 2012-8-30 下午3:27:53
 */
@EnumInfo({
	"Y|固定日",
	"P|建账日"
})
public enum CycleStartDay{
	/**
	 * 固定日
	 */
	Y, 
	/**
	 * 建账日
	 */
	P
}