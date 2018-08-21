package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"A|本金全额",
	"B|全部剩余本金",
	"G|全部剩余本金+逾期利息",
	"C|本期全部本金",
	"D|本期全部本金+逾期利息",
	"E|本期剩余应还本金",
	"F|本期剩余应还本息和"
	
})
public enum PnitType {
	/**
	 * 本金全额
	 */
	A,
	
	/**
	 * 全部剩余本金
	 */
	B,
	
	/**
	 * 全部剩余本金+逾期利息
	 */
	G,
	
	/**
	 * 本期全部本金
	 */
	C,
	
	/**
	 * 本期全部本金+逾期利息
	 */
	D,
	
	/**
	 * 本期剩余应还本金
	 */
	E,
	
	/**
	 * 本期剩余应还本息和
	 */
	F,
	
}
