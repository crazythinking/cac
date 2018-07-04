package net.engining.pcx.cc.process.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalTxnPostHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostOl;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.infrastructure.shared.model.QCactInternalTxnPostHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactIntrnlTxnPostOl;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.QCactTxnPost;
import net.engining.pcx.cc.process.service.TxnPostQueryService;

@Service
public class TxnPostQueryServiceImpl implements TxnPostQueryService {
	
	@PersistenceContext
	private EntityManager em;
	

	//TODO 好像多查了
	@Override
	public List<CactTxnPost> getTxnPostsByOnlineTxnSeq(String onlineTxnSeq) {
		QCactTxnPost qTxnPost = QCactTxnPost.cactTxnPost;
		
		List<CactTxnPost> txnPost = new JPAQueryFactory(em)
				.select(qTxnPost)
				.from(qTxnPost)
				.where(
						qTxnPost.txnDetailSeq.eq(onlineTxnSeq)
						.and(qTxnPost.txnDetailType.eq(TxnDetailType.O))
						)
				.fetch();
		
		QCactTxnHst qTxnHst = QCactTxnHst.cactTxnHst;
		List<CactTxnHst> txnHst = new JPAQueryFactory(em)
				.select(qTxnHst)
				.from(qTxnHst)
				.where(
						qTxnHst.txnDetailSeq.eq(onlineTxnSeq)
						.and(qTxnHst.txnDetailType.eq(TxnDetailType.O))
						)
				.fetch();
		
		List<CactTxnPost> txnPostList = new ArrayList<CactTxnPost>();
		
		for(CactTxnHst cactTxnHst : txnHst){
			CactTxnPost post = new CactTxnPost();
			BeanUtils.copyProperties(cactTxnHst, post);
			txnPostList.add(post);
		}
		txnPost.addAll(txnPostList);
		return txnPost;
	}
	
	@Override
	public List<CactIntrnlTxnPostOl> getInternalTxnPost4OnlineByOnlineTxnSeq(String onlineTxnSeq){
		QCactIntrnlTxnPostOl qTxnPost = QCactIntrnlTxnPostOl.cactIntrnlTxnPostOl;
		List<CactIntrnlTxnPostOl> txnPost = new JPAQueryFactory(em)
				.select(qTxnPost)
				.from(qTxnPost)
				.where(
						qTxnPost.txnDetailSeq.eq(onlineTxnSeq)
						.and(qTxnPost.txnDetailType.eq(TxnDetailType.O))
						)
				.fetch();
		
		if (txnPost.isEmpty()){
			QCactInternalTxnPostHst qTxnPostHst = QCactInternalTxnPostHst.cactInternalTxnPostHst;
			List<CactInternalTxnPostHst> list = new JPAQueryFactory(em)
					.select(qTxnPostHst)
					.from(qTxnPostHst)
					.where(
							qTxnPostHst.txnDetailSeq.eq(onlineTxnSeq)
							.and(qTxnPostHst.txnDetailType.eq(TxnDetailType.O))
							)
					.fetch();
			for (CactInternalTxnPostHst hst : list){
				CactIntrnlTxnPostOl online = new CactIntrnlTxnPostOl();
				BeanUtils.copyProperties(hst, online);
				txnPost.add(online);
			}
		}
		return txnPost;
	}

	//TODO 好像多查了
	@Override
	public List<CactTxnPost> getTxnPostsByAccountSeq(Integer accountSeq) {
		QCactTxnPost qTxnPost = QCactTxnPost.cactTxnPost;
		List<CactTxnPost> txnPost = new JPAQueryFactory(em)
				.select(qTxnPost)
				.from(qTxnPost)
				.where(
						qTxnPost.acctSeq.eq(accountSeq)
						)
				.fetch();
		
		QCactTxnHst qTxnHst = QCactTxnHst.cactTxnHst;
		List<CactTxnHst> txnHst = new JPAQueryFactory(em)
				.select(qTxnHst)
				.from(qTxnHst)
				.where(
						qTxnHst.acctSeq.eq(accountSeq) 
						 )
				.fetch();
		
		List<CactTxnPost> txnPostList = new ArrayList<CactTxnPost>();
		
		for(CactTxnHst cactTxnHst : txnHst){
			CactTxnPost post = new CactTxnPost();
			BeanUtils.copyProperties(cactTxnHst, post);
			txnPostList.add(post);
		}
		txnPost.addAll(txnPostList);
		return txnPost;
	}

}
