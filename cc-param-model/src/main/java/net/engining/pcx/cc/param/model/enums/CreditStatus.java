package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 信用状况
 * @author Ronny
 *
 */
@EnumInfo({
	"N|正常",
	"D|拖欠"
})
public enum CreditStatus {

	/**
	 *	正常 
	 */
	N,
	/**
	 *	拖欠 
	 */
	D
}
