package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 附加费用计算方式
 * @author Ronny
 *
 */
@EnumInfo({
	"R|附加金额按比例计算",
	"A|附加金额为固定金额"
})
public enum CalcMethod {
	/**
	 * 按比例
	 */
	R,
	/**
	 * 固定金额
	 */
	A
}
