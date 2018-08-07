/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.account.NewPostService;
import net.engining.pcx.cc.process.service.account.PostDetail;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;


/**
 * 自动还款
 *
 */
@Service
@StepScope
public class Cc1800P365AutoRepay implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private NewPostService newPostService;
	
	/**
	 * 最小还款额及账龄计算业务组件
	 */
	@Autowired
	private UComputeDueAndAgeCode cc1800UComputeDueAndAgeCode;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		for (List<Cc1800IAccountInfo> acctInfos : item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : acctInfos)
			{

//				Date pmtDueDate = cc1800IAccountInfo.getCactAccount().getPmtDueDate();
//				Date graceDate = cc1800IAccountInfo.getCactAccount().getGraceDate();
				//判断是否全额还款，如果全额还款，则不需要扣款
				if(cc1800IAccountInfo.getCactAccount().getGraceDaysFullInd() == false
						&& cc1800IAccountInfo.getCactAccount().getQualGraceBal().compareTo(BigDecimal.ZERO) > 0){
						
						if(cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.BL
								|| cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.CC
//								|| cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.CL
								){
							
							if (cc1800IAccountInfo.getCactAccount().getAutoPayAcctSeqInSystem() == null) {
								continue;
							}
							
							//获取到活期账户
							Cc1800IAccountInfo piAcctItem = getPIAcct(item, cc1800IAccountInfo.getCactAccount().getAutoPayAcctSeqInSystem());
							
							if (piAcctItem == null) {
								throw new IllegalArgumentException("自动还款的活期账户[" + cc1800IAccountInfo.getCactAccount().getAutoPayAcctSeqInSystem()
										+ "]和贷款账户[" + cc1800IAccountInfo.getCactAccount().getAcctSeq()
										+ "]不属于同一个客户");
							}
							
							//全部应还款金额
							BigDecimal repayAmt = cc1800IAccountInfo.getCactAccount().getQualGraceBal();
							//活期账户余额
							BigDecimal piAmt = BigDecimal.ZERO;
							for (CactSubAcct cactSubAcct : piAcctItem.getCactSubAccts()) {
								piAmt = piAmt.add(cactSubAcct.getEndDayBal());
							}
							piAmt = piAmt.abs();
							//活期账户余额和全部应还款金额不等于0才做结转
							if ( repayAmt.compareTo(BigDecimal.ZERO) != 0
									&& piAmt.compareTo(BigDecimal.ZERO) != 0) {
								
								//如果是最后一期，先结息
								if(cc1800IAccountInfo.getCactAccount().getCurrentLoanPeriod().intValue() == cc1800IAccountInfo.getCactAccount().getTotalLoanPeriod()
										&& cc1800IAccountInfo.getCactAccount().getFirstOverdueDate() != null)
								{
									AcctModel acctModel = item.getAcctModelMap().get(cc1800IAccountInfo.getCactAccount().getAcctSeq());

									newInterestService.settleInterest(acctModel, acctModel, batchDate, cc1800IAccountInfo.getCactAccount().getAcctSeq().toString(), TxnDetailType.A);
									
									updateAccountAtStmtDay(cc1800IAccountInfo);
									updateSubAcctAtStmtDay(cc1800IAccountInfo);
								}
								
								BigDecimal min = piAmt.compareTo(repayAmt) >=  0 ? repayAmt : piAmt;
								
								//活期账户转出，成功后，循环信用、消费分期账户再转入 
								if (doPost(piAcctItem.getAcctModel(), SysTxnCd.S37, min))
								{
									doPost(cc1800IAccountInfo.getAcctModel(), SysTxnCd.S36, min);
								}		
								//全额还款标志会在全额还款步骤更新
							}	
						}
					}
				}
			}
		return item;
	}
	
	/**
	 * 获取活期账户
	 * @return
	 */
	private Cc1800IAccountInfo getPIAcct(Cc1800IPostingInfo item, Integer piAcctSeq){
		Cc1800IAccountInfo piAcctItem = null;
		for (List<Cc1800IAccountInfo> acctInfos : item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : acctInfos)
			{
				if(cc1800IAccountInfo.getCactAccount().getAcctSeq().equals(piAcctSeq)){
					piAcctItem = cc1800IAccountInfo;
					break;
				}
			}
		}
		return piAcctItem;
	}
	
	private boolean doPost(AcctModel targetModel, SysTxnCd txnCd, BigDecimal amt)
	{
		Account acctParam = newComputeService.retrieveAccount(targetModel.getCactAccount());
		Map<SysTxnCd, String> txnCdMap = acctParam.sysTxnCdMapping;

		PostDetail detail = new PostDetail();
		
		detail.setTxnDate(new Date());
		detail.setTxnTime(new Date());
		detail.setPostTxnType(PostTxnTypeDef.M);
		detail.setPostCode(txnCdMap.get(txnCd));
		detail.setTxnAmt(amt);
		detail.setPostAmt(amt);
		detail.setTxnCurrCd(targetModel.getCactAccount().getCurrCd());
		detail.setPostCurrCd(targetModel.getCactAccount().getCurrCd());
		
		return newPostService.postToAccount(targetModel, batchDate, detail, true, 0);
	}
	
	/**
	 * 更新帐户状态
	 * @param accountInfo
	 */
	private void updateAccountAtStmtDay(Cc1800IAccountInfo accountInfo) {
		// 修改注意事项：
		// 下一账单日字段必须最后更新，否则还款日等相关字段计算会错误

		CactAccount account = accountInfo.getCactAccount();	
		// 设置全部应还款额
		account.setQualGraceBal(newComputeService.calcQualGraceBal(accountInfo.getCactSubAccts()));
		if(account.getQualGraceBal().compareTo(BigDecimal.ZERO) > 0){
			account.setGraceDaysFullInd(false);
		}
		// 设置还款日余额为0
		account.setPmtDueDayBal(account.getQualGraceBal());
		cc1800UComputeDueAndAgeCode.computeMinDue(account, accountInfo.getCactSubAccts(), accountInfo.getCactAgeDues(), batchDate.toDate());
	}
	
	/**
	 * 更新信用计划统计信息
	 * @param item
	 */
	private void updateSubAcctAtStmtDay(Cc1800IAccountInfo item){
		for (CactSubAcct cactSubAcct : item.getCactSubAccts()){		
				cactSubAcct.setStmtHist(cactSubAcct.getStmtHist()+1);
				
				cactSubAcct.setBeginBal(cactSubAcct.getEndDayBal());
		}
	}
}
