package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 分期手续费收取方式
 * @author Ronny
 *
 */
@EnumInfo({
	"F|一次性收取",
	"E|分期收取"
})
public enum LoanFeeMethod {

	/**
	 *	一次性收取 
	 */
	F,

	/**
	 *	分期收取 
	 */
	E
}
