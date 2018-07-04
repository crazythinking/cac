package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"OTC|柜面",
	"ATM|ATM",
	"POS|POS普通",
	"PHT|POSMOTO手工",
	"PHE|POSMOTO电子",
	"CS|客服",
	"IVR|IVR",
	"EB|EBANK网银",
	"MB|EBANK手机银行",
	"HOST|内管",
	"THIRD|第三方支付"
	 
})
/**
 * 交易渠道
 */
public enum AuthTransTerminal{
	/**
	 * 柜面
	 */
	OTC, 
	/**
	 * ATM
	 */
	ATM, 
	/**
	 * POS普通
	 */
	POS, 
	/**
	 * POS手工MOTO
	 */
	PHT, 
	/**
	 * POS手工电子
	 */
	PHE, 
	/**
	 * IVR
	 */
	IVR, 
	/**
	 * EBANK网银
	 */
	EB, 
	/**
	 * EBANK手机银行
	 */
	MB, 
	/**
	 * 内管
	 */
	HOST, 
	/**
	 * 第三方支付
	 */
	THIRD, 
	/**
	 * 客服
	 */
	CS
	}