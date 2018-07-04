package net.engining.pcx.cc.process.service.ledger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.BalTransferPostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.impl.InternalAccountService;
import net.engining.pcx.cc.process.service.support.LoanTransformEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class TransformInternalListener implements ApplicationListener<LoanTransformEvent>
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	private InternalAccountService internalAccountService;
	
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
		
		// 处理余额
		internalAccountService.postByCode(
				balTransferPostCode.internalAcctPostCodes, 
				provider7x24.getBalance(cactSubAcct), 
				cactSubAcct.getCurrCd(), 
				event.getTxnDetailSeq(), 
				event.getTxnDetailType(), 
				provider7x24.getCurrentDate().toDate());

		// 处理利息计提
		internalAccountService.postByCode(
				balTransferPostCode.internalAcctPostCodes4IntAccrual, 
				cactSubAcct.getIntAccrual(), 
				cactSubAcct.getCurrCd(), 
				event.getTxnDetailSeq(), 
				event.getTxnDetailType(), 
				provider7x24.getCurrentDate().toDate());
		
		// 处理罚息计提
		internalAccountService.postByCode(
				balTransferPostCode.internalAcctPostCodes4IntPenaltyAccrual,
				cactSubAcct.getIntPenaltyAccrual(),
				cactSubAcct.getCurrCd(),
				event.getTxnDetailSeq(),
				event.getTxnDetailType(),
				provider7x24.getCurrentDate().toDate());
	}
}
