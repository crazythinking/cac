/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.enums.ParamBaseType;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pg.parameter.ParameterFacility;


/**
 * 罚息 － 计息
 * @author lichenjun
 *
 */
@Service
@StepScope
public class Cc1800P26PenaltyReceivable implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;

	@Autowired
	private ParameterFacility parameterFacility;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Autowired
	private NewAgeService newAgeService;
	
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
						cactSubAcct.setLastPenalizedInterestDate(null);
						continue;
					}

					Account account = newComputeService.retrieveAccount(cactAccount);
					
					LocalDate startDate;
					if(cactSubAcct.getLastPenalizedInterestDate() == null)
					{
						// 罚息起息日为账户上宽限日
						startDate = new LocalDate(cactAccount.getGraceDate());
					}
					else
					{
						startDate = new LocalDate(cactSubAcct.getLastPenalizedInterestDate()).plusDays(1);
					}
					
					if (startDate.isAfter(batchDate))
					{
						// 还没到起息日，不处理
						continue;
					}
					
					//交易止付控制,中民项目控制停止计罚息
					String txnCode= "IFPSTOP" ;//交易码
					String valiCode="T";
					String blockCode=cactAccount.getBlockCode();
					if( blockCode!=null && StringUtils.containsAny(blockCode, valiCode) ){
						Boolean result = blockCodeUtils.getTransControl(txnCode, blockCode, account);
						if (result) continue;
					}
					
					if (cactSubAcct.getLastPenalizedInterestDate() == null)
					{
						// 如果上次罚息日期为空，表示当前周期是第一次逾期，需要补记宽限日期间的罚息
						startDate = new LocalDate(cactAccount.getPmtDueDate());
					}
					
					BigDecimal pnitBal= newComputeService.getComputePnitBal(cactAccount, cactSubAcct ,  account );
					InterestTable interestTable = parameterFacility.loadParameter(
							InterestTable.class, 
							cactSubAcct.getPenalizedInterestCode(), 
							account.intParamBaseType == ParamBaseType.Fixed ? cactAccount.getSetupDate() : batchDate.toDate());
					BigDecimal interest = newInterestService.calcInterest(startDate, batchDate.plusDays(1), pnitBal, ImmutableList.of(interestTable), newComputeService.getReceivableScale(), InterestCycleRestMethod.NA);
					
					if (interest != null)
					{
						// 确实进行了罚息操作
						
						//罚息利息改为分开记，所以这里不再将罚息累计到应收利息 
						cactSubAcct.setPenalizedAmt(cactSubAcct.getPenalizedAmt().add(interest));
						cactSubAcct.setLastUpdateDate(new Date());
						cactSubAcct.setLastPenalizedInterestDate(batchDate.toDate());
						
					}
				}		
			}
		}
		return item;
	}
	

}
