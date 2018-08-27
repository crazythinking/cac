package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"MRT|等额本金",
	"MRF|等额本金-剩余靠前",
	"MRG|等额本金-剩余靠前特殊固定日还款",
	"MSV|等额本息",
	"MSB|等额本息-剩余靠前",
	"MSF|等额本息-剩余靠前特殊固定日还款",
	"OPT|一次还本付息",
	"IFP|分期先息后本",
	"IIF|分期先息后本(利息前置)",
	"MPA|按账期还款(信用支付)",
	"IWP|利随本清",
	"PSV|等本等息",
	"PSZ|非标等本等息，逾期利息按应还未还本金计算"
})
public enum PaymentMethod {
	
	/**
	 * 等额本金
	 */
	MRT,
	/**
	 * 等额本金-剩余靠前
	 */
	MRF,
	/**
	 * 等额本金-剩余靠前特殊固定日还款
	 */
	MRG,
	
	/**
	 * 等额本息
	 */
	MSV,
	/**
	 * 等额本息-剩余靠前
	 */
	MSB,
	/**
	 * 等额本息-剩余靠前特殊固定日还款
	 */
	MSF,
	
	/**
	 * 一次还本付息
	 */
	OPT,
	
	/**
	 * 分期先息后本
	 */
	IFP,
	/**
	 * 分期先息后本(利息前置)
	 */
	IIF,
	
	/**
	 * 循环信用：按账期还款(信用支付)
	 */
	MPA,
	/**
	 * 利随本清
	 */
	IWP,
	
	/**
	 * 等本等息
	 */
	PSV,
	
	/**
	 * 非标等本等息，逾期利息按应还未还本金计算
	 */
	PSZ
	
}
