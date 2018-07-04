package net.engining.pcx.cc.process.service.ledger.refactor;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.process.service.ledger.NewLedgerService;
import net.engining.pcx.cc.process.service.support.refactor.DirectAccountingEvent;

/**
 * 日终批量记账 产生总账入账交易的监听
 * 
 * @author xiachuanhu
 *
 */
@Service
public class EndDayAccountingLedgerListener implements ApplicationListener<DirectAccountingEvent> {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private NewLedgerService newLedgerService;
	
	@Override
	public void onApplicationEvent(DirectAccountingEvent event) {
		doLedgerPost(event.getAcctSeq(),event.getPostCode(),event.getPostAmount(),event.getPostDate(),event.getClearDate(),event.getTrdate(),event.getTxnDetailType(),event.getTxnDetailSeq());
		
	}

	private void doLedgerPost(Integer acctSeq, String postCode, BigDecimal postAmount, Date postDate, Date clearDate,
			Date trdate, TxnDetailType txnDetailType, String txnDetailSeq) {

		if (postCode == null || postAmount.signum() == 0) {
			// 没配code或没有金额不用处理
			return;
		}

		// FIXME
		// 需要
		newLedgerService.postLedger(acctSeq, postCode, postAmount ,postDate,clearDate,trdate,txnDetailType,txnDetailSeq);
	
		
	}

}
