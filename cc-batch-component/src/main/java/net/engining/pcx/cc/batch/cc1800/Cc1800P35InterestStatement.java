package net.engining.pcx.cc.batch.cc1800;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;

/**
 * 结息
 */
@Service
@StepScope
public class Cc1800P35InterestStatement implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	
	/**
	 * 数据持久化
	 */
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 入账通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values()) {
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos){
				interestStatement(cc1800IAccountInfo, item.getAcctModelMap().get(cc1800IAccountInfo.getCactAccount().getAcctSeq()));
			}
		}
		return item;
	}
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;

	private Cc1800IAccountInfo interestStatement(Cc1800IAccountInfo item, AcctModel model) {
		
		LocalDate businessDate = new LocalDate(systemStatusFacility.getSystemStatus().businessDate);

		CactAccount cactAccount = model.getCactAccount();

		//如果是一次性授信贷款、借记活期、借记定期、循环信用，则继续执行
		BusinessType type = item.getCactAccount().getBusinessType();
		if(type == BusinessType.BL 
				|| type == BusinessType.PI 
				|| type == BusinessType.FT
				|| type == BusinessType.CC
				){
			
			//结息日
			Date interestDate = cactAccount.getInterestDate();
			
			Account acctParam = commonCompute.retrieveAccount(cactAccount);
			//	CactAccount account = item.getCactAccount();
			//如果当天是结息日，则结息，结息结束后，更新下一结息日
			
			if(new LocalDate(interestDate).isEqual(batchDate))
			{
				if(item.getCactAccount().getBusinessType() == BusinessType.PI)
				{
					newInterestService.settleInterest(model, model, businessDate, item.getCactAccount().getAcctSeq().toString(), TxnDetailType.A);
				}
				else
				{
					newInterestService.settleInterest(model, model, batchDate, item.getCactAccount().getAcctSeq().toString(), TxnDetailType.A);
					newInterestService.settlePenalty(model, model, batchDate, item.getCactAccount().getAcctSeq().toString(), TxnDetailType.A);
				}
				//存款类和贷款类都是在结息日结息
				//存款类的总期数不等于-1并且当前期数和总期数不相等时,更新当前期数。总期数等于-1或者当前期数和总期数不相等时更新结息日。
				//贷款类的期数更新放在账单汇总处理的最后完成。
				//更新结息日和上一结息日，注意：此处只做存款类的结息日更新，贷款类的结息日更新需要在账单汇总处理的最后完成，因为之前需要按照结息日计算当期的最后还款日和宽限日。
				if (type == BusinessType.PI || type == BusinessType.FT) {
					if(-1 != item.getCactAccount().getTotalLoanPeriod()
							&& item.getCactAccount().getCurrentLoanPeriod().intValue() != item.getCactAccount().getTotalLoanPeriod()){
						item.getCactAccount().setCurrentLoanPeriod(item.getCactAccount().getCurrentLoanPeriod() + 1);
					}
					if(-1 == item.getCactAccount().getTotalLoanPeriod()
							||item.getCactAccount().getCurrentLoanPeriod().intValue() != item.getCactAccount().getTotalLoanPeriod()) {
						item.getCactAccount().setLastInterestDate(interestDate);
						item.getCactAccount().setInterestDate(commonCompute.getNextInterstDate(cactAccount, interestDate, acctParam, item.getCactAccount().getBillingCycle()));
					} 
				}
			}
		}
		return item;
	}
}
