package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.param.model.enums.TierInd;
import net.engining.pg.parameter.HasEffectiveDate;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 利率表
 */
public class InterestTable implements HasEffectiveDate, Serializable, Comparable<InterestTable>{

	private static final long serialVersionUID = -5859826109972302412L;

	@PropertyInfo(name = "利率表标识", length = 20)
	public String interestCode;
	
	/**
	 * 描述
	 */
	@PropertyInfo(name = "描述", length = 40)
	public String description;

	/**
	 * 利息累计计算方式： F - 使用全部金额（full） T - 采用分段金额（tier）
	 */
	@PropertyInfo(name = "利息累计计算方式", length = 1)
	public TierInd tierInd;

	public List<RateCalcMethod> chargeRates;

	/**
	 * 与记息周期乘数合并，形成利率周期长度 如：6个月，cycleBase = M, cycleBaseMult = 6
	 */
	@PropertyInfo(name = "最小计息周期单位", length = 1)
	public Interval cycleBase;

	/**
	 * 与记息周期单位合并，形成利率周期长度 如：6个月期利率，cycleBase = M, cycleBaseMult = 6
	 */
	@PropertyInfo(name = "最小计息周期乘数", length = 4)
	public Integer cycleBaseMult;

	/**
	 * 表示利率计算规则中的数字是多少周期产生的利息 如 3.5%年利率，rateBaseInterval = Y, chargeRates.rate =
	 * 0.035
	 */
	@PropertyInfo(name = "利率基准周期", length = 1)
	public Interval rateBaseInterval;

	/**
	 * 如需同步行内利率，则设置该利率编码
	 */
	@PropertyInfo(name = "同步利率编码", length = 20)
	public String bankRateCode;

	/**
	 * 设置利率浮动比例
	 */
	@PropertyInfo(name = "上浮比例", length = 9, precision = 2)
	public BigDecimal upRate;

	/**
	 * 设置利率浮动比例
	 */
	@PropertyInfo(name = "下浮比例", length = 9, precision = 2)
	public BigDecimal downRate;

	@PropertyInfo(name = "生效日期")
	public Date effectiveDate;

	@Override
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	@Override
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Override
	public int compareTo(InterestTable o) {
		
		ComparisonChain chain = ComparisonChain.start()
				.compare(cycleBase, o.cycleBase)
				.compare(cycleBaseMult, o.cycleBaseMult)
				.compare(rateBaseInterval, o.rateBaseInterval);
		
		//FIXME 现在暂时只比较一个值，以后改成对象转成json穿，放到map比较
		Map<Integer, BigDecimal> rateCalcMethodMap = new HashMap<Integer, BigDecimal>();
		int n = 0;
		for(RateCalcMethod rateCalcMethod : chargeRates){
			rateCalcMethodMap.put(n, rateCalcMethod.rate);
			n++;
		}
		
		Map<Integer, BigDecimal> oldRateCalcMethodMap = new HashMap<Integer, BigDecimal>();
		int n1 = 0;
		for(RateCalcMethod rateCalcMethod : o.chargeRates){
			oldRateCalcMethodMap.put(n1, rateCalcMethod.rate);
			n1++;
		}
		
		MapDifference<Integer, BigDecimal> diffs= Maps.difference(rateCalcMethodMap, oldRateCalcMethodMap);
		
		//返回set1中包含且set2中不包含的元素，所以必须再倒过来比一遍，才能确定是否相同
//		Set<RateCalcMethod> diffs1 = Sets.difference(Sets.newHashSet(chargeRates), Sets.newHashSet(o.chargeRates));
//		Set<RateCalcMethod> diffs2 = Sets.difference(Sets.newHashSet(o.chargeRates), Sets.newHashSet(chargeRates));
//		
//		if(diffs1.size() != 0 || diffs2.size() != 0) {
//			return 1;
//		}
//		else{
//			return chain.result();
//		}
		
		if(!diffs.areEqual()){
			return 1;
		}
		else{
			return chain.result();
		}
				
	}
	
	public static void main(String[] args){
		// 0利率
		InterestTable it = new InterestTable();
		it.interestCode = "Z0001";
		it.description = "0利率";
		it.tierInd = TierInd.F;
		it.cycleBase = Interval.D;
		it.cycleBaseMult = 1;
		it.rateBaseInterval = Interval.Y;
		List<RateCalcMethod> list = new ArrayList<RateCalcMethod>();
		RateCalcMethod oc = new RateCalcMethod();
		oc.rate = BigDecimal.valueOf(0);
		oc.rateCeil = BigDecimal.valueOf(9999999999999l);
		oc.rateBase = BigDecimal.ZERO;
		list.add( oc );
		it.chargeRates = list;
		
		InterestTable it2 = new InterestTable();
		it2.interestCode = "Z0001";
		it2.description = "0利率";
		it2.tierInd = TierInd.F;
		it2.cycleBase = Interval.D;
		it2.cycleBaseMult = 1;
		it2.rateBaseInterval = Interval.Y;
		List<RateCalcMethod> list2 = new ArrayList<RateCalcMethod>();
		RateCalcMethod oc2 = new RateCalcMethod();
		oc2.rate = BigDecimal.valueOf(0);
		oc2.rateCeil = BigDecimal.valueOf(9999999999999l);
		oc2.rateBase = BigDecimal.ZERO;
		list2.add( oc2 );
		it2.chargeRates = list2;
		
		System.out.println(it.compareTo(it2));
	} 
}