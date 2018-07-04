package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import net.engining.pcx.cc.param.model.enums.PaymentCalcMethod;
import net.engining.pcx.cc.param.model.enums.PostAvailiableInd;
import net.engining.pg.support.meta.PropertyInfo;


/**
 * 账户状态锁定码
 * @author Ronny
 *
 */
public class BlockCodeControl implements Serializable {

	private static final long serialVersionUID = -6443949660391506845L;

	/**
     * 0-9 : age
     * A-Z
     */
    @PropertyInfo(name="锁定码", length=1)
    public String blockCode;    

    /**
     * 入账许可指示
     */
    @PropertyInfo(name="入账许可指示", length=1)
    public PostAvailiableInd postInd;

    /**
     * 到期续卡标识
     */
    @PropertyInfo(name="到期续卡", length=1)
    public Boolean renewInd;

    /**
     * 是否进行日常利息累积
     */
    @PropertyInfo(name="进行日常利息累积", length=1)
    public Boolean intAccuralInd;

    /**
     * 是否免除利息
     */
    @PropertyInfo(name="免除利息", length=1)
    public Boolean intWaiveInd;

    /**
     * 是否免除交易费
     */
    @PropertyInfo(name="免除交易费", length=1)
    public Boolean txnFeeWaiveInd; 

    /**
     * 是否免除超限费
     */
    @PropertyInfo(name="免除超限费", length=1)
    public Boolean ovrlmtFeeWaiveInd;
    
    /**
     * 是否免除滞纳金
     */
    @PropertyInfo(name="免除滞纳金", length=1)
    public Boolean lateFeeWaiveInd; 

    /**
     * 是否输出账单
     */
    @PropertyInfo(name="输出账单", length=1)
    public Boolean stmtInd;

    /**
     * 最小还款额计算方式
     */
    @PropertyInfo(name="最小还款额计算方式", length=1)
    public PaymentCalcMethod paymentInd;

    /**
     * 分期支持标识
     */
    @PropertyInfo(name="分期支持", length=1)
    public Boolean loanInd; 

    /**
     * 是否入催
     */
    @PropertyInfo(name="进行催收", length=1)
    public Boolean collectionInd; 

    /**
     * blockcode需要控制的交易代码列表
     */
    public List<String> transList;

    /**
     * 系统自动添加标志
     */
    @PropertyInfo(name="系统自动添加标志", length=1)
    public Boolean sysInd;

    /**
     * 信函代码
     */
    @PropertyInfo(name="信函代码", length=8)
    public String letterCd;
    
    /**
     * 是否允许核销
     */
    @PropertyInfo(name="是否允许核销", length=1)
    public Boolean canWriteOff;

}