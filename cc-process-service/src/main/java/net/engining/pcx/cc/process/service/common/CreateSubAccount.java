package net.engining.pcx.cc.process.service.common;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.SubAcct;

import org.springframework.stereotype.Service;

/**
 * @author yinxia
 * 创建子账户
 */
@Service
public class CreateSubAccount {

	@PersistenceContext
	private EntityManager em;
	
	/**
	 * @param subAcct 已经确定的子账户参数
	 * @param cactAccount 账户
	 * @param cactSubAccts 该账户下所关联的所有子账户
	 * @param batchDate 创建日期
	 * @param stmtHist 子账户账期
	 * @return 新建的子账户记录
	 */
	public CactSubAcct createSubAccount(SubAcct subAcct, CactAccount cactAccount, List<CactSubAcct> cactSubAccts, Date batchDate, Integer stmtHist) {
		CactSubAcct newSubAcct = new CactSubAcct();
		newSubAcct.setOrg(cactAccount.getOrg());
		newSubAcct.setSubAcctType(subAcct.subAcctType);
	    newSubAcct.setAcctSeq(cactAccount.getAcctSeq());
	    newSubAcct.setBusinessType(cactAccount.getBusinessType());
		newSubAcct.setSubacctParamId(subAcct.subAcctId);
		newSubAcct.setStmtHist(stmtHist);
		//已删除期末余额（endBal）
		/*先把期初余额初始化为零，因为这里没有子账户组的概念，考虑到冲销能够找到最早的子账户，
		所以在确定期初余额的时候，需要累积该账户的所有子账户中paramid相同并且multSaleInd为true的期末当期余额。
		
		除了这些子账户，paramid相同并且multSaleInd为false认为是单笔交易对应的子账户，这类子账户是不会再有新的交易
		追加上去的，所以不需要根据这类子账户再创建出新的子账户。
		paramid不相同的子账户无需累积，直接跳过就行了。*/
		newSubAcct.setBeginBal(BigDecimal.ZERO);
		/*for (CactSubAcct sa : cactSubAccts){
			if (subAcct.subAcctId.equals(sa.getParamId()) && subAcct.multSaleInd)
			{
				newSubAcct.setBeginBal( newSubAcct.getBeginBal().add( sa.getEndBal()) );
			}
		}*/
		newSubAcct.setCurrBal(BigDecimal.ZERO);
		newSubAcct.setEndDayBal(BigDecimal.ZERO);
		newSubAcct.setCurrCd(cactAccount.getCurrCd());
		//newSubAcct.setEndBal(BigDecimal.ZERO);
		newSubAcct.setTotDueAmt(BigDecimal.ZERO);
		newSubAcct.setIntPending(BigDecimal.ZERO);
		newSubAcct.setIntReceivable(BigDecimal.ZERO);
		newSubAcct.setSetupDate(batchDate);
		newSubAcct.setLastUpdateDate(new Date());
		newSubAcct.setIntAccrual(BigDecimal.ZERO);
		newSubAcct.setEndDayBeforeBal(BigDecimal.ZERO);
		newSubAcct.setPenalizedAmt(BigDecimal.ZERO);
		newSubAcct.setIntPenaltyAccrual(BigDecimal.ZERO);
		newSubAcct.setPenalizedInterestCode(subAcct.penalizedInterestTable);
		newSubAcct.setAddupAmt(BigDecimal.ZERO);
		em.persist(newSubAcct);
		
		return newSubAcct;
	}

}
