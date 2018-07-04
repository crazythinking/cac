package net.engining.pcx.cc.batch.cc2100;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnHst;
import net.engining.pg.batch.sdk.KeyBasedStreamReader;

/**
 * TmTxnHst对象读取, 条件是查询当天入账的交易
 * 
 * @author Heyu.wang
 */
@Service
@Scope("step")
public class Cc2100R extends KeyBasedStreamReader<Integer, CactTxnHst> {

	@PersistenceContext
	private EntityManager em;

	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	protected List<Integer> loadKeys() {
		QCactTxnHst qCactTxnHst = QCactTxnHst.cactTxnHst;
		return new JPAQueryFactory(em)
				.select(qCactTxnHst.txnSeq)
				.from(qCactTxnHst)
				.where(qCactTxnHst.postDate.eq(batchDate))
				.orderBy(qCactTxnHst.txnSeq.asc())
				.fetch();
	}

	@Override
	protected CactTxnHst loadItemByKey(Integer key) {
		return em.find(CactTxnHst.class, key);
	}
}
