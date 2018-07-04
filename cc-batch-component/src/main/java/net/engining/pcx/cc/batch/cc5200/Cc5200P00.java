package net.engining.pcx.cc.batch.cc5200;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostSum;
import net.engining.pcx.cc.param.model.InternalAccount;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 批量内部账户入账
 */
@Service
@Scope("step")
public class Cc5200P00 implements ItemProcessor<CactIntrnlTxnPostSum, Object> {
	 
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ParameterFacility facility;

	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;


	@Override
	public Object process(CactIntrnlTxnPostSum postSummar) throws Exception {
		
		CactInternalAcct cactInternalAcct = em.find(CactInternalAcct.class,postSummar.getInternalAcctId());
		if(cactInternalAcct == null){
			InternalAccount internalAccount = facility.getParameter(InternalAccount.class, postSummar.getInternalAcctId());
			cactInternalAcct = new CactInternalAcct();
			cactInternalAcct.setCrBal(BigDecimal.ZERO);
			cactInternalAcct.setDbBal(BigDecimal.ZERO);
			cactInternalAcct.setLastCrBal(BigDecimal.ZERO);
			cactInternalAcct.setLastDbBal(BigDecimal.ZERO);
			cactInternalAcct.setInternalAcctId(postSummar.getInternalAcctId());
			cactInternalAcct.setSetupDate(batchDate);
			cactInternalAcct.setLastUpdateDate(new Date());
			cactInternalAcct.setSubjectCd(internalAccount.subjectCd);
			cactInternalAcct.setInternalAcctName(internalAccount.desc);
			em.persist(cactInternalAcct);
		}
		InternalAccount internalAccount = facility.getParameter(InternalAccount.class, cactInternalAcct.getInternalAcctId());		
		Subject subject = facility.getParameter(Subject.class, internalAccount.subjectCd);
		writeOn(subject, TxnDirection.C, postSummar.getCrAmt(), cactInternalAcct);
		writeOn(subject, TxnDirection.D, postSummar.getDbAmt(), cactInternalAcct);
		return null;
	}
	
	private void writeOn(Subject subject , TxnDirection direction, BigDecimal postAmt , CactInternalAcct cactInternalAcct){
		// 计算余额,发生额
		switch (subject.balDbCrFlag) {
		case D: // 只允许借方余额
			switch (direction) {
			case D: // 借
				// 借方余额=原借方余额+借方金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(postAmt));
				break;
			case C: // 贷
				// 借方余额=原借方余额-贷方金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().subtract(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case C: // 只允许贷方余额
			switch (direction) {
			case D: // 借
				// 贷方余额=原贷方余额-借方金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().subtract(postAmt));
				break;
			case C: // 贷
				// 贷方余额=原贷方余额+贷方金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case B: // 按轧差金额
			// TODO 按轧差金额计算需要把一方置0

			switch (direction) {
			case D: // 借
				//
				BigDecimal tmpCrBal = cactInternalAcct.getCrBal().subtract(postAmt);
				if (tmpCrBal.compareTo(BigDecimal.ZERO) > 0) {
					cactInternalAcct.setCrBal(tmpCrBal);
				} else {
					cactInternalAcct.setCrBal(BigDecimal.ZERO);
					cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(tmpCrBal.negate()));
				}
				break;
			case C: // 贷
				//
				BigDecimal tmpDbBal = cactInternalAcct.getDbBal().subtract(postAmt);
				if (tmpDbBal.compareTo(BigDecimal.ZERO) > 0) {
					cactInternalAcct.setDbBal(tmpDbBal);
				} else {
					cactInternalAcct.setDbBal(BigDecimal.ZERO);
					cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(tmpDbBal.negate()));
				}
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case T: // 双向余额
			switch (direction) {
			case D: // 借
				// 借方余额=原借方余额+借方金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(postAmt));
				break;
			case C: // 贷
				// 贷方余额=原贷方余额+贷方金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		default:
			break;
		}
			
	}
}
