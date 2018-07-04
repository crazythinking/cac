package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 享受免息期类型
 * @author zhengpy
 *
 */
@EnumInfo({
	"C|继续享受免息期",
	"N|不再享受免息期"
})
public enum GraceIntType {
	/**
	 * 继续享受免息期
	 */
	C,
	/**
	 * 不再享受免息期
	 */
	N
}
