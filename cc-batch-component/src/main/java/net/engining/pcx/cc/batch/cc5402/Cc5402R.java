package net.engining.pcx.cc.batch.cc5402;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApInternalGltxn;
import net.engining.pcx.cc.infrastructure.shared.model.QApInternalGltxn;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * ApInternalGltxn对象读取，无状态Reader
 * 
 * @author hu
 */
public class Cc5402R extends AbstractKeyBasedStreamReader<Integer, ApInternalGltxn> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	@Override
	protected List<Integer> loadKeys() {
		QApInternalGltxn q = QApInternalGltxn.apInternalGltxn;
		return new JPAQueryFactory(em).select(q.seqId).from(q).where(q.txnDate.loe(batchDate)).orderBy(q.seqId.asc()).fetch();
	}

	@Override
	protected ApInternalGltxn loadItemByKey(Integer key) {
		return em.find(ApInternalGltxn.class, key);
	}

}
