package net.engining.pcx.cc.batch.cc5200;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostSum;
import net.engining.pcx.cc.infrastructure.shared.model.QCactIntrnlTxnPostSum;
import net.engining.pg.batch.sdk.AbstractKeyBasedReader;

/**
 * CactInternalTxnPostSummar对象读取
 * 
 */
@Service
@Scope("step")
public class Cc5200R extends AbstractKeyBasedReader<Integer, CactIntrnlTxnPostSum> {
	@PersistenceContext
	protected EntityManager em;
	
	@Override
	protected List<Integer> loadKeys() {
		
		QCactIntrnlTxnPostSum q = QCactIntrnlTxnPostSum.cactIntrnlTxnPostSum;
		return new JPAQueryFactory(em).select(q.seq).from(q).orderBy(q.seq.asc()).fetch();
	}

	@Override
	protected CactIntrnlTxnPostSum loadItemByKey(Integer key) {
		
		return em.find(CactIntrnlTxnPostSum.class, key);
	}
}
