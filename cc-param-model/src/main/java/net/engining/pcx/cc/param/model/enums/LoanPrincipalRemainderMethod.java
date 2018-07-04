package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"FP|首期收取",
	"LP|末期收取"
})
public enum LoanPrincipalRemainderMethod {
	/**
	 * 首期收取
	 */
	FP,
	/**
	 * 末期收取
	 */
	LP;
}
