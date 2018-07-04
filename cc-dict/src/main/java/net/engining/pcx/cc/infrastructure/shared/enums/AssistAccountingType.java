package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 辅助核算项类型
 * @author luxue
 *
 */
@EnumInfo({ 
	"DEPARTMENT|部门", 
	"PRODUCT|产品", 
	"SITEM|服务项目", 
	"TRENCH|渠道", 
	"ACTIVITY|活动", 
	"FUNDING|资金方", 
	"SUPPLIERR|供应商档案",
	"CUSTR|客户档案", 
	"PERSONR|人员档案", 
	"BANKR|银行档案", 
	"BANKACCT|银行帐户", 
	"MERCHANTS|客商" 
	})
public enum AssistAccountingType {
	/**
	 * DEPARTMENT - 部门
	 */
	DEPARTMENT,
	/**
	 * PRODUCT - 产品
	 */
	PRODUCT,
	/**
	 * SITEM - 服务项目
	 */
	SITEM,
	/**
	 * TRENCH - 渠道
	 */
	TRENCH,
	/**
	 * ACTIVITY - 活动
	 */
	ACTIVITY,
	/**
	 * FUNDING - 资金方
	 */
	FUNDING,
	/**
	 * SUPPLIERR - 供应商档案
	 */
	SUPPLIERR,
	/**
	 * CUSTR - 客户档案
	 */
	CUSTR,
	/**
	 * PERSONR - 人员档案
	 */
	PERSONR,
	/**
	 * BANKR - 银行档案
	 */
	BANKR,
	/**
	 * BANKACCT - 银行帐户
	 */
	BANKACCT,
	/**
	 * MERCHANTS - 客商
	 */
	MERCHANTS

}
