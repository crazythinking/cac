package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"LOAN|贷款剩余本金",
	"LBAL|贷款应还本金",
	"OTAX|销项税",
	"ULBAL|联合方贷款应还本金",
	"ULOAN|联合方贷款剩余本金",
	"ADDLIMIT|授信额度增加",
	"SUBLIMIT|授信额度减少",
	"PAYM|溢缴款",
	"RECEIVABLE_INTEREST|应收利息",
	"ACCRUED_INTEREST|应计利息",
	"RECEIVABLE_PNIT|应收罚息",
	"ACCRUED_PNIT|应计罚息",
	"Attention_RECEIVABLE_INTEREST|关注类应收利息",
	"SFEE|费用",
	"INTE_OTAX|利息销项税",
	"PNIT_OTAX|罚息销项税",
	
})
public enum SubjectAmtType {
	/**
	 * 贷款剩余本金
	 */
	LOAN, 
	/**
	 * 贷款应还本金
	 */
	LBAL,
	/**
	 * 销项税
	 */
	OTAX,
	/**
	 * 联合方贷款应还本金
	 */
	ULBAL,
	/**
	 * 联合方贷款剩余本金
	 */
	ULOAN,
	/**
	 * 授信额度增加
	 */
	ADDLIMIT,
	/**
	 * 授信额度减少
	 */
	SUBLIMIT,
	/**
	 * 溢缴款
	 */
	PAYM,
	/**
	 * 应收利息
	 */
	RECEIVABLE_INTEREST,
	/**
	 * 关注类应收利息
	 */
	Attention_RECEIVABLE_INTEREST,
	/**
	 * 应计利息
	 */
	ACCRUED_INTEREST,
	/**
	 * 应收罚息
	 */
	RECEIVABLE_PNIT,
	/**
	 * 应计罚息
	 */
	ACCRUED_PNIT,
	/**
	 * 费用
	 */
	SFEE,
	/**
	 * 利息销项税
	 */
	INTE_OTAX,
	/**
	 * 罚息销项税
	 */
	PNIT_OTAX
}
