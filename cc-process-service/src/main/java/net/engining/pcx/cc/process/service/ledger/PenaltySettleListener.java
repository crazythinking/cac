package net.engining.pcx.cc.process.service.ledger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.service.support.PenaltySettleEvent;

/**
 * 结罚息处理
 * @author binarier
 *
 */
@Service
public class PenaltySettleListener extends AbstractInterestPenaltyListener implements ApplicationListener<PenaltySettleEvent>
{
	public static Map<AgeGroupCd, SysInternalAcctActionCd> accuralMap = ImmutableMap.of(
			 AgeGroupCd.Attention, SysInternalAcctActionCd.S008,
			 AgeGroupCd.Above4M3, SysInternalAcctActionCd.S012
	);
	
	private Map<AgeGroupCd, SysInternalAcctActionCd> reversalMap = ImmutableMap.of(
			 AgeGroupCd.Attention, SysInternalAcctActionCd.S009,
			 AgeGroupCd.Above4M3, SysInternalAcctActionCd.S013
	);

	@Override
	public void onApplicationEvent(PenaltySettleEvent event)
	{
		doProcess(event.getSubAcctId(), event.getAmount(), event.getPostDate(), event.getTxnDetailSeq(), event.getTxnDetailType());
	}

	@Override
	protected Map<String, List<String>> extractInternalPostCodeMap(SubAcct subAcct)
	{
		//取利息对应配置
		return subAcct.interestPenaltyPostingInternalPostCode;
	}

	@Override
	protected BigDecimal extractAccuralAmount(CactAccount cactAccount, CactSubAcct cactSubAcct, LocalDate postDate)
	{
		//取利息计提额
		return cactSubAcct.getIntPenaltyAccrual();
	}

	@Override
	protected void updateAccuralAmount(CactSubAcct cactSubAcct, BigDecimal processed)
	{
		cactSubAcct.setIntPenaltyAccrual(cactSubAcct.getIntPenaltyAccrual().subtract(processed));
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
			result = SysTxnCd.S42;
			break;
		case Reversal:
			result = SysTxnCd.S43;
			break;
		default:
			throw new IllegalArgumentException("should not be here");
		}
		return result;
	}
}
