package net.engining.pcx.cc.process.service.ledger;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.InOutFlagDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlVolDtl;
import net.engining.pcx.cc.param.model.enums.PostGlInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pg.support.core.context.Provider4Organization;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会计分离拆分处理服务 批量直接记账
 * 
 * @author xiachuanhu
 *
 */
@Service
public class NewSplitLedgerService {
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	Provider4Organization provider4Organization;

	private static String currency = "156";

	/**
	 * 入会计分离拆分交易流水表
	 * 
	 * @param trdate
	 * @param crSubCd
	 * @param dbSubCd
	 * @param inoutFlag
	 * @param accountingAmt
	 * @param volSeq
	 * @param txnDirection
	 * @param assistData
	 */
	@Transactional
	public void postSplitLedger(Date trdate, LocalDate postDate, String crSubCd, String dbSubCd, InOutFlagDef inoutFlag,
			BigDecimal accountingAmt, int volSeq, TxnDirection txnDirection, String assistData, PostGlInd postGlInd,String gltSeq,RedBlueInd redBlueInd) {
			ApGlVolDtl agvd = new ApGlVolDtl();
			// 插入分录流水表
			agvd.setOrg(provider4Organization.getCurrentOrganizationId());
			agvd.setBranchNo(provider4Organization.getCurrentOrganizationId());
			agvd.setTxnDetailType(TxnDetailType.C);
			agvd.setTxnDetailSeq(gltSeq);
			agvd.setVolDt(postDate.toDate());
			// agvd.setTxnBrcd(item.getAcqBranch());
			agvd.setTransDate(trdate);
			agvd.setCurrCd(currency);
			agvd.setPostGlInd(postGlInd);
			agvd.setCrsubjectCd(crSubCd);
			agvd.setDbsubjectCd(dbSubCd);
			agvd.setInOutFlag(inoutFlag);
			agvd.setSubjAmount(accountingAmt);
			agvd.setRedBlueInd(redBlueInd);
			agvd.setTxnDirection(txnDirection);
			agvd.setAssistAccountData(assistData);
			agvd.setBizDate(postDate.toDate());
			agvd.setVolSeq(volSeq);
			agvd.fillDefaultValues();
			em.persist(agvd);
			volSeq++;
	}

}
