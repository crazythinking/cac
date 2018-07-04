package net.engining.pcx.cc.param.model;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import net.engining.pcx.cc.param.model.enums.ChargeDateInd;
import net.engining.pcx.cc.param.model.enums.OverlimitCalcInd;
import net.engining.pcx.cc.param.model.enums.TierInd;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 超限费
 */
public class OverlimitCharge implements Serializable {

	private static final long serialVersionUID = -833152617511390854L;

	/**
	 * 描述
	 */
	@PropertyInfo(name="描述", length=20)  
    public String description;
	
	/**
	 * 超限费收取日期：
	 * P - 超限当天收取（posting date）
	 * C - 账单日收取（cycle date）
	 */
	@PropertyInfo(name="收取日期", length=1)  
    public ChargeDateInd chargeDateInd;
	
    /**
     * 超限费计算方法：
     * H - 最高超限>金额
     * P - 超限费入账当天超限金额
     */
    @PropertyInfo(name="计算基准金额", length=1)
    public OverlimitCalcInd calcInd;

    /**
     * 超限费比例计算方法
     * F - 按照全部金额计算
     * T - 分段计算
     */     
    @PropertyInfo(name="计算方法", length=1)
    public TierInd tierInd;
    
    public List<RateCalcMethod> chargeRates;

    /**
     * 超限费最小值
     * 如果计算出的超限费小于该金额，按该金额收取
     */
    @PropertyInfo(name="单笔最小金额", length=15, precision=2)
    public BigDecimal minCharge;

    /**
     * 超限费最大值
     */
    @PropertyInfo(name="单笔最大金额", length=15, precision=2)
    public BigDecimal maxCharge;

    /**
     * 年度累计超限费最大值
     */
    @PropertyInfo(name="年累计最大金额", length=15, precision=2)
    public BigDecimal yearMaxCharge;

    /**
     * 年度最大次数
     */
    @PropertyInfo(name="年累计最大次数", length = 2)
    public Integer yearMaxCnt;
}