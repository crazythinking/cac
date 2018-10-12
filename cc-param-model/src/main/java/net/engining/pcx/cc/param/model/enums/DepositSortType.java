package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"D|按账期反序冲销", 
	"A|按账期正序冲销"
})

/**
 * 按账期冲销顺序类型
 * 
 */
public enum DepositSortType{
	/**
	 *	 按账期反序冲销
	 */
	D, 
	/**
	 *	 按账期正序冲销
	 */
	A, 
}