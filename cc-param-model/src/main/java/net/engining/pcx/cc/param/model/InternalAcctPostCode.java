package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pcx.cc.param.model.enums.PostProcessor;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 内部账户交易代码
 */
public class InternalAcctPostCode implements Serializable {

	private static final long serialVersionUID = -7694875491782901840L;

	/**
     * 内部账户交易代码
     */
    @PropertyInfo(name="交易代码", length=20)
    public String internalAcctPostCode;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=80)
    public String description;
    
    /**
     * 内部账户入账处理器映射关系
     * <p>key - 内部账户编号{@link InternalAccount}</p>
     * value - 入账处理器（借记DN、贷记CN）
     */
//    public Map<String, PostProcessor> internalAcctPostMapping;
    
    /**
	 * 内部账户编号
	 */
	@PropertyInfo(name="内部账户编号", length=30)
	public String internalAccountId;
	
	/**
     * 入账处理器
     * 目前支持两种：借记正常、贷记正常
     */
    @PropertyInfo(name="入账处理器", length=2)
    public PostProcessor processor;
    
    /**
     * 红蓝字标识
     */
    @PropertyInfo(name="红蓝字标识", length=1)
    public RedBlueInd redBlueInd;
    
    /**
     * 内部账户冲撤交易代码
     */
    @PropertyInfo(name="内部账户冲撤交易代码", length=20)
    public String internalAcctReversePostCode;
    
}