/**
 * 
 */
package net.engining.pcx.cc.process.service.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.PaymentCalcMethod;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;

/**
 * TODO 类命名不合理
 * @author linwk
 * 
 * 拖欠处理：最小还款额(DUE)、计算账龄
 */
@Service
public class UComputeDueAndAgeCode {

	/**
	 * 账龄代码列表
	 */
	public static final String AGE_CD = "0123456789";
	
	/**
	 * 未欠款账龄代码
	 */
	public static final String AGE0 = "0";
	
	/**
	 * 呆滞账龄代码
	 */
	public static final String AGE5 = "5";

	/**
	 * 溢缴款账龄代码
	 */
	public static final String OVERPMT_AGE_CD = "C";

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 入账通用业务组件
	 */
	@Autowired
	private NewComputeService newComputeService ;
	
	/**
	 * 参数获取组件
	 */
	@Autowired
	private ParameterFacility parameterCacheFacility;
	
	/**
	 * 锁定码处理业务组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 计算最小还款额
	 * 账单日处理
	 */
	public void computeMinDue(CactAccount cactAccount, List<CactSubAcct> cactSubAccts, List<CactAgeDue> cactAgeDues, Date batchDate) {
		logger.debug("Starting computing Min Due for account [{}], account type is [{}], batch date is [{}]"
				, cactAccount.getAcctNo()
				, cactAccount.getBusinessType()
				, cactAccount.getCurrCd()
				, batchDate.toString());
		
		// 获取账户参数
	    Account account = newComputeService.retrieveAccount(cactAccount);
		
		// 最小还款额
		BigDecimal minDue;
		
		//	根据锁定码上的要求全额还款指示进行最小还款额计算
		PaymentCalcMethod pcm = blockCodeUtils.getMergedPaymentInd(cactAccount.getBlockCode(), account); 
		minDue = calcCommonMinDue(cactAccount, cactSubAccts, cactAgeDues, pcm); 

		//	设置最小还款额的小数位数
		minDue = minDue.setScale(2, RoundingMode.HALF_UP);
		
		// 	如果最小还款额 + 所有拖欠金额大于当前余额
		//	最小还款额 = 当前余额 - 所有拖欠金额
		if (minDue.add(cactAccount.getTotDueAmt()).compareTo(cactAccount.getCurrBal()) > 0){
			minDue = cactAccount.getCurrBal().subtract(cactAccount.getTotDueAmt());
		}
		
		//	如果最小还款额小于0，则最小还款额 =0
		if (minDue.compareTo(BigDecimal.ZERO) < 0){
			minDue = BigDecimal.ZERO;
		}
		
		// 更新最小还款额
		if (minDue.compareTo(BigDecimal.ZERO) > 0){
			Date dueDate = batchDate;
			if (cactAccount.getCurrentLoanPeriod() < cactAccount.getTotalLoanPeriod()
					|| cactAccount.getTotalLoanPeriod() == -1) {
				switch(account.delqDayInd) {
				case P:
					dueDate = cactAccount.getPmtDueDate();
					break;
				case G:
					dueDate = cactAccount.getGraceDate();
					break;
				case C:
					dueDate = cactAccount.getLastInterestDate();
					break;
				default:
					throw new IllegalArgumentException("参数account的delqDayInd（到期还款日类型）错误！ account:[" + account + "]");
				}
			}
			setMinDueByAgeCd(cactAccount, cactAgeDues, minDue.setScale(2, RoundingMode.HALF_UP), dueDate);	
		}
		
		//	最小还款额合计
//		BigDecimal totDueAmt = BigDecimal.ZERO;
						
		//	循环累加所有账龄对应的最小还款额
//		for (char c : "987654321".toCharArray()){
//			totDueAmt = totDueAmt.add(getMinDueByAgeCd(cactAccount, c));
//		}
		
		//	更新账户上的最小还款额合计
		if (minDue.add(cactAccount.getTotDueAmt()).compareTo(cactAccount.getQualGraceBal()) > 0 ) {
			cactAccount.setTotDueAmt(cactAccount.getQualGraceBal());
		}
		else {
			cactAccount.setTotDueAmt(minDue.add(cactAccount.getTotDueAmt()));
		}
		
		
	}
	
	/**
	 * 计算当期最小还款额: 根据汇总最小还款额和历史最小还款额进行差额计算
	 * @param cactAccount
	 * @param cactAgeDues
	 * @param pcm
	 */
	private BigDecimal calcCommonMinDue(CactAccount cactAccount, List<CactSubAcct> cactSubAccts, List<CactAgeDue> cactAgeDues, PaymentCalcMethod pcm){
		// 最小还款额
		BigDecimal accountMinDue = BigDecimal.ZERO;
		/*
		// 首期
		if(cactAgeDues == null || cactAgeDues.size() == 0) {
			accountMinDue = cactAccount.getQualGraceBal();
		} else {
			for (CactAgeDue cactAgeDue : cactAgeDues){
				
				BigDecimal subAcctMinDue = BigDecimal.ZERO;
				switch(pcm){
				case N :
					// 
					subAcctMinDue = cactAccount.getQualGraceBal().subtract(cactAgeDue.getAgeDueAmt()); 
					break;
				default :
					throw new IllegalArgumentException("无法处理锁定码给出的还款指示！ PaymentCalcMethod:[" + pcm + "]");
				}

				accountMinDue = accountMinDue.add(subAcctMinDue);
			}
		}*/
		switch(pcm){
		case N :
			// 循环所有的子账户
			for (CactSubAcct cactSubAcct : cactSubAccts) {
				if (cactSubAcct.getStmtHist() == 0) {
					// 获取子账户参数
					SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);

					// 判断余额成分对应的是否计入全额应还款金额参数为true的余额成分计入全额还款金额
					if (subAcct.graceQualify) {
						Integer ageCd = 0;
						if (cactAccount.getAgeCd().compareTo("C") == 0 || cactAccount.getAgeCd() == null) {
							ageCd = 0;
						}
						else {
							ageCd = Integer.valueOf(cactAccount.getAgeCd());
						}
						logger.debug("account [{}], subAcct [{}], subAcct.endDayBal [{}], ageCd [{}], subAcctParam.subAcctId [{}]"
								, cactAccount.getAcctSeq()
								, cactSubAcct.getSubAcctId()
								, cactSubAcct.getEndDayBal()
								, ageCd
								, subAcct.subAcctId
								);
						accountMinDue = accountMinDue.add(cactSubAcct.getEndDayBal().multiply(subAcct.minPaymentRates.get(ageCd)));
					}
				}	
			}
			break;
		case B:
			// 首期
			if(cactAgeDues == null || cactAgeDues.size() == 0) {
				accountMinDue = cactAccount.getQualGraceBal();
			} else {
				for (CactAgeDue cactAgeDue : cactAgeDues){
					
					BigDecimal subAcctMinDue = BigDecimal.ZERO;
					subAcctMinDue = cactAccount.getQualGraceBal().subtract(cactAgeDue.getAgeDueAmt()); 
					accountMinDue = accountMinDue.add(subAcctMinDue);
				}
			}
			break;
		default :
			throw new IllegalArgumentException("无法处理锁定码给出的还款指示！ PaymentCalcMethod:[" + pcm + "]");
		}		
		return accountMinDue;
	}
	
	/**
	 * 根据账龄更新对应的最小未还款额
	 * @param account 账户表
	 * @param minDue 最小还款额
	 */
	/**
	 * @param cactAccount
	 * @param minDue
	 */
	private void setMinDueByAgeCd(CactAccount cactAccount, List<CactAgeDue> cactAgeDues, BigDecimal minDue, Date processDate)
	{
		CactAgeDue cactAgeDue = new CactAgeDue();
		cactAgeDue.setOrg(cactAccount.getOrg());
		cactAgeDue.setAcctSeq(cactAccount.getAcctSeq());
		cactAgeDue.setAgeDueAmt(minDue);
		cactAgeDue.setGraceDate(processDate);
		Integer period = 0;
		for (CactAgeDue ageDue : cactAgeDues) {
			if (ageDue.getPeriod() > period) {
				period = ageDue.getPeriod();
			}
		}
		cactAgeDue.setPeriod(period + 1);
		cactAgeDue.setBizDate(provider7x24.getCurrentDate().toDate());
		cactAgeDue.fillDefaultValues();
		em.persist(cactAgeDue);
		cactAgeDues.add(cactAgeDue);
	}
	
	/**
	 * 追加最小还款额: 追加对应账号和期数的最小还款额，如果没有对应期数，就追加到最近（期数最大）的一期
	 * @param cactAccount
	 * @param cactAgeDues
	 * @param pcm
	 */
	public void additionalMinDue(CactAccount cactAccount, List<CactAgeDue> cactAgeDues, int period, BigDecimal amount){
		int maxPeriod = cactAgeDues.get(0).getPeriod();
		CactAgeDue cactAgeDue = cactAgeDues.get(0);
		for (CactAgeDue ageDue : cactAgeDues) {
			if (ageDue.getPeriod() == period) {
				cactAgeDue = ageDue;
				break;
			}
			if (ageDue.getPeriod() > maxPeriod) {
				maxPeriod = ageDue.getPeriod();
				cactAgeDue = ageDue;
			}
		}
		cactAgeDue.setAgeDueAmt(cactAgeDue.getAgeDueAmt().add(amount));
		cactAccount.setTotDueAmt(cactAccount.getTotDueAmt().add(amount));
		cactAccount.setQualGraceBal(cactAccount.getQualGraceBal().add(amount));
	}
}
