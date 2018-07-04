package net.engining.pcx.cc.batch.cc5600;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlBalHst;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 总帐信息复制至历史表
 * @author Ronny
 *
 */
public class Cc5600P01Backup implements ItemProcessor<ApGlBal, ApGlBal>{
	
	@Autowired
	private ParameterFacility parameterFacility;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	
	@PersistenceContext
	private EntityManager em;
	
	public ApGlBal process(ApGlBal item){
		
		Subject subject = parameterFacility.loadParameter(Subject.class, item.getSubjectCd());
		
		ApGlBalHst hst = new ApGlBalHst();
		hst.setBranchNo(item.getBranchNo());
		hst.setCrAmt(item.getCrAmt());
		hst.setCrBal(item.getCrBal());
		hst.setCrCount(item.getCrCount());
		hst.setDbAmt(item.getDbAmt());
		hst.setDbBal(item.getDbBal());
		hst.setDbCount(item.getDbCount());
		hst.setLastCrBal(item.getLastCrBal());
		hst.setLastDbBal(item.getLastDbBal());
		hst.setLastMthCrBal(item.getLastMthCrBal());
		hst.setLastMthDbBal(item.getLastMthDbBal());
		hst.setLastQtrCrBal(item.getLastQtrCrBal());
		hst.setLastQtrDbBal(item.getLastQtrDbBal());
		hst.setLastYrCrBal(item.getLastYrCrBal());
		hst.setLastYrDbBal(item.getLastYrDbBal());
		hst.setMtdCrAmt(BigDecimal.ZERO);
		hst.setMtdDbAmt(BigDecimal.ZERO);
		hst.setOrg(item.getOrg() == null ? "*" : item.getOrg());
		hst.setBizDate(batchDate);
		hst.setQtdCrAmt(BigDecimal.ZERO);
		hst.setQtdDbAmt(BigDecimal.ZERO);
		hst.setSubjectCd(item.getSubjectCd());
		hst.setYtdCrAmt(BigDecimal.ZERO);
		hst.setYtdDbAmt(BigDecimal.ZERO);
		hst.setSubjectName(subject.name);
		hst.setSubjectType(subject.type);
		em.persist(hst);
		return item;
	}
	


}