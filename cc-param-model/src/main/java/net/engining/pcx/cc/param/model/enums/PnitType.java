package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"A|本金全额",
	"B|本金余额 ",
	"C|本期全部本金",
	"D|本期本息和",
	"E|全部逾期本金",
	"F|全部逾期本息和",
	"G|剩余本金+逾期利息",
	
})
public enum PnitType {
	/**
	 * 本金全额
	 */
	A,
	
	/**
	 * 本金余额
	 */
	B,
	
	/**
	 * 本期全部本金
	 */
	C,
	
	/**
	 * 本期本息和
	 */
	D,
	
	/**
	 * 全部逾期本金
	 */
	E,
	
	/**
	 * 全部逾期本息和
	 */
	F,
	
	/**
	 * 剩余本金+逾期利息
	 */
	G,
}
