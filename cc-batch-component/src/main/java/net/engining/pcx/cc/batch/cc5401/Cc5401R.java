package net.engining.pcx.cc.batch.cc5401;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApSubjectSummary;
import net.engining.pcx.cc.infrastructure.shared.model.QApSubjectSummary;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

public class Cc5401R extends AbstractKeyBasedStreamReader<Integer, ApSubjectSummary> {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Integer> loadKeys()
	{
		QApSubjectSummary q = QApSubjectSummary.apSubjectSummary;
		return new JPAQueryFactory(em).select(q.seq).from(q).fetch();
	}

	@Override
	protected ApSubjectSummary loadItemByKey(Integer key) {
		return em.find(ApSubjectSummary.class, key);
	}

}
