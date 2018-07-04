package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"CASH|取现余额",
	"CONS|消费余额",
	"PAYM|溢缴款",
	"LOAN|贷款剩余本金",
	"TOPY|贷款应还本金",
	"DISP|争议余额",
	"INTE|利息余额",
	"SFEE|费用余额"
})
public enum BalanceType {
	/**
	 * 取现余额
	 */
	CASH,
	
	/**
	 * 消费
	 */
	CONS,
	
	/**
	 * 溢缴款
	 */
	PAYM,
	
	/**
	 * 贷款剩余本金
	 */
	LOAN,
	
	/**
	 * 贷款当期应还本金
	 */
	TOPY,
	
	/**
	 * 争议
	 */
	DISP,
	
	/**
	 * 利息
	 */
	INTE,
	
	/**
	 * 费用
	 */
	SFEE
}
