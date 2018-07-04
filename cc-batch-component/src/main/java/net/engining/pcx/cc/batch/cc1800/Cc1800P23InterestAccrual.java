package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pcx.cc.process.service.impl.InternalAccountService;
import net.engining.pcx.cc.process.service.ledger.InterestSettleListener;
import net.engining.pcx.cc.process.service.ledger.NewLedgerService;


/**
 * 利息 － 计提
 * @author lichenjun
 *
 */
@Service
@Scope("step")
public class Cc1800P23InterestAccrual implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo>
{
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private NewAgeService newAgeService;

	@Autowired
	private InternalAccountService internalAccountService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Autowired
	private NewLedgerService newLedgerService;
	
	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item)
	{
		// 迭代所有账户
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos)
			{
				for (CactSubAcct cactSubAcct : cc1800IAccountInfo.getCactSubAccts())
				{
					CactAccount cactAccount = cc1800IAccountInfo.getCactAccount();
					Account account = newComputeService.retrieveAccount(cactAccount);
					SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);

					//	判断锁定码定义是否需要进行计提
					if (!blockCodeUtils.getMergedIntAccuralInd(cactAccount.getBlockCode(), account))
					{
						continue;
					}
					
					LocalDate startDate;
					if(cactSubAcct.getLastAccrualInterestDate() == null)
					{
						startDate = newComputeService.calcSetupDate(cactSubAcct, cactAccount, subAcct);
					}
					else
					{
						startDate = new LocalDate(cactSubAcct.getLastAccrualInterestDate()).plusDays(1);
					}
					
					// 计提使用的利率表
					InterestTable accrualTable = newInterestService.calcAccrualInterestTable(cactSubAcct, cactAccount, startDate, batchDate.plusDays(1));
					
					if (accrualTable != null)
					{
						updateAccrual(cactSubAcct, cactAccount, subAcct, account, accrualTable, startDate);
					}
				}
			}
		}
		return item;
	}


	private void updateAccrual(CactSubAcct cactSubAcct, CactAccount cactAccount, SubAcct subAcct, Account account, InterestTable interestTable, LocalDate startDate)
	{
		if (subAcct.intWaive && cactAccount.getGraceDaysFullInd())
		{
			// 可免利息累积后,记入延迟利息,对于此种情况,不用计提。
			return;
		}

		if (cactSubAcct.getIntAccrual() == null)
		{
			cactSubAcct.setIntAccrual(BigDecimal.ZERO);
		}
		
		SysTxnCd sysTxnCd = null;
		Map<AgeGroupCd, SysInternalAcctActionCd> internalMap = null;

		BigDecimal interest;
		if (interestTable.interestCode.equals(cactSubAcct.getInterestCode()))
		{
			// 正常计提
			interest = newInterestService.calcInterest(
					startDate,
					batchDate.plusDays(1),
					cactSubAcct.getEndDayBal(),
					ImmutableList.of(interestTable),
					newComputeService.getAccrualScale(),
					InterestCycleRestMethod.NA);
			// 计提
			sysTxnCd = SysTxnCd.S40;
			internalMap = InterestSettleListener.accuralMap;
		}
		else
		{
			//表示计提利率表进行升/降档了，需要重新计算计提并且进行补提/冲减
			interest = newInterestService.calcInterest(
					newComputeService.calcSetupDate(cactSubAcct, cactAccount, subAcct),	//取建账日期
					batchDate.plusDays(1),
					cactSubAcct.getEndDayBal(),
					ImmutableList.of(interestTable),
					newComputeService.getAccrualScale(),
					InterestCycleRestMethod.NA);
			
			// 可能没起息
			if (interest != null)
			{
				// 根据绝对值的大小比较来确定是计提还是冲减
				if (interest.abs().compareTo(cactSubAcct.getIntAccrual().abs()) > 0)
				{
					// 计提
					sysTxnCd = SysTxnCd.S40;
					internalMap = InterestSettleListener.accuralMap;
				}
				else
				{
					// 冲减
					sysTxnCd = SysTxnCd.S41;
					internalMap = InterestSettleListener.reversalMap;
				}
				
				// 计算与已计提的差额，表示还需要额外计提的金额
				interest = interest.subtract(cactSubAcct.getIntAccrual());
			}
		}
		
		if (interest != null)
		{
			// 已计提
			
			// 更新计提字段
			cactSubAcct.setIntAccrual(cactSubAcct.getIntAccrual().add(interest));
			cactSubAcct.setLastUpdateDate(new Date());
			cactSubAcct.setLastAccrualInterestDate(batchDate.toDate());
			cactSubAcct.setInterestCode(interestTable.interestCode);
	
			if (interest.signum() != 0)
			{
				// 总账
				if(TransformType.D.equals(account.carryType)){
					newLedgerService.postLedger(
							cactAccount.getAcctSeq(),
							account.sysTxnCdMapping.get(sysTxnCd),
							interest.abs(),
							batchDate,
							cactAccount.getAcctSeq().toString(),
							TxnDetailType.A,
							newAgeService.calcAgeGroupBySterm( null ) //传入逾期标志
							);
				}else{
					newLedgerService.postLedger(
							cactAccount.getAcctSeq(),
							account.sysTxnCdMapping.get(sysTxnCd),
							interest.abs(),
							batchDate,
							cactAccount.getAcctSeq().toString(),
							TxnDetailType.A
							);
				}
				
				
				//利息计提内部账户入账
				if (account.internalAcctPostMapping != null)
				{
					SysInternalAcctActionCd sysInternalAcctActionCd = internalMap.get(TransformType.D.equals(account.carryType)?
							newAgeService.calcAgeGroupBySterm(null) : //按期结转
							newAgeService.calcAgeGroupCd(cactAccount.getAgeCd())
							);
					List<String> internalAcctPostCodes = account.internalAcctPostMapping.get(sysInternalAcctActionCd);
					if (internalAcctPostCodes != null)
					{
						for(String postCode2 :internalAcctPostCodes)
						{
							internalAccountService.postByCode(postCode2, interest.abs(), cactAccount.getCurrCd(), cactAccount.getAcctSeq().toString(), TxnDetailType.A, batchDate.toDate());
						}
					}
				}
			}
		}
	}
}
