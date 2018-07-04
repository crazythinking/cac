package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"M|MEMO类交易", 
	"D|借记交易", 
	"C|贷记交易"
})

/**
 * 借贷标志
 * 
 * @author heyu.wang
 */
public enum DbCrInd{
	/**
	 *	 MEMO类交易
	 */
	M, 
	/**
	 *	 借记交易
	 */
	D, 
	/**
	 * 	贷记交易
	 */
	C
}