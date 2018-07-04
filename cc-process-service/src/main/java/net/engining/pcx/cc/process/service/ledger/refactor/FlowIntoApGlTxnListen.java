package net.engining.pcx.cc.process.service.ledger.refactor;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.process.service.ledger.FlowIntoApGlTxnService;
import net.engining.pcx.cc.process.service.support.refactor.FlowIntoApGlTxnEvent;

/**
 * 形态转移 余额结转 产生总账入账交易的监听
 * @author luxue
 *
 */
@Service
public class FlowIntoApGlTxnListen implements ApplicationListener<FlowIntoApGlTxnEvent>
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private FlowIntoApGlTxnService flowIntoApGlTxnService;

	@Override
	public void onApplicationEvent(FlowIntoApGlTxnEvent event)
	{
		//加载数据
		doLedgerPost(event, event.getAcctSeq(),event.getPostCode(),event.getPostAmount(),event.getTxnDetailSeq(),event.getTransDate(),event.getClearDate());
	}

	private void doLedgerPost(FlowIntoApGlTxnEvent event, Integer acctSeq, String postCode, BigDecimal amount,String txnDetailSeq,Date transDate,Date clearDate)
	{
		
		if (postCode == null || amount.signum() == 0)
		{
			//没配code或没有金额不用处理
			return;
		}
		
		//FIXME 根据新的账龄组定义处理,定义新账龄组
		//需要
		flowIntoApGlTxnService.intoApGlTxn(event.getAcctSeq(),event.getPostCode(),event.getPostAmount(),event.getTxnDetailSeq(),event.getTransDate(),event.getClearDate());
	}
}
