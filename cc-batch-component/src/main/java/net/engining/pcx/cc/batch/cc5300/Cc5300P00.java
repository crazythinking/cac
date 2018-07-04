package net.engining.pcx.cc.batch.cc5300;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalAcctHst;
import net.engining.pcx.cc.param.model.InternalAccount;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 内部账户入账完毕后，将内部账户的当期余额移至昨日余额,并将内部账户表记录备份至历史表
 */
@Service
@Scope("step")
public class Cc5300P00 implements ItemProcessor<CactInternalAcct, Object> {
	 
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ParameterFacility facility;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@Override
	public Object process(CactInternalAcct internalAcct) throws Exception {
		InternalAccount internalAccount = facility.loadParameter(InternalAccount.class, internalAcct.getInternalAcctId());
		CactInternalAcctHst acctHst = new CactInternalAcctHst();
		acctHst.setCrBal(internalAcct.getCrBal());
		acctHst.setDbBal(internalAcct.getDbBal());
		acctHst.setInternalAcctId(internalAcct.getInternalAcctId());
		acctHst.setLastCrBal(internalAcct.getLastCrBal());
		acctHst.setLastDbBal(internalAcct.getLastDbBal());
		acctHst.setBizDate(systemStatusFacility.getSystemStatus().processDate);
		acctHst.setSubjectCd(internalAccount.subjectCd);
		acctHst.setInternalAcctName(internalAccount.desc);
		em.persist(acctHst);
		
		internalAcct.setLastCrBal(internalAcct.getCrBal());
		internalAcct.setLastDbBal(internalAcct.getDbBal());
		
		return null;
	}
}
