package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"MRT|等额本金-剩余靠后",
	"MRF|等额本金-剩余靠前",
	"MSV|等额本息",
	"OPT|一次还本付息",
	"IFP|分次付息一次还本",
	"MPA|按账期还款(信用支付)",
	"IWP|利随本清",
	"MRG|等额本金-剩余靠前特殊固定日还款",
	"MSF|等额本息-剩余靠前特殊固定日还款",
	"PSV|等本等息"
})
public enum PaymentMethod {
	/**
	 * 等额本金-剩余靠后
	 */
	MRT,
	/**
	 * 等额本金-剩余靠前
	 */
	MRF,
	/**
	 * 等额本息
	 */
	MSV,
	/**
	 * 一次还本付息
	 */
	OPT,
	
	/**
	 * 分次付息一次还本
	 */
	IFP,
	/**
	 * 循环信用：按账期还款(信用支付)
	 */
	MPA,
	/**
	 * 利随本清
	 */
	IWP,
	/**
	 * 等额本金-剩余靠前特殊固定日还款
	 */
	MRG,
	/**
	 * 等额本息-剩余靠前特殊固定日还款
	 */
	MSF,
	/**
	 * 等本等息
	 */
	PSV
}
