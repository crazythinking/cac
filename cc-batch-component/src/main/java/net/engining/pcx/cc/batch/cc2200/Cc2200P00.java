package net.engining.pcx.cc.batch.cc2200;

import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.batch.cc1900.StmtInfoItem;
import net.engining.pcx.cc.batch.common.PrintStmtUtils;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactReprintReg;
import net.engining.pcx.cc.infrastructure.shared.model.CactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnHst;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pg.parameter.ParameterFacility;

/**
 * CMD320 – 补打账单接口文件生成
 * <p>
 * 输入：补打账单通知文件
 * <p>
 * 输出：账单汇总信息接口、账单的账单交易接口
 * <p>
 * 
 * @author heyu.wang
 * 
 */
@Service
@Scope("step")
public class Cc2200P00 implements ItemProcessor<CactReprintReg, StmtInfoItem> {

	@Autowired
	private PrintStmtUtils printStmtUtils;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private ParameterFacility unifiedParameter;
	
	@Override
	public StmtInfoItem process(CactReprintReg item) throws Exception {

		/*
		 * 根据补打账单记录，取得指定账户指定账单日的账单汇总历史表TM_STMT_HST记录
		 */
		QCactStmtHst qCactStmtHst = QCactStmtHst.cactStmtHst;
		// FIXME 查询逻辑错误，本外币账户具有相同的acctNo，acctSeq一个账户一个，不能直接相等
		Iterable<CactStmtHst> tmStmtHstIterable = new JPAQueryFactory(em)
				.select(qCactStmtHst)
				.from(qCactStmtHst)
				.where(
						qCactStmtHst.org.eq(item.getOrg()),
						qCactStmtHst.acctSeq.eq(item.getAcctNo()), 
						qCactStmtHst.stmtDate.eq(item.getStmtDate()))
				.fetch();
				
		/*
		 * 根据补打账单记录，获取指定账户指定账单日的入账交易历史表TM_TXN_HST记录
		 */
		QCactTxnHst qCactTxnHst = QCactTxnHst.cactTxnHst;
		// FIXME 查询逻辑错误，本外币账户具有相同的acctNo，acctSeq一个账户一个，不能直接相等
		Iterable<CactTxnHst> txnHstIterable = new JPAQueryFactory(em)
				.select(qCactTxnHst)
				.from(qCactTxnHst)
				.where(
						qCactTxnHst.org.eq(item.getOrg()), 
						qCactTxnHst.acctSeq.eq(item.getAcctNo()),
						qCactTxnHst.stmtDate.eq(item.getStmtDate()))
				.fetch();

		/*
		 * 生成账单交易历史记录 
		 */
		StmtInfoItem outputItem = new StmtInfoItem();
		CactTxnHst cactTxnHst = null;
		for (Iterator<CactTxnHst> iterator = txnHstIterable.iterator(); iterator.hasNext();) { // 循环账户的未出账单交易
			cactTxnHst = iterator.next();

			// 记录账单交易历史，并清理未出账单交易历史表
			// 输出到实体账单交易当期接口文件
			PostCode postCd = unifiedParameter.getParameter(PostCode.class, cactTxnHst.getPostCode());
			if (postCd.stmtInd) { // 非memo交易，需要输出账单
				outputItem.getStmttxnInterfaceItems().add(printStmtUtils.createStmttxnItem(cactTxnHst));
			}
		}

		// 生成补打账单汇总信息接口文件
		CactAccount account = null;
		CactStmtHst stmtHst = null;
		for (Iterator<CactStmtHst> iterator = tmStmtHstIterable.iterator(); iterator.hasNext();) { // 循环账户的未出账单交易
			stmtHst = iterator.next();
			account = em.find(CactAccount.class, stmtHst.getAcctSeq());
			assert account != null;
			outputItem.getStmtInterfaceItems().add(printStmtUtils.createStmtItem(stmtHst, account));
		}

		em.remove(item);
		
		return outputItem;
	}
}
