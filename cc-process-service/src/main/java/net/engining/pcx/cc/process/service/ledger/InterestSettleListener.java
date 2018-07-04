package net.engining.pcx.cc.process.service.ledger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.support.InterestSettleEvent;

/**
 * 结息处理
 * @author binarier
 *
 */
@Service
public class InterestSettleListener extends AbstractInterestPenaltyListener implements ApplicationListener<InterestSettleEvent>
{
	@Autowired
	private NewComputeService newComputeService;

	@Autowired
	private NewInterestService newInterestService;

	public static Map<AgeGroupCd, SysInternalAcctActionCd> accuralMap = ImmutableMap.of(
			 AgeGroupCd.Normality, SysInternalAcctActionCd.S014,
			 AgeGroupCd.Attention, SysInternalAcctActionCd.S006,
			 AgeGroupCd.Above4M3, SysInternalAcctActionCd.S010
	);
	
	public static Map<AgeGroupCd, SysInternalAcctActionCd> reversalMap = ImmutableMap.of(
			 AgeGroupCd.Normality, SysInternalAcctActionCd.S015,
			 AgeGroupCd.Attention, SysInternalAcctActionCd.S007,
			 AgeGroupCd.Above4M3, SysInternalAcctActionCd.S011
	);

	@Override
	public void onApplicationEvent(InterestSettleEvent event)
	{
		doProcess(event.getSubAcctId(), event.getAmount(), event.getPostDate(), event.getTxnDetailSeq(), event.getTxnDetailType());
	}

	@Override
	protected Map<String, List<String>> extractInternalPostCodeMap(SubAcct subAcct)
	{
		//取利息对应配置
		return subAcct.interestPostingInternalPostCode;
	}

	@Override
	protected BigDecimal extractAccuralAmount(CactAccount cactAccount, CactSubAcct cactSubAcct, LocalDate postDate)
	{
		//取利息计提额
		if (cactSubAcct.getBusinessType() == BusinessType.ID)	
		{
			// 智能存款特殊处理，要计算出当前余额变化后的差额
			LocalDate startDate = new LocalDate(cactSubAcct.getSetupDate());

			InterestTable interestTable = newInterestService.calcAccrualInterestTable(cactSubAcct, cactAccount, startDate, postDate);
			
			BigDecimal newAccrual = newInterestService.calcInterest(
					startDate,
					postDate,
					cactSubAcct.getCurrBal(),		//这里因为是在重算计提，所以应该拿当前余额
					Arrays.asList(interestTable),
					newComputeService.getAccrualScale(),
					InterestCycleRestMethod.Daily);
			
			// 参与补提的已计提金额  = 已计提总额 - 剩余本金计提总额		
			return cactSubAcct.getIntAccrual().subtract(newAccrual);
		}
		else
		{
			//其它类型只要简单取当前所有计提额
			return cactSubAcct.getIntAccrual();
		}
	}

	@Override
	protected void updateAccuralAmount(CactSubAcct cactSubAcct, BigDecimal processed)
	{
		cactSubAcct.setIntAccrual(cactSubAcct.getIntAccrual().subtract(processed));
	}

	@Override
	protected SysInternalAcctActionCd determineSysInternalAcctActionCd(Type type, AgeGroupCd ageGroupCd)
	{
		SysInternalAcctActionCd result = null;
		switch (type)
		{
		case Accural:
			result = accuralMap.get(ageGroupCd);
			break;
		case Reversal:
			result = reversalMap.get(ageGroupCd);
			break;
		default:
			throw new IllegalArgumentException("should not be here");
		}
		return result;
	}

	@Override
	protected SysTxnCd determineLedgerSysTxnCd(Type type)
	{
		SysTxnCd result = null;
		switch (type)
		{
		case Accural:
			result = SysTxnCd.S40;
			break;
		case Reversal:
			result = SysTxnCd.S41;
			break;
		default:
			throw new IllegalArgumentException("should not be here");
		}
		return result;
	}
}
