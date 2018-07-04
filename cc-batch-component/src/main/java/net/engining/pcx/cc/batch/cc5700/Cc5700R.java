package net.engining.pcx.cc.batch.cc5700;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAccount;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * 单笔利率调整（为后续利率变更做准备）
 * @author tuyi
 */
public class Cc5700R extends AbstractKeyBasedStreamReader<Integer, CactAccount> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Integer> loadKeys() {
		QCactAccount qCactAccount = QCactAccount.cactAccount;
		//FIXME 写死44451，肯定不对啊
		return new JPAQueryFactory(em).select(qCactAccount.acctSeq).from(qCactAccount).where( qCactAccount.acctSeq.eq(44451)).fetch();
	}

	@Override
	protected CactAccount loadItemByKey(Integer key) {
		return em.find(CactAccount.class, key);
	}


}
