package net.engining.pcx.cc.batch.cc5403;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApInternalSubjectSum;
import net.engining.pcx.cc.infrastructure.shared.model.QApInternalSubjectSum;
import net.engining.pg.batch.sdk.KeyBasedStreamReader;

public class Cc5403R extends KeyBasedStreamReader<Integer, ApInternalSubjectSum> {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Integer> loadKeys()
	{
		QApInternalSubjectSum q = QApInternalSubjectSum.apInternalSubjectSum;
		return new JPAQueryFactory(em).select(q.seq).from(q).fetch();
	}

	@Override
	protected ApInternalSubjectSum loadItemByKey(Integer key) {
		return em.find(ApInternalSubjectSum.class, key);
	}

}
