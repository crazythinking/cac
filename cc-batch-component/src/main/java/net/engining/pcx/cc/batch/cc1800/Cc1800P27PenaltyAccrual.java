/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.ParamBaseType;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pcx.cc.process.service.impl.InternalAccountService;
import net.engining.pcx.cc.process.service.ledger.NewLedgerService;
import net.engining.pcx.cc.process.service.ledger.PenaltySettleListener;
import net.engining.pg.parameter.ParameterFacility;


/**
 * 罚息 － 计提
 * @author lichenjun
 *
 */
@Service
@StepScope
public class Cc1800P27PenaltyAccrual implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;

	@Autowired
	private ParameterFacility parameterFacility;
	
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
				CactAccount cactAccount = cc1800IAccountInfo.getCactAccount();

				for (CactSubAcct cactSubAcct : cc1800IAccountInfo.getCactSubAccts())
				{
					//如果不逾期就不收取罚息,把罚息计息和罚息计提的日期置null。
					//例如：第一次逾期，然后还清，第二次再逾期，如果第一次逾期的罚息计息日期没有清空，那么第二次逾期时的罚息起始日期就错了，应该是从宽限日开始算起。
					if(newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()) == AgeGroupCd.Normality)
					{
						cactSubAcct.setLastUpdateDate(new Date());
						cactSubAcct.setLastAccrualintepenaltyDate(null);
						continue;
					}

					SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
					Account account = newComputeService.retrieveAccount(cactAccount);
					
					LocalDate startDate;
					if(cactSubAcct.getLastAccrualintepenaltyDate() == null)
					{
						// 罚息起息日为账户上宽限日
						startDate = new LocalDate(cactAccount.getGraceDate());
					}
					else
					{
						startDate = new LocalDate(cactSubAcct.getLastAccrualintepenaltyDate()).plusDays(1);
					}
					
					if (startDate.isAfter(batchDate))
					{
						// 还没到起息日，不处理
						continue;
					}
					
					if (cactSubAcct.getLastAccrualintepenaltyDate() == null)
					{
						// 如果上次罚息日期为空，表示当前周期是第一次逾期，需要补记宽限日期间的罚息
						startDate = new LocalDate(cactAccount.getPmtDueDate());
					}
					
					//交易止付控制,中民项目控制停止计罚息
					String txnCode= "IFPSTOP" ;//交易码
					String valiCode="T";
					String blockCode=cactAccount.getBlockCode();
					if( blockCode!=null && StringUtils.containsAny(blockCode, valiCode) ){
						Boolean result = blockCodeUtils.getTransControl(txnCode, blockCode, account);
						if (result) continue;
					}
					
					
					InterestTable interestTable = parameterFacility.loadParameter(
							InterestTable.class, 
							cactSubAcct.getPenalizedInterestCode(), 
							account.intParamBaseType == ParamBaseType.Fixed ? cactAccount.getSetupDate() : batchDate.toDate());

					// 复制一份，准备修改为计提利率表
					interestTable = (InterestTable)SerializationUtils.clone(interestTable);
					interestTable.cycleBase = subAcct.interestAccruedMethod;
					interestTable.cycleBaseMult = 1;
					
					//根据罚息规则算罚息,默认使用日终余额
					BigDecimal pnitBal= newComputeService.getComputePnitBal(cactAccount, cactSubAcct ,  account );
					BigDecimal interest = newInterestService.calcInterest(startDate, batchDate.plusDays(1), pnitBal, ImmutableList.of(interestTable), newComputeService.getAccrualScale(), InterestCycleRestMethod.NA);
					
					if (interest != null)
					{
						// 确实进行了计提操作

						// 计提字段
						cactSubAcct.setIntPenaltyAccrual(
								MoreObjects.firstNonNull(cactSubAcct.getIntPenaltyAccrual(), BigDecimal.ZERO)
								.add(interest));
						cactSubAcct.setLastUpdateDate(new Date());
						cactSubAcct.setLastAccrualintepenaltyDate(batchDate.toDate());
						// 更新当前罚息代码
						cactSubAcct.setPenalizedInterestCode(interestTable.interestCode);
						
						String loanState="1";
						// 总账
						if(TransformType.D.equals(account.carryType)){
							
							//罚息计提状态判断,逾期或者非应计
							if( (Days.daysBetween(new LocalDate(cactSubAcct.getSetupDate()), batchDate).getDays() )>=90
									&&  (cactSubAcct.getSubAcctType().equals("LBAL")||  cactSubAcct.getSubAcctType().equals("INTE")  ) //罚息计提只对本金或者利息
									){
								loanState="2";
							}
							
							newLedgerService.postLedger(
									cactAccount.getAcctSeq(),
									account.sysTxnCdMapping.get(SysTxnCd.S42),
									interest.abs(),
									batchDate,
									cactAccount.getAcctSeq().toString(),
									TxnDetailType.A,
									newAgeService.calcAgeGroupBySterm( loanState ) //传入逾期标志
									);
						}else{
							newLedgerService.postLedger(
									cactAccount.getAcctSeq(),
									account.sysTxnCdMapping.get(SysTxnCd.S42),
									interest.abs(),
									batchDate,
									cactAccount.getAcctSeq().toString(),
									TxnDetailType.A);
						}

						//罚息计提内部账户入账
						if (account.internalAcctPostMapping != null)
						{
							SysInternalAcctActionCd sysInternalAcctActionCd = PenaltySettleListener.accuralMap.get(TransformType.D.equals(account.carryType)?
									newAgeService.calcAgeGroupBySterm( loanState ) : //按期结转
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
		return item;
	}
	

}
