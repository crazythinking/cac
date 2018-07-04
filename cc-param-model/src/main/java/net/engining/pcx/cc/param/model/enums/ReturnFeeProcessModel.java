package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;


/**
 * 退货手续费处理方式
 */
@EnumInfo({
	"R|按比例退还",
	"N|已收不退，未收不收"
})
public enum ReturnFeeProcessModel {
	/**
	 * 按比例退还
	 */
	R,
	
	/**
	 * 已收不退，未收不收
	 */
	N
}
