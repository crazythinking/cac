package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pg.parameter.ParameterFacility;

 
/**
 * @author daniel
 *对贷款已结清的贷款，上锁定码
 */
@Service
@Scope("step")
public class Cc1800P77AddEndBlockCode implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	/**
	 * 维护账户和信用计划数据
	 */
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos : item.getAccountList().values()) {
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos){
				updateBlockCode(cc1800IAccountInfo);
			}
		}
		return item;
	}
	
	private Cc1800IAccountInfo updateBlockCode(Cc1800IAccountInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("贷款结清，更新blockcode=P Org["+item.getCactAccount().getOrg()
					+"],AcctNo["+item.getCactAccount().getAcctNo()
					+"],BusinessType["+item.getCactAccount().getBusinessType()
					+"],CurrCd["+item.getCactAccount().getCurrCd()
					+"],CactSubAccts.size["+item.getCactSubAccts().size()
					+"]");
		}
		
		BigDecimal subAmtSum = BigDecimal.ZERO; 
		for (CactSubAcct cactSubAcct : item.getCactSubAccts()) {
				subAmtSum = subAmtSum.add(cactSubAcct.getIntPending()
						.add(cactSubAcct.getIntReceivable())
						.add(cactSubAcct.getPenalizedAmt()));
		}
		if(subAmtSum.compareTo(BigDecimal.ZERO) == 0){
			
			item.getCactAccount().setBlockCode("P");
		}
		
		return item;
	}
}
