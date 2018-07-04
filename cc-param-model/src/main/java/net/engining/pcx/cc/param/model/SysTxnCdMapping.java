package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 系统产生交易代码与入账交易代码对应
 */
public class SysTxnCdMapping implements Serializable {

	private static final long serialVersionUID = -8600680279271421622L;

	/**
     * 系统产生交易代码
     */
    @PropertyInfo(name="系统交易码", length=3)
    public SysTxnCd sysTxnCd;

    /**
     * 入账交易代码
     */
    @PropertyInfo(name="入账交易代码", length=8)
    public String postCode;
}

