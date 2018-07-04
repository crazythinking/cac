package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pcx.cc.param.model.enums.AdjustIndicator;
import net.engining.pcx.cc.param.model.enums.PostProcessor;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 交易代码
 */
public class PostCode implements Serializable {

	private static final long serialVersionUID = -7694875491782901840L;

	/**
     * 交易代码
     */
    @PropertyInfo(name="交易代码", length=8)
    public String postCode;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=80)
    public String description;

    /**
     * 简要描述
     * 出现在对账单上面的描述
     */
    @PropertyInfo(name="简要描述", length=80)
    public String shortDesc;

    /**
     * 入账是否要检查BlockCode
     * Y/N
     */
    @PropertyInfo(name="入账前检查锁定码", length=1)
    public Boolean blkcdCheckInd;

    /**
     * 交易出具对账单标识
     * Y/N
     */
    @PropertyInfo(name="需要出具对账单", length=1)
    public Boolean stmtInd;
    
    /**
     * 调整标识
     * 客服或者内管对该交易的调整标识
     * N-非调整交易码
     * D-可借记调整交易码
     * C-可贷记调整交易码
     */
    @PropertyInfo(name="调整标识", length=1)
    public AdjustIndicator adjustInd;
    
    /**
     * 入账处理器
     * 目前支持三种：借记正常、贷记正常、贷记转出
     */
    @PropertyInfo(name="入账处理器", length=2)
    public PostProcessor processor;
    
	/**
	 * 子帐户类型标识
	 * key = {@link SubAcctType}
	 */
	@PropertyInfo(name="子帐户类型标识", length=6)
	public String subAcctType;
	
	/**
     * 撤销或冲正交易代码
     */
    @PropertyInfo(name="冲撤交易代码", length=8)
    public String reversePostCode; 
    
    /**
     * 是否冲正交易
     * false-否
     * true-冲正交易
     */
    @PropertyInfo(name="冲正交易", length=1)
    public Boolean isReversal;
    
    /**
     * 是否会计记帐
     * 
     */
    @PropertyInfo(name="是否分户会计记帐", length=1)
    public Boolean isAccounting;
}