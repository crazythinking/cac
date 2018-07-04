package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.google.common.collect.ComparisonChain;

import net.engining.pg.support.meta.PropertyInfo;

public class RateCalcMethod implements Serializable, Comparable<RateCalcMethod>{
	
	private static final long serialVersionUID = -7647415205800225667L;

	@PropertyInfo(name="比率", length=9, precision=6)
    public BigDecimal rate;

    @PropertyInfo(name="对应最大金额", length=15, precision=2)
    public BigDecimal rateCeil;

    @PropertyInfo(name="对应基准金额", length=15, precision=2)
    public BigDecimal rateBase;

	@Override
	public int compareTo(RateCalcMethod o) {
		return ComparisonChain.start()
				.compare(rate, o.rate)
				.result();
	}

	public static void main(String[] args){
		RateCalcMethod r1 = new RateCalcMethod();
		r1.rate = BigDecimal.ONE;
		r1.rateBase = BigDecimal.ZERO;
		r1.rateCeil = BigDecimal.ZERO;
		
		RateCalcMethod r2 = new RateCalcMethod();
		r2.rate = BigDecimal.ONE;
		r2.rateBase = BigDecimal.ZERO;
		r2.rateCeil = BigDecimal.TEN;
		
		System.out.println(r1.compareTo(r2));
	}
}
