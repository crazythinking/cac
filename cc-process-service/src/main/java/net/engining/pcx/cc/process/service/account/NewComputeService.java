package net.engining.pcx.cc.process.service.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.gm.infrastructure.enums.SystemStatusType;
import net.engining.gm.param.model.CurrencyCd;
import net.engining.gm.param.model.OrganizationInfo;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.ProductCc;
import net.engining.pcx.cc.param.model.RateCalcMethod;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.CalcMethod;
import net.engining.pcx.cc.param.model.enums.ComputInteHT;
import net.engining.pcx.cc.param.model.enums.CycleStartDay;
import net.engining.pcx.cc.param.model.enums.DelqTolInd;
import net.engining.pcx.cc.param.model.enums.DownpmtTolInd;
import net.engining.pcx.cc.param.model.enums.GenAcctMethod;
import net.engining.pcx.cc.param.model.enums.ParamBaseType;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.param.model.enums.PnitType;
import net.engining.pcx.cc.param.model.enums.TierInd;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;

/**
 * @author linwk
 * 入账公共计算类
 */
/**
 * @author BO
 *
 */
@Service
public class NewComputeService {
				
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private ParameterFacility parameterCacheFacility;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private NewInterestService newInterestService;
	
	private int accrualScale = 2;
	
	private int receivableScale = 6;
	
	private int interestRateScale = 10;
	
	private int balanceScale = 2;
	
	/**
	 * 计算计息截止日期；
	 * 如果按日计息的情况下，子账户是“LBAL” 或 子账户是“LOAN”且当前业务日期不是最后一期的到期日，计息截止日就是当前业务日期+1；
	 * 否则就是当前业务日期；
	 * @param cactAccount
	 * @param cactSubAcct
	 * @param paymentPlan
	 * @param endDate
	 * @param tables
	 * @return
	 */
	public LocalDate calcEndDate(CactAccount cactAccount, CactSubAcct cactSubAcct,
			PaymentPlan paymentPlan, LocalDate endDate,
			List<InterestTable> tables) {
		//当前业务日期是否最后一期到期日
		PaymentPlanDetail lastDetail = paymentPlan.getDetailsMap().get(paymentPlan.getTotalLoanPeriod());
		LocalDate curDate = provider7x24.getCurrentDate();
		boolean lastDay = false;
		if(lastDetail.getPaymentDate().compareTo(curDate.toDate()) == 0){
			lastDay = true;
		}
		
		//如果当前期数在最后一期之前，还款日当天的利息在还款日当天收掉(因为到期日当天还款，日终批量还是会在当期计一天利息)，最后一期中还款的话当日就不收利息，其实是没有结清。
		//总体原则是当天放款当天收息，当天还款就不收息。
		if(
			/**这一段可以去掉
			//cactAccount.getTotalLoanPeriod() > 1 && //如果只有一期，跑N天收N-1天利息
			//业务日当天预修的利息应该每一期都要修
			//cactAccount.getCurrentLoanPeriod() + 1 < paymentPlan.getTotalLoanPeriod() &&
			**/
			//这段逻辑依赖于批量顺序，本金先计息，再结息的情况下，LOAN类型子账户从创建日起每日计息；LBAL类型子账户创建日不计息，之后每天计息；
			//因此算头不算尾时，LOAN不需要（在未跑批的情况下）预修当前业务日所属的利息；但LBAL则需要
			
			(cactSubAcct.getSubAcctType().equals("LBAL") 
					|| (cactSubAcct.getSubAcctType().equals("LOAN") && !lastDay)) &&
			tables.get(0).cycleBase == Interval.D &&
			tables.get(0).cycleBaseMult == 1) {
			endDate = endDate.plusDays(1);
		}
		return endDate;
	}
	
	/**
	 * 计算当前子账户的起息日；
	 * 在子账户上次计息日期为空时，根据起息日类型确定第一次起息日；但对于“LBAL”，需要再加1天，因为产生该子账户的时候就是结息日，已经计息，所以开始计息日是下一日；
	 * 其他情况只要上次计息日期+1
	 * @param cactSubAcct
	 * @param cactAccount
	 * @param subAcct
	 * @param account
	 * @return
	 */
	public LocalDate calcStartDate(CactSubAcct cactSubAcct, CactAccount cactAccount, SubAcct subAcct, Account account)
	{
		LocalDate startDate;
		if(cactSubAcct.getLastComputingInterestDate() == null)
		{
			// 第一次计息
			startDate = calcSetupDate(cactSubAcct, cactAccount, subAcct);
			// LBAL子账户修利息的时候只修当天就可以了，setUpDate的时候不用计息,导致lastComputingInterestDate是null
			if ("LBAL".equals(cactSubAcct.getSubAcctType())){
				startDate = startDate.plusDays(1);
			}
			//对于LOAN子账户，存在不计头的情况
			if ("LOAN".equals(cactSubAcct.getSubAcctType()) && 
					(ComputInteHT.NHNT.equals(account.computInteHT) || ComputInteHT.NHYT.equals(account.computInteHT))){
				startDate = startDate.plusDays(1);
			}
		}
		else
		{
			startDate = new LocalDate(cactSubAcct.getLastComputingInterestDate()).plusDays(1);
		}
		return startDate;
	}

	/**
	 * 计算余额成分（子账户）的起息日
	 * @param cactSubAcct
	 * @param cactAccount
	 * @param subAcct
	 * @return
	 */
	public LocalDate calcSetupDate(CactSubAcct cactSubAcct, CactAccount cactAccount, SubAcct subAcct)
	{
		LocalDate setupDate;
		switch (subAcct.intAccumFrom)
		{
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
	 * 获取账户类型参数对象
	 * @param paramId 账户参数代码
	 * @return 账户类型参数对象
	 */
	public Account retrieveAccount(CactAccount cactAccount)
	{
		Account account = parameterCacheFacility.getParameter(Account.class, 
				cactAccount.getAcctParamId(), cactAccount.getSetupDate());;
		
		if (account == null){
			throw new IllegalArgumentException("account type does not match product definition");
		}
		return account;
	}
	
	/**
	 * 获取子账户类型参数对象
	 * @param subAcctParamId 子账户参数代码
	 * @param cactAccount 账户对象
	 * @return 子账户类型参数对象
	 */
	public SubAcct retrieveSubAcct(String subAcctParamId, CactAccount cactAccount)
	{
		SubAcct subAcct = parameterCacheFacility.getParameter(SubAcct.class, 
				subAcctParamId, cactAccount.getSetupDate());
		
		if (subAcct == null){
			throw new IllegalArgumentException("sub account type does not match product definition");
		}
		return subAcct;
	}
	
	/**
	 * 获取子账户类型参数对象
	 * @param acctSeq
	 * @param subAcctType
	 * @return
	 */
	public SubAcct retrieveSubAcct(Integer acctSeq, String subAcctType){
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);
		Account account = parameterCacheFacility.getParameter(Account.class, cactAccount.getAcctParamId(), cactAccount.getSetupDate());
		String subAcctParamId = account.subAcctParam.get(subAcctType);
		
		return retrieveSubAcct(subAcctParamId, cactAccount);
	}

	/**
	 * 获取子账户类型参数对象
	 */
	public SubAcct retrieveSubAcct(CactSubAcct cactSubAcct, CactAccount cactAccount)
	{
		return retrieveSubAcct(cactSubAcct.getSubacctParamId(), cactAccount);
	}

	/**
	 * 获取子账户类型参数对象
	 */
	public SubAcct retrieveSubAcct(CactSubAcct cactSubAcct)
	{
		return retrieveSubAcct(cactSubAcct, em.find(CactAccount.class, cactSubAcct.getAcctSeq()));
	}

	/**
	 * 获取利率参数
	 * @param interestCode 利率参数代码
	 * @param cactAccount  账户参数
	 * @return 利率参数对象
	 */
	public InterestTable retrieveInterestTable(String interestCode, CactAccount cactAccount)
	{
		// 取建账日的账户参数
		Account account = retrieveAccount(cactAccount);
		
		InterestTable interestTable;
		if(account.intParamBaseType != null && 
				account.intParamBaseType.equals(ParamBaseType.Fixed)){
			interestTable = parameterCacheFacility.getParameter(InterestTable.class, 
					interestCode, cactAccount.getSetupDate());
		} else {
			interestTable = parameterCacheFacility.getParameter(InterestTable.class, interestCode, systemStatusFacility.getSystemStatus().businessDate);
		}
		
		if (interestTable == null){
			throw new IllegalArgumentException("interest table type does not match product definition");
		}
		return interestTable;
	}
	
	/**
	 * 获取下期账单的还款日
	 * @param account 账户表信息
	 * @return 下期账单还款日
	 */
	@SuppressWarnings("deprecation")
	public Date getNextPaymentDay(CactAccount cactAccount){
		// 获取账户参数
		Account account = retrieveAccount(cactAccount);
		
		// 还款日
		Date paymentDay = null;
		/**
		//根据账户属性中的还款日类型，决定哪天作为最后还款日
		switch (account.paymentDueDay){
		//最后还款日类型为账单日后的固定天数
		//最后还款日= 上一账单日 + 固定天数
		case D: paymentDay = DateUtils.addDays(cactAccount.getNextStmtDate(), account.pmtDueDays);break;
		
		//最后还款日类型为账单日后固定日期
		case F: 
			paymentDay = DateUtils.setDays(cactAccount.getNextStmtDate(), account.pmtDueDays);
			while (paymentDay.before(cactAccount.getNextStmtDate())){
				paymentDay = DateUtils.addMonths(paymentDay, 1);
			}
			break;
		
		//找不到值抛异常
		default : throw new IllegalArgumentException("账户属性中还款日类型不正确");
		}
		*/
		if (account.isLockPaymentDay) {
			//先算出结息日是1月份所对应的最后还款日，以后的最后还款日都取这一天。
			Date stdDate = DateUtils.setMonths(cactAccount.getSetupDate(), 0);
			stdDate = DateUtils.setDays(stdDate, 1);
			Date junInterestDate = getNextInterstDate(cactAccount, stdDate, account, cactAccount.getBillingCycle());
			if(account.pmtDueDays == null)
				account.pmtDueDays = 0;
			Date junPaymentDate = DateUtils.addDays(junInterestDate, account.pmtDueDays);
			int payDay = junPaymentDate.getDate();
			paymentDay = processPaymentDay(cactAccount.getInterestDate(), payDay);
			if (paymentDay.before(cactAccount.getInterestDate())) {
				paymentDay = DateUtils.addMonths(paymentDay, 1);
			}
		}
		else {
			paymentDay = DateUtils.addDays(cactAccount.getInterestDate(), account.pmtDueDays);
		}
		return paymentDay;
	}
	
	/**
	 * 计算账户下所有子账户的余额
	 * @param cactSubAccts
	 * @return
	 */
	public BigDecimal getAcctTotalBal( List<CactSubAcct> cactSubAccts ) {
		
		BigDecimal totalBal = BigDecimal.ZERO;
		// 计算账户参与超限计算的总余额
		for (CactSubAcct cactSubAcct : cactSubAccts ) {
			SubAcct subAcct = retrieveSubAcct(cactSubAcct);
			// 参与超限计算 == True, 账户总余额 = 账户当前余额
			if ( subAcct.overlimitQualify ) 
				totalBal = totalBal.add(cactSubAcct.getEndDayBal());
		}
		return totalBal;
	}

	/**
	 * 汇率转换
	 * @param businessType
	 * @param productCd
	 * @param currCd
	 * @param amt
	 * @return
	 */
	public BigDecimal exchange(BusinessType businessType, String productCd, String currCd, BigDecimal amt) {
		OrganizationInfo org = parameterCacheFacility.getParameter( OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY );
		ProductCc productCc = parameterCacheFacility.getParameter( ProductCc.class, productCd );
		CurrencyCd currency = parameterCacheFacility.getParameter( CurrencyCd.class, currCd );
		
		BigDecimal rate = BigDecimal.ONE;
		
		//这张卡片包含本币账户
		if(productCc.accountParams.get(businessType).containsKey(org.baseCurrencyCode)) {
			//如果传入的币种是本币就直接返回金额，否则兑换成本币
			if( org.baseCurrencyCode.equals( currCd ) ) {
				rate = BigDecimal.ONE;
			}
			else {
				rate = currency.conversionRt;
			}
		}
		
		return amt.multiply(rate);
	}
	
	/**
	 * 计算账单所有应还款额
	 * 
	 * @param accountInfo
	 * @return
	 */
	public BigDecimal calcQualGraceBal(List<CactSubAcct> cactSubAccts) {
		// 应还款额
		BigDecimal graceBal = BigDecimal.ZERO;

		// 循环所有的子账户
		for (CactSubAcct cactSubAcct : cactSubAccts) {
			// 获取子账户参数
			SubAcct subAcct = retrieveSubAcct(cactSubAcct);

			// 判断余额成分对应的是否计入全额应还款金额参数为true的余额成分计入全额还款金额
				if (subAcct.graceQualify) {
					graceBal = graceBal.add(cactSubAcct.getEndDayBal());
				}
		}

		if (graceBal.compareTo(BigDecimal.ZERO) < 0){
			graceBal = BigDecimal.ZERO;
		}
		return graceBal;
	}
	
	/**
	 * 计算下一约定还款日期
	 * @param account
	 * @return
	 */
	public Date getNextDDDay(CactAccount cactAccount){
		// 获取账户参数
//		Account account = retrieveAccount(cactAccount);
				
		// 还款日
		Date ddDay = null;
		/*ddDay = DateUtils.addDays(getNextPaymentDay(cactAccount), - account.directDbDays);	*/	
		return ddDay;
	}
	
	/**
	 * 获取账期下余额
	 * @param cactSubAcct 子账户信息
	 * @param stmt 账期
	 * @return 账期余额
	 */
	public BigDecimal getStmtBal(CactSubAcct cactSubAcct ){
		
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		BigDecimal totalBalance = new JPAQueryFactory(em)
				.select(qCactSubAcct.endDayBal.sum().coalesce(BigDecimal.ZERO))
				.from(qCactSubAcct)
				.where(qCactSubAcct.acctSeq.eq(cactSubAcct.getAcctSeq() ),
						qCactSubAcct.subAcctType.ne("LOAN"),
						qCactSubAcct.stmtHist.eq(cactSubAcct.getStmtHist() )
						).fetchOne();
		
		if(totalBalance==null) totalBalance=BigDecimal.ZERO;
		
		return totalBalance;
	}
	
	/**
	 * FIXME 逻辑似乎有问题，待检查；
	 * 根据规则确定计算罚息的基础金额
	 * @param cactSubAcct 子账户信息
	 * @param cactAccount 账户信息
	 * @param Account 账户参数
	 * @return 罚息额
	 */
	public BigDecimal getComputePnitBal(CactAccount cactAccount,CactSubAcct cactSubAcct , Account account){
		BigDecimal pnitBal= new BigDecimal(0);
		BigDecimal subBal=getStmtBal(cactSubAcct); //账期余额，不包含未到期
		
		//是否计算罚息
		if(account.isPnit){
			//对于全额本金计算罚息，利息余额成分计算罚息即可
			if(subBal.compareTo(new BigDecimal(0))>0)
			if( PnitType.A.equals(account.pnitType) && "INTE".equals( cactSubAcct.getSubAcctType() ) ){ 
					pnitBal=cactAccount.getTotalLoanPrincipalAmt();//本金全额 
			
			}
			//贷款应还未还本金算罚息
			else if(PnitType.B.equals(account.pnitType) && "INTE".equals( cactSubAcct.getSubAcctType() ) ){
				BigDecimal totalCapi=getTotalCapi(cactSubAcct);
				if(subBal.compareTo(new BigDecimal(0))>0)
					pnitBal=totalCapi;
				
			}
			//本期全部本金： 到期的本金算罚息。 比如第一期到期本金10w，利息500，还了部分，罚息按10w算	
			else if(PnitType.C.equals(account.pnitType)&& "LBAL".equals( cactSubAcct.getSubAcctType() )  ){ 
				if(subBal.compareTo(new BigDecimal(0))>0) pnitBal=cactSubAcct.getBeginBal();
			
			//本期本息和：   到期的本金和利息算罚息。 比如第一期到期本金10w，利息500，还了部分，罚息按10w+500算
			}else if( PnitType.D.equals(account.pnitType)  ){
				if(subBal.compareTo(new BigDecimal(0))>0) pnitBal=cactSubAcct.getBeginBal();
				
			//全部逾期本金： 逾期多少本金就算多少罚息  比如第一期到期本金10w，利息500，还了部分，罚息按剩余逾期本金算
			}else if( PnitType.E.equals(account.pnitType)&& "LBAL".equals( cactSubAcct.getSubAcctType() ) ){
				if(subBal.compareTo(new BigDecimal(0))>0) pnitBal=cactSubAcct.getEndDayBal();
			
			//全部逾期本息和：   逾期多少本金和利息就算多少罚息。 比如第一期到期本金10w，利息500，还了部分，罚息按剩余逾期本金和利息算
			}else if( PnitType.F.equals(account.pnitType)){
				 pnitBal=cactSubAcct.getEndDayBal();
			
			//剩余本金+逾期利息： 剩余未还本金+逾期利息算罚息。 贷款应还未还本金+逾期的利息算罚息
			}else if(PnitType.G.equals(account.pnitType)&& "INTE".equals( cactSubAcct.getSubAcctType()) ){
				BigDecimal totalCapi=getTotalCapi(cactSubAcct);
				if(subBal.compareTo(new BigDecimal(0))>0)pnitBal=totalCapi.add(cactSubAcct.getEndDayBal() );
				
			}
			
		}
		else {
			pnitBal= BigDecimal.ZERO;
			
		}
		
		return pnitBal;
	}
	
	/**
	 * 获取账期下余额
	 * @param cactSubAcct 子账户信息
	 * @param stmt 账期
	 * @return 账期余额
	 */
	public BigDecimal getTotalCapi(CactSubAcct cactSubAcct ){
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
//		List<CactSubAcct> subAccts = new JPAQueryFactory(em)
//				.select(qCactSubAcct)
//				.from(qCactSubAcct)
//				.where(qCactSubAcct.acctSeq.eq(cactSubAcct.getAcctSeq() ),
//						(qCactSubAcct.subAcctType.eq("LOAN").or( qCactSubAcct.subAcctType.eq("LBAL"))  )
//						)
//				.fetch();
//		BigDecimal totalBalance= new BigDecimal(0);
//		for(CactSubAcct subacct : subAccts){
//			totalBalance = totalBalance.add(subacct.getCurrBal());
//		}
		
		//重构如下，提高性能
		BigDecimal totalBalance = new JPAQueryFactory(em)
				.select(qCactSubAcct.currBal.sum().coalesce(BigDecimal.ZERO))
				.from(qCactSubAcct)
				.where(qCactSubAcct.acctSeq.eq(cactSubAcct.getAcctSeq() ),
						(qCactSubAcct.subAcctType.eq("LOAN").or( qCactSubAcct.subAcctType.eq("LBAL"))  )
						)
				.fetchOne();
		
		if(totalBalance==null) totalBalance=BigDecimal.ZERO;
		
		return totalBalance;
	}
	
	/**
	 * 获取单个账户的下期账单的宽限日
	 * @param account 账户信息
	 * @return 下期账单宽限日
	 */
	public Date getNextGraceDay(CactAccount cactAccount){
		// 账户参数
		Account account = retrieveAccount(cactAccount);
		
		// 宽限日
		Date graceDay = null;
		
		if (cactAccount.getPmtDueDate() != null) {
			graceDay = DateUtils.addDays(cactAccount.getPmtDueDate(), account.pmtGracePrd);
		}
		// 下一还款日
//		Date paymentDay = getNextPaymentDay(cactAccount);
		
		//根据账户属性中的还款日类型，决定哪天作为最后还款日
		/*switch (account.paymentDueDay){
		//最后还款日类型为账单日后的固定天数或者账单日后固定日期
		//最后还款日= 还款日 + 宽限期天数
		case D: 
		case F: 
			graceDay = DateUtils.addDays(paymentDay, account.pmtGracePrd);
			break;
		
		//找不到值抛异常
		default : throw new IllegalArgumentException("账户属性中还款日类型不正确");
		}*/
		
		return graceDay;
	}
	
	/**
	 * 计算下一账单日期
	 * @param date 当前处理日期
	 * @param acctParam 账户参数代码
	 * @param billingCycle  账单周期
	 */
	public Date getNextInterstDate(CactAccount cactAccount, Date date, Account acctParam, String billingCycle) {
		if(StringUtils.isBlank(billingCycle)){
			billingCycle = String.valueOf(acctParam.dIntSettleDay);
		}
		Date interestDate = null;
		//设置结息日
		if(acctParam.intSettleStartMethod.equals(CycleStartDay.P)){
			switch(acctParam.intUnit){
				case D : interestDate = DateUtils.addDays(date, acctParam.intUnitMult); break;
				case W : interestDate = DateUtils.addDays(date, acctParam.intUnitMult*7); break;
				case M : 
					if(acctParam.paymentMethod.equals(PaymentMethod.MRG)|| acctParam.paymentMethod.equals(PaymentMethod.MSF)  ){
						Date beginDay= getBeginDay(cactAccount.getAcctNo());//如果是首次放款,返回的day为空；
						if(beginDay!=null){
							Calendar  calendar = Calendar.getInstance() ; 
							calendar.setTime(cactAccount.getSetupDate());//贷款发放日期
							
							Calendar  calendar1 = Calendar.getInstance() ; 
							calendar1.setTime(beginDay);//首笔贷款发放日
							int day= calendar1.get(Calendar.DAY_OF_MONTH)>=28?28:calendar1.get(Calendar.DAY_OF_MONTH);
							//非首次放款,下次计息日在当前期次为0：第一期的下次计息日需要判断,后面的期次都是+1个月
							int period= cactAccount.getCurrentLoanPeriod();
							if(period==0){//放款的时候,下次计息日的判断
								if(calendar.get(Calendar.DAY_OF_MONTH)<=day || beginDay.equals(cactAccount.getSetupDate()) ){ //当日的多次放款
									calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
									interestDate = DateUtils.addMonths(calendar.getTime(), acctParam.intUnitMult*(cactAccount.getCurrentLoanPeriod()+1) );
								}else{
									calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
									interestDate = DateUtils.addMonths(calendar.getTime(), acctParam.intUnitMult*(cactAccount.getCurrentLoanPeriod()+2));
								}
							}else{//period主要用于批量时的判断
								//下次还款日+1个月
								interestDate = DateUtils.addMonths(date, 1);
							}
						}else{
							Calendar  calendar = Calendar.getInstance();  
							calendar.setTime(date);
							int day=calendar.get(Calendar.DAY_OF_MONTH)>=28?28:calendar.get(Calendar.DAY_OF_MONTH);
							calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
							interestDate = DateUtils.addMonths(calendar.getTime(), acctParam.intUnitMult*(cactAccount.getCurrentLoanPeriod()+1));
						}
					}else{
						interestDate = DateUtils.addMonths(date, acctParam.intUnitMult);
					}
					break;
				case Y : 
					interestDate = DateUtils.addYears(date, acctParam.intUnitMult); 
					break;
				default :{
					throw new IllegalArgumentException("最小结息周期单位"+acctParam.intUnit+"暂不支持");
				}
			}
		}else{
			interestDate = interestDate(date,acctParam, billingCycle);	
		}
		return interestDate;
	}
	
	private  Date interestDate(Date processDate,Account acctParam,String billingCycle){
		
		if(billingCycle == null){
			billingCycle = String.valueOf(acctParam.dIntSettleDay);
		}
		
		//标记当前时间
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(processDate);
		Date nowDate = DateUtils.truncate(cal1.getTime(), Calendar.HOUR);
		//标记结息日期
		Calendar cal2 = Calendar.getInstance();
		cal2.set(cal1.get(Calendar.YEAR) - 1,11,1,0,0,0);
		
		//设置 day字段
		if(acctParam.dIntSettleDay == 99){

			int maxDay = cal2.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal2.set(Calendar.DATE, maxDay);
		}else{
			cal2.set(Calendar.DATE, Integer.parseInt(billingCycle));
		}
		Date natDate = DateUtils.truncate(cal2.getTime(), Calendar.HOUR);
		
		while(nowDate.compareTo(natDate) >= 0){
			//这里存在临界值问题，暂定根据批量状态标志判断
			//批量标志位为N，计算的结息日和nowDate相等，则nowDate为结息日，如果批量标志位B,计算的结息日和nowDate相等，则nowDate不为结息日
			if(systemStatusFacility.getSystemStatus().systemStatus == SystemStatusType.N 
					&& nowDate.compareTo(natDate) == 0){
				return natDate;
			}
			if(acctParam.intUnit == Interval.M){		
				natDate = DateUtils.addMonths(natDate, acctParam.intUnitMult);
			}else if(acctParam.intUnit == Interval.Y){
				natDate = DateUtils.addYears(natDate, acctParam.intUnitMult);
			}else
				throw new IllegalArgumentException("最小结息周期单位"+acctParam.intUnit+"配置有误");
		}
		return natDate;
	}
	
	/**
	 * 设置日期的day字段如果大于当月最大日期则取月末日期
	 * @param paymentDay
	 * @param dayOfMth
	 * @return
	 */
	private Date processPaymentDay(Date paymentDay, int dayOfMth){
		Calendar cal = Calendar.getInstance();
		cal.setTime(paymentDay);
		int maxDayofMth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(dayOfMth < maxDayofMth){
			return DateUtils.setDays(paymentDay, dayOfMth);
		}else{
			return DateUtils.setDays(paymentDay, maxDayofMth);
		}
	}
	
	/**
	 * 分段比例计算金额公用方法(可指定靠档金额baseAmount)
	 * @param tierInd not null
	 * @param chargeRates not null
	 * @param calcAmount 以此金额作为计算基础 not null
	 * @param baseAmount 以此金额作为靠档基础 not null
	 * @return
	 */
	public BigDecimal calcTieredAmount(TierInd tierInd, List<RateCalcMethod> chargeRates, BigDecimal calcAmount, BigDecimal baseAmount){

		// 检测输入参数是否为空
		if (tierInd ==null || chargeRates == null || calcAmount == null || baseAmount == null){
			throw new IllegalArgumentException("输入的参数为null，无法处理");
		}

		// 检测多级费率的最大金额是否按照从小到大排列
		BigDecimal preRateCeil = BigDecimal.ZERO;
		for (RateCalcMethod rateDef : chargeRates)
		{
			if (rateDef.rateCeil.compareTo(preRateCeil) < 0)
			{
				throw new IllegalArgumentException("参数中最大值列表并未按照从小到大排序");
			}
			preRateCeil = rateDef.rateCeil;	
		}
		
		// 开始计算
		BigDecimal resultAmount = BigDecimal.ZERO;
		
		// 根据分段计费类型进行不同的计算
		switch(tierInd){
		case F: 
			// 使用全部金额作为计算金额
			for (int i = 0 ; i < chargeRates.size(); i++){
				// 规则参数符号无关，故用abs()
				if (baseAmount.abs().compareTo(chargeRates.get(i).rateCeil) <= 0){
					resultAmount = calcAmount.multiply(chargeRates.get(i).rate);
					// 如果存在基准金额，则附加基准金额
					if (chargeRates.get(i).rateBase != null){
						resultAmount = resultAmount.add(chargeRates.get(i).rateBase);
					}
					return resultAmount;
				}
			}
			// 如果计算基础金额大于参数中配置的最大交易金额，则抛出异常
			throw new IllegalArgumentException("实际交易金额大于参数配置的最大交易金额");
		case T: 
			// 采用分段金额作为计算金额
			// 尚未计算金额
			BigDecimal current = calcAmount.abs();// 规则参数符号无关，故用abs()

			for (int i = 0 ; (i < chargeRates.size()) && (current.signum() == 1) ; i++)
			{
				BigDecimal minus = chargeRates.get(i).rateCeil.compareTo(current) > 0 ? current : chargeRates.get(i).rateCeil;
				resultAmount = resultAmount.add(minus.multiply(chargeRates.get(i).rate));
				current = current.subtract(minus);
				if (chargeRates.get(i).rateBase != null){
					resultAmount = resultAmount.add(chargeRates.get(i).rateBase);
				}
			}
			if (current.compareTo(BigDecimal.ZERO) > 0){
				throw new IllegalArgumentException("实际交易金额大于参数配置的最大交易金额");
			}
			return resultAmount;
		default:
			throw new IllegalArgumentException("无法处理的分段计费类型 tierInd:[" + tierInd.toString() + "]");
		}
	}
	
	/**
	 * 第一笔贷款开始日期,根据AcctNo
	 * TODO 方法名不合理，待重构
	 * @param acctNo
	 * @return
	 */
	public Date getBeginDay(Integer acctNo){
		Date beginDay=null;
		QCactAccount qCactAccount = QCactAccount.cactAccount;
		List<Date> setupDates = new JPAQueryFactory(em)
				.select(qCactAccount.setupDate)
				.from(qCactAccount)
				.where(
						qCactAccount.acctNo.eq(acctNo) ,
						qCactAccount.businessType.eq(BusinessType.BL)
					 )
				.orderBy(qCactAccount.acctSeq.asc())
				.fetch();
		
		if(Optional.fromNullable(setupDates).isPresent() && !setupDates.isEmpty()){
			
			beginDay=setupDates.get(0); 
		}
		return beginDay;
	}
	
	/**
	 * 第一笔贷款开始日期,根据AcctSeq
	 * TODO acctSeq是主键，查询逻辑不合理，待重构
	 * @param acctSeq
	 * @return
	 */
	public Date getBeginDayByAcctSeq(Integer acctSeq){
//		Integer day=null;
//		Integer acctNo=null;
//		QCactAccount qCactAccount = QCactAccount.cactAccount;
//		List<Integer> acctNos = new JPAQueryFactory(em)
//				.select(qCactAccount.acctNo)
//				.from(qCactAccount)
//				.where(
//						qCactAccount.acctSeq.eq(acctSeq),
//						qCactAccount.businessType.eq(BusinessType.BL)
//					 )
//				.fetch();
//		
//		if(Optional.fromNullable(acctNos).isPresent() && !acctNos.isEmpty()){
//			acctNo=acctNos.get(0);
//		}
		
//		JPAQuery query = new JPAQuery(em).from(qCactAccount)
//				.where(qCactAccount.acctNo.eq(acctNo) 
//						,qCactAccount.businessType.eq(BusinessType.BL)
//					 ).orderBy(qCactAccount.acctSeq.asc());
//		List<CactAccount> accts = query.list(qCactAccount) ;
//		if(accts!=null && accts.size()>0){
//			Calendar  calendar = Calendar.getInstance(); 
//			calendar.setTime(accts.get(0).getSetupDate()); 
//			day= calendar.get(Calendar.DAY_OF_MONTH)>=28?28:calendar.get(Calendar.DAY_OF_MONTH);
//		}
		
//		return day;
		
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);
		return cactAccount.getSetupDate();
	}
	
//	public boolean isFirstPcikupLoan(Integer acctSeq){
//		boolean iflag=false;
//		Integer acctNo=null;
//		QCactAccount qCactAccount = QCactAccount.cactAccount;
//		JPAQuery query1 = new JPAQuery(em).from(qCactAccount)
//				.where(qCactAccount.acctSeq.eq(acctSeq) 
//						,qCactAccount.businessType.eq(BusinessType.BL)
//					 );
//		List<CactAccount> accts1 = query1.list(qCactAccount) ;
//		if(accts1!=null && accts1.size()>0){
//			acctNo=accts1.get(0).getAcctNo();//取账号编号
//		}
//		Date setupDate=null;
//		JPAQuery query = new JPAQuery(em).from(qCactAccount)
//				.where(qCactAccount.acctNo.eq(acctNo) 
//					   ,qCactAccount.businessType.eq(BusinessType.BL)
//					 ).orderBy(qCactAccount.acctSeq.asc());
//		List<CactAccount> accts = query.list(qCactAccount) ;
//		if(accts!=null && accts.size()>0){
//			setupDate=accts.get(0).getSetupDate(); 
//		}
//		
//		return iflag;
//	}
	
	/**
	 * 构造利率对象
	 * @param inttable
	 * @param newrate
	 * @return
	 */
	public InterestTable setupIntTable(InterestTable inttable , BigDecimal newrate) {
			BigDecimal rate= newrate;//目前只有一种利率的情况
			InterestTable it = new InterestTable();
			it.description = "利率变更";
			it.tierInd = inttable.tierInd;
			it.cycleBase = inttable.cycleBase;
			it.cycleBaseMult = inttable.cycleBaseMult;
			it.effectiveDate =inttable.effectiveDate;
			it.rateBaseInterval = inttable.rateBaseInterval;
			
			List<RateCalcMethod> list = new ArrayList<RateCalcMethod>();
			RateCalcMethod oc = new RateCalcMethod();
			oc.rate = rate;
			oc.rateCeil = BigDecimal.valueOf(9999999999999l);
			oc.rateBase = BigDecimal.ZERO;
			list.add(oc);
			it.chargeRates = list;
			return it;
		}
		
	/**
	 * 构造未到期本金子账户对象
	 * TODO 方法名不合理，待重构
	 * @param subAcct
	 * @param IntCode
	 * @return
	 */
	public SubAcct setupSubAcct(SubAcct subAcct, String IntCode ) {
		SubAcct sa = new SubAcct();
		sa.description = "贷款剩余本金-子账户";
		sa.subAcctType = "LOAN";
		sa.balanceDirection = subAcct.balanceDirection;
		sa.balanceType = subAcct.balanceType;
		sa.graceQualify = false;
		sa.intAccumFrom = subAcct.intAccumFrom;
		sa.interestPostCode = "IB000044";
		sa.intTables = new ArrayList<String>();
		sa.intTables.add(IntCode); 
		sa.intWaive = false;
		sa.minPaymentRates = new HashMap<Integer, BigDecimal>();
		sa.minPaymentRates.put(0, BigDecimal.ONE);
		sa.minPaymentRates.put(1, BigDecimal.ONE);
		sa.minPaymentRates.put(2, BigDecimal.ONE);
		sa.minPaymentRates.put(3, BigDecimal.ONE);
		sa.minPaymentRates.put(4, BigDecimal.ONE);
		sa.minPaymentRates.put(5, BigDecimal.ONE);
		sa.minPaymentRates.put(6, BigDecimal.ONE);
		sa.minPaymentRates.put(7, BigDecimal.ONE);
		sa.minPaymentRates.put(8, BigDecimal.ONE);
		sa.minPaymentRates.put(9, BigDecimal.ONE);
		sa.overlimitQualify = true;
		sa.effectiveDate=subAcct.effectiveDate;
		sa.penalizedInterestPastDuePostCode = "IB000055";
		sa.penalizedInterestTable = "Z0001";
		sa.planPurgeDays = 30;
		sa.postponeDays = 0;
		sa.supportStmtLoan = false;
		sa.transMergeMethod = subAcct.transMergeMethod;
		sa.writeOffInd = false;
		sa.writeOffPostCode = "IB000027";
		sa.interestAccruedMethod = subAcct.interestAccruedMethod;
		sa.depositPostCode = "IB000032";
		sa.depositInternalPostCode = subAcct.depositInternalPostCode;
		sa.balTransferMap =subAcct.balTransferMap ;
		//贷款剩余本金子账户结束
		sa.subAcctCategory = subAcct.subAcctCategory;
		
		return sa;
	}
	
	/**
	 * 构造主账户
	 * @param acctount
	 * @param IntCode
	 * @return
	 */
	public Account setupAcct(Account acctount, String IntCode ) {
		
		//设置主账户参数
		Account acct = new Account();
		acct.description = acctount.description;
		acct.isLockPaymentDay = false;
		acct.businessType =acctount.businessType;
		acct.currencyCode = "156";
		acct.genAcctMethod = GenAcctMethod.N;
		acct.intFirstPeriodAdj = true;
		acct.carryType=TransformType.D;
		acct.pnitType=acctount.pnitType;
		acct.advanceType=acctount.advanceType;
		acct.isPnit=true;
		acct.effectiveDate=acctount.effectiveDate;
		
		acct.pmtDueDays=0;
		acct.blockcode = acctount.blockcode;
		acct.subAcctParam = new HashMap<String, String>();
		acct.subAcctParam.put("PAYM", acctount.subAcctParam.get("PAYM") );
		acct.subAcctParam.put("INTE", acctount.subAcctParam.get("INTE"));//默认
		acct.subAcctParam.put("LOAN", IntCode );
		acct.subAcctParam.put("LBAL", acctount.subAcctParam.get("LBAL"));
		acct.subAcctParam.put("SFEE", acctount.subAcctParam.get("SFEE"));//默认
		acct.subAcctParam.put("PNIT", acctount.subAcctParam.get("PNIT"));//默认
		acct.intSettleStartMethod = acctount.intSettleStartMethod ;
		acct.intUnit =acctount.intUnit;
		acct.intUnitMult = acctount.intUnitMult;
		acct.dIntSettleDay = 99;
		acct.intSettleFrequency = acctount.intSettleFrequency;
		acct.defaultAuthType = acctount.defaultAuthType;
		acct.paymentMethod = acctount.paymentMethod;
		
		acct.pmtGracePrd = acctount.pmtGracePrd;
		acct.delqDayInd = acctount.delqDayInd;
		acct.delqTolInd = DelqTolInd.A;
		acct.effectiveDate= acctount.effectiveDate ;
		
		acct.delqTol = BigDecimal.ZERO;
		acct.delqTolPerc = BigDecimal.ZERO;
		acct.loanFeeCalcMethod = CalcMethod.R;
		acct.feeRate = BigDecimal.valueOf(0);
		acct.paymentHier =acctount.paymentHier;
		//科目规则
		acct.sysTxnCdMapping = acctount.sysTxnCdMapping;
		
		acct.feeAmount = BigDecimal.ZERO;
		acct.crMaxbalNoStmt = BigDecimal.ZERO;
		acct.stmtMinBal = BigDecimal.ZERO;
		acct.downpmtTolInd = DownpmtTolInd.A;
		acct.downpmtTol = BigDecimal.ZERO;
		
		//内部帐规则
		acct.internalAcctPostMapping = acctount.internalAcctPostMapping;
		return acct;
	}
	
	/**
	 * 传入总金额,算出本金
	 * @param bal
	 * @param acctSeq
	 * @return
	 */
	public BigDecimal calcPrincipalAmt (BigDecimal  bal,Integer acctSeq){
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);
		Account account = retrieveAccount(cactAccount);
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> subAccts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(cactAccount.getAcctSeq())
						)
				.fetch();
		CactSubAcct loanSubAcct = null;
		for(CactSubAcct cactSubAcct : subAccts){
			if (cactSubAcct.getSubAcctType().equals("LOAN")) {
				loanSubAcct = cactSubAcct;
			}
		}
		SubAcct subAcct = retrieveSubAcct(loanSubAcct, cactAccount);
		
		LocalDate startDate = cactAccount.getLastInterestDate()==null ? new LocalDate(cactAccount.getSetupDate())
				:new LocalDate( cactAccount.getLastInterestDate() ); //起息日
		LocalDate endDate = provider7x24.getCurrentDate();
		List<InterestTable> tables = newInterestService.retrieveInterestTable(cactAccount, loanSubAcct, account, subAcct, endDate);
		List<RateCalcMethod> dailyRates = newInterestService.convertRates(Interval.D, tables.get(0));
		BigDecimal rate=(dailyRates.get(0).rate).multiply( BigDecimal.valueOf(Days.daysBetween(startDate, endDate).getDays()<=0?0:
			Days.daysBetween(startDate, endDate).getDays())) ;
		
		BigDecimal principalAmt =  bal.divide(
				(new BigDecimal(1).add( rate ) ), 2, RoundingMode.HALF_UP )
				 ;
		return principalAmt;
	}
	
	public List<RateCalcMethod>  getRates(InterestTable interest,int mul, int div){
		InterestTable clonInterTable = (InterestTable) SerializationUtils.clone(interest);
		List<RateCalcMethod> rates = new ArrayList<RateCalcMethod>();
		for(RateCalcMethod rate : clonInterTable.chargeRates){
			rate.rate = rate.rate.multiply(BigDecimal.valueOf(mul)).divide(BigDecimal.valueOf(div), 12, RoundingMode.HALF_UP);
			rates.add(rate);
		}
		return rates;
	}
	
	/**
	 * 取贷款利率
	 * @param cactAccount
	 * @return
	 */
	public BigDecimal getLoanRate(CactAccount cactAccount){
		BigDecimal rate= null ;
		Account accountParam = retrieveAccount(cactAccount);
		//取未到期本金子账户利率参数
		SubAcct subAcctParam = retrieveSubAcct(accountParam.subAcctParam.get("LOAN"), cactAccount);
		//取当前的利率参数
		InterestTable inttable =  retrieveInterestTable(subAcctParam.intTables.get(0), cactAccount);
		OrganizationInfo org = null;
		
		switch ( inttable.rateBaseInterval ){
		case D : 
			org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			rate= inttable.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays));
			break;
		case M :
			rate= inttable.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(12));
			break;
		case W :
			org = parameterCacheFacility.getParameter(OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY);
			rate=inttable.chargeRates.get(0).rate.multiply(BigDecimal.valueOf(org.annualInterestRateBenchmarkDays).divide(BigDecimal.valueOf(7)));
			break;
		default :
			rate=inttable.chargeRates.get(0).rate;
		}
		return rate;
	}
	
	public int getAccrualScale()
	{
		return accrualScale;
	}

	public int getReceivableScale()
	{
		return receivableScale;
	}

	public void setReceivableScale(int receivableScale)
	{
		this.receivableScale = receivableScale;
	}

	public int getInterestRateScale()
	{
		return interestRateScale;
	}

	public void setInterestRateScale(int interestRateScale)
	{
		this.interestRateScale = interestRateScale;
	}

	public void setAccrualScale(int accrualScale)
	{
		this.accrualScale = accrualScale;
	}

	public int getBalanceScale()
	{
		return balanceScale;
	}

	public void setBalanceScale(int balanceScale)
	{
		this.balanceScale = balanceScale;
	}
	
}