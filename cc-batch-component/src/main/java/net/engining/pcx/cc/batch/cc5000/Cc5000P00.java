package net.engining.pcx.cc.batch.cc5000;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 同步批量过程中发生的联机交易的账户余额
 */
@Service
@Scope("step")
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
