package net.engining.pcx.cc.batch.cc3200;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.infrastructure.shared.model.AuthUnmatch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

/**
 * 根据条件删除AuthUnmatch中的反向交易
 * 
 * @author Bo
 * 
 */
@Service
public class Cc3200P00 implements ItemProcessor<AuthUnmatch, Object> {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Object process(AuthUnmatch item) throws Exception {
		AuthUnmatch m = em.find(AuthUnmatch.class, item.getTxnSeqId());
		em.remove(m);
		return null;
	}
}
