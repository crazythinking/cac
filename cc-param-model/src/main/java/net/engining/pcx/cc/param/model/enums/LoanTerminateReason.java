package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 分期中止原因代码
 * @author Ronny
 *
 */
@EnumInfo({
	"V|持卡人主动终止（volunteer）",
	"M|银行业务人员手工终止（manual）",
	"D|逾期自动终止（delinquency）",
	"R|锁定码终止(Refund)"
})
public enum LoanTerminateReason {

	/**
	 *	持卡人主动终止（volunteer） 
	 */
	V,
	/**
	 *	银行业务人员手工终止（manual） 
	 */
	M,
	/**
	 *	逾期自动终止（delinquency） 
	 */
	D,
	/**
	 *	锁定码终止(Refund)
	 */
	R
}
