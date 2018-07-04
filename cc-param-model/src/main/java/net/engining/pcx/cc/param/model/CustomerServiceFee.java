package net.engining.pcx.cc.param.model;


import java.io.Serializable;
import java.math.BigDecimal;

import net.engining.pg.support.meta.PropertyInfo;

/**
 * 客户参数费用表
 */
public class CustomerServiceFee implements Serializable {

	private static final long serialVersionUID = 5399776468521956522L;

	/**
     * 费用收取金额
     * 0 表示不收费
     */
    @PropertyInfo(name="费用收取金额", length=15, precision=2)
    public BigDecimal fee;

    /**
     * 入账交易代码
     */
    @PropertyInfo(name="交易代码", length=4)
    public String postCode;
}