package net.engining.pcx.cc.batch.cc3200;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnStatusDef;
import net.engining.pcx.cc.infrastructure.shared.model.AuthUnmatch;
import net.engining.pcx.cc.infrastructure.shared.model.QAuthUnmatch;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * 根据条件删除AuthUnmatch中的反向交易
 * 
 * @author Bo
 * 
 */
@Service
@Scope("step")
public class Cc3200R extends AbstractKeyBasedStreamReader<Integer, AuthUnmatch> {

	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Integer> loadKeys() {
		QAuthUnmatch q = QAuthUnmatch.authUnmatch;
		return new JPAQueryFactory(em).select(q.txnSeqId).from(q).where(q.txnStatus.notIn(TxnStatusDef.N, TxnStatusDef.A)).fetch();
	}

	@Override
	protected AuthUnmatch loadItemByKey(Integer key) {
		return em.find(AuthUnmatch.class, key);
	}
}
