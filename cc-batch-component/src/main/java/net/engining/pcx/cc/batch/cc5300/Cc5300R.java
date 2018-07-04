package net.engining.pcx.cc.batch.cc5300;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactInternalAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactInternalAcct;
import net.engining.pg.batch.sdk.AbstractKeyBasedReader;

/**
 * CactInternalAcct对象读取
 * 
 */
@Service
@Scope("step")
public class Cc5300R extends AbstractKeyBasedReader<String, CactInternalAcct> {
	@PersistenceContext
	protected EntityManager em;
	
	@Override
	protected List<String> loadKeys() {
		
		QCactInternalAcct q = QCactInternalAcct.cactInternalAcct;
		return new JPAQueryFactory(em).select(q.internalAcctId).from(q).fetch();
	}

	@Override
	protected CactInternalAcct loadItemByKey(String key) {
		
		return em.find(CactInternalAcct.class, key);
	}
}
