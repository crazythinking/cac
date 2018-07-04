package net.engining.pcx.cc.process.service;

import java.util.List;

import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostOl;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;

/**
 * 入账明细查询服务
 * @author Ronny
 *
 */
public interface TxnPostQueryService {

	/**
	 * 用联机交易流水号查询入账交易
	 * @param onlineTxnSeq
	 * @return
	 */
	List<CactTxnPost> getTxnPostsByOnlineTxnSeq(String onlineTxnSeq);
	
	/**
	 * 用联机交易流水号查询内部帐入账交易
	 * @param onlineTxnSeq
	 * @return
	 */
	List<CactIntrnlTxnPostOl> getInternalTxnPost4OnlineByOnlineTxnSeq(String onlineTxnSeq);
	
	/**
	 * 用account seq查询入账交易
	 * @param onlineTxnSeq
	 * @return
	 */
	List<CactTxnPost> getTxnPostsByAccountSeq(Integer accountSeq);
}
