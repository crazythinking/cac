package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"D|借方", 
	"C|贷方",
	"A|双向"
})
public enum BalanceDirection {
	/**
	 *	 借方
	 */
	D, 
	/**
	 * 	贷方
	 */
	C,
	/**
	 *  双向
	 */
	A
}
