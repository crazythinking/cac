package net.engining.pcx.cc.process.service.ledger;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.support.PostPostEvent;

/**
 * CactTxnPost当日入账交易表，产生入账后的入账事件监听；<br>
 * 针对需要进行后续总账处理的入账交易，根据PostPostEvent产生总账交易流水AP_GL_TXN
 * @author binarier
 *
 */
@Service
public class PostPostLedgerListener implements ApplicationListener<PostPostEvent>
{
	@Autowired
	private NewLedgerService newLedgerService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional
	public void onApplicationEvent(PostPostEvent event)
	{
		checkArgument(event.getPostAmount().signum() > 0);
		Account account = newComputeService.retrieveAccount(event.getAcctModel().getCactAccount() );
		
		if(TransformType.D.equals(account.carryType)) {
			CactSubAcct cactSubAcct = em.find(CactSubAcct.class, event.getSubAcctId());
			String loanState = null;// ||
									// cactSubAcct.getSubAcctType().equals("INTE")
			if (cactSubAcct.getSubAcctType().equals("LOAN")) { // 结息出来的利息为正常
				loanState = null; // 正常利息
			} else {// 罚息余额成分有两种,逾期和非应计
				QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
				List<CactSubAcct> accts = new JPAQueryFactory(em)
						.select(qCactSubAcct)
						.from(qCactSubAcct)
						.where(qCactSubAcct.acctSeq.eq(cactSubAcct.getAcctSeq() ),
								qCactSubAcct.subAcctType.eq("LBAL"),  //因为结息在形态转移前面,当罚息结息时,需判断该笔余额成分是否已经满足转非条件
								qCactSubAcct.stmtHist.eq(cactSubAcct.getStmtHist())
								)
						.fetch();
				if (Optional.fromNullable(accts).isPresent() && !accts.isEmpty()) {
					if ((Days.daysBetween(new LocalDate(accts.get(0).getSetupDate()), event.getPostDate())
							.getDays()) > 90) {
						loanState = "2"; // 非应计
					} else {
						loanState = cactSubAcct.getStmtHist() == 0 ? null : "1";// 逾期状态判断，新生成的余额成分为0
					}
				}
			}
			newLedgerService.postLedger(event.getAcctModel().getCactAccount().getAcctSeq(), event.getPostCode(),
					event.getPostAmount(), event.getPostDate(), event.getTxnPostSeq().toString(), TxnDetailType.P,
					newAgeService.calcAgeGroupBySterm(loanState) // 传入逾期标志
			);
		} else {
			newLedgerService.postLedger(event.getAcctModel().getCactAccount().getAcctSeq(), event.getPostCode(),
					event.getPostAmount(), event.getPostDate(), event.getTxnPostSeq().toString(), TxnDetailType.P); // TODO
																											// review
		}
	}

}
