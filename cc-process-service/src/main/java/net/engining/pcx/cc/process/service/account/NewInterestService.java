package net.engining.pcx.cc.process.service.account;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.process.service.support.CommonLogicUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.gm.param.model.IntTaxRate;
import net.engining.gm.param.model.OrganizationInfo;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.RateCalcMethod;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.AccrualInterestType;
import net.engining.pcx.cc.param.model.enums.ParamBaseType;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;
import net.engining.pcx.cc.process.service.support.InterestSettleEvent;
import net.engining.pcx.cc.process.service.support.PenaltySettleEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;

@Service
public class NewInterestService
{
	/**
	 * 利率周期计算时不满整周期时的剩余日期处理方式
	 * @author binarier
	 *
	 */
	public enum InterestCycleRestMethod {
		/**
		 * 不计息 
		 */
		NA,
		/**
		 * 递归处理，即重新靠档
		 */
		Recursive,
		/**
		 * 转换成日息处理
		 */
		Daily
	}
	
	@Autowired
	private NewComputeService newComputeService;

	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Autowired
	private ParameterFacility parameterFacility;
	
	@Autowired
	private UComputeDueAndAgeCode uComputeDueAndAgeCode;
	
	@Autowired
	private NewPostService newPostService;
	
	@Autowired
	private ApplicationContext ctx;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	/**
	 * 换算表格（B表示基准年天数，数组中用0表示）
	 *            1       7      B/12    B
	 *            D       W       M      Y
	 * 1     D   1/1     1/7     12/B   1/B
	 * 7     W   7/1     1/1     84/B   7/B
	 * B/12  M   B/12    B/84    1/1    1/12
	 * B     Y   B/1     B/7     12/1    1/1
	 */
	private static final int[][][] intervalConvertTable = new int[][][]{
		new int[][]{new int[]{1,1},  new int[]{1,7},  new int[]{12,0}, new int[]{1,0}},
		new int[][]{new int[]{7,1},  new int[]{1,1},  new int[]{84,0}, new int[]{7,0}},
		new int[][]{new int[]{0,12}, new int[]{0,84}, new int[]{1,1},  new int[]{1,12}},
		new int[][]{new int[]{0,1},  new int[]{0,7},  new int[]{12,1}, new int[]{1,1}}
	};
	
	public NewInterestService()
	{
		//一次性的预防措施，因为要用这个ordinal来做数组索引
		checkArgument(Interval.D.ordinal() == 0);
		checkArgument(Interval.W.ordinal() == 1);
		checkArgument(Interval.M.ordinal() == 2);
		checkArgument(Interval.Y.ordinal() == 3);
	}

	

	/**
	 * 取利率表对应的计息周期。由于不希望把joda-time扩展到接口上，所以该方法不写在 {@link InterestTable} 类中。
	 * @param table
	 * @return
	 */
	private Period calcCyclePeriod(InterestTable table)
	{
		switch (table.cycleBase)
		{
		case D:
			return Period.days(table.cycleBaseMult);
		case W:
			return Period.weeks(table.cycleBaseMult);
		case M:
			return Period.months(table.cycleBaseMult);
		case Y:
			return Period.years(table.cycleBaseMult);
		default:
			throw new IllegalArgumentException("should not be here");
		}
	}
	
	/**
	 * 计算利息(可指定靠档金额baseAmount)
	 * @param startDate
	 * @param endDate
	 * @param principalAmount
	 * @param interestTables
	 * @param scale
	 * @param restMethod
	 * @param baseAmount
	 * @return
	 */
    public BigDecimal calcInterest(LocalDate startDate, LocalDate endDate, BigDecimal principalAmount, List<InterestTable> interestTables, int scale, InterestCycleRestMethod restMethod, BigDecimal baseAmount)
    {
		BigDecimal interestAmount = BigDecimal.ZERO;
		//记录是否计息成功
		boolean hasProcessed = false;

		checkArgument(startDate.compareTo(endDate) <= 0, "利息计算的startDate不可大于endDate");
		checkArgument(!interestTables.isEmpty(), "利息计算的interestTables不可为空");
		checkArgument(restMethod != InterestCycleRestMethod.Recursive, "暂不支持递归靠档");

		// 处理剩余的周期进行靠档
		// 确定利率表
		InterestTable targetTable = fitInterestTable(startDate, endDate, interestTables);
		if (targetTable != null)	//有一个都靠不了档的情况，这样不计息
		{
			//靠档的计息周期
			Period cyclePeriod = calcCyclePeriod(targetTable);
			// 结束日期在下一周期起始日期或之后（不在之前），表示当前满足整周期  [startDate, endDate)
			while (!endDate.isBefore(startDate.plus(cyclePeriod)))
			{
				hasProcessed = true;
				// 先转换成计息周期对应的利率
				List<RateCalcMethod> cycleRates = convertRates(targetTable.cycleBase, targetTable);
	
				// 当前周期利息
				BigDecimal cycleInterest = newComputeService.calcTieredAmount(targetTable.tierInd, cycleRates, principalAmount, baseAmount)
						.multiply(BigDecimal.valueOf(targetTable.cycleBaseMult))
						.setScale(scale, RoundingMode.HALF_UP);
	
				interestAmount = interestAmount.add(cycleInterest);

				startDate = startDate.plus(cyclePeriod);
			}
			
			if (startDate.isBefore(endDate) && restMethod == InterestCycleRestMethod.Daily)
			{
				hasProcessed = true;
				// 最后剩余的天数按日息来处理
				List<RateCalcMethod> dailyRates = convertRates(Interval.D, targetTable);
				//不满整计息周期的剩余天数,乘上日利率得出利息
				interestAmount = interestAmount.add(
						newComputeService.calcTieredAmount(targetTable.tierInd, dailyRates, principalAmount, baseAmount)
							.multiply(BigDecimal.valueOf(Days.daysBetween(startDate, endDate).getDays()))
							.setScale(scale, RoundingMode.HALF_UP)
						);	// endDate表示闭区间，所以直接是这个days的值
			}
		}
		
		return hasProcessed ? interestAmount : null;
	}
    
    /**
     * 计算利息
     * @param startDate
     * @param endDate
     * @param principalAmount
     * @param interestTables
     * @param scale
     * @param restMethod
     * @return
     */
    public BigDecimal calcInterest(LocalDate startDate, LocalDate endDate, BigDecimal principalAmount, List<InterestTable> interestTables, int scale, InterestCycleRestMethod restMethod)
    {
    	return calcInterest(startDate, endDate, principalAmount, interestTables, scale, restMethod, principalAmount);
    }
    
    public List<RateCalcMethod> convertRates(Interval targetInterval, InterestTable interestTable)
	{
    	if (targetInterval == interestTable.rateBaseInterval)
    	{
    		//不用转换
    		return interestTable.chargeRates;
    	}
    	
    	//进行利率转换
		List<RateCalcMethod> result = new ArrayList<RateCalcMethod>();
		
		int rate[] = intervalConvertTable[targetInterval.ordinal()][interestTable.rateBaseInterval.ordinal()];
		
		//TODO 尝试使用double计算
		BigDecimal mul = BigDecimal.valueOf(rate[0] != 0 ? rate[0]: parameterFacility.loadUniqueParameter(OrganizationInfo.class).annualInterestRateBenchmarkDays);
		BigDecimal div = BigDecimal.valueOf(rate[1] != 0 ? rate[1]: parameterFacility.loadUniqueParameter(OrganizationInfo.class).annualInterestRateBenchmarkDays);
		
		for (RateCalcMethod rcm : interestTable.chargeRates)
		{
			RateCalcMethod newRate = new RateCalcMethod();
			//先乘再除
			newRate.rate = rcm.rate.multiply(mul).divide(div, 10, RoundingMode.HALF_UP);
			newRate.rateBase = rcm.rateBase;
			newRate.rateCeil = rcm.rateCeil;
			result.add(newRate);
		}
		return result;
	}

	/**
	 * 结息
	 * <ol>
	 *   <li>根据锁定码设置未入账利息减免标志。</li>
	 *   <li>判断是否循环信用账户并且全额已还款，如果是，不免息就收取应收利息。如果否，不免息就收取应收利息和未决利息。</li>
	 * </ol>
	 */
	public List<CactTxnPost> settleInterest(AcctModel settleAcctModel, AcctModel targetModel, LocalDate postDate, String txnDetailSeq, TxnDetailType txnDetailType)
	{
		CactAccount cactAccount = settleAcctModel.getCactAccount();
		List<CactSubAcct> cactSubAccts = settleAcctModel.getCactSubAccts();
		
		// 获取账户参数
		Account account = newComputeService.retrieveAccount(cactAccount);
		//	锁定码指示是否对未入帐利息进行减免
		Boolean intWaiveInd = blockCodeUtils.getMergedIntWaiveInd(cactAccount.getBlockCode(), account);
		
		//入账时可能新建子账户，所以要复制一个列表
		for(CactSubAcct cactSubAcct : Lists.newArrayList(cactSubAccts))
		{
			SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
			
			//先处理利息
			BigDecimal intAmount = cactSubAcct.getIntReceivable();
			
			if (!(cactAccount.getBusinessType() == BusinessType.CC && cactAccount.getGraceDaysFullInd()))
			{
				//循环信用账户特殊处理
				intAmount = intAmount.add(cactSubAcct.getIntPending());
			}
			intAmount = intAmount.setScale(2, RoundingMode.HALF_UP);

			// 判断blockcode指示是否免息，如果不免息，则进行利息入账处理
			if (!intWaiveInd && intAmount.signum() != 0)
			{
				//利息入账
				doPost(targetModel, subAcct.interestPostCode, intAmount.abs(), postDate, txnDetailSeq, txnDetailType, 0);
				//利息入账事件
				InterestSettleEvent event = new InterestSettleEvent(this);
				event.setSubAcctId(cactSubAcct.getSubAcctId());
				event.setAmount(intAmount.abs());
				event.setPostDate(postDate);
				event.setTxnDetailSeq(txnDetailSeq);
				event.setTxnDetailType(txnDetailType);
				ctx.publishEvent(event);
				
				//利息税
				BigDecimal intTaxAmt = BigDecimal.ZERO;
				
				//利息是贷方金额的利息，并且利息入账成功就代扣利息税
				if (Boolean.TRUE.equals(account.withHoldingInt) && intAmount.signum() < 0)
				{
					IntTaxRate intTaxRate = parameterFacility.loadParameter(IntTaxRate.class, cactAccount.getCurrCd());
					//两级控制都是才收，这里不应该这么设计，以后再改吧....
					if (intTaxRate.withHoldingInt)
					{
						intTaxAmt = intAmount.abs().multiply(intTaxRate.taxRt).setScale(2, RoundingMode.HALF_UP);
						if (intTaxAmt.signum() != 0)
						{
							doPost(targetModel, account.intTaxPostCode, intTaxAmt, postDate, txnDetailSeq, txnDetailType, 0);
						}
					}
				}
			}

			//循环信用账户在已全额还款的前提下，只减免非当期交易产生的未决利息。
			//当期交易产生的未决利息是否免息的判断，应该在宽限日的时候看是否已全额还款。
			if (cactAccount.getBusinessType() != BusinessType.CC || cactSubAcct.getStmtHist() > 0)
			{
				cactSubAcct.setIntPending(BigDecimal.ZERO);
			}
			// 应收利息清0
			cactSubAcct.setIntReceivable(BigDecimal.ZERO);
			// 计提利息清0
//			cactSubAcct.setIntAccrual(BigDecimal.ZERO);//由事件处理程序负责
			// 积数清0
			cactSubAcct.setAddupAmt(BigDecimal.ZERO);
		}
		return null;
	}

	public void settlePenalty(AcctModel settleAcctModel, AcctModel targetModel, LocalDate postDate, String txnDetailSeq, TxnDetailType txnDetailType)
	{
		if (provider7x24.shouldDeferPenaltySettle())
		{
			return;
		}
		
		CactAccount cactAccount = settleAcctModel.getCactAccount();
		List<CactSubAcct> cactSubAccts = settleAcctModel.getCactSubAccts();
		
		//入账时可能新建子账户，所以要复制一个列表
		for(CactSubAcct cactSubAcct : Lists.newArrayList(cactSubAccts))
		{
			SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);

			//处理罚息
			BigDecimal penalizedAmount = cactSubAcct.getPenalizedAmt();
			penalizedAmount = penalizedAmount.setScale(newComputeService.getBalanceScale(), RoundingMode.HALF_UP);
			if (penalizedAmount.signum() != 0)
			{
				doPost(targetModel, subAcct.penalizedInterestPastDuePostCode, penalizedAmount.abs(), postDate, txnDetailSeq, txnDetailType, cactSubAcct.getStmtHist());
	
				//罚息入账事件
				PenaltySettleEvent event = new PenaltySettleEvent(this);
				event.setSubAcctId(cactSubAcct.getSubAcctId());
				event.setAmount(penalizedAmount.abs());
				event.setTxnDetailSeq(txnDetailSeq);
				event.setTxnDetailType(txnDetailType);
				event.setPostDate(postDate);
				ctx.publishEvent(event);
	
				//入账成功后，追加罚息金额到指定期数的最小还款额
				uComputeDueAndAgeCode.additionalMinDue(cactAccount, settleAcctModel.getCactAgeDues(), cactAccount.getCurrentLoanPeriod() - cactSubAcct.getStmtHist() + 1, penalizedAmount);

				// 罚息清0
				cactSubAcct.setPenalizedAmt(BigDecimal.ZERO);
			}
			
		}		
	}
	
	protected void doPost(AcctModel model, String postCodeId, BigDecimal amount, LocalDate postDate, String txnDetailSeq, TxnDetailType txnDetailType, Integer stmtHist)
	{

		CactAccount cactAccount = model.getCactAccount();
		
		// 根据交易码，查找交易码对象
		PostCode postCode = parameterFacility.getParameter(PostCode.class, postCodeId);

		Date now = new Date();
		PostDetail detail = CommonLogicUtils.setupPostDetail4PostTxnTypeDef(amount, cactAccount, postCode.postCode, now, PostTxnTypeDef.M);
		detail.setTxnDetailSeq(txnDetailSeq);
		detail.setTxnDetailType(txnDetailType);
		
		newPostService.postToAccount(model, postDate, detail, true, stmtHist);
	}

	/**
	 * 按起始/结束日期计算利率表靠档
	 * @param startDate
	 * @param endDate
	 * @param tables
	 * @return
	 */
	public InterestTable fitInterestTable(final LocalDate startDate, LocalDate endDate, List<InterestTable> tables)
	{
		InterestTable result = null;
		LocalDate maxDate = startDate;
		for (InterestTable interestTable : tables)
		{
			LocalDate cycleDate = startDate.plus(calcCyclePeriod(interestTable)); // 以startDate为基础的计息周期结束的日期
			if (cycleDate.isAfter(maxDate) && !endDate.isBefore(cycleDate))		// [startDate, cycleDate) 为当前利率表对应的周期，endDate在整个周期后即为靠中。 
			{
				maxDate = cycleDate;
				result = interestTable;
			}
		}
		return result;
	}
	
	/**
	 * 计算计提时使用的利率表，最终返回的利率表为修改过计提周期的深复制对象
	 * @param cactSubAcct
	 * @param cactAccount
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public InterestTable calcAccrualInterestTable(CactSubAcct cactSubAcct, CactAccount cactAccount, final LocalDate startDate, LocalDate endDate)
	{
		Account account = newComputeService.retrieveAccount(cactAccount);
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);

		//原则上来说，需要按起始日期判断周期长短，虽然不太可能出现比如30天和1个月比的情况，所以这里把startDate带入
		Ordering<InterestTable> interestTableOrdering = new Ordering<InterestTable>()
		{
			@Override
			public int compare(@Nullable InterestTable left, @Nullable InterestTable right)
			{
				return startDate.plus(calcCyclePeriod(left)).compareTo(startDate.plus(calcCyclePeriod(right)));
			}
		};
		
		//准备利率表列表
		List<InterestTable> tables = retrieveInterestTable(cactAccount, cactSubAcct, account, subAcct, endDate);

		InterestTable result = null;
		// 出于维护考虑，默认使用最长周期
		switch (MoreObjects.firstNonNull(account.accrualInterestType, AccrualInterestType.L))
		{
			case L : 
				result = interestTableOrdering.max(tables);
				break;
			case S :
				result = interestTableOrdering.min(tables);
				break;
			case C : 
				{
					result = fitInterestTable(startDate, endDate, tables);
				}
				break;
			default:
				throw new IllegalArgumentException("should not be here");
		}
		
		if (result != null)
		{
			result = SerializationUtils.clone(result);
			result.cycleBase = subAcct.interestAccruedMethod;
			result.cycleBaseMult = 1;
		}
		return result;
	}
	
	public List<InterestTable> retrieveInterestTable(Account account, SubAcct subAcct, LocalDate startDate, LocalDate currentDate)
	{
		List<InterestTable> tables = new ArrayList<InterestTable>();
		Date effectiveDate;
		// 出于维护考虑，默认使用浮动利率
		switch (MoreObjects.firstNonNull(account.intParamBaseType, ParamBaseType.Float))
		{
		case Fixed:
			effectiveDate = startDate.toDate();
			break;
		case Float:
			effectiveDate = currentDate.minusDays(1).toDate();
			break;
		default:
			throw new IllegalArgumentException("should not be here");
		}
		for (String interest : subAcct.intTables)
		{
			tables.add(parameterFacility.loadParameter(InterestTable.class, interest, effectiveDate));
		}
		return tables;
	}

	public List<InterestTable> retrieveInterestTable(CactAccount cactAccount, CactSubAcct cactSubAcct, Account account,
													 SubAcct subAcct, LocalDate currentDate)
	{
		return retrieveInterestTable(account, subAcct, new LocalDate(cactAccount.getSetupDate()), currentDate);
	}

	/**
	 * 按子账户取指定日期的生效利率表，一般作为联机时的简捷调用方法
	 * @param subAcctId
	 * @param currentDate
	 * @return
	 */
	public List<InterestTable> retrieveInterestTable(int subAcctId, LocalDate currentDate)
	{
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, subAcctId);
		CactAccount cactAccount = em.find(CactAccount.class, cactSubAcct.getAcctSeq());
		Account account = newComputeService.retrieveAccount(cactAccount);
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
		return retrieveInterestTable(cactAccount, cactSubAcct, account, subAcct, currentDate);
	}

	public BigDecimal trialCalculateInterest(int subAcctId, BigDecimal principalAmount, final LocalDate startDate,
											 LocalDate endDate, int scale, boolean compoundInterest,
											 InterestCycleRestMethod restMethod)
	{
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, subAcctId);
		CactAccount cactAccount = em.find(CactAccount.class, cactSubAcct.getAcctSeq());
		Account account = newComputeService.retrieveAccount(cactAccount);
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
		
		return trialCalculateInterest(account, subAcct, principalAmount, startDate, endDate, scale, compoundInterest, restMethod);
	}
	/**
	 * 通用试算方法
	 * @param account 账户参数
	 * @param subAcct 子账户参数
	 * @param principalAmount 本金额
	 * @param startDate 计算起始日期
	 * @param endDate 计算结束日期（不含）
	 * @param scale 计算中间结果精度
	 * @param compoundInterest 是否在每个最长结息周期内将利息滚入本金
	 * @param restMethod 最后结息周期不满足时的处理方式
	 * @return
	 */
	public BigDecimal trialCalculateInterest(Account account, SubAcct subAcct, BigDecimal principalAmount,
											 final LocalDate startDate, LocalDate endDate, int scale,
											 boolean compoundInterest, InterestCycleRestMethod restMethod)
	{
		List<InterestTable> tables = retrieveInterestTable(account, subAcct, startDate, endDate);
		
		// 由于是试算，需要先按最长周期进行处理
		
		//原则上来说，需要按起始日期判断周期长短，虽然不太可能出现比如30天和1个月比的情况，所以这里把startDate带入
		Ordering<InterestTable> order = new Ordering<InterestTable>()
		{
			@Override
			public int compare(@Nullable InterestTable left, @Nullable InterestTable right)
			{
				return startDate.plus(calcCyclePeriod(left)).compareTo(startDate.plus(calcCyclePeriod(right)));
			}
		};
		
		// 先按最长周期处理
		// 取最长周期
		InterestTable maxTable = order.max(tables);
		Period cyclePeriod = calcCyclePeriod(maxTable);
		// 先转换成计息周期对应的利率
		List<RateCalcMethod> cycleRates = convertRates(maxTable.cycleBase, maxTable);
		LocalDate currentStartDate = startDate;
		BigDecimal interestAmount = BigDecimal.ZERO;
		// 结束日期在下一周期起始日期或之后（不在之前），表示当前满足整周期  [startDate, endDate)
		while (!endDate.isBefore(currentStartDate.plus(cyclePeriod)))
		{
			// 当前周期利息
			BigDecimal cycleInterest = newComputeService.calcTieredAmount(maxTable.tierInd, cycleRates, principalAmount, principalAmount)
					.multiply(BigDecimal.valueOf(maxTable.cycleBaseMult))
					.setScale(scale, RoundingMode.HALF_UP);

			interestAmount = interestAmount.add(cycleInterest);
			if (compoundInterest)
			{
				// 如果利息为了试算，要在公式中滚入本金
				principalAmount = principalAmount.add(cycleInterest);
			}
			currentStartDate = currentStartDate.plus(cyclePeriod);
		}

		//如果还有，则继续按正常计算方式计算
		if (startDate.isBefore(endDate))
		{
			BigDecimal rest = calcInterest(currentStartDate, endDate, principalAmount, tables, scale, restMethod);
			if (rest != null) {
				interestAmount = interestAmount.add(rest);
			}
		}
		
		return interestAmount;
	}


	
}
