package net.engining.pcx.cc.batch.cc3100;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.param.model.SystemStatus;
import net.engining.pcx.cc.infrastructure.shared.model.AuthUnmatch;
import net.engining.pcx.cc.infrastructure.shared.model.QAuthUnmatch;
import net.engining.pg.batch.sdk.KeyBasedStreamReader;

/**
 * 备份前一天到当天的数据到AuthHst
 * 
 * @author Bo
 * 
 */
@Service
@Scope("step")
public class Cc3100R extends KeyBasedStreamReader<Integer, AuthUnmatch> {
	@Autowired
	private SystemStatusFacility systemStatusfacility;

	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Integer> loadKeys() {
		SystemStatus systemStatus = systemStatusfacility.getSystemStatus();
		Date lastBatchDate = systemStatus.lastProcessDate;
		Date currBatchDate = systemStatus.processDate;
		QAuthUnmatch qAuthUnmatch = QAuthUnmatch.authUnmatch;
		return new JPAQueryFactory(em)
				.select(qAuthUnmatch.txnSeqId)
				.from(qAuthUnmatch)
				.where(
						qAuthUnmatch.logBizDate.gt(lastBatchDate)
						.and(qAuthUnmatch.logBizDate.loe(currBatchDate)))
				.fetch();
	}

	@Override
	protected AuthUnmatch loadItemByKey(Integer key) {
		return em.find(AuthUnmatch.class, key);
	}
}
