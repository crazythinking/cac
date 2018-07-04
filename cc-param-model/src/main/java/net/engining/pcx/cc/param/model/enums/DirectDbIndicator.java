package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 约定还款日指示
 * @author Ronny
 *
 */
@EnumInfo({
	"P|到期还款日前若干天",
	"F|每月固定日期"
})
public enum DirectDbIndicator {
	/**
	 * 到期还款日前若干天
	 */
	P,
	/**
	 * 每月固定日期
	 */
	F;
}