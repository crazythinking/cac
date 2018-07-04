package net.engining.pcx.cc.process.service.ledger.refactor;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.InOutFlagDef;
import net.engining.pcx.cc.param.model.enums.PostGlInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.ledger.NewLedgerService;
import net.engining.pcx.cc.process.service.ledger.NewSplitLedgerService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pcx.cc.process.service.support.refactor.DirectAccountSplitEvent;

/**
 * 批量直接记账 产生会计分录拆分交易的监听
 * 
 * @author xiachuanhu
 *
 */
@Service
public class DirectAccountSplitLedgerListener implements ApplicationListener<DirectAccountSplitEvent> {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private NewSplitLedgerService newSplitLedgerService;

	@Override
	public void onApplicationEvent(DirectAccountSplitEvent event) {

		doSplitPost(event.getTrdate(),event.getPostDate(), event.getCrSubCd(), event.getDbSubCd(), event.getInoutFlag(),event.getAccountingAmt(),event.getVolSeq(),event.getTxnDirection(),event.getAssistData(),event.getPostGlInd(),event.getGltSeq(),event.getRedBlueInd());
	}

	private void doSplitPost(Date trdate, LocalDate postDate,String crSubCd, String dbSubCd, InOutFlagDef inoutFlag,
			BigDecimal accountingAmt, int volSeq, TxnDirection txnDirection, String assistData,PostGlInd postGlInd,String gltSeq,RedBlueInd redBlueInd) {
		if ( accountingAmt.signum() == 0) {
			// 没配code或没有金额不用处理
			return;
		}
		newSplitLedgerService.postSplitLedger(trdate,postDate, crSubCd, dbSubCd, inoutFlag, accountingAmt, volSeq, txnDirection, assistData,postGlInd,gltSeq,redBlueInd);
	}

}
