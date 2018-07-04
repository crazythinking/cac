package net.engining.pcx.cc.batch.cc1800;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;

/**
 * 账单日最小还款额处理
 * @author linwk
 *
 */
@Service
@Scope("step")
public class Cc1800P40ComputeDue implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 最小还款额及账龄计算业务组件
	 */
	@Autowired
	private UComputeDueAndAgeCode cc1800UComputeDueAndAgeCode;
	
	/**
	 * 入账通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute ;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		for (List<Cc1800IAccountInfo> Cc1800IAccountInfos : item.getAccountList().values()) {
			for (Cc1800IAccountInfo cc1800IAccountInfo : Cc1800IAccountInfos){
				computeDue(cc1800IAccountInfo);
			}
		}
		return item;
	}
	
	private Cc1800IAccountInfo computeDue(Cc1800IAccountInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("账单日最小还款额处理：Org["+item.getCactAccount().getOrg()
					+"],AcctNo["+item.getCactAccount().getAcctNo()
					+"],BusinessType["+item.getCactAccount().getBusinessType()
					+"],CurrCd["+item.getCactAccount().getCurrCd()
					+"],BatchDate["+batchDate
					+"],NextStmtDate["+item.getCactAccount().getInterestDate()
					+"]");
		}
		Account account = commonCompute.retrieveAccount(item.getCactAccount());
		if (account.businessType != BusinessType.CC && account.businessType != BusinessType.BL) {
			return item;
		}
		// 判断当天是否账单日
		if (batchDate.equals(item.getCactAccount().getInterestDate())){
			logger.debug("批量日期为账单日期,开始处理最小还款额");
			cc1800UComputeDueAndAgeCode.computeMinDue(item.getCactAccount(), item.getCactSubAccts(), item.getCactAgeDues(), batchDate);
		}
		
		logger.debug("账单日最小还款额处理Process finished! AccountNo:[" + item.getCactAccount().getAcctNo() + "] BusinessType:[" 
				+ item.getCactAccount().getBusinessType() +"],CurrCd["+item.getCactAccount().getCurrCd() + "] ");

		return item;
	}

}
