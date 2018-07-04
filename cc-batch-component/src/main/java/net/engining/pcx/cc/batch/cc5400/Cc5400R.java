package net.engining.pcx.cc.batch.cc5400;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlTxn;
import net.engining.pcx.cc.infrastructure.shared.model.QApGlTxn;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * ApGltxn对象读取，无状态Reader
 * 
 * @author jiangfan
 */
public class Cc5400R extends AbstractKeyBasedStreamReader<String, ApGlTxn> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	@Override
	protected List<String> loadKeys() {
		QApGlTxn q = QApGlTxn.apGlTxn;
		return new JPAQueryFactory(em).select(q.gltSeq).from(q).where(q.postDate.loe(batchDate)).orderBy(q.gltSeq.asc()).fetch();
	}

	@Override
	protected ApGlTxn loadItemByKey(String key) {
		return em.find(ApGlTxn.class, key);
	}

}
