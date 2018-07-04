package net.engining.pcx.cc.batch.cc1400;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactCancelReg;
import net.engining.pcx.cc.infrastructure.shared.model.QCactCancelReg;
import net.engining.pg.batch.sdk.KeyBasedStreamReader;


/**
 * 关闭账户，要求按顺序执行，不能并发
 * 
 * @author yinxia
 * 
 */
@Service
public class Cc1400R extends KeyBasedStreamReader<Integer, CactCancelReg> {

	@PersistenceContext
	protected EntityManager em;

	@Override
	protected List<Integer> loadKeys() {
		QCactCancelReg q = QCactCancelReg.cactCancelReg;
		
		return new JPAQueryFactory(em)
				.select(q.requestSeq)
				.from(q)
				.orderBy(q.requestSeq.asc())		//要求按顺序执行，不能并发
				.fetch();
	}

	@Override
	protected CactCancelReg loadItemByKey(Integer key) {
		return em.find(CactCancelReg.class, key);
	}
}
