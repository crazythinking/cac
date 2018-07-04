package net.engining.pcx.cc.batch.cc6000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnPost;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * 
 * key-有延迟冲销交易的账户号
 * 
 * @author Li Yinxia
 *
 */
public class Cc6000R extends AbstractKeyBasedStreamReader<Integer, Cc6000IAccountInfo> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private SystemStatusFacility facility;

	@Override
	protected List<Integer> loadKeys() {
		QCactTxnPost q = QCactTxnPost.cactTxnPost;

		return new JPAQueryFactory(em)
				.select(q.acctSeq)
				.from(q)
				.where(q.postingFlag.eq(PostingFlag.FTD).and(q.postDate.eq(facility.getSystemStatus().businessDate)))
				.groupBy(q.acctSeq)
				.orderBy(q.acctSeq.asc())
				.fetch();
	}

	@Override
	protected Cc6000IAccountInfo loadItemByKey(Integer key) {

		Cc6000IAccountInfo info = new Cc6000IAccountInfo();

		// 入账交易
		QCactTxnPost qCactTxnPost = QCactTxnPost.cactTxnPost;
		info.setCactTxnPosts(
				new JPAQueryFactory(em)
				.select(qCactTxnPost)
				.from(qCactTxnPost)
				.where(qCactTxnPost.acctSeq.eq(key)
						.and(qCactTxnPost.postingFlag.eq(PostingFlag.FTD)
								.and(qCactTxnPost.postDate.eq(facility.getSystemStatus().businessDate))))
				.fetch());

		// 依次取子表

		info.setCactAccount(em.find(CactAccount.class, key));

		String currCd = info.getCactAccount().getCurrCd();

		// 子账户）
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		info.setCactSubAccts(
				new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(qCactSubAcct.acctSeq.eq(key).and(qCactSubAcct.currCd.eq(currCd)))
				.fetch());

		// 历史最小还款额
		QCactAgeDue qCactAgeDue = QCactAgeDue.cactAgeDue;
		info.setCactAgeDues(
				new JPAQueryFactory(em)
				.select(qCactAgeDue)
				.from(qCactAgeDue)
				.where(qCactAgeDue.acctSeq.eq(key))
				.orderBy(qCactAgeDue.graceDate.asc())
				.fetch());

		if (logger.isDebugEnabled()) {
			logger.debug("入账前数据收集：Org[" + info.getCactAccount().getOrg() + "],CurrCd["
					+ info.getCactAccount().getCurrCd() + "],AcctNo[" + info.getCactAccount().getAcctNo()
					+ "],SubAccts.size[" + info.getCactSubAccts().size() + "]");
		}
		return info;
	}
}
