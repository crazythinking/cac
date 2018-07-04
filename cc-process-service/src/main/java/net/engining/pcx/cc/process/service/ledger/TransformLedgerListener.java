package net.engining.pcx.cc.process.service.ledger;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.BalTransferPostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.support.LoanTransformEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;

/**
 * 形态转移 产生总账入账交易的监听
 * @author luxue
 *
 */
@Service
public class TransformLedgerListener implements ApplicationListener<LoanTransformEvent>
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	private NewLedgerService newLedgerService;
	
	@Autowired
	private NewComputeService newComputeService;

	@Override
	public void onApplicationEvent(LoanTransformEvent event)
	{
		//加载数据
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, event.getSubAcctId());
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct);

		if (subAcct.balTransferMap == null || !subAcct.balTransferMap.containsKey(BalTransferPostCode.key(event.getOrginalAgeGroupCd(), event.getNewAgeGroupCd())))
		{
			//不配不处理
			return;
		}

		BalTransferPostCode balTransferPostCode = subAcct.balTransferMap.get(BalTransferPostCode.key(event.getOrginalAgeGroupCd(), event.getNewAgeGroupCd()));
		
		doLedgerPost(event, cactSubAcct.getAcctSeq(), balTransferPostCode.postCode, provider7x24.getBalance(cactSubAcct));	
		doLedgerPost(event, cactSubAcct.getAcctSeq(), balTransferPostCode.postCode4IntAccrual, cactSubAcct.getIntAccrual());	
		doLedgerPost(event, cactSubAcct.getAcctSeq(), balTransferPostCode.postCode4IntPenaltyAccrual, cactSubAcct.getIntPenaltyAccrual());	
	}

	private void doLedgerPost(LoanTransformEvent event, int acctSeq, String postCode, BigDecimal amount)
	{
		
		if (postCode == null || amount.signum() == 0)
		{
			//没配code或没有金额不用处理
			return;
		}
		if((event.getOrginalAgeGroupCd().equals(AgeGroupCd.Normality) 
				&& event.getNewAgeGroupCd().equals(AgeGroupCd.S)) //逐期正常转逾期
			    || (
			    		event.getOrginalAgeGroupCd().equals(AgeGroupCd.S) 
			    		&& event.getNewAgeGroupCd().equals(AgeGroupCd.Z)
			    		)  //逐期逾期转非应计
				)
		{
			newLedgerService.postLedger(acctSeq, postCode, amount, provider7x24.getCurrentDate(), event.getTxnDetailSeq(), event.getTxnDetailType()
					,event.getNewAgeGroupCd()==AgeGroupCd.S?AgeGroupCd.Attention:AgeGroupCd.Above4M3 );
		}
		else {
			newLedgerService.postLedger(acctSeq, postCode, amount, provider7x24.getCurrentDate(), event.getTxnDetailSeq(), event.getTxnDetailType());
		}
	}
}
