package net.engining.pcx.cc.process.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.BalanceType;
import net.engining.pcx.cc.process.service.AccountQueryService;
import net.engining.pcx.cc.process.service.account.NewComputeService;

/**
 * 账户查询服务
 * @author Ronny
 *
 */
@Service
public class AccountQueryServiceImpl implements AccountQueryService{

	@PersistenceContext
	private EntityManager em;
		
	@Autowired
	private NewComputeService newComputeService;

	@Override
	public BigDecimal getSystemBal(BusinessType type, String currCd) {
		QCactAccount qCactAccount = QCactAccount.cactAccount;
		BigDecimal sysLimit = new JPAQueryFactory(em)
				.select(qCactAccount.currBal.sum().coalesce(BigDecimal.ZERO))
				.from(qCactAccount)
				.where(qCactAccount.businessType.eq(type),
						qCactAccount.currCd.eq(currCd))
				.fetchOne();
		if(!Optional.fromNullable(sysLimit).isPresent())
			sysLimit = BigDecimal.ZERO;
		
 		return sysLimit;
	}

	@Override
	public BigDecimal getCustBal(int acctNo, String currCd) {
		QCactAccount qCactAccount = QCactAccount.cactAccount;
		BigDecimal custLimit = new JPAQueryFactory(em)
				.select(qCactAccount.currBal.sum().coalesce(BigDecimal.ZERO))
				.from(qCactAccount)
				.where(
						qCactAccount.acctNo.eq(acctNo),
						qCactAccount.currCd.eq(currCd))
				.fetchOne();
		if(!Optional.fromNullable(custLimit).isPresent())
			custLimit = BigDecimal.ZERO;
		
		return custLimit;
	}

	@Override
	public BigDecimal getCustAcctBal(int acctSeq) {
		CactAccount acct = em.find(CactAccount.class, acctSeq);
		return acct.getCurrBal();
	}

	@Override
	public BigDecimal getCustAcctBal(int acctNo, BusinessType type, String currCd) {
		QCactAccount qAcct = QCactAccount.cactAccount;
		BigDecimal bal = new JPAQueryFactory(em)
				.select(qAcct.currBal.sum().coalesce(BigDecimal.ZERO))
				.from(qAcct)
				.where(qAcct.acctNo.eq(acctNo),
						qAcct.businessType.eq(type),
						qAcct.currCd.eq(currCd))
				.fetchOne();
		if(!Optional.fromNullable(bal).isPresent())
			bal = BigDecimal.ZERO;
		
		return bal;
	}

	@Override
	public BigDecimal getSubAcctBal(int acctSeq, String subAcctType,
			String currCd) {
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		BigDecimal bal = new JPAQueryFactory(em)
				.select(qCactSubAcct.currBal.sum().coalesce(BigDecimal.ZERO))
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(acctSeq),
						qCactSubAcct.subAcctType.eq(subAcctType),
						qCactSubAcct.currCd.eq(currCd)
						)
				.fetchOne();
		if(!Optional.fromNullable(bal).isPresent())
			bal = BigDecimal.ZERO;
		
		return bal;
	}

	@Override
	public BigDecimal getLoanAndTopyByAccountSeq(int accountSeq) {
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> accts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(accountSeq)
					 )
				.fetch();
		
		BigDecimal bal = BigDecimal.ZERO;
		for(CactSubAcct acct : accts){
			SubAcct subacct = newComputeService.retrieveSubAcct(acct);
			if(subacct.balanceType.equals(BalanceType.LOAN)
					||subacct.balanceType.equals(BalanceType.TOPY)){
				bal = bal.add(acct.getCurrBal());
			}
			
		}
		return bal;
	}
	
	@Override
	public BigDecimal getArrearsByAccountSeq(int accountSeq) {
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> accts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(accountSeq) 
					 )
				.fetch();
		
		BigDecimal bal = BigDecimal.ZERO;
		for(CactSubAcct acct : accts){
			SubAcct subacct = newComputeService.retrieveSubAcct(acct);
			if(subacct.balanceType.equals(BalanceType.LOAN)
					||subacct.balanceType.equals(BalanceType.TOPY) 
					|| subacct.balanceType.equals(BalanceType.INTE)
					|| subacct.balanceType.equals(BalanceType.SFEE)
					|| subacct.balanceType.equals(BalanceType.PAYM)
					){
				bal = bal.add(acct.getCurrBal());
			}
			
		}
		return bal;
	}
	
	@Override
	public BigDecimal getArrearsExceptFeeByAccountSeq(int accountSeq) {
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> accts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(accountSeq) 
					 )
				.fetch();
		
		BigDecimal bal = BigDecimal.ZERO;
		for(CactSubAcct acct : accts){
			SubAcct subacct = newComputeService.retrieveSubAcct(acct);
			if(subacct.balanceType.equals(BalanceType.LOAN)
					||subacct.balanceType.equals(BalanceType.TOPY) 
					|| subacct.balanceType.equals(BalanceType.INTE)
					|| subacct.balanceType.equals(BalanceType.PAYM)
					){
				bal = bal.add(acct.getCurrBal());
			}
			
		}
		return bal;
	}

	@Override
	public List<CactSubAcct> getSubAcctsByAccountSeq(int accountSeq) {
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> accts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(accountSeq)
						)
				.fetch();
		 
		return accts;
	}

	@Override
	public BigDecimal getPaymentsByAccountSeq(int accountSeq) {
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> accts = new JPAQueryFactory(em)
				.select(qCactSubAcct)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(accountSeq) 
					 )
				.fetch();
		
		BigDecimal bal = BigDecimal.ZERO;
		for(CactSubAcct acct : accts){
				bal = bal.add(acct.getCurrBal());
				bal = bal.add(acct.getIntReceivable().setScale(2, RoundingMode.HALF_UP));
				bal = bal.add(acct.getPenalizedAmt().setScale(2, RoundingMode.HALF_UP));
		}
		return bal;
	}

}