package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 内部账户状态
 * 
 * @author heyu.wang
 *
 */
@EnumInfo({
	"OPEN|打开",
	"CLOSE|关闭",
	"DESTROY|销户"
})
public enum InternalAccountStatus {
	
    /**
     * 打开
     */
    OPEN,

    /**
     * 关闭
     */
    CLOSE,
    
    /**
     * 销户
     */
    DESTROY
}
