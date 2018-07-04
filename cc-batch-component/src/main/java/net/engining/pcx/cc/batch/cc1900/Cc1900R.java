package net.engining.pcx.cc.batch.cc1900;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnUnstmt;
import net.engining.pg.batch.sdk.AbstractKeyBasedStreamReader;

/**
 * 未出账单信息reader
 * <p>
 * 前提：账单汇总信息表记录已经更新
 * <p>
 * 读取所有账单日等于当前批量日期的账单汇总信息记录(按账户、账户类型排序)，同时从未出账单交易表中取出账单日期为当前批量日期的记录
 * <p>
 * 
 * @author Heyu.wang
 */
@Service
@Scope("step")
public class Cc1900R extends AbstractKeyBasedStreamReader<CactStmtKey, Cc1900I> {
	@PersistenceContext
	private EntityManager em;

	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	protected List<CactStmtKey> loadKeys() {
		//只要取两个字段，stmtDate是过滤条件
		QCactStmtHst qCactStmtHst = QCactStmtHst.cactStmtHst;
		List<Tuple> result = new JPAQueryFactory(em)
				.select(qCactStmtHst.acctSeq, qCactStmtHst.currCd, qCactStmtHst.stmtDate)
				.from(qCactStmtHst)
				.where(qCactStmtHst.stmtDate.eq(batchDate))
				.orderBy(qCactStmtHst.acctSeq.asc(), qCactStmtHst.currCd.asc())
				.fetch();

		List<CactStmtKey> keys = new ArrayList<CactStmtKey>();
		for (Tuple objs : result)
		keys.add( new CactStmtKey(objs.get(qCactStmtHst.acctSeq), objs.get(qCactStmtHst.currCd), objs.get(qCactStmtHst.stmtDate) ));
		return keys;
	}

	@Override
	protected Cc1900I loadItemByKey(CactStmtKey stmtHstKey) {
		Cc1900I info = new Cc1900I();
		QCactStmtHst qCactStmtHst = QCactStmtHst.cactStmtHst;
		CactStmtHst cactStmtHst = new JPAQueryFactory(em)
				.select(qCactStmtHst)
				.from(qCactStmtHst)
				.where(
						qCactStmtHst.acctSeq.eq(stmtHstKey.getAcctSeq()),
						qCactStmtHst.currCd.eq(stmtHstKey.getCurrCd()),
						qCactStmtHst.stmtDate.eq(stmtHstKey.getStmtDate()))
				.fetchOne();
		info.setStmtHst(cactStmtHst);
		
		QCactTxnUnstmt qCactTxnUnstmt = QCactTxnUnstmt.cactTxnUnstmt;
		info.setTxnUnstmts(new JPAQueryFactory(em)
				.select(qCactTxnUnstmt)
				.from(qCactTxnUnstmt)
				.where(
						qCactTxnUnstmt.acctSeq.eq(cactStmtHst.getAcctSeq())
						.and(qCactTxnUnstmt.postCurrCd.eq(cactStmtHst.getCurrCd()))
						.and(qCactTxnUnstmt.stmtDate.eq(cactStmtHst.getStmtDate())))
				.orderBy(qCactTxnUnstmt.txnSeq.asc())
				.fetch());
		
		return info;
	}

}
