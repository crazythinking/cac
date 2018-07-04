package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 分配方式
 * @author Ronny
 *
 */
@EnumInfo({
	"F|按月平分",
	"S|分配表"
})
public enum DistributeMethod {
	/**
	 * 按月平分
	 */
	F,
	/**
	 * 分配表
	 */
	S
}
