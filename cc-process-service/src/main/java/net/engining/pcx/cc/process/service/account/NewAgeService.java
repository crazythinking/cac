package net.engining.pcx.cc.process.service.account;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;
import net.engining.pcx.cc.process.service.support.LoanTransformEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;

@Service
public class NewAgeService
{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private NewComputeService commonCompute ;

	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private Provider7x24 provider7x24;

	/**
	 * 账龄计算
	 * 账龄增加（账单日处理）
	 * 账龄降低（还款交易、贷记调整、退货）
	 * @param model
	 * @param txnDetailSeq
	 * @param txnDetailType
	 */
	public void updateAgeCode(AcctModel model, String txnDetailSeq, TxnDetailType txnDetailType)
	{
		CactAccount cactAccount = model.getCactAccount();
		List<CactSubAcct> cactSubAccts = model.getCactSubAccts();

		LocalDate currDate = provider7x24.getCurrentDate();
		
		if (logger.isDebugEnabled()) {
			logger.debug("账龄计算-前:BlockCode["+cactAccount.getBlockCode()
					+"],AgeCd["+cactAccount.getAgeCd()
					+"]");
		}
		
		
		// TODO 目前只实现账龄拖欠金额阈值控制，拖欠百分比控制暂不实现
		// 获取账户参数，取得账户参数中账龄提升最小阈值
		Account account = commonCompute.retrieveAccount(cactAccount);
		
		// 原账龄
		String orginalAgeCd = cactAccount.getAgeCd();
		
		// 新账龄
		String newAgeCd = null;
		
		if (cactAccount.getFirstOverdueDate() != null)
		{
			int cd = (Days.daysBetween(new LocalDate(cactAccount.getFirstOverdueDate()), currDate).getDays()) / 30 + 1;
			if (cd > 9) {
				cd = 9;
			}
			
			//不允许升账龄时返回原账龄; 目前联机的情况下不允许提什账龄
			if (!provider7x24.allowRaiseAge() && cd > Integer.parseInt(orginalAgeCd))
			{
				cd = Integer.parseInt(orginalAgeCd);
			} 
			
			newAgeCd = String.valueOf(cd);
		}
	
		// 如果新账龄仍为null,则根据当前余额的数值判断账龄代码
		if (newAgeCd == null)
		{
			newAgeCd = UComputeDueAndAgeCode.AGE0;
		}
		
		// 判断新旧账龄是否相同
		if (!orginalAgeCd.equals(newAgeCd))
		{
			// 设置新账龄
			cactAccount.setAgeCd(newAgeCd);
			
			//	设置最后账龄提升日期 //TODO 这里判断账龄大小的逻辑考虑重构
			if (!newAgeCd.equals(UComputeDueAndAgeCode.AGE0) && UComputeDueAndAgeCode.AGE_CD.indexOf(newAgeCd) > UComputeDueAndAgeCode.AGE_CD.indexOf(orginalAgeCd)){
				cactAccount.setLastAgingDate(currDate.toDate());	
			}
			
			String newBlockCode = cactAccount.getBlockCode();
			//	判断原账龄是否是拖欠，如果是则remove掉原先的锁定码
			if (StringUtils.contains("123456789", orginalAgeCd))
				newBlockCode = blockCodeUtils.removeBlockCode(newBlockCode, orginalAgeCd, account);
			
			//	判断新账龄是否需要上锁定码，如果需要上，则增加锁定码
			if (StringUtils.contains("123456789", newAgeCd))
				newBlockCode = blockCodeUtils.addBlockCode(newBlockCode, newAgeCd, account);
			
			cactAccount.setBlockCode(newBlockCode);
			
			AgeGroupCd preAgeGroupCd = calcAgeGroupCd(orginalAgeCd);
			AgeGroupCd newAgeGroupCd = calcAgeGroupCd(newAgeCd);
			
			//发布形态转换事件
			for (CactSubAcct cactSubAcct : cactSubAccts)
			{
				LoanTransformEvent event = new LoanTransformEvent(this);
				event.setSubAcctId(cactSubAcct.getSubAcctId());
				event.setTxnDetailSeq(txnDetailSeq);
				event.setTxnDetailType(txnDetailType);
				event.setOrginalAgeCd(orginalAgeCd);
				event.setNewAgeCd(newAgeCd);
				event.setOrginalAgeGroupCd(preAgeGroupCd);
				event.setNewAgeGroupCd(newAgeGroupCd);
				ctx.publishEvent(event);
			}
		}
	}

	/**
	 * 计算账龄组
	 * @param ageCd
	 * @return
	 */
	public AgeGroupCd calcAgeGroupCd(String ageCd)
	{
		AgeGroupCd group;
		if (UComputeDueAndAgeCode.AGE0.equals(ageCd)) {
			group = AgeGroupCd.Normality;
		}
		else if (ageCd.compareTo("1") >= 0 && ageCd.compareTo("3") <= 0) {
			group = AgeGroupCd.Attention;
		}
		else if (ageCd.compareTo("4") == 0) {
			group = AgeGroupCd.Secondary;
		}
		else if (ageCd.compareTo("5") >= 0 && ageCd.compareTo("6") <= 0) {
			group = AgeGroupCd.Suspicious;
		}
		else if (ageCd.compareTo("7") >= 0 && ageCd.compareTo("13") <= 0) {
			group = AgeGroupCd.Loss;
		}
		else {
			group = AgeGroupCd.Above4M3;
		}
		return group;
	}
	
	/**
	 * 按期结转
	 * @param state
	 * @return
	 */
	public AgeGroupCd calcAgeGroupBySterm(String state)
	{
		AgeGroupCd group;
		if (state!=null && state.equals("1")) {
			group = AgeGroupCd.Attention;
		}
		else if (state!=null && state.equals("2")) {
			group = AgeGroupCd.Above4M3;
		}else{
			group = AgeGroupCd.Normality;
		}
		return group;
	}
	
}
