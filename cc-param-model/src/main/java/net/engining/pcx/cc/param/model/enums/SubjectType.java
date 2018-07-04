package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 科目类型：
 * A - 资产类
 * B - 负债类
 * C - 损益类
 * D - 共同类
 * E - 所有者权益
 * F - 表外类(账户呆账类)
 * @author Heyu.wang
 * @date 2012-8-16 下午4:19:21
 */
@EnumInfo({
	"A|资产类",
	"B|负债类",
	"C|损益类",
	"D|共同类",
	"E|所有者权益",
	"F|表外类(账户呆账类)"
})
public enum SubjectType {
	/**
	 * 资产类
	 */
	A, 
	
	/**
	 * 负债类
	 */
	B, 
	
	/**
	 * 损益类
	 */
	C, 
	
	/**
	 * 共同类
	 */
	D, 
	
	/**
	 * 所有者权益
	 */
	E, 

	/**
	 * 表外类(账户呆账类)
	 */
	F
}