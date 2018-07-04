package net.engining.pcx.cc.batch.cc2200;

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
import net.engining.pcx.cc.infrastructure.shared.model.CactReprintReg;
import net.engining.pcx.cc.infrastructure.shared.model.QCactReprintReg;
import net.engining.pg.batch.sdk.KeyBasedStreamReader;

/**
 * TmReprintReg对象读取
 * 
 * @author Heyu.wang
 */
@Service
@Scope("step")
public class Cc2200R extends KeyBasedStreamReader<Integer, CactReprintReg> {
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Integer> loadKeys() {
		SystemStatus systemStatus = systemStatusFacility.getSystemStatus();
		Date lastBatchDate = systemStatus.lastProcessDate;
		Date currBatchDate = systemStatus.processDate;
		QCactReprintReg qCactReprintReg = QCactReprintReg.cactReprintReg;
		
		// 上次跑批日期(不含) 至跑批日期(含)
		return new JPAQueryFactory(em)
				.select(qCactReprintReg.reprintSeq)
				.from(qCactReprintReg)
				.where(
						qCactReprintReg.txnDate.after(lastBatchDate)
						.and(qCactReprintReg.txnDate.loe(currBatchDate)))
				.orderBy(qCactReprintReg.reprintSeq.asc())
				.fetch();
		
	}

	@Override
	protected CactReprintReg loadItemByKey(Integer key) {
		return em.find(CactReprintReg.class, key);
	}

}
