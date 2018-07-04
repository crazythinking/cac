package net.engining.pcx.cc.batch.cc2300;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus;
import net.engining.pcx.cc.infrastructure.shared.model.CactLoan;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

/**
 * 判断子账户余额是否大于0如果是的话，更新分期状态为活动
 * @author yinxia
 *
 */
@Service
public class Cc2300P00 implements ItemProcessor<CactLoan, Object> {
	
	@PersistenceContext
	private EntityManager em;
	
	public Object process(CactLoan cactloan) throws Exception {
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, cactloan.getLoanPrinSubAcctId());
		if(!cactSubAcct.getEndDayBal().equals(BigDecimal.ZERO)){
			cactloan.setLoanStatus(LoanStatus.A);
		}
		return null;
	}
}
