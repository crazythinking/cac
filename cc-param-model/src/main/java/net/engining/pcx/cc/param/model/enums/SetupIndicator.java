package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 锁定码类别
 * @author Ronny
 *
 */
@EnumInfo({
	"S|自动",
	"M|手工",
	"A|手工自动皆可"
})
public enum SetupIndicator {
	/**
	 *	自动
	 */
	S,
	/**
	 * 手工
	 */
	M,
	/**
	 * 手工自动皆可
	 */
	A
}
