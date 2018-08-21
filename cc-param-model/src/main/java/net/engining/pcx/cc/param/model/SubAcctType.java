package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pg.support.meta.PropertyInfo;

/**
 * 子帐户类型
 */
public class SubAcctType implements Serializable {

	private static final long serialVersionUID = -6405896117422664294L;
	
	public enum SubAcctTypeDef{
		LOAN, LBAL, INTE, PNIT, PAYM, SFEE
	}

	/**
	 * 子帐户类型标识
	 * 
	 */
	@PropertyInfo(name="子帐户类型标识", length=8)
	public String subAcctType;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;
    
}