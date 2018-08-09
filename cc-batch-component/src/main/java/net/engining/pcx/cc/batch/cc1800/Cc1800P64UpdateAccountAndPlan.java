/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.service.account.NewComputeService;

/**
 * 维护账户和子账户
 * @author yinxia
 * 
 */
@Service
@StepScope
public class Cc1800P64UpdateAccountAndPlan implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	@Autowired
	private NewComputeService newComputeService;
	
	
	/**
	 * 维护账户和信用计划数据
	 */
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos : item.getAccountList().values()) {
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos){
				updateAccountAndSubAcct(cc1800IAccountInfo);
			}
		}
		return item;
	}
	
	private Cc1800IAccountInfo updateAccountAndSubAcct(Cc1800IAccountInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("维护账户和信用计划数据：Org["+item.getCactAccount().getOrg()
					+"],AcctNo["+item.getCactAccount().getAcctNo()
					+"],BusinessType["+item.getCactAccount().getBusinessType()
					+"],CurrCd["+item.getCactAccount().getCurrCd()
					+"],CactSubAccts.size["+item.getCactSubAccts().size()
					+"]");
		}
		
		/*//账期是零则当前余额是零或者账期大于零则期末当期余额是零，并且应收利息是零，就删除该子账户。*/
//		for (CactSubAcct cactSubAcct : item.getCactSubAccts()) {
//            if( (cactSubAcct.getStmtHist() == 0 && cactSubAcct.getCurrBal().equals(BigDecimal.ZERO) && cactSubAcct.getIntReceivable().equals(BigDecimal.ZERO)) || 
//            	(cactSubAcct.getStmtHist() > 0 && cactSubAcct.getEndBal().equals(BigDecimal.ZERO) && cactSubAcct.getIntReceivable().equals(BigDecimal.ZERO))){
//            	//em.remove(cactSubAcct);
//            	item.getCactSubAccts().remove(cactSubAcct);
//            }
//            else{
//            	em.persist(cactSubAcct);
//            }
//
//		}
		List<CactSubAcct> removes = new ArrayList<CactSubAcct>();
		for (CactSubAcct cactSubAcct : item.getCactSubAccts()) {
			SubAcct subAcctParam = newComputeService.retrieveSubAcct(cactSubAcct, item.getCactAccount());
			if(cactSubAcct.getCurrBal().signum() == 0
					&& cactSubAcct.getTotDueAmt().signum() == 0
					&& cactSubAcct.getEndDayBal().signum() == 0
					&& cactSubAcct.getEndDayBeforeBal().signum() == 0
					&& cactSubAcct.getPenalizedAmt().signum() == 0
					&& cactSubAcct.getIntPending().signum() == 0
					&& cactSubAcct.getIntReceivable().signum() == 0
					&& cactSubAcct.getAddupAmt().signum() == 0
					&& cactSubAcct.getIntAccrual().signum() == 0
					&& cactSubAcct.getIntPenaltyAccrual().signum() == 0
					&& batchDate.after(DateUtils.addDays(cactSubAcct.getSetupDate(), subAcctParam.planPurgeDays))){
	            	em.remove(cactSubAcct);
	            	removes.add(cactSubAcct);
			}
		}
		
		item.getCactSubAccts().removeAll(removes);
		return item;
	}
}
