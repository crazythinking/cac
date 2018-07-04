package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 内部账户交易代码映射
 */
public class SysInternalAcctPostMapping implements Serializable {

	private static final long serialVersionUID = -7694875491782901840L;

	/**
     * 系统产生交易代码
     */
    @PropertyInfo(name="系统交易码", length=3)
    public SysInternalAcctActionCd sysInternalAcctActionCd;
    
	/**
     * 内部账户交易代码
     */
    @PropertyInfo(name="内部账户入账交易代码", length=20)
    public List<String> internalAcctPostCode;
    
}