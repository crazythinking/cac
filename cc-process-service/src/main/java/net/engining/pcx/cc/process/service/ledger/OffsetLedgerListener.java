package net.engining.pcx.cc.process.service.ledger;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.support.OffsetEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;

/**
 * 冲销发生时的总账处理
 * @author binarier
 *
 */
@Service
public class OffsetLedgerListener implements ApplicationListener<OffsetEvent>
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private NewLedgerService newLedgerService;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@Autowired
	private NewComputeService newComputeService;

	@Override
	@Transactional
	public void onApplicationEvent(OffsetEvent event)
	{
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, event.getSubAcctId());
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct);
		CactAccount cactAccount = em.find(CactAccount.class, cactSubAcct.getAcctSeq());
		Account account = newComputeService.retrieveAccount(cactAccount);
		if(TransformType.D.equals(account.carryType)) {
			String loanState=null ;//||  cactSubAcct.getSubAcctType().equals("INTE")
			if(cactSubAcct.getSubAcctType().equals("LOAN")  ){ //结息出来的利息为正常
				loanState=null; //正常利息
			}else{//罚息余额成分有两种,逾期和非应计
				QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
				List<CactSubAcct> accts = new JPAQueryFactory(em)
						.select(qCactSubAcct)
						.from(qCactSubAcct)
						.where(qCactSubAcct.acctSeq.eq(cactSubAcct.getAcctSeq() ),
								qCactSubAcct.subAcctType.eq("LBAL"),  //因为结息在形态转移前面,当罚息结息时,需判断该笔余额成分是否已经满足转非条件,罚息余额成分晚结出来
								qCactSubAcct.stmtHist.eq(cactSubAcct.getStmtHist())
								)
						.fetch();
				if(Optional.fromNullable(accts).isPresent() && !accts.isEmpty() ){
					if( (Days.daysBetween(new LocalDate( accts.get(0).getSetupDate() ), provider7x24.getCurrentDate() ).getDays() )>90 ){
						loanState="2"; //非应计
					}else{
						Date graceDay = DateUtils.addDays(accts.get(0).getSetupDate(), account.pmtGracePrd);  //单笔余额成分的宽限日,不用系统宽限期
						
						if(  provider7x24.getCurrentDate().isEqual(new LocalDate(graceDay)) ||
							 provider7x24.getCurrentDate().isBefore(new LocalDate(graceDay))    //宽限期内都是正常还款
										){
							loanState=null;
						}else{
							loanState="1";
						}
					}
				}
			}
			newLedgerService.postLedger(event.getAcctSeq(), subAcct.depositPostCode, event.getAmount(), 
					event.getPostDate(), event.getTxnDetailSeq(), event.getTxnDetailType(),newAgeService.calcAgeGroupBySterm( loanState) 
					);

		}else{
			newLedgerService.postLedger(event.getAcctSeq(), subAcct.depositPostCode, event.getAmount(), event.getPostDate(), event.getTxnDetailSeq(), event.getTxnDetailType());
		}
	}
}
