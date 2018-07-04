package net.engining.pcx.cc.batch.cc5100;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostBt;
import net.engining.pcx.cc.infrastructure.shared.model.QCactIntrnlTxnPostBt;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * CactInternalTxnPost4Batch对象读取
 * 
 */
public class Cc5100R extends AbstractKeyBasedStreamReader<Integer, CactIntrnlTxnPostBt> {
	@PersistenceContext
	protected EntityManager em;

	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	protected List<Integer> loadKeys() {

		QCactIntrnlTxnPostBt q = QCactIntrnlTxnPostBt.cactIntrnlTxnPostBt;
		return new JPAQueryFactory(em)
				.select(q.txnSeq)
				.from(q)
				.where(q.postDate.eq(batchDate).and(q.postingFlag.eq(PostingFlag4InternalAcct.FFF)))
				.orderBy(q.txnSeq.asc())
				.fetch();
	}

	@Override
	protected CactIntrnlTxnPostBt loadItemByKey(Integer key) {

		return em.find(CactIntrnlTxnPostBt.class, key);
	}
}
