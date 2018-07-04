package net.engining.pcx.cc.process.service.ledger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.impl.InternalAccountService;

/**
 * 利息/罚息 结息时入内部账、总账的通用监听处理逻辑；
 * 该监听主要用于日间联机交易（非日终批量）期间，还款（贷款）或提款（存款）交易时需要结利息/罚息，从而引起的计提利息与计提罚息存在不同的情况；
 * 这种情况需要将结转的利息/罚息的值与已经计提入账利息/罚息的值计算差额，然后相应进行补提或冲减的入账操作；
 * 这里把两个处理写在一起的最根本原因，是它们都基于同一个intAccural(计提利息)或intPenaltyAccural(计提罚息)进行跟踪，在计提/冲减之后要进行更新
 * @author binarier
 *
 */
public abstract class AbstractInterestPenaltyListener
{
	/**
	 * 内部户入账代码类型（计提/冲减）
	 * @author binarier
	 *
	 */
	protected enum Type
	{
		/**
		 * 计提 
		 */
		Accural,
		/**
		 * 冲减 
		 */
		Reversal
	};
	
	@Autowired
	private InternalAccountService internalAccountService;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Autowired
	private NewLedgerService newLedgerService;

	/**
	 * 子类负责从子账户参数中取内部户入账代码表
	 * @param subAcct
	 * @return
	 */
	protected abstract Map<String, List<String>> extractInternalPostCodeMap(SubAcct subAcct);
	
	/**
	 * 子类负责从子账户中取计提金额
	 * @param cactSubAcct
	 * @param postDate TODO
	 * @return
	 */
	protected abstract BigDecimal extractAccuralAmount(CactAccount cactAccount, CactSubAcct cactSubAcct, LocalDate postDate);
	
	/**
	 * 子类负责返回计提/冲减和账龄组对应的系统内部行为代码
	 * @return
	 */
	protected abstract SysInternalAcctActionCd determineSysInternalAcctActionCd(Type type, AgeGroupCd ageGroupCd);

	/**
	 * 子类负责返回总账计提/冲减对应系统入账代码
	 * @return
	 */
	protected abstract SysTxnCd determineLedgerSysTxnCd(Type type);
	
	/**
	 * 子类负责更新accural，把已处理部份更新掉
	 * @param processed
	 */
	protected abstract void updateAccuralAmount(CactSubAcct cactSubAcct, BigDecimal processed);
	
	public void doProcess(int subAcctId, BigDecimal postAmount, LocalDate postDate, String txnDetailSeq, TxnDetailType txnDetailType){
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, subAcctId);
		doProcess(cactSubAcct, postAmount, postDate, txnDetailSeq, txnDetailType);
	}
	
	public void doProcess(CactSubAcct cactSubAcct, BigDecimal postAmount, LocalDate postDate, String txnDetailSeq, TxnDetailType txnDetailType)
	{
		//加载数据
		CactAccount cactAccount = em.find(CactAccount.class, cactSubAcct.getAcctSeq());
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
		Account account = newComputeService.retrieveAccount(cactAccount);

		Map<String, List<String>> internalPostCodeMap = extractInternalPostCodeMap(subAcct);
		
		if (internalPostCodeMap != null)
		{
			// 如果配了，就处理内部户
			// 这里处理的是结息/罚息交易本身的内部户处理，它对应的总账是跟着入账交易走的
			// TODO 有待进一步重构
			internalAccountService.postByCode(
					internalPostCodeMap.get(cactAccount.getAgeCd()),
					postAmount,
					cactAccount.getCurrCd(),
					txnDetailSeq,
					txnDetailType,
					postDate.toDate());
		}
		
		BigDecimal accuralAmount = extractAccuralAmount(cactAccount, cactSubAcct, postDate);
		BigDecimal overAmount = postAmount.subtract(accuralAmount.abs());	//表示入账多的金额
		
		if (overAmount.signum() != 0)
		{
			//处理内部户补提/冲减
			//确定内部户入账代码
			SysInternalAcctActionCd internalAcctActionCd=null;
			
			Type type = overAmount.signum() > 0 ? Type.Accural : Type.Reversal;		//多入账则冲减，少则补提	
			String loanState=null ;
			
			if( TransformType.D.equals(account.carryType)  ){
				if(cactSubAcct.getSubAcctType().equals("LOAN") ){
					internalAcctActionCd = determineSysInternalAcctActionCd(type, newAgeService.calcAgeGroupCd("0") ); //正常利息冲销
				}else{
					loanState="1";
					//罚息余额成分有两种,逾期和非应计
					if( (Days.daysBetween(new LocalDate(cactSubAcct.getSetupDate()), postDate).getDays() )>90  //程序处理是先结息，后转非，所以补提冲销需要大于90天
							&&  (cactSubAcct.getSubAcctType().equals("LBAL")||  cactSubAcct.getSubAcctType().equals("INTE")||  cactSubAcct.getSubAcctType().equals("SFEE")  ) //罚息计提只对本金或者利息
							){
						loanState="2";
					}
					internalAcctActionCd = determineSysInternalAcctActionCd
							(type,newAgeService.calcAgeGroupBySterm( loanState ) );
					
				}
				if (internalAcctActionCd != null)
				{
					// 如果配了，就处理内部户
					List<String> internalAcctPostCodes = account.internalAcctPostMapping.get(internalAcctActionCd);
					internalAccountService.postByCode(
							internalAcctPostCodes,
							overAmount.abs(),
							cactAccount.getCurrCd(),
							txnDetailSeq,
							txnDetailType,
							postDate.toDate());
				}

				//处理总账补提/冲减
				SysTxnCd sysTxnCd = determineLedgerSysTxnCd(type);
				
				newLedgerService.postLedger(
						cactAccount.getAcctSeq(),
						account.sysTxnCdMapping.get(sysTxnCd),
						overAmount.abs(),
						postDate,
						txnDetailSeq,
						txnDetailType,
						newAgeService.calcAgeGroupBySterm( loanState ) 
						);
			}else{
				internalAcctActionCd = determineSysInternalAcctActionCd(type, newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()));
				
				if (internalAcctActionCd != null)
				{
					// 如果配了，就处理内部户
					List<String> internalAcctPostCodes = account.internalAcctPostMapping.get(internalAcctActionCd);
					internalAccountService.postByCode(
							internalAcctPostCodes,
							overAmount.abs(),
							cactAccount.getCurrCd(),
							txnDetailSeq,
							txnDetailType,
							postDate.toDate());
				}

				//处理总账补提/冲减
				SysTxnCd sysTxnCd = determineLedgerSysTxnCd(type);
				
				newLedgerService.postLedger(
						cactAccount.getAcctSeq(),
						account.sysTxnCdMapping.get(sysTxnCd),
						overAmount.abs(),
						postDate,
						txnDetailSeq,
						txnDetailType);
			}
			
		}

		//最后更新accural，把已处理部份更新掉
		updateAccuralAmount(cactSubAcct, accuralAmount);
	}
}
