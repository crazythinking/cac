package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 调整标识
 * @author Ronny
 *
 */
@EnumInfo({
	"N|不允许调整",
	"D|可借记调整",
	"C|可贷记调整",
	"B|可双向调整"
})
public enum AdjustIndicator {
	
     /**
     * 非调整交易码
     */
    N,
    /**
     * 可借记调整交易码
     */
    D,
    /**
     * 可贷记调整交易码
     */
    C,
    /**
     * 双向可调
     */
    B
}
