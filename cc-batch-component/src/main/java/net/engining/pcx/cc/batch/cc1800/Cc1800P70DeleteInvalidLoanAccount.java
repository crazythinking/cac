package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

/**
 * @author yangyiqi
 * 删除已还款及冲正后失效的贷款账号
 */
@Service
@Scope("step")
public class Cc1800P70DeleteInvalidLoanAccount implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos : item.getAccountList().values()) {
			
			List<Cc1800IAccountInfo> deleteAccounts = new ArrayList<Cc1800IAccountInfo>();
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos){
				boolean deleteAccountSuccessful = deleteInvalidLoanAccount(cc1800IAccountInfo);
				
				if(deleteAccountSuccessful){
					deleteAccounts.add(cc1800IAccountInfo);
				}
			}
			
			//TODO 确定是否需要从上下文中移除已删除的账号
			if(deleteAccounts.size() > 0){
				cc1800IAccountInfos.removeAll(deleteAccounts);
			}
		}
		
		return item;
	}
	
	private boolean deleteInvalidLoanAccount(Cc1800IAccountInfo cc1800IAccountInfo){
		
		if (logger.isDebugEnabled()) {
			logger.debug("删除已还款及冲正后失效的贷款账号：Org["+cc1800IAccountInfo.getCactAccount().getOrg()
					+"],AcctNo["+cc1800IAccountInfo.getCactAccount().getAcctNo()
					+"],BusinessType["+cc1800IAccountInfo.getCactAccount().getBusinessType()
					+"],CurrBal["+cc1800IAccountInfo.getCactAccount().getCurrBal()
					+"]");
		}
		
		boolean deleteAccountSuccessful = false;
		
		if(isInvalidLoanAccount(cc1800IAccountInfo)){
			deleteAccount(cc1800IAccountInfo);
			deleteAccountSuccessful = true;
		}
		
		return deleteAccountSuccessful;
	}
	
	private void deleteAccount(Cc1800IAccountInfo cc1800IAccountInfo){

		for (CactSubAcct cactSubAcct : cc1800IAccountInfo.getCactSubAccts()) {
			em.remove(cactSubAcct);
		}
		
		em.remove(cc1800IAccountInfo.getCactAccount());
		
	}
	
	private boolean isInvalidLoanAccount(Cc1800IAccountInfo cc1800IAccountInfo){
		boolean isInvalidLoanAccount = false;
		
		CactAccount cactAccount = cc1800IAccountInfo.getCactAccount();
		if(cactAccount.getBusinessType() == BusinessType.BL 
				&& cactAccount.getCurrBal().compareTo(BigDecimal.ZERO) == 0){
			
			boolean allSubAcctBalIsZero = true;
			for(CactSubAcct cactSubAcct : cc1800IAccountInfo.getCactSubAccts()){
				if(cactSubAcct.getIntPending().compareTo(BigDecimal.ZERO) != 0
						|| cactSubAcct.getIntReceivable().compareTo(BigDecimal.ZERO) != 0){
					allSubAcctBalIsZero = false;
				}
			}
			
			if(allSubAcctBalIsZero){
				isInvalidLoanAccount = true;
			}
		}
		
		return isInvalidLoanAccount;
	}

}
