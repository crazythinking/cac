package net.engining.pcx.cc.batch.cc5000;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

/**
 * 同步批量过程中发生的联机交易的账户余额
 */
@Service
@StepScope
public class Cc5000P00 implements ItemProcessor<CactSubAcct, Object> {
	 
	@PersistenceContext
	private EntityManager em;

	@Override
	public Object process(CactSubAcct cactSubAcct) throws Exception {
		
			cactSubAcct.setEndDayBal(cactSubAcct.getCurrBal());
		    em.persist(cactSubAcct);
		    return null;
	}
}
