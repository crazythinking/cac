package net.engining.pcx.cc.process.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.jpa.impl.JPAQueryFactory;

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
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.param.model.enums.PrePaySettlementType;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import net.engining.pcx.cc.process.service.PaymentPlanService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewPaymentPlanCalcService;
import net.engining.pcx.cc.process.service.account.NewPaymentPlanCalcService.TempPaymentPlanDetailExt;
import net.engining.pcx.cc.process.service.common.InterestTableConvertService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.utils.DateUtilsExt;

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
	private InterestTableConvertService interestTableConvertUtils;

	@Autowired
	private NewComputeService newComputeService;

	@Autowired
	private NewInterestService newInterestService;

	@Autowired
	private Provider7x24 provider7x24;

	@Autowired
	private NewPaymentPlanCalcService newPaymentPlanCalcService;
	
//	@Autowired
//	private NewAgeService newAgeService;

	private QCactLoanPaymentPlan qLoanPaymentPlan = QCactLoanPaymentPlan.cactLoanPaymentPlan;

	private QCactLoanPaymentDetail qLoanPaymentDetail = QCactLoanPaymentDetail.cactLoanPaymentDetail;

	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
			InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod, BigDecimal feeAmount, BigDecimal feeRate, Date postDate,
			int pmtDueDays) {
		return regPaymentPlan(totalPeriod, interval, mult, paymentMethod, loanAmount, it, loanFeeMethod, loanFeeCalcMethod, feeAmount, feeRate,
				postDate, pmtDueDays, false, 0);
	}

	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
			InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod, BigDecimal feeAmount, BigDecimal feeRate, Date postDate,
			Boolean intFirstPeriodAdj, int fixedPmtDay) {
		return regPaymentPlan(totalPeriod, interval, mult, paymentMethod, loanAmount, it, loanFeeMethod, loanFeeCalcMethod, feeAmount, feeRate,
				postDate, 0, false, fixedPmtDay);
	}

	@Override
	public PaymentPlan regPaymentPlan(Integer totalPeriod, Interval interval, Integer mult, PaymentMethod paymentMethod, BigDecimal loanAmount,
			InterestTable it, LoanFeeMethod loanFeeMethod, CalcMethod loanFeeCalcMethod, BigDecimal feeAmount, BigDecimal feeRate, Date postDate,
			int pmtDueDays, Boolean intFirstPeriodAdj, int fixedPmtDay) {

		// 涉及到参数的修改，所以在此做深复制操作
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

		// 统一设置paymentPlan的年化利率
		switch (it.rateBaseInterval) {
			case D:
				org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
				paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays)));
				break;
			case M:
				paymentPlan.setYearRate(it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(12)));
				break;
			case W:
				org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
				paymentPlan.setYearRate(
						it.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays).divide(BigDecimal.valueOf(7))));
				break;
			default:
				paymentPlan.setYearRate(it.chargeRates.get(0).rate);
		}

		List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
		Map<Integer, PaymentPlanDetail> detailsMap = new HashMap<Integer, PaymentPlanDetail>();
		TempPaymentPlanDetailExt tempPaymentPlanDetailExt = null;
		BigDecimal totalBal = loanAmount;
		BigDecimal leftBal = loanAmount;
		for (int i = 0; i < totalPeriod; i++) {
			PaymentPlanDetail detail = new PaymentPlanDetail();
			detail.setLoanPeriod(i + 1);

			// 计算应收费用；不再cc内计算费用，费用计算将由专门模块代替；
			// detail.setFeeAmt(BigDecimal.ZERO);
			// if (loanFeeMethod == LoanFeeMethod.E){
			// if (loanFeeCalcMethod == CalcMethod.A){
			// detail.setFeeAmt(feeAmount);
			// }
			// else{
			// detail.setFeeAmt(totalBal.multiply(feeRate).setScale(2,BigDecimal.ROUND_HALF_UP));
			// }
			// }
			// else if (i == 0){
			// if (loanFeeCalcMethod == CalcMethod.A){
			// detail.setFeeAmt(feeAmount);
			// }
			// else{
			// detail.setFeeAmt(totalBal.multiply(feeRate));
			// }
			// }

			// 计算还款日, 还款固定日为0或大于31(月内最大值)时，均表示非固定日还款
			if (fixedPmtDay != 0 || fixedPmtDay > 31) {
				// 确定fixedDate
				detail = newPaymentPlanCalcService.setupPaymentDate(interval, intFirstPeriodAdj, paymentMethod, postDate, fixedPmtDay, mult, 0, i, detail);
				
			}
			else {
				detail = newPaymentPlanCalcService.setupPaymentDate(interval, intFirstPeriodAdj, paymentMethod, postDate, mult, pmtDueDays, i, detail);
			}

			// 计算应收利息
			// 根据利率参数的计息单位转换为日利率/月利率/年利率
			List<RateCalcMethod> calcRates = interestTableConvertUtils.convertRate(interestParam);
			LocalDate lastPaymentDate = new LocalDate(i == 0 ? postDate : details.get(i - 1).getPaymentDate());
			detail = newPaymentPlanCalcService.setupInterestAmt(interval, totalPeriod, paymentMethod, interestParam, i, postDate, mult, calcRates, leftBal,
					loanAmount, detail, lastPaymentDate.toDate());

			// 计算本金
			tempPaymentPlanDetailExt = newPaymentPlanCalcService.setupPrincipalBal(paymentMethod, totalPeriod, i, leftBal, totalBal, mult, calcRates, detail);
			//剩余本金需要在这里接收，由于上一期计算改变了本金的值
			leftBal = tempPaymentPlanDetailExt.getLeftBal();

			// setup 原始计划金额
			detail.setOrigPrincipalBal(detail.getPrincipalBal());
			detail.setOrigInterestAmt(detail.getInterestAmt());
			// detail.setOrigFeeAmt(detail.getFeeAmt());

			detail.getAcctTypeAndAmtMap().put("INTE", detail.getInterestAmt());
			detail.getAcctTypeAndAmtMap().put("LBAL", detail.getPrincipalBal());
			// detail.getAcctTypeAndAmtMap().put("SFEE", detail.getFeeAmt());
			detail.getAcctTypeAndAmtMap().put("PNIT", detail.getPenalizedAmt());
			details.add(detail);
			detailsMap.put(detail.getLoanPeriod(), detail);
		}

		paymentPlan.setDetails(details);
		paymentPlan.setDetailsMap(detailsMap);
		return paymentPlan;
	}

	public BigDecimal getCalculateInte(CactAccount cactAccount) {
		Account account = newComputeService.retrieveAccount(cactAccount);
		BigDecimal intAmt = new BigDecimal(0);
		if (cactAccount.getCurrentLoanPeriod() >= cactAccount.getTotalLoanPeriod())
			return intAmt; // 说明最后一期已经结转出来,无需试算
		// 余额成分提前结出来的情况
		Integer period = cactAccount.getCurrentLoanPeriod() + 1; // 结转以后的期次
		boolean flag = false;
		if (PrePaySettlementType.D.equals(account.advanceType) && provider7x24.getCurrentDate().compareTo(new LocalDate(cactAccount.getInterestDate())) == 0)
			flag = true;// 还款日期为到期日时，按到期日当天的利息做计算

		if (PrePaySettlementType.M.equals(account.advanceType) || flag) { // 提前还款参数为收取按月利息
			PaymentPlan paymentPlan = getPaymentPlan(cactAccount.getAcctSeq());
			if (paymentPlan == null)
				return null;
			PaymentPlanDetail paymentPlanDetail = paymentPlan.getDetailsMap().get(period);
			intAmt = paymentPlanDetail.getInterestAmt();
		}
		else {
			QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
			List<CactSubAcct> subAccts = new JPAQueryFactory(em).select(qCactSubAcct).from(qCactSubAcct)
					.where(qCactSubAcct.acctSeq.eq(cactAccount.getAcctSeq())).fetch();
			CactSubAcct loanSubAcct = null;
			for (CactSubAcct cactSubAcct : subAccts) {
				if (cactSubAcct.getSubAcctType().equals("LOAN")) {
					loanSubAcct = cactSubAcct;
				}
			}
			SubAcct subAcctParam = newComputeService.retrieveSubAcct(account.subAcctParam.get("LOAN"), cactAccount);
			InterestTable interest = newComputeService.retrieveInterestTable(subAcctParam.intTables.get(0), cactAccount);
			List<RateCalcMethod> calcRates = new ArrayList<RateCalcMethod>();
			OrganizationInfo orgInfoParam = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			if (interest.rateBaseInterval == Interval.W) { // 日利率转换
				calcRates = newComputeService.getRates(interest, 1, 7);
			}
			else if (interest.rateBaseInterval == Interval.M) {
				calcRates = newComputeService.getRates(interest, 12, orgInfoParam.annualInterestRateBenchmarkDays);
			}
			else if (interest.rateBaseInterval == Interval.Y) {
				calcRates = newComputeService.getRates(interest, 1, orgInfoParam.annualInterestRateBenchmarkDays);
			}

			LocalDate startDate = cactAccount.getLastInterestDate() == null ? new LocalDate(cactAccount.getSetupDate())
					: new LocalDate(cactAccount.getLastInterestDate());
			int mult = Days.daysBetween(startDate, new LocalDate(provider7x24.getCurrentDate())).getDays();
			intAmt = ((loanSubAcct.getCurrBal().multiply(calcRates.get(0).rate).multiply(BigDecimal.valueOf(mult <= 0 ? 0 : mult))).setScale(4,
					RoundingMode.HALF_UP));
		}
		return intAmt;
	}
	
	/**
	 * 判断是否需要持久化还款计划
	 * @param acctSeq
	 * @param custId
	 * @param acctParamId
	 * @param plan
	 * @param saveDate
	 */
	public void judgePersistPaymentPlan(Integer acctSeq, String custId, String acctParamId, PaymentPlan plan, Date saveDate){
		long n = new JPAQueryFactory(em)
				.select(qLoanPaymentPlan.planSeq)
				.from(qLoanPaymentPlan)
				.where(qLoanPaymentPlan.acctSeq.eq(acctSeq)).fetchCount();
		if(n <= 0L) {
			savePaymentPlan(acctSeq, custId, acctParamId, plan, saveDate);
		}
		else {
			updatePaymentPlan(acctSeq, custId, acctParamId, plan, saveDate);
		}
	}
	
	@Override
	public void savePaymentPlan(Integer acctSeq, String custId, String acctParamId, PaymentPlan plan, Date saveDate) {
		if (plan != null) {
			// 还款计划主表
			CactLoanPaymentPlan paymentPlan = new CactLoanPaymentPlan();
			paymentPlan.setAcctSeq(acctSeq);
			paymentPlan.setCustId(custId);
			paymentPlan.setAcctParamId(acctParamId);
			paymentPlan.setPaymentMethod(plan.getPaymentMethod());
			paymentPlan.setTotalLoanPeriod(plan.getTotalLoanPeriod());
			paymentPlan.setTotalLoanPrincipalAmt(plan.getTotalLoanPrincipalAmt());
			paymentPlan.setYearRate(plan.getYearRate());
			paymentPlan.setLeftLoanPeriod(plan.getLeftLoanPeriod());
			paymentPlan.setLeftLoanPrincipalAmt(plan.getLeftLoanPrincipalAmt());
			paymentPlan.setPostDate(plan.getPostDate());
			paymentPlan.setBizDate(provider7x24.getCurrentDate().toDate());
			paymentPlan.fillDefaultValues();
			em.persist(paymentPlan);

			// 还款计划明细表
			for (PaymentPlanDetail detail : plan.getDetails()) {
				CactLoanPaymentDetail paymentDetail = new CactLoanPaymentDetail();
				paymentDetail.setAcctSeq(acctSeq);
				// paymentDetail.setFeeAmt(detail.getFeeAmt());
				paymentDetail.setInterestAmt(detail.getInterestAmt());
				paymentDetail.setLoanPeriod(detail.getLoanPeriod());
				paymentDetail.setPaymentDate(detail.getPaymentDate());
				paymentDetail.setPaymentNatureDate(detail.getPaymentNatureDate());
				paymentDetail.setPlanSeq(paymentPlan.getPlanSeq());
				paymentDetail.setPrincipalBal(detail.getPrincipalBal());
				paymentDetail.setBizDate(provider7x24.getCurrentDate().toDate());
				paymentDetail.fillDefaultValues();
				em.persist(paymentDetail);
			}
		}
	}

	@Override
	public void updatePaymentPlan(Integer acctSeq, String custId, String acctParamId, PaymentPlan plan, Date saveDate) {

		CactLoanPaymentPlan loanPlan = new JPAQueryFactory(em)
				.select(qLoanPaymentPlan)
				.from(qLoanPaymentPlan)
				.where(qLoanPaymentPlan.acctSeq.eq(acctSeq)).fetchOne();
		
		if (loanPlan != null && isDiffPaymentPlan(plan, loanPlan)) {
			loanPlan.setTotalLoanPeriod(plan.getTotalLoanPeriod());
			loanPlan.setTotalLoanPrincipalAmt(plan.getTotalLoanPrincipalAmt());
			loanPlan.setYearRate(plan.getYearRate());
			loanPlan.setLeftLoanPeriod(plan.getLeftLoanPeriod());
			loanPlan.setLeftLoanPrincipalAmt(plan.getLeftLoanPrincipalAmt());
			loanPlan.setPostDate(plan.getPostDate());
			loanPlan.setBizDate(provider7x24.getCurrentDate().toDate());
			loanPlan.setLastUpdateDate(new Date());
			
			List<CactLoanPaymentDetail> cactloanpaymentdetail = new JPAQueryFactory(em)
					.select(qLoanPaymentDetail)
					.from(qLoanPaymentDetail)
					.where(qLoanPaymentDetail.planSeq.eq(loanPlan.getPlanSeq())).fetch();
			if (cactloanpaymentdetail != null && !cactloanpaymentdetail.isEmpty()) {
				for (PaymentPlanDetail detail : plan.getDetails()) {
					for (CactLoanPaymentDetail loanDetail : cactloanpaymentdetail) {
						if (loanDetail.getLoanPeriod() == detail.getLoanPeriod() && isDiffPaymentPlanDetail(detail, loanDetail)) {
							// loanDetail.setFeeAmt(detail.getFeeAmt());
							loanDetail.setInterestAmt(detail.getInterestAmt());
							loanDetail.setPaymentDate(detail.getPaymentDate());
							loanDetail.setPaymentNatureDate(detail.getPaymentNatureDate());
							loanDetail.setPrincipalBal(detail.getPrincipalBal());
							loanDetail.setBizDate(provider7x24.getCurrentDate().toDate());
							loanDetail.setLastUpdateDate(new Date());
						}
					}
				}
			}
		}

	}
	
	/**
	 * 比较持久化的还款主计划，与修正后的还款主计划
	 * @param plan
	 * @param loanPlan
	 * @return
	 */
	private boolean isDiffPaymentPlan(PaymentPlan plan, CactLoanPaymentPlan loanPlan){
		if(plan.getTotalLoanPeriod().compareTo(loanPlan.getTotalLoanPeriod()) != 0){
			return true;
		}
		if(plan.getLeftLoanPeriod().compareTo(loanPlan.getLeftLoanPeriod()) != 0){
			return true;
		}
		if(plan.getTotalLoanPrincipalAmt().compareTo(loanPlan.getTotalLoanPrincipalAmt()) != 0){
			return true;
		}
		if(plan.getLeftLoanPrincipalAmt().compareTo(loanPlan.getLeftLoanPrincipalAmt()) != 0){
			return true;
		}
		if(plan.getYearRate().compareTo(loanPlan.getYearRate()) != 0){
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * 比较持久化的还款计划明细，与修正后的还款计划明细
	 * @param plan
	 * @param detail
	 * @return
	 */
	private boolean isDiffPaymentPlanDetail(PaymentPlanDetail detail, CactLoanPaymentDetail loanDetail){
		if(detail.getInterestAmt().setScale(2, RoundingMode.HALF_UP).compareTo(loanDetail.getInterestAmt().setScale(2, RoundingMode.HALF_UP)) != 0 ){
			return true;
		}
		if(detail.getPrincipalBal().setScale(2, RoundingMode.HALF_UP).compareTo(loanDetail.getPrincipalBal().setScale(2, RoundingMode.HALF_UP)) != 0 ){
			return true;
		}
		if(detail.getPaymentDate().compareTo(loanDetail.getPaymentDate()) != 0 ){
			return true;
		}
		return false;
		
	}

	@Override
	public PaymentPlan findLatestPaymentPlan(Integer acctSeq) {
		CactLoanPaymentPlan cactPlan = new JPAQueryFactory(em).select(qLoanPaymentPlan).from(qLoanPaymentPlan).where(qLoanPaymentPlan.acctSeq.eq(acctSeq)).orderBy(qLoanPaymentPlan.planSeq.desc()).fetchFirst();
		if (cactPlan != null) {
			PaymentPlan plan = new PaymentPlan();
			plan.setCreateDate(cactPlan.getSetupDate());
			plan.setLeftLoanPeriod(cactPlan.getLeftLoanPeriod());
			plan.setLeftLoanPrincipalAmt(cactPlan.getLeftLoanPrincipalAmt());
			plan.setPaymentMethod(cactPlan.getPaymentMethod());
			plan.setPostDate(cactPlan.getPostDate());
			plan.setTotalLoanPeriod(cactPlan.getTotalLoanPeriod());
			plan.setTotalLoanPrincipalAmt(cactPlan.getTotalLoanPrincipalAmt());
			plan.setYearRate(cactPlan.getYearRate());

			QCactLoanPaymentDetail qLoanPaymentDetail = QCactLoanPaymentDetail.cactLoanPaymentDetail;
			List<CactLoanPaymentDetail> detailList = new JPAQueryFactory(em)
					.select(qLoanPaymentDetail)
					.from(qLoanPaymentDetail)
					.where(
							qLoanPaymentDetail.planSeq.eq(cactPlan.getPlanSeq()))
					.orderBy(qLoanPaymentDetail.paymentDate.asc())
					.fetch();
			List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
			for (CactLoanPaymentDetail item : detailList) {
				PaymentPlanDetail detail = new PaymentPlanDetail();
				// detail.setFeeAmt(item.getFeeAmt());
				detail.setInterestAmt(item.getInterestAmt());
				detail.setLoanPeriod(item.getLoanPeriod());
				detail.setPaymentDate(item.getPaymentDate());
				detail.setPaymentNatureDate(item.getPaymentNatureDate());
				detail.setPrincipalBal(item.getPrincipalBal());
				details.add(detail);
			}
			plan.setDetails(details);
			return plan;
		}
		return null;
	}

	private void updatePaymentPlanDetail(String subAcctType, BigDecimal subAmt, PaymentPlanDetail paymentPlanDetail) {

		if (subAcctType.equals("INTE")) {
			paymentPlanDetail.setInterestAmt(subAmt);
		}
		else if (subAcctType.equals("LBAL")) {
			paymentPlanDetail.setPrincipalBal(subAmt);
		}
		// else if(subAcctType.equals("SFEE")){
		// paymentPlanDetail.setFeeAmt(subAmt);
		// }
		else if (subAcctType.equals("PNIT")) {
			paymentPlanDetail.setPenalizedAmt(subAmt);
		}
	}

	/**
	 * 计算利息开始日，以上次计息日+1天为开始
	 * 
	 * @param cactSubAcct
	 * @param cactAccount
	 * @param subAcct
	 * @param account
	 * @return
	 */
	private LocalDate calcStartDate(CactSubAcct cactSubAcct, CactAccount cactAccount, SubAcct subAcct, Account account) {
		LocalDate startDate;
		if (cactSubAcct.getLastComputingInterestDate() == null) {
			// 第一次计息
			startDate = calcSetupDate(cactSubAcct, cactAccount, subAcct);
			// LBAL子账户修利息的时候只修当天就可以了，setUpDate的时候不用计息,导致lastComputingInterestDate是null
			if ("LBAL".equals(cactSubAcct.getSubAcctType()))
				startDate = startDate.plusDays(1);
		}
		else {
			startDate = new LocalDate(cactSubAcct.getLastComputingInterestDate()).plusDays(1);
		}
		return startDate;
	}

	/**
	 * 计算利息截止日，
	 * 
	 * @param cactAccount
	 * @param cactSubAcct
	 * @param paymentPlan
	 * @param endDate
	 * @param tables
	 * @return
	 */
	private LocalDate calcEndDate(CactAccount cactAccount, CactSubAcct cactSubAcct, PaymentPlan paymentPlan, LocalDate endDate, List<InterestTable> tables) {
		// 当前业务日期是否最后一期到期日
		PaymentPlanDetail lastDetail = paymentPlan.getDetailsMap().get(paymentPlan.getTotalLoanPeriod());
		LocalDate curDate = provider7x24.getCurrentDate();
		boolean lastDay = false;
		if (lastDetail.getPaymentDate().compareTo(curDate.toDate()) == 0) {
			lastDay = true;
		}

		// 如果当前期数在最后一期之前，还款日当天的利息在还款日当天收掉(因为到期日当天还款，日终批量还是会在当期计一天利息)，最后一期中还款的话当日就不收利息。
		// 总体原则是当天放款当天收息，当天还款就不收息。
		if (
		/**
		 * 这一段可以去掉 //cactAccount.getTotalLoanPeriod() > 1 && //如果只有一期，跑N天收N-1天利息
		 * //业务日当天预修的利息应该每一期都要修 //cactAccount.getCurrentLoanPeriod() + 1 <
		 * paymentPlan.getTotalLoanPeriod() &&
		 **/
		// 这段逻辑依赖于批量顺序，本金先计息，再结息的情况下，LOAN类型子账户从创建日起每日计息；LBAL类型子账户创建日不计息，之后每天计息；
		// 因此算头不算尾时，LOAN不需要（在未跑批的情况下）预修当前业务日所属的利息；但LBAL则需要

		(cactSubAcct.getSubAcctType().equals("LBAL") || (cactSubAcct.getSubAcctType().equals("LOAN") && !lastDay))
				&& tables.get(0).cycleBase == Interval.D && tables.get(0).cycleBaseMult == 1) {
			endDate = endDate.plusDays(1);
		}
		return endDate;
	}

	/**
	 * 计算账户余额成分（SubAcct）建立日期
	 * 
	 * @param cactSubAcct
	 * @param cactAccount
	 * @param subAcct
	 * @return
	 */
	private LocalDate calcSetupDate(CactSubAcct cactSubAcct, CactAccount cactAccount, SubAcct subAcct) {
		LocalDate setupDate;
		switch (subAcct.intAccumFrom) {
			case C:
				// 取上一账单日。这类余额肯定存在上一账单日数据
				setupDate = new LocalDate(cactAccount.getLastInterestDate()).plusDays(subAcct.postponeDays);
				break;
			case P:
				// 取子账户建立日期
				setupDate = new LocalDate(cactSubAcct.getSetupDate()).plusDays(subAcct.postponeDays);
				break;
			default:
				throw new IllegalArgumentException("should not be here");
		}
		return setupDate;
	}

	/**
	 * 获取cc完整的静态的还款计划及其明细
	 * 
	 * @param acctSeq
	 * @return
	 */
	private PaymentPlan getPaymentPlan(Integer acctSeq) {
		PaymentPlan paymentPlan = new PaymentPlan();
		CactLoanPaymentPlan cactLoanPaymentPlan = new JPAQueryFactory(em).select(qLoanPaymentPlan).from(qLoanPaymentPlan).where(qLoanPaymentPlan.acctSeq.eq(acctSeq)).fetchOne();
		if (cactLoanPaymentPlan != null) {
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
		}
		else {
			return null;
		}

		List<CactLoanPaymentDetail> cactLoanPaymentDetails = new JPAQueryFactory(em)
				.select(qLoanPaymentDetail)
				.from(qLoanPaymentDetail)
				.where(qLoanPaymentDetail.planSeq.eq(cactLoanPaymentPlan.getPlanSeq())).fetch();
		if (cactLoanPaymentDetails != null) {
			List<PaymentPlanDetail> details = new ArrayList<PaymentPlanDetail>();
			Map<Integer, PaymentPlanDetail> detailsMap = new HashMap<Integer, PaymentPlanDetail>();
			for (CactLoanPaymentDetail cactLoanPaymentDetail : cactLoanPaymentDetails) {
				PaymentPlanDetail paymentPlanDetail = new PaymentPlanDetail();
				paymentPlanDetail.setLoanPeriod(cactLoanPaymentDetail.getLoanPeriod());
				paymentPlanDetail.setPaymentDate(cactLoanPaymentDetail.getPaymentDate());
				paymentPlanDetail.setPaymentNatureDate(cactLoanPaymentDetail.getPaymentNatureDate());
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

	/**
	 * 用于实时根据subAcct当前情况，动态计算修正还款计划
	 */
	@Override
	public PaymentPlan searchPaymentPlan(Integer acctSeq) {

		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);
		Account account = newComputeService.retrieveAccount(cactAccount);

		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> subAccts = new JPAQueryFactory(em).select(qCactSubAcct).from(qCactSubAcct).where(qCactSubAcct.acctSeq.eq(acctSeq)).fetch();
		// 根据subAccts计算总余额
//		BigDecimal totalCurrBal = new BigDecimal(0);
//		for (CactSubAcct cactSubAcct : subAccts) {
//			// 余额+未结罚息+未结利息
//			totalCurrBal = totalCurrBal.add(cactSubAcct.getCurrBal().add(cactSubAcct.getPenalizedAmt()).add(cactSubAcct.getIntReceivable()));
//		}

		// 余额成分LOAN的参数
		SubAcct loanSubAcctParam = newComputeService.retrieveSubAcct(account.subAcctParam.get("LOAN"), cactAccount);
		// LOAN的利率表参数，即该笔贷款的利率参数
		InterestTable loanIt = newComputeService.retrieveInterestTable(loanSubAcctParam.intTables.get(0), cactAccount);

		PaymentPlan paymentPlan = null;
		// 等额本息需要根据还款情况重新生成还款计划，所以这种还款方式重修还款计划时需要以原始的还款计划为基础结构；且还款冲销时需要同时更新cc的静态还款计划
		if (account.paymentMethod.equals(PaymentMethod.MSV) || account.paymentMethod.equals(PaymentMethod.MSF)
				|| account.paymentMethod.equals(PaymentMethod.MSB)) {
			paymentPlan = getPaymentPlan(acctSeq);
		}
		if (paymentPlan == null) {
			// 固定日还款
			if (account.isLockPaymentDay) {
				paymentPlan = regPaymentPlan(
						cactAccount.getTotalLoanPeriod(), 
						account.intUnit, 
						account.intUnitMult, 
						account.paymentMethod,
						cactAccount.getTotalLoanPrincipalAmt(), 
						loanIt, 
						account.loanFeeMethod, 
						account.loanFeeCalcMethod, 
						account.feeAmount,
						account.feeRate, 
						cactAccount.getSetupDate(), 
						account.pmtDueDays, 
						account.intFirstPeriodAdj, 
						account.fixedPmtDay
						);
			}
			else {
				paymentPlan = regPaymentPlan(
						cactAccount.getTotalLoanPeriod(), 
						account.intUnit, 
						account.intUnitMult, 
						account.paymentMethod,
						cactAccount.getTotalLoanPrincipalAmt(), 
						loanIt, 
						account.loanFeeMethod, 
						account.loanFeeCalcMethod, 
						account.feeAmount,
						account.feeRate, 
						cactAccount.getSetupDate(), 
						account.pmtDueDays, 
						account.intFirstPeriodAdj, 
						0
						);
			}
		}
		paymentPlan.setAcctSeq(acctSeq);
		paymentPlan.setProdAcctParamId(account.paramId);
		paymentPlan.setProdAcctParamDesc(account.description);

		// 原始还款计划每期应还本金，用于计算是否提前还款。
		Map<Integer, BigDecimal> regPrincipalsMap = new HashMap<Integer, BigDecimal>();
		// 还款计划明细，按期保存的计划明细，用于后面逻辑计算
		Map<Integer, PaymentPlanDetail> detailsMap = paymentPlan.getDetailsMap();

		// 循环还款计划明细，先清零各项余额，后面的逻辑根据subAcct重修
		for (PaymentPlanDetail paymentPlanDetail : detailsMap.values()) {
			paymentPlanDetail.setInterestAmt(BigDecimal.ZERO);
			paymentPlanDetail.setPenalizedAmt(BigDecimal.ZERO);
			paymentPlanDetail.setFeeAmt(BigDecimal.ZERO);
			paymentPlanDetail.setTotalRepayAmt(BigDecimal.ZERO);
			regPrincipalsMap.put(paymentPlanDetail.getLoanPeriod(), paymentPlanDetail.getPrincipalBal());
			// 这里的逻辑影响到后面溢缴款冲销，怀疑原本的逻辑有问题，现在先全部清零，后面的逻辑会循环除去LOAN和PAYM以外的子账户，重新加上各自子账户的currBal的值
			for (String s : paymentPlanDetail.getAcctTypeAndAmtMap().keySet()) {
				paymentPlanDetail.getAcctTypeAndAmtMap().put(s, BigDecimal.ZERO);
			}
			// 上面的逻辑不清除还款计划的每期本金，因为本金不会变；
			// 但是还款方式为等额本息类型时，每期应还本金也要清空，已经发生期数的还款计划根据子账户修，未发生的根据剩余本金重新计算。
			if (account.paymentMethod.equals(PaymentMethod.MSV) || account.paymentMethod.equals(PaymentMethod.MSF)
					|| account.paymentMethod.equals(PaymentMethod.MSB)) {
				paymentPlanDetail.setPrincipalBal(BigDecimal.ZERO);
			}
			//根据偏移量修正还款计划的还款日然日
			Date natureDate = DateUtilsExt.addDays(paymentPlanDetail.getPaymentDate(), -(provider7x24.getOffset4BizDate2NatureDate(new LocalDate(cactAccount.getSetupDate()))));
			paymentPlanDetail.setPaymentNatureDate(natureDate);
		}

		// 每期的利息余额-按子账户为维度分别统计应计利息，四舍五入保留两位小数后，再相加汇总到当前一期的应还利息上，与结息步骤保持一致，否则会出现误差。
		// 例如：两个子账户应计利息分别为49.31506,如果先相加，再四舍五入保留两位小数。与先四舍五入保留两位小数再相加，会相差1分钱。
		Map<Integer, BigDecimal> intAmts = new HashMap<Integer, BigDecimal>();
		// 溢缴款余额
		BigDecimal paymAmt = BigDecimal.ZERO;
		// 当前贷款期数(从0开始，每到一期结息日的时候增加1，最大值为贷款总期数。所以该值表示的含义是当前这期的结束，期数+1表示从下期开始到下期结束的任意时间点。)
		// 以3期举例，1月1日为建账日期，当前期数为0，对应的是第一期的还款计划。
		// 2月1日为第一期的结息日，日终批量后，当前期数为1，表示的含义是第一期结束，第二期开始，所以对应的是第二期的还款计划。
		// 3月1日为第二期的结息日，日终批量后，当前期数为2，表示的含义是第二期结束，第三期开始，所以对应的是第三期的还款计划。
		// 4月1日为第三期的结息日，日终批量后，当前期数为3，表示的含义是第三期结束。因为总共只有三期，所以该值最大为3。
		Integer currentLoanPeriod = cactAccount.getCurrentLoanPeriod() > cactAccount.getTotalLoanPeriod() ? cactAccount.getTotalLoanPeriod() : cactAccount.getCurrentLoanPeriod();
		
		// 取所有的LOAN和LBAL子账户，余额加总，做为贷款欠款本金总额
		BigDecimal totalLoanAndLbalPrincipal = BigDecimal.ZERO;
		// LOAN子账户余额，主要用于等额本息计算
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		// 每期的未入账罚息金额
		Map<Integer, BigDecimal> penalizedAmtMap = new HashMap<Integer, BigDecimal>();
		
		// ======================以下逻辑修还款计划中的每期的贷款利息、本金和罚息；根据当前子账户信息填充应还明细
		for (CactSubAcct cactSubAcct : subAccts) {
			BigDecimal intAmt = BigDecimal.ZERO;
			SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
			Integer period = cactAccount.getCurrentLoanPeriod() + 1 - cactSubAcct.getStmtHist();
			//fix bug 2018-9-18: 由于LOAN的cactSubAcct.getStmtHist()也会在结息日升账期，几乎与cactAccount.getCurrentLoanPeriod()同步；
			//所以这里的逻辑并不能获得该LOAN的CactSubAcct正所属哪一期，而其他CactSubAcct不会有此问题;
			if("LOAN".equals(cactSubAcct.getSubAcctType())){
				period = cactAccount.getCurrentLoanPeriod() + 1;
			}
			// 最后一期以后产生的利息、罚息，并入最后一期
			if (period > cactAccount.getTotalLoanPeriod()) {
				period = cactAccount.getTotalLoanPeriod();
			}

			// 累加所有未结罚息，将其计入当前期数的应收罚息
			if (cactSubAcct.getPenalizedAmt().compareTo(BigDecimal.ZERO) > 0) {
				if (penalizedAmtMap.get(period) != null) {
					penalizedAmtMap.put(period, penalizedAmtMap.get(period).add(cactSubAcct.getPenalizedAmt().setScale(2, RoundingMode.HALF_UP)));
				}
				else {
					penalizedAmtMap.put(period, cactSubAcct.getPenalizedAmt().setScale(2, RoundingMode.HALF_UP));
				}
			}
			// 累加所有未结利息，将其计入当前期数的应收利息
			if (cactSubAcct.getIntReceivable().compareTo(BigDecimal.ZERO) > 0) {
				intAmt = intAmt.add(cactSubAcct.getIntReceivable());
				intAmts.put(period,intAmt);
			}
			
			// ======================以下逻辑针对未到结息日时，确定是否需要临时计算当前未结利息
			// 当前业务日期是否早于当期结息日
			if(provider7x24.getCurrentDate().isBefore(new LocalDate(cactAccount.getInterestDate()))){
				// 提前还款当期计息标准为按日
				if(PrePaySettlementType.D.equals(account.advanceType)){
					// 先确定起息日
					LocalDate startDate = calcStartDate(cactSubAcct, cactAccount, subAcct, account);
					LocalDate endDate = provider7x24.getCurrentDate();
					// 获取利率表
					List<InterestTable> tables = newInterestService.retrieveInterestTable(cactAccount, cactSubAcct, account, subAcct, endDate);
					// 计算利息的金额基数
					BigDecimal principalAmount = provider7x24.getBalance(cactSubAcct);
					// 等本等息类型的特殊逻辑：用全部贷款本金计算
					if (account.paymentMethod == PaymentMethod.PSV && principalAmount.compareTo(BigDecimal.ZERO) != 0) {
						principalAmount = cactAccount.getTotalLoanPrincipalAmt();
					}
					// 下面的逻辑是按日计息，还款的时候也会收当日的利息，未满结息周期的，当期内的已过天数按日息来处理
					List<RateCalcMethod> dailyRates = newInterestService.convertRates(Interval.D, tables.get(0));
					endDate = calcEndDate(cactAccount, cactSubAcct, paymentPlan, endDate, tables);
					// 不满整计息周期的剩余天数,乘上日利率得出利息
					// 为了解决还款冲销差0.01的情况，先算出endDate前一天的利息；再加一天利息来计算，确保于六位转两位四舍五入后的差异与批量计息的逻辑一致
					// endDate表示闭区间，所以直接是这个days的值
					intAmt = intAmt.add(newComputeService.calcTieredAmount(tables.get(0).tierInd, dailyRates, principalAmount, principalAmount)
							.multiply(BigDecimal.valueOf(Days.daysBetween(startDate, endDate.plusDays(-1)).getDays())).setScale(2, RoundingMode.HALF_UP));
					BigDecimal tmpInt = newComputeService.calcTieredAmount(tables.get(0).tierInd, dailyRates, principalAmount, principalAmount)
							.multiply(BigDecimal.valueOf(Days.daysBetween(endDate.plusDays(-1), endDate).getDays())).setScale(2, RoundingMode.HALF_UP);
					intAmt = intAmt.add(tmpInt);
				}
				// 提前还款当期计息标准为按结息周期靠前，取原还款计划该期的利息作为应还利息
				else if(PrePaySettlementType.M.equals(account.advanceType)){
					// 获取该期的原始还款计划明细的该期原始利息；period从1开始，所以-1
					if("LOAN".equals(cactSubAcct.getSubAcctType())){
						BigDecimal tmpInt = paymentPlan.getDetails().get(period-1).getOrigInterestAmt();
						intAmt = intAmt.add(tmpInt);
					}
					//FIXME 2018-9-18: 直接从原始计划里取只针对LOAN是正确的，因为原始还款计划计算的利息，只是按LOAN计算的；
					//然而对于其他类型的SubAcct，是结转后产生的，原始计划不可能针对这些SubAcct产生利息，因此需要按周期进行计算；
					
				}
				else{
					logger.debug("cactAccount[{}]，该笔贷款的提前还款当期计息标准参数为按还款计划不变，无需计算",cactAccount.getAcctSeq());
				}
				intAmts.put(period,intAmt);
			}
			// ======================End
			
			// ======================修正还款计划明细
			// 修正应还罚息
			if (cactSubAcct.getSubAcctType().equals("PNIT") && cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) >= 0) {
				detailsMap.get(period).setPenalizedAmt(detailsMap.get(period).getPenalizedAmt().add(cactSubAcct.getCurrBal()));
			}
			
			// 统计剩余未还本金总额
			if (cactSubAcct.getSubAcctType().equals("LBAL") && cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) >= 0) {
				//修正应还本金；还款方式为等额本息类型时，由于前面逻辑对该类型的"LBAL"本金已清零，这里进行重新计算修正
				if (account.paymentMethod == PaymentMethod.MSV || account.paymentMethod.equals(PaymentMethod.MSF)
						|| account.paymentMethod.equals(PaymentMethod.MSB)) {
					detailsMap.get(period).setPrincipalBal(detailsMap.get(period).getPrincipalBal().add(cactSubAcct.getCurrBal()));
				}
				totalLoanAndLbalPrincipal = totalLoanAndLbalPrincipal.add(cactSubAcct.getCurrBal());
			}
			if (cactSubAcct.getSubAcctType().equals("LOAN") && cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) >= 0) {
				totalLoanAndLbalPrincipal = totalLoanAndLbalPrincipal.add(cactSubAcct.getCurrBal());
				loanPrincipal = loanPrincipal.add(cactSubAcct.getCurrBal());
			}

			// 修正应还利息
			if (cactSubAcct.getSubAcctType().equals("INTE") && cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) >= 0) {
				detailsMap.get(period).setInterestAmt(detailsMap.get(period).getInterestAmt().add(cactSubAcct.getCurrBal()));
			}

			// 修正应还费用
//			if (cactSubAcct.getSubAcctType().equals("SFEE") && cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) >= 0) {
//				detailsMap.get(period).setFeeAmt(detailsMap.get(period).getFeeAmt().add(cactSubAcct.getCurrBal()));
//			}
			
			// 存下溢缴款，供后面逻辑冲销溢缴款
			if (cactSubAcct.getSubAcctType().equals("PAYM") && cactSubAcct.getCurrBal().compareTo(BigDecimal.ZERO) < 0) {
				paymAmt = cactSubAcct.getCurrBal().abs();
			}
			
			// 对"LOAN"、"PAYM"以外余额成分的当期余额（不包括未结部分）修正到各期的待冲销余额MAP(“余额成分->金额(K->V)) : acctTypeAndAmtMap
			if ((!cactSubAcct.getSubAcctType().equals("LOAN")) && (!cactSubAcct.getSubAcctType().equals("PAYM"))) {
				if (detailsMap.get(period).getAcctTypeAndAmtMap().get(cactSubAcct.getSubAcctType()) != null) {
					detailsMap.get(period).getAcctTypeAndAmtMap().put(cactSubAcct.getSubAcctType(), detailsMap.get(period).getAcctTypeAndAmtMap().get(cactSubAcct.getSubAcctType()).add(cactSubAcct.getCurrBal()));
				}
				else {
					detailsMap.get(period).getAcctTypeAndAmtMap().put(cactSubAcct.getSubAcctType(), cactSubAcct.getCurrBal());
				}
			}

		}
		// ====================== CactSubAcct循环结束

		// 修正，将所有未结罚息更新至对应期数的应还罚息
		for (Integer period : penalizedAmtMap.keySet()) {
			detailsMap.get(period).setPenalizedAmt(detailsMap.get(period).getPenalizedAmt().add(penalizedAmtMap.get(period)));
			if (detailsMap.get(period).getAcctTypeAndAmtMap().get("PNIT") != null) {
				detailsMap.get(period).getAcctTypeAndAmtMap().put("PNIT",
						detailsMap.get(period).getAcctTypeAndAmtMap().get("PNIT").add(penalizedAmtMap.get(period)));
			}
			else {
				detailsMap.get(period).getAcctTypeAndAmtMap().put("PNIT", penalizedAmtMap.get(period));
			}
		}
		// 修正，将所有未结利息更新至对应期数的应还利息
		for (Integer period: intAmts.keySet()) {
			BigDecimal intAmt = intAmts.get(period);
			if (intAmt.compareTo(BigDecimal.ZERO) > 0) {
				//TODO 取决于业务要修到在本期，还是当期；默认修到本期；
				//由于每期的利息在未发生逾期时，是与原始计划相同的，但是一旦发生变化，为保持原计划的可理解性，所以考虑逾期产生的利息修在即将到期的这一期；(结息的时候，新生成的利息子账户账期是0)
				//period = cactAccount.getCurrentLoanPeriod() + 1 < cactAccount.getTotalLoanPeriod() ? cactAccount.getCurrentLoanPeriod() + 1 : cactAccount.getTotalLoanPeriod();

				detailsMap.get(period).setInterestAmt(detailsMap.get(period).getInterestAmt().add(intAmt));
				
				if (detailsMap.get(period).getAcctTypeAndAmtMap().get("INTE") != null) {
					detailsMap.get(period).getAcctTypeAndAmtMap().put("INTE", detailsMap.get(period).getAcctTypeAndAmtMap().get("INTE").add(intAmt));
				}
				else {
					detailsMap.get(period).getAcctTypeAndAmtMap().put("INTE", intAmt);
				}
			}
		}

		// ====================== 如果还款方式不是等额本息类型，不需要重建还款计划
		// 修还款计划中每期的贷款本金，取所有的LOAN和LBAL子账户，余额加总，称为贷款欠款本金总额；
		// 按还款计划期数倒序循环，如果该期应还本金小于贷款欠款本金总额，则说明还未还款冲销到该期，则贷款欠款本金总额变为减去该期应还本金后的剩余金额；
		// 直到出现贷款欠款本金总额小于该期应还本金的时候，说明有提前还款发生，那么该期的应还本金就是减掉该期之后n期应还本金剩下的本金；
		if (account.paymentMethod != PaymentMethod.MSV && account.paymentMethod != PaymentMethod.MSB && account.paymentMethod != PaymentMethod.MSF) {
			for (int i = cactAccount.getTotalLoanPeriod(); i >= 1; i--) {
				if (totalLoanAndLbalPrincipal.compareTo(detailsMap.get(i).getPrincipalBal()) >= 0) {
					totalLoanAndLbalPrincipal = totalLoanAndLbalPrincipal.subtract(detailsMap.get(i).getPrincipalBal());
				}
				else {
					detailsMap.get(i).setPrincipalBal(totalLoanAndLbalPrincipal);
					totalLoanAndLbalPrincipal = BigDecimal.ZERO;
				}
			}
		}
		else {
			// 如果还款方式是等额本息类型，由于提前还款造成贷款剩余本金可能未按原始计划改变，此时需要重建还款计划；
			// 如果是已经结转到LBAL子账户的本金，这部分期数的应还本金根据LBAL修； -----在上面已经完成
			// 如果是当期应还的本金应该从LOAN子账户取上次计算的还款计划（持久化）按期数倒序依次的减去每期的应还本金，直到对应期数，算出当期的实际应还本金；
			// 下一期开始根据LOAN减去当期应还本金后的剩余本金，再根据剩余期数重新计算；
			BigDecimal recalcLoanPrincipal = loanPrincipal;// 用于重新计算等额本息的剩余本金；
			for (int i = cactAccount.getTotalLoanPeriod(); i >= currentLoanPeriod + 1; i--) {
				if (i > currentLoanPeriod + 1) {
					if (loanPrincipal.compareTo(regPrincipalsMap.get(i)) >= 0) {
						loanPrincipal = loanPrincipal.subtract(regPrincipalsMap.get(i));
					}
					else {
						loanPrincipal = BigDecimal.ZERO;
					}
				}
				else {
					detailsMap.get(i).setPrincipalBal(loanPrincipal);
					recalcLoanPrincipal = recalcLoanPrincipal.subtract(detailsMap.get(i).getPrincipalBal());
				}
			}
			//如果当前期数未超过总期数
			if (cactAccount.getTotalLoanPeriod() - currentLoanPeriod - 1 >= 0) {
				// 等额本息因提前还款，重新计算的还款计划
				PaymentPlan recalculatePaymentPlan = null;
				
				if(account.paymentMethod == PaymentMethod.MSV && account.paymentMethod == PaymentMethod.MSB){
					recalculatePaymentPlan = regPaymentPlan(
							cactAccount.getTotalLoanPeriod() - currentLoanPeriod - 1, 
							account.intUnit,
							account.intUnitMult, 
							account.paymentMethod, 
							recalcLoanPrincipal, 
							loanIt, 
							account.loanFeeMethod, 
							account.loanFeeCalcMethod,
							account.feeAmount, 
							account.feeRate, 
							detailsMap.get(currentLoanPeriod + 1).getPaymentDate(), 
							account.pmtDueDays
							);
				}
				else{// 固定日
					recalculatePaymentPlan = regPaymentPlan(
							cactAccount.getTotalLoanPeriod() - currentLoanPeriod - 1, 
							account.intUnit,
							account.intUnitMult, 
							account.paymentMethod, 
							recalcLoanPrincipal, 
							loanIt, 
							account.loanFeeMethod, 
							account.loanFeeCalcMethod,
							account.feeAmount, 
							account.feeRate, 
							detailsMap.get(currentLoanPeriod + 1).getPaymentDate(),
							account.intFirstPeriodAdj, 
							account.fixedPmtDay
							);
				}
				
				if (recalculatePaymentPlan != null && recalculatePaymentPlan.getDetails() != null && recalculatePaymentPlan.getDetails().size() > 0
						&& recalculatePaymentPlan.getDetailsMap() != null && recalculatePaymentPlan.getDetailsMap().size() > 0) {
					for (int i = 1; i <= cactAccount.getTotalLoanPeriod(); i++) {
						// 从第一期到当期的还款计划的本金，用旧的还款计划。下期开始的本金用重新计算的还款计划明细。
						if (i > currentLoanPeriod + 1) {
							paymentPlan.getDetailsMap().get(i)
									.setPrincipalBal(recalculatePaymentPlan.getDetailsMap().get(i - currentLoanPeriod - 1).getPrincipalBal());
						}
					}
				}
			}
		}
		// ====================== End 重建还款计划

		// 修正，溢缴款冲销
		if (paymAmt.compareTo(BigDecimal.ZERO) > 0) {
			// 因有溢缴款，所以账龄肯定为0，直接取账龄为0的冲销顺序
			List<SubAcctType> acctTypeList = account.paymentHier.get("0");

			// 构建TreeMap以保证排序
			TreeMap<Integer, PaymentPlanDetail> treeMap = new TreeMap<Integer, PaymentPlanDetail>(detailsMap);

			for (PaymentPlanDetail paymentPlanDetail : treeMap.values()) {
				// 如果有溢缴款，从当前期开始往后冲销
				if (paymAmt.compareTo(BigDecimal.ZERO) > 0) {

					for (SubAcctType st : acctTypeList) {
						BigDecimal subAmt = paymentPlanDetail.getAcctTypeAndAmtMap().get(st.subAcctType);

						if (subAmt != null && subAmt.compareTo(BigDecimal.ZERO) > 0 && paymAmt.compareTo(BigDecimal.ZERO) > 0) {

							if (subAmt.compareTo(paymAmt) <= 0) {

								paymAmt = paymAmt.subtract(subAmt);
								subAmt = BigDecimal.ZERO;
								updatePaymentPlanDetail(st.subAcctType, subAmt, paymentPlanDetail);

							}
							else {
								subAmt = subAmt.subtract(paymAmt);
								paymAmt = BigDecimal.ZERO;
								updatePaymentPlanDetail(st.subAcctType, subAmt, paymentPlanDetail);
								break;
							}
						}
					}
				}
			}
		}

		// 修正剩余贷款期数:先置0
		paymentPlan.setLeftLoanPeriod(Integer.valueOf(0));
		// 修正剩余贷款本金:先置0
		paymentPlan.setLeftLoanPrincipalAmt(BigDecimal.ZERO);
		for (PaymentPlanDetail detail : detailsMap.values()) {
			// detail.setFeeAmt(detail.getFeeAmt().setScale(2,
			// RoundingMode.HALF_UP));
			detail.setInterestAmt(detail.getInterestAmt().setScale(2, RoundingMode.HALF_UP));
			detail.setPenalizedAmt(detail.getPenalizedAmt().setScale(2, RoundingMode.HALF_UP));
			detail.setPrincipalBal(detail.getPrincipalBal().setScale(2, RoundingMode.HALF_UP));

			// 修正剩余贷款本金:累加所有还款计划中的未还本金
			paymentPlan.setLeftLoanPrincipalAmt(paymentPlan.getLeftLoanPrincipalAmt().add(detail.getPrincipalBal()));

			detail.setTotalRepayAmt(detail.getInterestAmt()
					// .add(detail.getFeeAmt())
					.add(detail.getPenalizedAmt()).add(detail.getPrincipalBal()).setScale(2, RoundingMode.HALF_UP));

			if (detail.getTotalRepayAmt().compareTo(BigDecimal.ZERO) > 0) {
				paymentPlan.setLeftLoanPeriod(paymentPlan.getLeftLoanPeriod() + 1);
			}

		}

		judgePersistPaymentPlan(acctSeq, cactAccount.getCustId(), cactAccount.getAcctParamId(), paymentPlan, paymentPlan.getCreateDate());

		for (PaymentPlanDetail paymentPlanDetail : paymentPlan.getDetailsMap().values()) {
			logger.info("还款日期{},还款业务日期{},期数{},应还总金额{},应还本金{},应还利息{},应还罚息{}", 
					paymentPlanDetail.getPaymentNatureDate(), 
					paymentPlanDetail.getPaymentDate(), 
					paymentPlanDetail.getLoanPeriod(),
					paymentPlanDetail.getTotalRepayAmt(), 
					paymentPlanDetail.getPrincipalBal(),
					// paymentPlanDetail.getFeeAmt(),
					paymentPlanDetail.getInterestAmt(), 
					paymentPlanDetail.getPenalizedAmt()
					);
		}

		return paymentPlan;
	}

//	/**
//	 * 还款计划修正
//	 * 
//	 */
//	public PaymentPlan modifyPaymentPlan(PaymentPlan paymentPlan, CactAccount cactAccount, Boolean flag) {
//		if (flag) { // 首次放款的时候需要根据按日或者按月修还款计划表
//			for (int i = 1; i <= paymentPlan.getDetailsMap().size(); i++) {
//				Date paymentDate = paymentPlan.getDetailsMap().get(i).getPaymentDate();
//
//				if (provider7x24.getCurrentDate().compareTo(new LocalDate(paymentDate)) <= 0) { // 根据按日或者按月对还款计划修正
//					if (i > 1) {
//						paymentPlan.getDetailsMap().get(i).setInterestAmt(new BigDecimal(0));
//						paymentPlan.getDetailsMap().get(i)
//								.setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(new BigDecimal(0)));
//					}
//					else {
//						Account account = newComputeService.retrieveAccount(cactAccount);
//						if (PrePaySettlementType.M.equals(account.advanceType))
//							continue;
//						BigDecimal intAmount = getCalculateInte(cactAccount);// 取未决利息
//						paymentPlan.getDetailsMap().get(i).setInterestAmt(paymentPlan.getDetailsMap().get(i).getInterestAmt().add(intAmount));
//						paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(
//								paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(paymentPlan.getDetailsMap().get(i).getInterestAmt()));
//					}
//				}
//			}
//			return paymentPlan;
//		}
//		BigDecimal calBal = new BigDecimal(0);
//		List<CactSubAcct> subAccts = accountQueryService.getSubAcctsByAccountSeq(cactAccount.getAcctSeq());
//		for (CactSubAcct cactSubAcct : subAccts) {
//			if (cactSubAcct.getSubAcctType().equals("LOAN")) {
//				calBal = cactSubAcct.getCurrBal(); // 冲销以后的金
//				break;
//			}
//		}
//		if (calBal.compareTo(new BigDecimal(0)) == 0)
//			return paymentPlan; // 无未还的情况
//		// 修还款计划信息
//		int firstPeriod = 1;
//		for (int i = 1; i <= paymentPlan.getDetailsMap().size(); i++) {
//			Date paymentDate = paymentPlan.getDetailsMap().get(i).getPaymentDate();
//
//			if (provider7x24.getCurrentDate().compareTo(new LocalDate(paymentDate)) <= 0) { // 根据按日或者按月对还款计划修正
//				if (firstPeriod == 1) {
//					// 已经结息的不做还款计划修正
//					if (cactAccount.getLastInterestDate() != null
//							&& cactAccount.getLastInterestDate().compareTo(cactAccount.getInterestDate()) == 0) {
//						firstPeriod++;
//						continue;
//					}
//					BigDecimal intAmount = getCalculateInte(cactAccount);
//					paymentPlan.getDetailsMap().get(i).setInterestAmt(intAmount);
//					paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(intAmount));
//				}
//				else {
//					paymentPlan.getDetailsMap().get(i).setInterestAmt(new BigDecimal(0));
//					paymentPlan.getDetailsMap().get(i).setTotalRepayAmt(paymentPlan.getDetailsMap().get(i).getPrincipalBal().add(new BigDecimal(0)));
//				}
//				firstPeriod++;
//			}
//		}
//		return paymentPlan;
//	}

//	public BigDecimal getSubAcctPenalizedAmt(CactAccount cactAccount, CactSubAcct cactSubAcct) {
//		BigDecimal penalizedAmt = new BigDecimal(0);
//		LocalDate processDate = new LocalDate(provider7x24.getCurrentDate().toDate()); // 业务日期
//		LocalDate graceDate = new LocalDate(cactAccount.getGraceDate());
//		LocalDate startDate;
//		Account account = newComputeService.retrieveAccount(cactAccount);
//		if (newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()) == AgeGroupCd.Normality) {
//			// 宽限期当天晚上在批量拉起与罚息结息之间还款，所以需要重算罚息
//			if (graceDate != null && processDate.isAfter(graceDate) && cactAccount.getTotDueAmt().compareTo(BigDecimal.ZERO) > 0
//					&& cactAccount.getFirstOverdueDate() == null && (cactAccount.getAgeCd().equals("0") || cactAccount.getAgeCd().equals("C"))) {
//				// 取宽限期内这几天的罚息
//				InterestTable interestTable = parameterFacility.loadParameter(InterestTable.class, cactSubAcct.getPenalizedInterestCode(),
//						account.intParamBaseType == ParamBaseType.Fixed ? cactAccount.getSetupDate() : processDate.toDate());
//				BigDecimal interest = newInterestService.calcInterest(new LocalDate(cactAccount.getPmtDueDate()), processDate,
//						cactSubAcct.getCurrBal(), ImmutableList.of(interestTable), newComputeService.getReceivableScale(),
//						InterestCycleRestMethod.NA);
//				if (interest == null)
//					interest = new BigDecimal(0);
//				return interest;
//			}
//			return penalizedAmt;
//		}
//		else {// 逾期的情况下罚息计算
//			if (cactSubAcct.getLastPenalizedInterestDate() == null) { // 在逾期和非应计的情况下,计算罚息,为空则说明当前第2次和以后期次的罚息刚做结转出来
//				// 罚息起息日为账户上宽限日
//				startDate = new LocalDate(cactAccount.getGraceDate());
//			}
//			else {
//				startDate = new LocalDate(cactSubAcct.getLastPenalizedInterestDate()).plusDays(1); // 上次罚息计息日加1
//			}
//			if (cactSubAcct.getLastPenalizedInterestDate() == null && (processDate.isBefore(startDate) || processDate.isEqual(startDate))) // 宽限日当天和宽限期前
//			{
//				// 还没到起息日，不处理
//				return penalizedAmt;
//			}
//			InterestTable interestTable = parameterFacility.loadParameter(InterestTable.class, cactSubAcct.getPenalizedInterestCode(),
//					account.intParamBaseType == ParamBaseType.Fixed ? cactAccount.getSetupDate() : processDate.toDate());
//			BigDecimal interest = newInterestService.calcInterest(new LocalDate(cactAccount.getPmtDueDate()), processDate, cactSubAcct.getCurrBal(),
//					ImmutableList.of(interestTable), newComputeService.getReceivableScale(), InterestCycleRestMethod.NA);
//			if (interest == null)
//				interest = new BigDecimal(0);
//			return interest;
//
//		}
//	}

	@Override
	public PaymentPlan reCreatePaymentPlan(PaymentPlan paymentPlan, CactAccount cactAccount, Account acctParam) {
		paymentPlan.setTotalLoanPeriod(cactAccount.getTotalLoanPeriod());
		paymentPlan.setTotalLoanPrincipalAmt(cactAccount.getTotalLoanPrincipalAmt());

		List<PaymentPlanDetail> details = paymentPlan.getDetails();
		Map<Integer, PaymentPlanDetail> detailsMap = paymentPlan.getDetailsMap();
		int beginPeriod = cactAccount.getTotalLoanPeriod() - paymentPlan.getDetailsMap().size(); // 还款计划开始期次
		if (beginPeriod == 0)
			return paymentPlan;

		for (PaymentPlanDetail paymentPlanDetail : paymentPlan.getDetailsMap().values()) {
			paymentPlanDetail.setLoanPeriod(beginPeriod + 1);
			beginPeriod++;
		}

		Date paymentDate = null;
		for (int i = 1; i <= cactAccount.getTotalLoanPeriod() - paymentPlan.getDetailsMap().size(); i++) {
			PaymentPlanDetail detail = new PaymentPlanDetail();
			detail.setLoanPeriod(i);
			detail.setInterestAmt(BigDecimal.ZERO);
			detail.setPenalizedAmt(BigDecimal.ZERO);
			// detail.setFeeAmt(BigDecimal.ZERO);
			detail.setTotalRepayAmt(BigDecimal.ZERO);
			detail.setPrincipalBal(BigDecimal.ZERO);
			if (i == 1)
				paymentDate = cactAccount.getSetupDate();
			paymentDate = newComputeService.getNextInterstDate(cactAccount, paymentDate, acctParam, cactAccount.getBillingCycle());
			detail.setPaymentDate(paymentDate);
			//根据偏移量计算还款计划的还款日然日
			Date natureDate = DateUtilsExt.addDays(detail.getPaymentDate(), -(provider7x24.getOffset4BizDate2NatureDate(new LocalDate(cactAccount.getSetupDate()))));
			detail.setPaymentNatureDate(natureDate);
			details.add(detail);
			detailsMap.put(i, detail);
		}

		paymentPlan.setDetails(details);
		paymentPlan.setDetailsMap(detailsMap);
		return paymentPlan;
	}

}
