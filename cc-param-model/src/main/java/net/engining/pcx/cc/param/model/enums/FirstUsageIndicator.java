package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 首次用卡标志
 * @author Ronny
 *
 */
@EnumInfo({
	"A|未用卡",
	"B|已首次用卡",
	"C|已首次用卡且已收年费"
})
public enum FirstUsageIndicator {

	/**
	 *	未用卡 
	 */
	A,
	/**
	 *	已首次用卡 
	 */
	B,
	/**
	 * 	已首次用卡且已收年费
	 */
	C
}
