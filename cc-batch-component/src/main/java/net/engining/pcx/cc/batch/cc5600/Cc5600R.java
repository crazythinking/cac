package net.engining.pcx.cc.batch.cc5600;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.infrastructure.shared.model.QApGlBal;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * @author yinxia
 */
public class Cc5600R extends AbstractKeyBasedStreamReader<Integer, ApGlBal> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Integer> loadKeys() {
		QApGlBal q = QApGlBal.apGlBal;
		return new JPAQueryFactory(em).select(q.id).from(q).orderBy(q.id.asc()).fetch();
	}

	@Override
	protected ApGlBal loadItemByKey(Integer key) {
		return em.find(ApGlBal.class, key);
	}



}
