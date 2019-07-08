package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pg.support.meta.PropertyInfo;

/**
 * 余额成分类型
 */
public class SubAcctType implements Serializable {

	private static final long serialVersionUID = -6405896117422664294L;

	/**
	 * 默认余额成分类型，不做扩展，子类自行定义新的枚举
	 */
	public enum DefaultSubAcctType{

		/**
		 * 未到期本金
		 */
		LOAN,

		/**
		 * 到期本金
		 */
		LBAL,

		/**
		 * 到期利息
		 */
		INTE,

		/**
		 * 已结转罚息
		 */
		PNIT,

		/**
		 * 溢缴款
		 */
		PAYM,

		/**
		 * 通用记账费用
		 */
		SFEE
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