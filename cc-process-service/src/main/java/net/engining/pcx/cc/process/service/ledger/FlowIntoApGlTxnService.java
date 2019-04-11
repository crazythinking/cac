package net.engining.pcx.cc.process.service.ledger;

import net.engining.pcx.cc.infrastructure.shared.enums.PostTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlTxn;
import net.engining.pcx.cc.param.model.enums.PostGlInd;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.support.core.context.Provider4Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class FlowIntoApGlTxnService {
	
	@Autowired
	private Provider4Organization provider4Organization;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@PersistenceContext
	private EntityManager em;
	
	@Transactional
	public void intoApGlTxn(Integer acctSeq, String postCode, BigDecimal amount,String txnDetailSeq,Date transDate,Date clearDate){
		ApGlTxn apGltxn = new ApGlTxn();
		apGltxn.setAcctSeq(acctSeq);
		apGltxn.setCurrCd("156");
		apGltxn.setPostCode(postCode);
		apGltxn.setPostAmount(amount);
		apGltxn.setPostType(PostTypeDef.SYSM);
		apGltxn.setPostGlInd(PostGlInd.Normal);
		apGltxn.setOwingBranch(provider4Organization.getCurrentOrganizationId());
		apGltxn.setTxnDetailType(TxnDetailType.O); 
		apGltxn.setTxnDetailSeq(txnDetailSeq); 
		apGltxn.setPostDate(provider7x24.getCurrentDate().toDate());
		apGltxn.setBizDate(provider7x24.getCurrentDate().toDate());
		apGltxn.fillDefaultValues();
		em.persist(apGltxn);
	}

}
