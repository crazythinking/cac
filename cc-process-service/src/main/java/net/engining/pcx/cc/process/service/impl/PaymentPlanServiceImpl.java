package net.engining.pcx.cc.process.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.gm.param.model.OrganizationInfo;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactLoanPaymentDetail;
import net.engining.pcx.cc.infrastructure.shared.model.CactLoanPaymentPlan;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactLoanPaymentDetail;
import net.engining.pcx.cc.infrastructure.shared.model.QCactLoanPaymentPlan;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.RateCalcMethod;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.SubAcctType;
import net.engining.pcx.cc.param.model.enums.CalcMethod;
import net.engining.pcx.cc.param.model.enums.LoanFeeMethod;
import net.engining.pcx.cc.param.model.enums.ParamBaseType;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import net.engining.pcx.cc.process.service.AccountQueryService;
import net.engining.pcx.cc.process.service.PaymentPlanService;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.common.InterestTableConvertService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;

@Service
public class PaymentPlanServiceImpl implements PaymentPlanService {

	/**
	 * 系统日志
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ParameterFacility parameterCacheFacility;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@Autowired
	private InterestTableConvertService interestTableConvertUtils;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private ParameterFacility parameterFacility;
	
	@Autowired
	private AccountQueryService accountQueryService;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	private QCactLoanPaymentPlan qLoanPaymentPlan = QCactLoanPaymentPlan.cactLoanPaymentPlan;
	
	private QCactLoanPaymentDetail qLoanPaymentDetail = QCactLoanPaymentDetail.cactLoanPaymentDetail;
	
	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval,
			Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
			InterestTable it, LoanFeeMethod loanFeeMethod,
			CalcMethod loanFeeCalcMethod, BigDecimal feeAmount,
			BigDecimal feeRate, Date postDate, int pmtDueDays) {
		return regPaymentPlan(totalPeriod, interval, mult, paymentMethod, loanAmount, it, loanFeeMethod, loanFeeCalcMethod, feeAmount, feeRate, postDate, pmtDueDays, false);
	}
	
	
	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, 
			Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount, 
			InterestTable it, LoanFeeMethod loanFeeMethod, 
			CalcMethod loanFeeCalcMethod, BigDecimal feeAmount, 
			BigDecimal feeRate,	Date postDate, int pmtDueDays,
			Boolean intFirstPeriodAdj) {
		//涉及到参数的修改，所以在此做深复制操作
		InterestTable interestParam = (InterestTable) SerializationUtils.clone(it);
		PaymentPlan paymentPlan = new PaymentPlan();
		paymentPlan.setCreateDate(postDate);
		paymentPlan.setPostDate(postDate);
		paymentPlan.setPaymentMethod(paymentMethod);
		paymentPlan.setTotalLoanPeriod(totalPeriod);
		paymentPlan.setTotalLoanPrincipalAmt(loanAmount);
		paymentPlan.setLeftLoanPeriod(totalPeriod);
		paymentPlan.setLeftLoanPrincipalAmt(loanAmount);
		OrganizationInfo org = null;
		switch (it.rateBaseInterval){
		case D : 
			org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays)));
			break;
		case M :
			paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(12)));
			break;
		case W :
			org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays).divide(BigDecimal.valueOf(7))));
			break;
		default :
			paymentPlan.setYearRate(it.chargeRates.get(0).rate);
		}
		List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
		Map <Integer ,PaymentPlanDetail> detailsMap = new HashMap<Integer, PaymentPlanDetail>();
		BigDecimal totalBal = loanAmount;
		BigDecimal leftBal = loanAmount;
		LocalDate beginDate=null ;
		for (int i = 0 ; i < totalPeriod ; i ++){
			PaymentPlanDetail detail = new PaymentPlanDetail();
			detail.setLoanPeriod(i + 1);
			
			// 计算应收费用
//			detail.setFeeAmt(BigDecimal.ZERO);
//			if (loanFeeMethod == LoanFeeMethod.E){
//				if (loanFeeCalcMethod == CalcMethod.A){
//					detail.setFeeAmt(feeAmount);
//				}
//				else{
//					detail.setFeeAmt(totalBal.multiply(feeRate).setScale(2,BigDecimal.ROUND_HALF_UP));
//				}
//			}
//			else if (i == 0){
//				if (loanFeeCalcMethod == CalcMethod.A){
//					detail.setFeeAmt(feeAmount);
//				}
//				else{
//					detail.setFeeAmt(totalBal.multiply(feeRate));
//				}
//			}
			
			// 计算还款日
			switch (interval){
			case D:
				if (intFirstPeriodAdj != null && intFirstPeriodAdj){
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addDays(postDate, mult * (i + 1)), pmtDueDays));
				}
				else{
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addDays(DateUtils.addDays(postDate, -1), mult * (i + 1)), pmtDueDays));
				}
				break;
			case W:
				detail.setPaymentDate(DateUtils.addDays(DateUtils.addDays(postDate, mult * (i + 1) * 7), pmtDueDays));
				break;
			case M:
				switch(paymentMethod){
				case MRG:
					Calendar  calendar = Calendar.getInstance();  //对应到28号
					calendar.setTime(postDate);
					int day=calendar.get(Calendar.DAY_OF_MONTH)>=28?28:calendar.get(Calendar.DAY_OF_MONTH);
					calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
					postDate=calendar.getTime();
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addMonths(postDate, mult * (i + 1)), pmtDueDays));
					break;
				default:
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addMonths(postDate, mult * (i + 1)), pmtDueDays));
					break;
				}
				break;
			case Y:
				detail.setPaymentDate(DateUtils.addDays(DateUtils.addYears(postDate, mult * (i + 1)), pmtDueDays));
				break;
			}
			// 计算应收利息
			List<RateCalcMethod> calcRates = interestTableConvertUtils.convertRate(interestParam);
			detail.setInterestAmt((leftBal.multiply(calcRates.get(0).rate).multiply(BigDecimal.valueOf(mult))).setScale(2, RoundingMode.HALF_UP));

			//中民项目一次还本按月付息，末期利息特殊处理
			if(  i+1 == totalPeriod && paymentMethod.equals(PaymentMethod.IFP) && 
					it.rateBaseInterval.equals(Interval.Y)&& interval.equals(Interval.M) ){
				BigDecimal lastInterestAmt=
				(totalBal.multiply(it.chargeRates.get(0).rate).divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP ).multiply(new BigDecimal(totalPeriod) ))
				.subtract( detail.getInterestAmt().multiply( new BigDecimal(totalPeriod-1) ) )		;
				detail.setInterestAmt(lastInterestAmt.setScale(2, RoundingMode.HALF_UP) );
			}
			
			BigDecimal rate= calcRates.get(0).rate;
			
			// 计算本金
			switch(paymentMethod){
				case MRT:
				{
					if( Interval.D.equals(it.cycleBase) && 1==it.cycleBaseMult ){ //一次还本按日计息，则重算利息
						detail.setInterestAmt(
								(leftBal.multiply( paymentPlan.getYearRate()  ).multiply
										//剩余金额*日利率*实际天数
										(BigDecimal.valueOf(Days.daysBetween(beginDate, new LocalDate(detail.getPaymentDate()) ).getDays())
												)).setScale(2, RoundingMode.HALF_UP) );
						beginDate= new LocalDate(detail.getPaymentDate()) ;
						
					}
					
					if (i == totalPeriod - 1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					break;
				}
				
				case MRF:
				{	
					if( Interval.D.equals(it.cycleBase) && 1==it.cycleBaseMult ){ //一次还本按日计息，则重算利息
						detail.setInterestAmt(
								(leftBal.multiply( rate ).multiply
										//剩余金额*日利率*实际天数
										(BigDecimal.valueOf(Days.daysBetween(beginDate, new LocalDate(detail.getPaymentDate()) ).getDays())
												)).setScale(2, RoundingMode.HALF_UP) );
						beginDate=new LocalDate(detail.getPaymentDate());
					}
					if (i == 0){
						detail.setPrincipalBal(totalBal
								.subtract(
										totalBal.divide(
												BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP)
												.multiply(BigDecimal.valueOf(totalPeriod -1))));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					else{
						detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					break;
				}
				
				case MSV:
				{
					if (i == totalPeriod -1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(getMSVRepayAmt(loanAmount, calcRates.get(0).rate.multiply(BigDecimal.valueOf(mult)), totalPeriod).setScale(2, RoundingMode.HALF_UP).subtract(detail.getInterestAmt()));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					break;
				}
				case OPT:
				{
					detail.setPrincipalBal(totalBal);
					if( Interval.D.equals(it.cycleBase) ){ //一次还本按日计息，则重算利息
						detail.setInterestAmt(
								(leftBal.multiply( rate ).multiply
										//剩余金额*日利率*实际天数
										(BigDecimal.valueOf(Days.daysBetween(new LocalDate(postDate), new LocalDate(detail.getPaymentDate()) ).getDays())
												)).setScale(2, RoundingMode.HALF_UP) );
					}
					break;
				}
				case IFP:
				{
					if (i == totalPeriod - 1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(BigDecimal.ZERO);
					}
					if( Interval.D.equals(it.cycleBase)  ){ //一次还本按日计息，则重算利息
						detail.setInterestAmt(
								(totalBal.multiply( rate ).multiply
										//剩余金额*日利率*实际天数
										(BigDecimal.valueOf(Days.daysBetween(new LocalDate(postDate), new LocalDate(detail.getPaymentDate()) ).getDays())
												)).setScale(2, RoundingMode.HALF_UP) );
						beginDate=new LocalDate(detail.getPaymentDate());
					}
					break;
				}
				case IWP:
				{
					detail.setPrincipalBal(leftBal);
					leftBal = BigDecimal.ZERO;
					break;
				}
				case PSV:
				{
					if (i == totalPeriod - 1){
						leftBal=totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP).multiply( new BigDecimal(totalPeriod - 1)) ;
						detail.setPrincipalBal(totalBal.subtract(leftBal));
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
					}
					break;
				}
			default:
				throw new IllegalArgumentException(paymentMethod + "暂不支持");
			}
			
			//setup 原始计划金额
			detail.setOrigPrincipalBal(detail.getPrincipalBal());
			detail.setOrigInterestAmt(detail.getInterestAmt());
//			detail.setOrigFeeAmt(detail.getFeeAmt());
			
			detail.getAcctTypeAndAmtMap().put("INTE", detail.getInterestAmt());
			detail.getAcctTypeAndAmtMap().put("LBAL", detail.getPrincipalBal());
//			detail.getAcctTypeAndAmtMap().put("SFEE", detail.getFeeAmt());
			detail.getAcctTypeAndAmtMap().put("PNIT", detail.getPenalizedAmt());
			details.add(detail);
			detailsMap.put(detail.getLoanPeriod(), detail);
		}
		paymentPlan.setDetails(details);
		paymentPlan.setDetailsMap(detailsMap);
		return paymentPlan;
	}

	@Override
	public BigDecimal getCalculateInte(  CactAccount cactAccount ){
		Account account = newComputeService.retrieveAccount(cactAccount);
		BigDecimal intAmt=new BigDecimal(0);
		if( cactAccount.getCurrentLoanPeriod() >=cactAccount.getTotalLoanPeriod()) return intAmt; //说明最后一期已经结转出来,无需试算
		//余额成分提前结出来的情况
		Integer period = cactAccount.getCurrentLoanPeriod() +1 ; //结转以后的期次
		boolean flag=false ;
		if("D".equals(account.advanceType ) && 
				provider7x24.getCurrentDate().compareTo( new LocalDate(cactAccount.getInterestDate()))==0 ) flag=true ;//还款日期为到期日时，按到期日当天的利息做计算
		
		if("M".equals(account.advanceType ) || flag){  //提前还款参数为收取按月利息
			PaymentPlan paymentPlan =  getPaymentPlan(cactAccount.getAcctSeq() );
			if(paymentPlan == null ) return null ;
			PaymentPlanDetail paymentPlanDetail =paymentPlan.getDetailsMap().get(period) ;
			intAmt=paymentPlanDetail.getInterestAmt();
		}else{
			QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
			List<CactSubAcct> subAccts = new JPAQueryFactory(em)
					.select(qCactSubAcct)
					.from(qCactSubAcct)
					.where(qCactSubAcct.acctSeq.eq(cactAccount.getAcctSeq()))
					.fetch();
			CactSubAcct loanSubAcct = null;
			for(CactSubAcct cactSubAcct : subAccts){
				if (cactSubAcct.getSubAcctType().equals("LOAN")) {
					loanSubAcct = cactSubAcct;
				}
			}
			SubAcct subAcctParam = newComputeService.retrieveSubAcct( account.subAcctParam.get("LOAN"), cactAccount );
			InterestTable interest =  newComputeService.retrieveInterestTable(subAcctParam.intTables.get(0), cactAccount );
			List<RateCalcMethod> calcRates = new ArrayList<RateCalcMethod>();
			OrganizationInfo orgInfoParam = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			if(interest.rateBaseInterval == Interval.W){ //日利率转换
				calcRates = newComputeService.getRates(interest, 1, 7);
			}else if(interest.rateBaseInterval == Interval.M){
				calcRates = newComputeService.getRates(interest, 12, orgInfoParam.annualInterestRateBenchmarkDays);
			}else if(interest.rateBaseInterval == Interval.Y){
				calcRates = newComputeService.getRates(interest, 1, orgInfoParam.annualInterestRateBenchmarkDays);
			}
			
			LocalDate startDate = cactAccount.getLastInterestDate()==null ? new LocalDate(cactAccount.getSetupDate()) : 
				new LocalDate(cactAccount.getLastInterestDate() ) ;
			int mult= Days.daysBetween(startDate , new LocalDate( provider7x24.getCurrentDate() ) ).getDays()  ;
			intAmt=((loanSubAcct.getCurrBal().multiply(calcRates.get(0).rate).multiply(BigDecimal.valueOf(mult<=0?0:mult))).setScale(2, RoundingMode.HALF_UP));
		}
		return intAmt;
	}
	
	
	@Override
	public void savePaymentPlan(Integer acctSeq, String custId,
			String acctParamId, PaymentPlan plan, Date saveDate) {
		if (plan != null){
			CactLoanPaymentPlan paymentPlan = new CactLoanPaymentPlan();
			paymentPlan.setAcctSeq(acctSeq);
			paymentPlan.setCustId(custId);
			paymentPlan.setAcctParamId(acctParamId);
			paymentPlan.setPaymentMethod(plan.getPaymentMethod());
			paymentPlan.setSetupDate(saveDate);
			paymentPlan.setTotalLoanPeriod(plan.getTotalLoanPeriod());
			paymentPlan.setTotalLoanPrincipalAmt(plan.getTotalLoanPrincipalAmt());
			paymentPlan.setYearRate(plan.getYearRate());
			paymentPlan.setLeftLoanPeriod(plan.getLeftLoanPeriod());
			paymentPlan.setLeftLoanPrincipalAmt(plan.getLeftLoanPrincipalAmt());
			paymentPlan.setPostDate(plan.getPostDate());
			em.persist(paymentPlan);
			for (PaymentPlanDetail detail : plan.getDetails()){
				CactLoanPaymentDetail paymentDetail = new CactLoanPaymentDetail();
				paymentDetail.setAcctSeq(acctSeq);
//				paymentDetail.setFeeAmt(detail.getFeeAmt());
				paymentDetail.setInterestAmt(detail.getInterestAmt());
				paymentDetail.setLoanPeriod(detail.getLoanPeriod());
				paymentDetail.setPaymentDate(detail.getPaymentDate());
				paymentDetail.setPlanSeq(paymentPlan.getPlanSeq());
				paymentDetail.setPrincipalBal(detail.getPrincipalBal());
				em.persist(paymentDetail);
			}
		}
	}
	
	@Override
	public void updatePaymentPlan(Integer acctSeq, String custId,
			String acctParamId, PaymentPlan plan, Date saveDate) {
			
			List<CactLoanPaymentPlan> cactLoanPaymentPlans = new JPAQueryFactory(em)
					.select(qLoanPaymentPlan)
					.from(qLoanPaymentPlan)
					.where(qLoanPaymentPlan.acctSeq.eq(acctSeq))
					.fetch();
			if (cactLoanPaymentPlans != null && cactLoanPaymentPlans.size()>0) {
				for(CactLoanPaymentPlan loanPlan : cactLoanPaymentPlans ){
					loanPlan.setAcctSeq(acctSeq);
					loanPlan.setCustId(custId);
					loanPlan.setAcctParamId(acctParamId);
					loanPlan.setPaymentMethod(plan.getPaymentMethod());
					loanPlan.setTotalLoanPeriod(plan.getTotalLoanPeriod());
					loanPlan.setTotalLoanPrincipalAmt(plan.getTotalLoanPrincipalAmt());
					loanPlan.setYearRate(plan.getYearRate());
					loanPlan.setLeftLoanPeriod(plan.getLeftLoanPeriod());
					loanPlan.setLeftLoanPrincipalAmt(plan.getLeftLoanPrincipalAmt());
					loanPlan.setPostDate(plan.getPostDate());
					break ;
				}
			}
			
			
			List<CactLoanPaymentDetail> cactloanpaymentdetail = new JPAQueryFactory(em)
					.select(qLoanPaymentDetail)
					.from(qLoanPaymentDetail)
					.where(qLoanPaymentDetail.acctSeq.eq(acctSeq))
					.fetch();
			if (cactloanpaymentdetail != null && cactloanpaymentdetail.size()>0) {
				for (PaymentPlanDetail detail : plan.getDetails()){
					for(CactLoanPaymentDetail loanDetail : cactloanpaymentdetail ){
						if(loanDetail.getLoanPeriod()== detail.getLoanPeriod() ){
//							loanDetail.setFeeAmt(detail.getFeeAmt());
							loanDetail.setInterestAmt(detail.getInterestAmt());
							loanDetail.setLoanPeriod(detail.getLoanPeriod());
							loanDetail.setPaymentDate(detail.getPaymentDate());
							loanDetail.setPrincipalBal(detail.getPrincipalBal());
						}
					}
				}
		}
			
	}
	
	@Override
	public PaymentPlan findLatestPaymentPlan(Integer acctSeq) {
		List<CactLoanPaymentPlan> planList = new JPAQueryFactory(em)
				.select(qLoanPaymentPlan)
				.from(qLoanPaymentPlan)
				.where(qLoanPaymentPlan.acctSeq.eq(acctSeq))
				.orderBy(qLoanPaymentPlan.planSeq.desc())
				.fetch();
		if (planList.size() > 0){
			PaymentPlan plan = new PaymentPlan();
			plan.setCreateDate(planList.get(0).getSetupDate());
			plan.setLeftLoanPeriod(planList.get(0).getLeftLoanPeriod());
			plan.setLeftLoanPrincipalAmt(planList.get(0).getLeftLoanPrincipalAmt());
			plan.setPaymentMethod(planList.get(0).getPaymentMethod());
			plan.setPostDate(planList.get(0).getPostDate());
			plan.setTotalLoanPeriod(planList.get(0).getTotalLoanPeriod());
			plan.setTotalLoanPrincipalAmt(planList.get(0).getTotalLoanPrincipalAmt());
			plan.setYearRate(planList.get(0).getYearRate());
			
			QCactLoanPaymentDetail qLoanPaymentDetail = QCactLoanPaymentDetail.cactLoanPaymentDetail;
			List<CactLoanPaymentDetail> detailList = new JPAQueryFactory(em)
					.select(qLoanPaymentDetail)
					.from(qLoanPaymentDetail)
					.where(qLoanPaymentDetail.planSeq.eq(planList.get(0).getPlanSeq()))
					.orderBy(qLoanPaymentDetail.paymentDate.asc())
					.fetch();
			List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
			for (CactLoanPaymentDetail item : detailList){
				PaymentPlanDetail detail = new PaymentPlanDetail();
//				detail.setFeeAmt(item.getFeeAmt());
				detail.setInterestAmt(item.getInterestAmt());
				detail.setLoanPeriod(item.getLoanPeriod());
				detail.setPaymentDate(item.getPaymentDate());
				detail.setPrincipalBal(item.getPrincipalBal());
				details.add(detail);
			}
			plan.setDetails(details);
			return plan;
		}
		return null;
	}
	@Override
	public PaymentPlan searchPaymentPlan(Integer acctSeq){
		
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);
		
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		Account account = newComputeService.retrieveAccount(cactAccount);
		
		List<CactSubAcct> subAccts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(qCactSubAcct.acctSeq.eq(acctSeq))
				.fetch();
		
		BigDecimal currBal=new BigDecimal(0); //总余额
		for (CactSubAcct cactSubAcct : subAccts){
			currBal = currBal.add(cactSubAcct.getCurrBal().add(cactSubAcct.getPenalizedAmt()) ); //余额+未结罚息
		}
		
		SubAcct subAcctParam = newComputeService.retrieveSubAcct(account.subAcctParam.get("LOAN"), cactAccount);
		InterestTable it =  newComputeService.retrieveInterestTable(subAcctParam.intTables.get(0), cactAccount);
		
		PaymentPlan paymentPlan =  getPaymentPlan(acctSeq);
		boolean firstFlag= false ;
		
		if(paymentPlan == null)	{
			if(account.paymentMethod.equals(PaymentMethod.MRG) || 
					account.paymentMethod.equals(PaymentMethod.MSF)){
				paymentPlan =  regPaymentPlan(
						cactAccount.getTotalLoanPeriod(), 
						account.intUnit,
						account.intUnitMult, 
						account.paymentMethod,
						cactAccount.getTotalLoanPrincipalAmt(),
						it ,
						account.loanFeeMethod,
						account.loanFeeCalcMethod, 
						account.feeAmount, 
						account.feeRate,
						cactAccount.getSetupDate(),
						account.pmtDueDays,
						account.intFirstPeriodAdj,
						cactAccount.getAcctNo()
				);
			}else{
				paymentPlan =  regPaymentPlan(
						cactAccount.getTotalLoanPeriod(), 
						account.intUnit,
						account.intUnitMult, 
						account.paymentMethod,
						cactAccount.getTotalLoanPrincipalAmt(),
						it ,
						account.loanFeeMethod,
						account.loanFeeCalcMethod, 
						account.feeAmount, 
						account.feeRate,
						cactAccount.getSetupDate(),
						account.pmtDueDays,
						account.intFirstPeriodAdj);
			}
			firstFlag= true ;
		}
		
		paymentPlan.setAcctSeq(acctSeq);
		paymentPlan.setProdAcctParamId(account.paramId);
		paymentPlan.setProdAcctParamDesc(account.description);
		
		Map <Integer ,PaymentPlanDetail> detailsMap = paymentPlan.getDetailsMap();
		 
		//利息余额
		BigDecimal intAmt = BigDecimal.ZERO;
		//溢缴款余额
		BigDecimal paymAmt = BigDecimal.ZERO;
		
		//当前贷款期数
		Integer currentLoanPeriod = cactAccount.getCurrentLoanPeriod() > cactAccount.getTotalLoanPeriod() ? 
					cactAccount.getTotalLoanPeriod() : cactAccount.getCurrentLoanPeriod();
		
		//如果过了最后一期，则清空全部还款计划;贷款余额为0
		if(cactAccount.getCurrentLoanPeriod() > cactAccount.getTotalLoanPeriod() - 1 ||
				currBal.compareTo(new BigDecimal(0))==0 )
		 {
			 for(PaymentPlanDetail cleanDetail : detailsMap.values())
			 {
				 cleanDetail.setPrincipalBal(BigDecimal.ZERO);
				 cleanDetail.setInterestAmt(BigDecimal.ZERO);
				 cleanDetail.setPenalizedAmt(BigDecimal.ZERO);
//				 cleanDetail.setFeeAmt(BigDecimal.ZERO);
				 cleanDetail.setTotalRepayAmt(BigDecimal.ZERO);
				 for (String s : cleanDetail.getAcctTypeAndAmtMap().keySet()){
					 cleanDetail.getAcctTypeAndAmtMap().put(s, BigDecimal.ZERO);
				 }
			 }
		 }
		 else
		 {
			 for (int i = 1; i <= currentLoanPeriod; i++){
					PaymentPlanDetail cleanDetail = detailsMap.get(Integer.valueOf(i));
					cleanDetail.setPrincipalBal(BigDecimal.ZERO);
					cleanDetail.setInterestAmt(BigDecimal.ZERO);
					cleanDetail.setPenalizedAmt(BigDecimal.ZERO);
//					cleanDetail.setFeeAmt(BigDecimal.ZERO);
					cleanDetail.setTotalRepayAmt(BigDecimal.ZERO);
					for (String s : cleanDetail.getAcctTypeAndAmtMap().keySet()){
						cleanDetail.getAcctTypeAndAmtMap().put(s, BigDecimal.ZERO);
					}
				}
			 
		 }
		
		Map<Integer, BigDecimal> penalizedAmtMap = new HashMap<Integer, BigDecimal>();
		
		// 根据当前子账户信息填充历史应还款
		for(CactSubAcct cactSubAcct : subAccts){
			Integer period = cactAccount.getCurrentLoanPeriod() +1 - cactSubAcct.getStmtHist();
			// 最后一期以后产生的利息费用，算入最后一期
			if (period > cactAccount.getTotalLoanPeriod()){
				period = cactAccount.getTotalLoanPeriod();
			}
			
			//BigDecimal penalizedamt= getSubAcctPenalizedAmt(cactAccount,cactSubAcct); //方法待验证
			//累加所有未入账罚息，将其计入当前期数的应收罚息
			if(cactSubAcct.getPenalizedAmt().compareTo(BigDecimal.ZERO)>0){
				if (penalizedAmtMap.get(period) != null){
					penalizedAmtMap.put(period, penalizedAmtMap.get(period).add(cactSubAcct.getPenalizedAmt().setScale(2, RoundingMode.HALF_UP)));
				}
				else{
					penalizedAmtMap.put(period,cactSubAcct.getPenalizedAmt().setScale(2, RoundingMode.HALF_UP));
				}
			}
			//累加所有未入账利息，将其计入当前期数的应收利息
			if(cactSubAcct.getIntReceivable().compareTo(BigDecimal.ZERO)>0){
				intAmt = intAmt.add(cactSubAcct.getIntReceivable());
			}
			//修正历史应还罚息
			if(cactSubAcct.getSubAcctType().equals("PNIT")&& cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO)>=0){
				logger.info("PNIT="+detailsMap.get(period).getPenalizedAmt());
				detailsMap.get(period).setPenalizedAmt(detailsMap.get(period).getPenalizedAmt().add(cactSubAcct.getCurrBal()));
			}
			//修正历史应还本金
			if(cactSubAcct.getSubAcctType().equals("LBAL")&& cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO)>=0){
				logger.info("LBAL="+detailsMap.get(period).getPrincipalBal());
				detailsMap.get(period).setPrincipalBal(cactSubAcct.getCurrBal());
			}
			
			//修正历史应还利息
			if(cactSubAcct.getSubAcctType().equals("INTE")&& cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO)>=0){
				logger.info("int="+detailsMap.get(period).getInterestAmt());
				detailsMap.get(period).setInterestAmt(cactSubAcct.getCurrBal());
			}
			
			//修正历史应还费用
//			if(cactSubAcct.getSubAcctType().equals("SFEE")&& cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO)>=0){
//				detailsMap.get(period).setFeeAmt(detailsMap.get(period).getFeeAmt().add(cactSubAcct.getCurrBal()));
//			}
			//存下溢缴款，
			if(cactSubAcct.getSubAcctType().equals("PAYM")&& cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) < 0){
				paymAmt = cactSubAcct.getCurrBal().abs();
			}
			if ((!cactSubAcct.getSubAcctType().equals("LOAN")) && (!cactSubAcct.getSubAcctType().equals("PAYM"))){
				if (detailsMap.get(period).getAcctTypeAndAmtMap().get(cactSubAcct.getSubAcctType()) != null){
					detailsMap.get(period).getAcctTypeAndAmtMap().put(cactSubAcct.getSubAcctType(), detailsMap.get(period).getAcctTypeAndAmtMap().get(cactSubAcct.getSubAcctType()).add(cactSubAcct.getCurrBal()));
				}
				else{
					detailsMap.get(period).getAcctTypeAndAmtMap().put(cactSubAcct.getSubAcctType(), cactSubAcct.getCurrBal());
				}
			}
		}
		
		//将所有未入账罚息更新至当前应还罚息
		for (Integer period : penalizedAmtMap.keySet()){
			detailsMap.get(period).setPenalizedAmt(detailsMap.get(period).getPenalizedAmt().add(penalizedAmtMap.get(period)));
			if (detailsMap.get(period).getAcctTypeAndAmtMap().get("PNIT") != null){
				detailsMap.get(period).getAcctTypeAndAmtMap().put("PNIT", detailsMap.get(period).getAcctTypeAndAmtMap().get("PNIT").add(penalizedAmtMap.get(period)));
			}
			else{
				detailsMap.get(period).getAcctTypeAndAmtMap().put("PNIT", penalizedAmtMap.get(period));
			}
		}
		
		if( intAmt.compareTo(BigDecimal.ZERO)>0){
			Integer period;
			if(currentLoanPeriod == 0) {
				period = 1;
			} else if(currentLoanPeriod > cactAccount.getTotalLoanPeriod()){
				period = cactAccount.getTotalLoanPeriod();
			} else {
				period = currentLoanPeriod;
			}
			detailsMap.get(period).setInterestAmt(detailsMap.get(period).getInterestAmt());
			if (detailsMap.get(period).getAcctTypeAndAmtMap().get("INTE") != null){
				detailsMap.get(period).getAcctTypeAndAmtMap().put("INTE", detailsMap.get(period).getAcctTypeAndAmtMap().get("INTE"));
			}
			else{
				detailsMap.get(period).getAcctTypeAndAmtMap().put("INTE", intAmt);
			}
			
		}
		
		//溢缴款冲销
		if(paymAmt.compareTo(BigDecimal.ZERO)>0){
			//因有溢缴款，所以账龄肯定为0，直接取账龄为0的冲销顺序
			List<SubAcctType> acctTypeList = account.paymentHier.get("0");
			
			//构建TreeMap以保证排序
			TreeMap<Integer, PaymentPlanDetail> treeMap = 
					new TreeMap<Integer, PaymentPlanDetail>(detailsMap);
			
			for(PaymentPlanDetail paymentPlanDetail : treeMap.values()){
				//如果有溢缴款，从当前期开始往后冲销
				if(paymAmt.compareTo(BigDecimal.ZERO)>0){
					
					for(SubAcctType st : acctTypeList){
						BigDecimal subAmt = paymentPlanDetail.getAcctTypeAndAmtMap().get(st.subAcctType);
						
						if(subAmt != null && subAmt.compareTo(BigDecimal.ZERO)>0 && paymAmt.compareTo(BigDecimal.ZERO)>0){
							
							if(subAmt.compareTo(paymAmt)<=0){
								
								paymAmt = paymAmt.subtract(subAmt);
								subAmt = BigDecimal.ZERO;
								updatePaymentPlanDetail(st.subAcctType ,subAmt ,paymentPlanDetail); 
								
							}else{
								subAmt = subAmt.subtract(paymAmt);
								paymAmt = BigDecimal.ZERO;
								updatePaymentPlanDetail(st.subAcctType ,subAmt ,paymentPlanDetail); 
								break;
							}
							
						}
						
					}
					
				 }
					
			}
			
		}
		
		// 修正剩余贷款期数
		paymentPlan.setLeftLoanPeriod(Integer.valueOf(0));
		
		// 修正剩余贷款本金:先置0
		paymentPlan.setLeftLoanPrincipalAmt(BigDecimal.ZERO);
		
		for(PaymentPlanDetail detail :detailsMap.values()){
			
			// 修正剩余贷款本金:累加所有还款计划中的未还本金
			paymentPlan.setLeftLoanPrincipalAmt(paymentPlan.getLeftLoanPrincipalAmt().add(detail.getPrincipalBal()));
			
			detail.setTotalRepayAmt(detail.getInterestAmt()
//					.add(detail.getFeeAmt())
					.add(detail.getPenalizedAmt())
					.add(detail.getPrincipalBal()).setScale(2,RoundingMode.HALF_UP));
			
			if (detail.getTotalRepayAmt().compareTo(BigDecimal.ZERO) > 0) {
				paymentPlan.setLeftLoanPeriod(paymentPlan.getLeftLoanPeriod() + 1);
			}
		}
		modifyPaymentPlan(paymentPlan,cactAccount ,firstFlag );
		
		for(PaymentPlanDetail paymentPlanDetail : paymentPlan.getDetailsMap().values()) {
			logger.info("日期{},期数{},应还总金额{},应还本金{},应还利息{},应还罚息{}",
					paymentPlanDetail.getPaymentDate(),
					paymentPlanDetail.getLoanPeriod(),
					paymentPlanDetail.getTotalRepayAmt(),
					paymentPlanDetail.getPrincipalBal(),
//					paymentPlanDetail.getFeeAmt(),
					paymentPlanDetail.getInterestAmt(),
					paymentPlanDetail.getPenalizedAmt());
		}
		
		
		return paymentPlan;
	}
	
	/**
	 * 还款计划修正
	 * 
	 */
	public PaymentPlan modifyPaymentPlan(PaymentPlan paymentPlan ,CactAccount cactAccount,Boolean flag){
		if(flag) { //首次放款的时候需要根据按日或者按月修还款计划表
			for(int i=1 ;i<=paymentPlan.getDetailsMap().size() ; i++){
				Date paymentDate=paymentPlan.getDetailsMap().get(i).getPaymentDate();
				
				if( provider7x24.getCurrentDate().compareTo(new LocalDate(paymentDate))<= 0  ){ //根据按日或者按月对还款计划修正
					if(i > 1 ){
						paymentPlan.getDetailsMap().get(i).setInterestAmt(new BigDecimal(0));
						paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(new BigDecimal(0))  );
					}else{
						Account account = newComputeService.retrieveAccount(cactAccount);
						if("M".equals(account.advanceType ))  continue ;
						BigDecimal intAmount= getCalculateInte(cactAccount) ;//取未决利息
						paymentPlan.getDetailsMap().get(i).setInterestAmt( paymentPlan.getDetailsMap().get(i).getInterestAmt().add(intAmount) );
						paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add
								(paymentPlan.getDetailsMap().get(i).getInterestAmt() )  );
					}
				}
			}
			return paymentPlan ;
		}
		BigDecimal calBal =new BigDecimal(0);
		List<CactSubAcct> subAccts=accountQueryService.getSubAcctsByAccountSeq(cactAccount.getAcctSeq());
		for(CactSubAcct cactSubAcct : subAccts){
			if (cactSubAcct.getSubAcctType().equals("LOAN")) {
				calBal=cactSubAcct.getCurrBal(); //冲销以后的金
				break ;
			}
		}
		if(calBal.compareTo(new BigDecimal(0))==0) return paymentPlan;  //无未还的情况
		//修还款计划信息
		int firstPeriod = 1 ;
		for(int i=1 ;i<=paymentPlan.getDetailsMap().size() ; i++){
			Date paymentDate=paymentPlan.getDetailsMap().get(i).getPaymentDate();
			
			if( provider7x24.getCurrentDate().compareTo(new LocalDate(paymentDate))<= 0  ){ //根据按日或者按月对还款计划修正
				if(firstPeriod ==1 ){
					 //已经结息的不做还款计划修正
					if(cactAccount.getLastInterestDate()!=null && cactAccount.getLastInterestDate().compareTo(cactAccount.getInterestDate())==0 ) {
						firstPeriod++ ;
						continue ;
					}
					BigDecimal intAmount= getCalculateInte(cactAccount) ;
					paymentPlan.getDetailsMap().get(i).setInterestAmt(intAmount);
					paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(intAmount)  );
				}else{
					paymentPlan.getDetailsMap().get(i).setInterestAmt(new BigDecimal(0));
					paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(new BigDecimal(0))  );
				}
				firstPeriod++ ;
			}
		}
		return paymentPlan ;
	}
	
	
	public void updatePaymentPlanDetail(String subAcctType,BigDecimal subAmt,PaymentPlanDetail paymentPlanDetail ){
		
		if(subAcctType.equals("INTE")){
			
			paymentPlanDetail.setInterestAmt(subAmt);
			
		}else if(subAcctType.equals("LBAL")){
			
			paymentPlanDetail.setPrincipalBal(subAmt);
			
//		}else if(subAcctType.equals("SFEE")){
			
//			paymentPlanDetail.setFeeAmt(subAmt);
			
		}else if(subAcctType.equals("PNIT")){
			
			paymentPlanDetail.setPenalizedAmt(subAmt);
		} 
	}
	
	/**
	 * 等额本息计算公式：B=a*i(1+i)^(n-1)/[(1+i)^N-1],a：贷款本金 ，i：贷款月利率， n：贷款月数
	 * @param totalAmt 贷款本金
	 * @param mRate 月利率
	 * @param mths  还款月数
	 * @return
	 */
	public BigDecimal getMSVRepayAmt(BigDecimal totalAmt, BigDecimal mRate, int mths){
		BigDecimal amt = (totalAmt.multiply(mRate).multiply((new BigDecimal(1).add(mRate)).pow(mths)))
				.divide((new BigDecimal(1).add(mRate)).pow(mths).subtract(new BigDecimal(1)), 6, RoundingMode.HALF_UP);
		return amt;
	}
	
	/**
	 * 等额本息第n个月还贷利息计算公式：X=BX-B= a*i(1+i)^N/[(1+i)^N-1]- a*i(1+i)^(n-1)/[(1+i)^N-1],
	 * （注：BX=等额本息还贷每月所还本金和利息总额，
		B=等额本息还贷每月所还本金，
		a=贷款总金额
		i=贷款月利率，
		N=还贷总月数，
		n=第n期还贷数
		X=等额本息还贷每月所还的利息）
	 * @param totalAmt 贷款总金额
	 * @param mRate 贷款月利率
	 * @param mths  还贷总月数
	 * @param nPeriod  第n期还贷数
	 * @return
	 */
	public BigDecimal getMSVInterest(BigDecimal totalAmt, BigDecimal mRate, int mths, int nPeriod){
		BigDecimal amt = (totalAmt.multiply(mRate).multiply((new BigDecimal(1).add(mRate)).pow(mths)))
				.divide((new BigDecimal(1).add(mRate)).pow(mths).subtract(new BigDecimal(1)), 6, RoundingMode.HALF_UP)
				.subtract((totalAmt.multiply(mRate).multiply((new BigDecimal(1).add(mRate)).pow(nPeriod-1)))
							.divide((new BigDecimal(1).add(mRate)).pow(mths).subtract(new BigDecimal(1)), 6, RoundingMode.HALF_UP));
		return amt;
	}
	
	public BigDecimal getSubAcctPenalizedAmt(CactAccount cactAccount,CactSubAcct cactSubAcct ){ 
		BigDecimal penalizedAmt = new BigDecimal(0);
		LocalDate processDate=new LocalDate(systemStatusFacility.getSystemStatus().businessDate); //业务日期
		LocalDate graceDate=  new LocalDate(cactAccount.getGraceDate());
		LocalDate startDate;
		Account account = newComputeService.retrieveAccount(cactAccount);
		if( newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()) == AgeGroupCd.Normality    ){
			//宽限期当天晚上在批量拉起与罚息结息之间还款，所以需要重算罚息
			if (graceDate!=null && processDate.isAfter( graceDate)
							&& cactAccount.getTotDueAmt().compareTo(BigDecimal.ZERO) > 0
							&& cactAccount.getFirstOverdueDate() == null 
							&& (cactAccount.getAgeCd().equals("0") || cactAccount.getAgeCd().equals("C"))) {				
				//取宽限期内这几天的罚息
				InterestTable interestTable = parameterFacility.loadParameter(
						InterestTable.class, 
						cactSubAcct.getPenalizedInterestCode(), 
						account.intParamBaseType == ParamBaseType.Fixed ? cactAccount.getSetupDate() : processDate.toDate());
				BigDecimal interest = newInterestService.calcInterest(new LocalDate(cactAccount.getPmtDueDate()), processDate,
				cactSubAcct.getCurrBal(),ImmutableList.of(interestTable), newComputeService.getReceivableScale(), InterestCycleRestMethod.NA);
				if(interest==null)interest=new BigDecimal(0);
				return 	interest ;
			}
			return penalizedAmt;
		}else{//逾期的情况下罚息计算
			if(cactSubAcct.getLastPenalizedInterestDate() == null){ //在逾期和非应计的情况下,计算罚息,为空则说明当前第2次和以后期次的罚息刚做结转出来
				 // 罚息起息日为账户上宽限日
				 startDate = new LocalDate(cactAccount.getGraceDate());
			}
			else
			{
				 startDate = new LocalDate(cactSubAcct.getLastPenalizedInterestDate()).plusDays(1); //上次结息日加1
			}
			if (cactSubAcct.getLastPenalizedInterestDate() == null && 
					( processDate.isBefore(startDate) || processDate.isEqual(startDate) )  ) //宽限日当天和宽限期前
			{
				// 还没到起息日，不处理
				return penalizedAmt;
			}
			InterestTable interestTable = parameterFacility.loadParameter(
					InterestTable.class, 
					cactSubAcct.getPenalizedInterestCode(), 
					account.intParamBaseType == ParamBaseType.Fixed ? cactAccount.getSetupDate() : processDate.toDate());
			BigDecimal interest = newInterestService.calcInterest(new LocalDate(cactAccount.getPmtDueDate()), processDate,
			cactSubAcct.getCurrBal(),ImmutableList.of(interestTable), newComputeService.getReceivableScale(), InterestCycleRestMethod.NA);
			if(interest==null)interest=new BigDecimal(0);
			return 	interest ;
			
		}
	}


	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval,
			Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
			InterestTable it, LoanFeeMethod loanFeeMethod,
			CalcMethod loanFeeCalcMethod, BigDecimal feeAmount,
			BigDecimal feeRate, Date postDate, int pmtDueDays, Integer acctno) {
		return regPaymentPlan(totalPeriod, interval, mult, paymentMethod, loanAmount, it, loanFeeMethod, loanFeeCalcMethod, feeAmount, feeRate, postDate, pmtDueDays, false,acctno);
	}

	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval,
			Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
			InterestTable it, LoanFeeMethod loanFeeMethod,
			CalcMethod loanFeeCalcMethod, BigDecimal feeAmount,
			BigDecimal feeRate, Date postDate, int pmtDueDays,
			Boolean intFirstPeriodAdj, Integer acctNo) {

		//涉及到参数的修改，所以在此做深复制操作
		InterestTable interestParam = (InterestTable) SerializationUtils.clone(it);
		PaymentPlan paymentPlan = new PaymentPlan();
		paymentPlan.setCreateDate(postDate);
		paymentPlan.setPostDate(postDate);
		paymentPlan.setPaymentMethod(paymentMethod);
		paymentPlan.setTotalLoanPeriod(totalPeriod);
		paymentPlan.setTotalLoanPrincipalAmt(loanAmount);
		paymentPlan.setLeftLoanPeriod(totalPeriod);
		paymentPlan.setLeftLoanPrincipalAmt(loanAmount);
		OrganizationInfo org = null;
		switch (it.rateBaseInterval){
		case D : 
			org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays)));
			break;
		case M :
			paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(12)));
			break;
		case W :
			org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays).divide(BigDecimal.valueOf(7))));
			break;
		default :
			paymentPlan.setYearRate(it.chargeRates.get(0).rate);
		}
		List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
		Map <Integer ,PaymentPlanDetail> detailsMap = new HashMap<Integer, PaymentPlanDetail>();
		BigDecimal totalBal = loanAmount;
		BigDecimal leftBal = loanAmount;
		LocalDate beginDate=null ;
		Date loanBeginDate=postDate ;
		Date beginDay= newComputeService.getBeginDay(acctNo); //首次放款日期
		Calendar  calendar1 = Calendar.getInstance() ; 
		calendar1.setTime(beginDay);
		int day= calendar1.get(Calendar.DAY_OF_MONTH)>=28?28:calendar1.get(Calendar.DAY_OF_MONTH);  //首次放款的日
		calendar1.setTime(postDate);
		calendar1.set(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH)+1, day);
		int betweenday=Days.daysBetween(new LocalDate(postDate), new LocalDate(calendar1.getTime())).getDays()  ;
		
		for (int i = 0 ; i < totalPeriod ; i ++){
			PaymentPlanDetail detail = new PaymentPlanDetail();
			detail.setLoanPeriod(i + 1);
			
			// 计算应收费用
//			detail.setFeeAmt(BigDecimal.ZERO);
//			if (loanFeeMethod == LoanFeeMethod.E){
//				if (loanFeeCalcMethod == CalcMethod.A){
//					detail.setFeeAmt(feeAmount);
//				}
//				else{
//					detail.setFeeAmt(totalBal.multiply(feeRate).setScale(2,BigDecimal.ROUND_HALF_UP));
//				}
//			}
//			else if (i == 0){
//				if (loanFeeCalcMethod == CalcMethod.A){
//					detail.setFeeAmt(feeAmount);
//				}
//				else{
//					detail.setFeeAmt(totalBal.multiply(feeRate));
//				}
//			}
			
			// 计算还款日
			switch (interval){
			case D:
				if (intFirstPeriodAdj != null && intFirstPeriodAdj){
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addDays(postDate, mult * (i + 1)), pmtDueDays));
				}
				else{
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addDays(DateUtils.addDays(postDate, -1), mult * (i + 1)), pmtDueDays));
				}
				break;
			case W:
				detail.setPaymentDate(DateUtils.addDays(DateUtils.addDays(postDate, mult * (i + 1) * 7), pmtDueDays));
				break;
			case M:
				if(paymentMethod.equals(PaymentMethod.MRG)|| paymentMethod.equals(PaymentMethod.MSF) ){
					Calendar  calendar = Calendar.getInstance();
					calendar.setTime(postDate);
					if(i==0){
						if( calendar.get(Calendar.DAY_OF_MONTH) <= day || beginDay.equals(postDate) ){
							calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
							postDate=calendar.getTime();
							detail.setPaymentDate(DateUtils.addDays(DateUtils.addMonths(postDate, mult * (i + 1)), pmtDueDays));
						}else{
							calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
							postDate=calendar.getTime();
							postDate=DateUtils.addMonths(postDate, 1);
							detail.setPaymentDate(DateUtils.addDays(DateUtils.addMonths(postDate, mult * (i + 1)), pmtDueDays));
						}
					}else{
						calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
						postDate=calendar.getTime();
						detail.setPaymentDate(DateUtils.addDays(DateUtils.addMonths(postDate, mult * (i + 1)), pmtDueDays));
					}
				}else{
					detail.setPaymentDate(DateUtils.addDays(DateUtils.addMonths(postDate, mult * (i + 1)), pmtDueDays));
				}
				break;
			case Y:
				detail.setPaymentDate(DateUtils.addDays(DateUtils.addYears(postDate, mult * (i + 1)), pmtDueDays));
				break;
			}
			// 计算应收利息
			List<RateCalcMethod> calcRates = interestTableConvertUtils.convertRate(interestParam);
			detail.setInterestAmt((leftBal.multiply(calcRates.get(0).rate).multiply(BigDecimal.valueOf(mult))).setScale(2, RoundingMode.HALF_UP));
		 
			// 计算本金
			switch(paymentMethod){
				case MRT:
				{
					if (i == totalPeriod - 1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					break;
				}
				
				case MRF:
				{
					if (i == 0){
						detail.setPrincipalBal(totalBal
								.subtract(
										totalBal.divide(
												BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP)
												.multiply(BigDecimal.valueOf(totalPeriod -1))));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					else{
						detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					break;
				}
				
				case MSV:
				{
					if (i == totalPeriod -1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(getMSVRepayAmt(loanAmount, calcRates.get(0).rate.multiply(BigDecimal.valueOf(mult)), totalPeriod).setScale(2, RoundingMode.HALF_UP).subtract(detail.getInterestAmt()));

						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					break;
				}
				case OPT:
				{
					detail.setPrincipalBal(totalBal);
					if( Interval.D.equals(it.cycleBase) && 1==it.cycleBaseMult ){ //一次还本按日计息，则重算利息
						detail.setInterestAmt(
								(leftBal.multiply( interestParam.chargeRates.get(0).rate ).multiply
										//剩余金额*日利率*实际天数
										(BigDecimal.valueOf(Days.daysBetween(new LocalDate(postDate), new LocalDate(detail.getPaymentDate()) ).getDays())
												)).setScale(2, RoundingMode.HALF_UP) );
					}
					break;
				}
				case IFP:
				{
					if (i == totalPeriod - 1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(BigDecimal.ZERO);
					}
					break;
				}
				case IWP:
				{
					detail.setPrincipalBal(leftBal);
					leftBal = BigDecimal.ZERO;
					break;
				}
				case MRG:
				{	
					if( Interval.D.equals(it.cycleBase) && 1==it.cycleBaseMult ){ //按日计息
						if (i == 0){
							detail.setPrincipalBal(totalBal
									.subtract(
											totalBal.divide(
													BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP)
													.multiply(BigDecimal.valueOf(totalPeriod -1))));
							
							beginDate=new LocalDate(loanBeginDate);
						}
						else{
							detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
						}
						//重算利息,这种还款方式只能按日利率算
						BigDecimal dayRate=  interestParam.chargeRates.get(0).rate;
						detail.setInterestAmt(
								leftBal.multiply(dayRate).multiply
									(BigDecimal.valueOf(Days.daysBetween(new LocalDate(beginDate), new LocalDate(detail.getPaymentDate()) ).getDays())	//剩余金额*日利率*实际天数
							).setScale(2, RoundingMode.HALF_UP) );
						leftBal = leftBal.subtract(detail.getPrincipalBal());
						beginDate=new LocalDate(detail.getPaymentDate()) ;
					}
					break;
				}
				case MSF:
				{
					if (i == totalPeriod -1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO; 
					}
					else{
						detail.setPrincipalBal(getMSVRepayAmt(loanAmount, calcRates.get(0).rate.multiply(BigDecimal.valueOf(mult)), totalPeriod).setScale(2, RoundingMode.HALF_UP).subtract(detail.getInterestAmt()));
						leftBal = leftBal.subtract(detail.getPrincipalBal());
					}
					if (i == 0  &&  !beginDay.equals(loanBeginDate) ){  //首期利息处理
						betweenday=Days.daysBetween(new LocalDate(loanBeginDate), new LocalDate(DateUtils.addMonths(detail.getPaymentDate() , -1))).getDays() ;
						BigDecimal interest1= loanAmount.multiply( calcRates.get(0).rate.divide(BigDecimal.valueOf(30),8,BigDecimal.ROUND_HALF_UP ).multiply(BigDecimal.valueOf(mult).multiply(BigDecimal.valueOf(betweenday)))).setScale(2, RoundingMode.HALF_UP);
						detail.setInterestAmt(detail.getInterestAmt().add(interest1) );
					}
					break;
				}
				case PSV:
				{
					if (i == totalPeriod - 1){
						detail.setPrincipalBal(leftBal);
						leftBal = BigDecimal.ZERO;
					}
					else{
						detail.setPrincipalBal(totalBal.divide(BigDecimal.valueOf(totalPeriod), 2, RoundingMode.HALF_UP));
					}
					break;
				}
			default:
				throw new IllegalArgumentException(paymentMethod + "暂不支持");
			}
			
			detail.setOrigInterestAmt(detail.getInterestAmt() );
			detail.setOrigFeeAmt(detail.getFeeAmt());
			detail.setOrigPrincipalBal(detail.getPrincipalBal() );
			detail.getAcctTypeAndAmtMap().put("INTE", detail.getInterestAmt());
			detail.getAcctTypeAndAmtMap().put("LBAL", detail.getPrincipalBal());
			detail.getAcctTypeAndAmtMap().put("SFEE", detail.getFeeAmt());
			detail.getAcctTypeAndAmtMap().put("PNIT", detail.getPenalizedAmt());
			details.add(detail);
			detailsMap.put(detail.getLoanPeriod(), detail);
		}
		paymentPlan.setDetails(details);
		paymentPlan.setDetailsMap(detailsMap);
		return paymentPlan;
	
	}

	private PaymentPlan getPaymentPlan(Integer acctSeq) {
		PaymentPlan paymentPlan = new PaymentPlan();
		CactLoanPaymentPlan cactLoanPaymentPlan = new JPAQueryFactory(em)
				.select(qLoanPaymentPlan)
				.from(qLoanPaymentPlan)
				.where(qLoanPaymentPlan.acctSeq.eq(acctSeq))
				.fetchOne();
		if(cactLoanPaymentPlan != null) {
			paymentPlan.setAcctSeq(cactLoanPaymentPlan.getAcctSeq());
			paymentPlan.setProdAcctParamId(cactLoanPaymentPlan.getAcctParamId());
			paymentPlan.setPaymentMethod(cactLoanPaymentPlan.getPaymentMethod());
			paymentPlan.setCreateDate(cactLoanPaymentPlan.getSetupDate());
			paymentPlan.setTotalLoanPeriod(cactLoanPaymentPlan.getTotalLoanPeriod());
			paymentPlan.setTotalLoanPrincipalAmt(cactLoanPaymentPlan.getTotalLoanPrincipalAmt());
			paymentPlan.setYearRate(cactLoanPaymentPlan.getYearRate());
			paymentPlan.setLeftLoanPeriod(cactLoanPaymentPlan.getLeftLoanPeriod());
			paymentPlan.setLeftLoanPrincipalAmt(cactLoanPaymentPlan.getLeftLoanPrincipalAmt());
			paymentPlan.setPostDate(cactLoanPaymentPlan.getPostDate());
		}else{
			return null ;
		}
			
		List<CactLoanPaymentDetail> cactLoanPaymentDetails = new JPAQueryFactory(em)
				.select(qLoanPaymentDetail)
				.from(qLoanPaymentDetail)
				.where(qLoanPaymentDetail.planSeq.eq(cactLoanPaymentPlan.getPlanSeq()))
				.fetch();
		if(cactLoanPaymentDetails != null) {
			List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
			Map <Integer ,PaymentPlanDetail> detailsMap = new HashMap<Integer, PaymentPlanDetail>();
			for(CactLoanPaymentDetail cactLoanPaymentDetail : cactLoanPaymentDetails) {
				PaymentPlanDetail paymentPlanDetail  = new PaymentPlanDetail();
				paymentPlanDetail.setLoanPeriod(cactLoanPaymentDetail.getLoanPeriod());
				paymentPlanDetail.setPaymentDate(cactLoanPaymentDetail.getPaymentDate());
				paymentPlanDetail.setFeeAmt(cactLoanPaymentDetail.getFeeAmt());
				paymentPlanDetail.setInterestAmt(cactLoanPaymentDetail.getInterestAmt());				
				paymentPlanDetail.setPrincipalBal(cactLoanPaymentDetail.getPrincipalBal());
				details.add(paymentPlanDetail);
				detailsMap.put(paymentPlanDetail.getLoanPeriod(), paymentPlanDetail);
			}
			paymentPlan.setDetails(details);
			paymentPlan.setDetailsMap(detailsMap);
		}
		return paymentPlan;
	}
		
	
	
	@Override
	public PaymentPlan reCreatePaymentPlan(PaymentPlan paymentPlan ,CactAccount cactAccount,Account acctParam){
		paymentPlan.setTotalLoanPeriod(cactAccount.getTotalLoanPeriod());
		paymentPlan.setTotalLoanPrincipalAmt(cactAccount.getTotalLoanPrincipalAmt());
		
		List<PaymentPlanDetail> details = paymentPlan.getDetails();
		Map <Integer ,PaymentPlanDetail> detailsMap=paymentPlan.getDetailsMap();
		int beginPeriod= cactAccount.getTotalLoanPeriod()-paymentPlan.getDetailsMap().size(); //还款计划开始期次
		if(beginPeriod==0)return paymentPlan;
		
		for (PaymentPlanDetail paymentPlanDetail : paymentPlan.getDetailsMap().values() ){
			paymentPlanDetail.setLoanPeriod(beginPeriod+1);
			beginPeriod++;
		}
		
		Date paymentDate=null ;
		for(int i=1 ; i<=cactAccount.getTotalLoanPeriod()-paymentPlan.getDetailsMap().size() ;i++ ){
				PaymentPlanDetail detail = new PaymentPlanDetail();
				detail.setLoanPeriod(i);
				detail.setInterestAmt(BigDecimal.ZERO);
				detail.setPenalizedAmt(BigDecimal.ZERO);
//				detail.setFeeAmt(BigDecimal.ZERO);
				detail.setTotalRepayAmt(BigDecimal.ZERO);
				detail.setPrincipalBal(BigDecimal.ZERO);
				if(i==1) paymentDate=cactAccount.getSetupDate();
				paymentDate=newComputeService.getNextInterstDate(cactAccount, paymentDate, acctParam, cactAccount.getBillingCycle());
				detail.setPaymentDate( paymentDate );
				details.add(detail);
				detailsMap.put(i, detail);
		} 
		
		paymentPlan.setDetails(details);
		paymentPlan.setDetailsMap(detailsMap);
		return paymentPlan;
	}
	
	
	
}
