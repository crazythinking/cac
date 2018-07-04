package net.engining.pcx.cc.process.service.ledger.refactor;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.param.model.BalTransferPostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.ledger.NewLedgerService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pcx.cc.process.service.support.refactor.LoanTransformEvent4Re;

/**
 * 形态转移 余额结转 产生总账入账交易的监听
 * @author luxue
 *
 */
@Service
public class TransformLedgerListener4Re implements ApplicationListener<LoanTransformEvent4Re>
{
	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	private NewLedgerService newLedgerService;
	
	@Autowired
	private NewComputeService newComputeService;

	@Override
	public void onApplicationEvent(LoanTransformEvent4Re event)
	{
		//加载数据
		SubAcct subAcct = newComputeService.retrieveSubAcct(event.getAcctSeq(), event.getCactSubAcct().getSubAcctType());

		if (subAcct.balTransferMap == null || !subAcct.balTransferMap.containsKey(BalTransferPostCode.key(event.getOrginalAgeGroupCd(), event.getNewAgeGroupCd())))
		{
			//不配不处理
			return;
		}

		BalTransferPostCode balTransferPostCode = subAcct.balTransferMap.get(BalTransferPostCode.key(event.getOrginalAgeGroupCd(), event.getNewAgeGroupCd()));
		
		doLedgerPost(event, event.getAcctSeq(), balTransferPostCode.postCode, event.getCactSubAcct().getCurrBal(), event.getClearDate(), event.getTransDate(), event.getNewAgeGroupCd());
		doLedgerPost(event, event.getAcctSeq(), balTransferPostCode.postCode4IntAccrual, event.getCactSubAcct().getIntAccrual(), event.getClearDate(), event.getTransDate(), event.getNewAgeGroupCd());	
		doLedgerPost(event, event.getAcctSeq(), balTransferPostCode.postCode4IntPenaltyAccrual, event.getCactSubAcct().getIntPenaltyAccrual(), event.getClearDate(), event.getTransDate(), event.getNewAgeGroupCd());
	}

	private void doLedgerPost(LoanTransformEvent4Re event, Integer acctSeq, String postCode, BigDecimal amount, LocalDate clearDate, LocalDate transDate, AgeGroupCd newAgeGroupCd)
	{
		
		if (postCode == null || amount.signum() == 0)
		{
			//没配code或没有金额不用处理
			return;
		}
		
		newLedgerService.postLedger(acctSeq, postCode, amount, provider7x24.getCurrentDate(), event.getTxnDetailSeq(), event.getTxnDetailType(), clearDate, transDate, newAgeGroupCd);
	}
}
