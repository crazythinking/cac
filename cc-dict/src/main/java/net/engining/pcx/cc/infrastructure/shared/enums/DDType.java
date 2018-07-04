package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 约定还款类型
 * @author licj
 *
 */
@EnumInfo({
    "M|最小额扣款",
    "F|全额扣款",
    "C|最小额储蓄购汇",
    "E|全额储蓄购汇"
})
public enum DDType {
    /** 最小额扣款 */	M,
    /** 全额扣款 */	F,
    /** 最小额储蓄购汇 */	C,
    /** 全额储蓄购汇 */	E;
}
