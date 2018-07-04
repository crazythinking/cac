package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"YHNT|计头不计尾",
	"NHYT|不计头计尾",
	"YHYT|计头计尾",
	"NHNT|不计头不计尾"
	
})
public enum ComputInteHT {
	/**
	 * 计头不计尾
	 */
	YHNT,
	
	/**
	 * 不计头计尾
	 */
	NHYT,
	
	/**
	 * 计头计尾
	 */
	YHYT,
	
	/**
	 * 不计头不计尾
	 */
	NHNT
	
}
