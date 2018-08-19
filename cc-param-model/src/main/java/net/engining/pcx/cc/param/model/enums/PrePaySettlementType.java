package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 提前还款时未结利息的计息类型
 * M按结息周期靠前	  D按日 N按还款计划不变
 * @author luxue
 *
 */
@EnumInfo({
	
	"M|按结息周期靠前",
	"N|按还款计划不变",
	"D|按日"
})
public enum PrePaySettlementType {

	/**
	 * 按结息周期靠前
	 */
	M,
	
	/**
	 * 按还款计划不变
	 */
	N,
	
	/**
	 * 按日
	 */
	D
}
