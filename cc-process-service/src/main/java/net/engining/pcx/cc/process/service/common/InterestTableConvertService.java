package net.engining.pcx.cc.process.service.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.gm.param.model.OrganizationInfo;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.RateCalcMethod;
import net.engining.pg.parameter.ParameterFacility;

@Service
public class InterestTableConvertService {

	/**
	 * 获取参数工具类
	 */
	@Autowired
	private ParameterFacility parameterCacheFacility;
	
	//日利率保留小数位
	private int dailyRatesScale = 12;
	
	/**
	 * 转成日利率
	 * @return
	 */
	public List<RateCalcMethod> convertToDayRate(InterestTable interest){
		
		OrganizationInfo orgInfoParam = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
		List<RateCalcMethod> rates = null;
		switch(interest.rateBaseInterval){
			case D: rates = interest.chargeRates; break;
			case W: rates = getRates(interest, 1, 7); break;
			case M: rates = getRates(interest, 12, orgInfoParam.annualInterestRateBenchmarkDays);break;
			case Y: rates = getRates(interest, 1, orgInfoParam.annualInterestRateBenchmarkDays);break;
		}
		return rates;
	}
	
	/**
	 * 转成月利率
	 * @param interest
	 * @return
	 */
	public List<RateCalcMethod> convertToMonthRates(InterestTable interest){
		   
	   List<RateCalcMethod> rates = new ArrayList<RateCalcMethod>();
	   OrganizationInfo orgInfoParam = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
	   switch (interest.rateBaseInterval) {
		case D:
			rates = getRates(interest, orgInfoParam.annualInterestRateBenchmarkDays, 12);
			break;
		case W:
			rates = getRates(interest, orgInfoParam.annualInterestRateBenchmarkDays, 7*12);
			break;
		case M:
			rates = interest.chargeRates;
			break;
		case Y:
			rates = getRates(interest, 1, 12);
			break;
		default:
			break;
	   }
	 return rates;
	}
	
	/**
	 * 利率转换,根据利率基准周期计算最小计息周期单位的利率
	 * @param interest
	 * @return
	 */
	public List<RateCalcMethod> convertRate(InterestTable interest){
		
		OrganizationInfo orgInfoParam = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
		List<RateCalcMethod> rates = new ArrayList<RateCalcMethod>();
		if(interest.cycleBase == interest.rateBaseInterval){
			rates = interest.chargeRates;
		}else{
			switch(interest.cycleBase){
			case D : 
				if(interest.rateBaseInterval == Interval.W){
					rates = getRates(interest, 1, 7);
				}else if(interest.rateBaseInterval == Interval.M){
					rates = getRates(interest, 12, orgInfoParam.annualInterestRateBenchmarkDays);
				}else if(interest.rateBaseInterval == Interval.Y){
					rates = getRates(interest, 1, orgInfoParam.annualInterestRateBenchmarkDays);
				}
				break;
			case W :
				if(interest.rateBaseInterval == Interval.D){
					rates = getRates(interest, 7, 1);
				}else if(interest.rateBaseInterval == Interval.M){
					rates = getRates(interest, 7*12, orgInfoParam.annualInterestRateBenchmarkDays);
				}else if(interest.rateBaseInterval == Interval.Y){
					rates = getRates(interest, 7, orgInfoParam.annualInterestRateBenchmarkDays);
				}
				break;
			case M :
				if(interest.rateBaseInterval == Interval.D){
					rates = getRates(interest, orgInfoParam.annualInterestRateBenchmarkDays, 12);
				}else if(interest.rateBaseInterval == Interval.W){
					rates = getRates(interest, orgInfoParam.annualInterestRateBenchmarkDays, 7*12);
				}else if(interest.rateBaseInterval == Interval.Y){
					rates = getRates(interest, 1, 12);
				}
				break;
			case Y :
				if(interest.rateBaseInterval == Interval.D){
					rates = getRates(interest, orgInfoParam.annualInterestRateBenchmarkDays, 1);
				}else if(interest.rateBaseInterval == Interval.W){
					rates = getRates(interest, orgInfoParam.annualInterestRateBenchmarkDays, 7);
				}else if(interest.rateBaseInterval == Interval.M){
					rates = getRates(interest, 12, 1);
				}
				break;
			}
		}
		return rates;
	}
	
	private List<RateCalcMethod>  getRates(InterestTable interest,int mul, int div){
		InterestTable clonInterTable = (InterestTable) SerializationUtils.clone(interest);
		List<RateCalcMethod> rates = new ArrayList<RateCalcMethod>();
		for(RateCalcMethod rate : clonInterTable.chargeRates){
			rate.rate = rate.rate.multiply(BigDecimal.valueOf(mul)).divide(BigDecimal.valueOf(div), dailyRatesScale, RoundingMode.HALF_UP);
			rates.add(rate);
		}
		return rates;
	}

	public int getDailyRatesScale() {
		return dailyRatesScale;
	}

	public void setDailyRatesScale(int dailyRatesScale) {
		this.dailyRatesScale = dailyRatesScale;
	}
	
	
}
