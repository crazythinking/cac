package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 入账许可指示
 * @author Ronny
 *
 */
@EnumInfo({
	"A|正常入账",
	"R|拒绝入账",
	"C|只允许贷记入账"
})
public enum PostAvailiableInd {

	/**
	 *	拒绝入账 
	 */
	R,
	
	/**
	 *	只允许贷记入账
	 */
	C,
	
	/**
	 *	正常入账 
	 */
	A;
	
	
}
