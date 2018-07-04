package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 计息类型
 * @author guopy
 * @date 2012-8-10 上午11:28:54
 */
@EnumInfo({ 
"L|存款靠档-分段计息",
"D|利率靠档-按日计息"
})
public enum AccrualType {
	/**
	 * 存款靠档-分段计息
	 */
	L,
	/**
	 * 利率靠档-按日计息
	 */
	D
}