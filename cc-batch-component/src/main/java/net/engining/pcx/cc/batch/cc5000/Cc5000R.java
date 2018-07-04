package net.engining.pcx.cc.batch.cc5000;

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
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactEndChangeAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pg.batch.sdk.AbstractKeyBasedReader;

/**
 * CactSubAcct对象读取
 * 
 */
@Service
@Scope("step")
public class Cc5000R extends AbstractKeyBasedReader<Integer, CactSubAcct> {
	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private SystemStatusFacility systemStatusfacility;

	@Override
	protected List<Integer> loadKeys() {

		SystemStatus systemStatus = systemStatusfacility.getSystemStatus();
		Date processDate = systemStatus.processDate;
		QCactEndChangeAcct qCactEndChangeAcct = QCactEndChangeAcct.cactEndChangeAcct;
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;

		return new JPAQueryFactory(em)
				.select(qCactEndChangeAcct.subAcctId)
				.from(qCactEndChangeAcct, qCactSubAcct)
				.where(qCactEndChangeAcct.txnDate.gt(processDate), qCactEndChangeAcct.acctSeq.eq(qCactSubAcct.acctSeq))
				.fetch();
	}

	@Override
	protected CactSubAcct loadItemByKey(Integer key) {

		return em.find(CactSubAcct.class, key);
	}
}
