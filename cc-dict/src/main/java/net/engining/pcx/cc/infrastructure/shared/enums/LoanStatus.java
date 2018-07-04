package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;


@EnumInfo({
	"P|大额分期申请",
	"I|注册但未活动",
	"A|活动状态(active)",
	"T|终止(terminate)",
	"F|完成(finish)"
})
public enum LoanStatus {
	/**
	 *	大额分期申请
	 */
	P,
	/**
	 *	注册但未活动 
	 */
	I,
	/**
	 *	活动状态(active) 
	 */
	A,
	/**
	 *	终止(terminate) 
	 */
	T,
	/**
	 *	完成(finish) 
	 */
	F
}
