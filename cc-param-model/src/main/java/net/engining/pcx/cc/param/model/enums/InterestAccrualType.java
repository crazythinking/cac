package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 利息计算方式
 */
@EnumInfo({
	"AddUp|积数法",
	"Iterative|逐笔法"
})
public enum InterestAccrualType {

	AddUp,
	
	Iterative
}
