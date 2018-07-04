package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import net.engining.pcx.cc.param.model.enums.TierInd;
import net.engining.pg.support.meta.PropertyInfo;


/**
 * 交易费参数
 */
public class TxnFee implements Serializable {

	private static final long serialVersionUID = -3485684742192831359L;
    
    /**
     * 费用交易代码
     * 指向参数PostCode的主键
     */
    @PropertyInfo(name="费用交易代码", length=6)
    public String feePostCode;

    /**
     * 最小费用
     * 计算出的金额小于该金额则不收取
     */
    @PropertyInfo(name="最小费用", length=15, precision=2)
    public BigDecimal minFee;

    /**
     * 最大费用 
     */
    @PropertyInfo(name="最大费用", length=15, precision=2)
    public BigDecimal maxFee;

    /**
     * 费用计算层级指示
     * F - 根据全部金额计算
     * T - 分段计算
     */
    @PropertyInfo(name="费用计算层级指示", length=1)
    public TierInd tierInd;

    /**
     * 溢缴款费率
     */
    @PropertyInfo(name="溢缴款费率", length=7, precision=4)
    public BigDecimal despositRate;
    
    /**
     * 费率
     */
    public List<RateCalcMethod> chargeRates;
}