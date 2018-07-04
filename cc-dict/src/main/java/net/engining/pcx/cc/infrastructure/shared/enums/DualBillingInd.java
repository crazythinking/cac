package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 本币溢缴款还外币指示
 * @author Ronny
 *
 */
@EnumInfo({
	"N|不还款",
	"M|最小额还款",
	"F|全额还款"
})
public enum DualBillingInd {

	/**
	 *	不还款 
	 */
	N,

	/**
	 *	最小额还款 
	 */
	M,

	/**
	 *	全额还款 
	 */
	F
}

