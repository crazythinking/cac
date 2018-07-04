package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;


/**
 * 计提利率类型
 * @author yangyiqi
 * @date 2014-12-23
 */
@EnumInfo({ 
"L|最长周期利率",
"C|靠档利率",
"S|最短周期利率"
})
public enum AccrualInterestType {
	/**
	 * 最长周期利率
	 */
	L,
	/**
	 * 靠档利率
	 */
	C,
	/**
	 * 最短周期利率
	 */
	S
}
