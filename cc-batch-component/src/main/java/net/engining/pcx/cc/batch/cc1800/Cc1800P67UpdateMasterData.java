package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.file.model.RejectTxnJournalRptItem;
import net.engining.pcx.cc.file.model.TxnJournalRptItem;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactCardGroup;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnReject;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnUnstmt;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pg.parameter.ParameterFacility;

/**
 * @author yinxia
 * 维护主表
 */
@Service
@StepScope
public class Cc1800P67UpdateMasterData implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 账务处理通用业务组件
	 */
	@Autowired
	private NewComputeService newComputeService;

	/**
	 * 获取参数工具类
	 */
	@Autowired
	private ParameterFacility parameterFacility;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	/**
	 * 数据持久化
	 */
	@PersistenceContext
	private EntityManager em;

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		Date batchDate = systemStatusFacility.getSystemStatus().processDate;
		// 迭代所有账户
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos)
			{
				updateMasterData(cc1800IAccountInfo, batchDate);
			}
		}
		return item;
	}
	
	private Cc1800IAccountInfo updateMasterData(Cc1800IAccountInfo item, Date batchDate) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("维护主表：Org["+item.getCactAccount().getOrg()
					+"],AcctNo["+item.getCactAccount().getAcctNo()
					+"],BusinessType["+item.getCactAccount().getBusinessType()
					+"],CurrCd["+item.getCactAccount().getCurrCd()
					+"],TxnPosts.size["+item.getCactTxnPosts().size()
					+"]");
		}
		// 更新账户的本金余额、取现余额、额度内分期余额、历史最高本金欠款、历史最高溢缴款、历史最高余额
		updateAccountBal(item.getCactAccount(), item.getCactSubAccts());
		
		// 账户交易历史、交易报表处理
		for (CactTxnPost cactTxnPost : Lists.newArrayList(item.getCactTxnPosts()))
		{
			if (logger.isDebugEnabled()) {
				logger.debug("维护主表：TxnSeq["+cactTxnPost.getTxnSeq()
						+"],PostingFlag["+cactTxnPost.getPostingFlag()
						+"]");
			}
			// 成功入账交易
			if (cactTxnPost.getPostDate().compareTo(batchDate) <= 0)
			{
				if (PostingFlag.F00 == cactTxnPost.getPostingFlag()) {
					// 入账交易历史表
					addTxnHst(item, cactTxnPost);
					// 当日交易流水报表
					addTxnJournal(item, cactTxnPost);
					// 未出账单历史表
					addTxnUnstmt(item, cactTxnPost);
					//更新卡组信息表
					for(CactCardGroup cactCardGroup : item.getCactCardGroups()) {
						updateCardGroupAmt(cactCardGroup, cactTxnPost, item.getCactAccount());
					}
				} 
				// 挂账交易
				else {
					// 挂账交易历史表
					addTxnReject(item, cactTxnPost);
					// 当日挂账交易流水报表
					addRejectTxnJournal(item, cactTxnPost);
				}
				
				em.remove(cactTxnPost);
				item.getCactTxnPosts().remove(cactTxnPost);
			}
		}
		
		//交易处理完后直接删除。
//		rCactTxnPost.delete(item.getCactTxnPosts());

		return item;
	}
	
	/**
	 * 更新账户的余额
	 * @param cactAccount
	 * @param cactSubAccts
	 */
	private void updateAccountBal(CactAccount cactAccount, List<CactSubAcct> cactSubAccts) {

		BigDecimal currBal = BigDecimal.ZERO;
		for (CactSubAcct cactSubAcct : cactSubAccts)
		{
			currBal = currBal.add(cactSubAcct.getCurrBal());
		}

		cactAccount.setCurrBal(currBal);

		// TODO 账户上的余额类型统计暂时不做

		
	}
	
	/**
	 * 更新卡组信息的余额
	 * @param 卡组信息表cactCardGroup
	 * @param 入账交易cactTxnPost
	 */
	private void updateCardGroupAmt(CactCardGroup cactCardGroup, CactTxnPost cactTxnPost, CactAccount cactAccount) {
		PostCode postCode = parameterFacility.loadParameter(PostCode.class, cactTxnPost.getPostCode());
		Account account = newComputeService.retrieveAccount(cactAccount);
		SubAcct subAcct = null;
		if (cactTxnPost.getSubAcctId() != null) {
			CactSubAcct cactSubAcct = em.find(CactSubAcct.class, cactTxnPost.getSubAcctId());
			subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
		}
		else if (cactTxnPost.getSubacctParamId() != null) {
			subAcct = newComputeService.retrieveSubAcct(cactTxnPost.getSubacctParamId(), cactAccount);
		}
		else{
			subAcct = newComputeService.retrieveSubAcct(account.subAcctParam.get(postCode.subAcctType), cactAccount);
		}
		BigDecimal amt = newComputeService.exchange(cactTxnPost.getBusinessType(), cactCardGroup.getProductCd(), cactTxnPost.getPostCurrCd(), cactTxnPost.getPostAmt());
		switch(subAcct.balanceType){
		//取现余额
		case CASH:
			cactCardGroup.setCtdCashAmt(cactCardGroup.getCtdCashAmt().add(amt));
			break;
		//消费
		case CONS:
			cactCardGroup.setCtdRetailAmt(cactCardGroup.getCtdRetailAmt().add(amt));
			break;
		default:
		}
		em.persist(cactCardGroup);
	}
	
	/**
	 * 未出账单历史
	 * 
	 * @param item
	 * @param txnPost
	 */
	private void addTxnUnstmt(Cc1800IAccountInfo acctInfo, CactTxnPost cactTxnPost) {
		
		if(cactTxnPost.getBusinessType() == BusinessType.BL
			||	cactTxnPost.getBusinessType() == BusinessType.CC){
			
			CactTxnUnstmt cactTxnUnstmt = new CactTxnUnstmt();
			cactTxnUnstmt.updateFromMap(cactTxnPost.convertToMap());
			cactTxnUnstmt.fillDefaultValues();
			// 数据持久化
			em.persist(cactTxnUnstmt);
			acctInfo.getCactTxnUnstmts().add(cactTxnUnstmt);
		}
	}

	/**
	 * 入账交易历史表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addTxnHst(Cc1800IAccountInfo acctInfo, CactTxnPost cactTxnPost) {
		
		CactTxnHst cactTxnHst = new CactTxnHst();
		cactTxnHst.updateFromMap(cactTxnPost.convertToMap());
		cactTxnHst.fillDefaultValues();
		// 数据持久化
		em.persist(cactTxnHst);
		acctInfo.getCactTxnHsts().add(cactTxnHst);
	}

	/**
	 * 当日交易流水报表【包括：当日内部生成交易表】
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addTxnJournal(Cc1800IAccountInfo acctInfo, CactTxnPost cactTxnPost) {
		
		TxnJournalRptItem txnJournal = new TxnJournalRptItem();
		txnJournal.org = cactTxnPost.getOrg();
		txnJournal.acctNo = cactTxnPost.getAcctSeq();
		txnJournal.businessType = cactTxnPost.getBusinessType();
		txnJournal.postingFlag = cactTxnPost.getPostingFlag();
		txnJournal.prePostingFlag = cactTxnPost.getPrePostingFlag();
		txnJournal.cardNo = cactTxnPost.getCardNo();
		txnJournal.txnCode = cactTxnPost.getPostCode();
		txnJournal.txnDate = cactTxnPost.getTxnDate();
		txnJournal.txnTime = cactTxnPost.getTxnTime();
		txnJournal.glPostAmt = cactTxnPost.getPostAmt();
		txnJournal.subAcctId = cactTxnPost.getSubAcctId();
		txnJournal.refNbr = cactTxnPost.getRefNbr();
		txnJournal.acctBlockCd = acctInfo.getCactAccount().getBlockCode();
		txnJournal.currCd = cactTxnPost.getPostCurrCd();
		txnJournal.acqBranchId = cactTxnPost.getAcqBranchId();
		txnJournal.txnShortDesc = cactTxnPost.getTxnShortDesc();
		
		acctInfo.getTxnJournals().add(txnJournal);
	}

	/**
	 * 挂账交易历史表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addTxnReject(Cc1800IAccountInfo acctInfo, CactTxnPost cactTxnPost) {
		
		CactTxnReject cactTxnReject = new CactTxnReject();
		cactTxnReject.updateFromMap(cactTxnPost.convertToMap());
		
		// 数据持久化
		em.persist(cactTxnReject);
		acctInfo.getCactTxnRejects().add(cactTxnReject);
	}

	/**
	 * 当日挂账交易流水报表
	 * @param acctInfo
	 * @param txnPost
	 */
	private void addRejectTxnJournal(Cc1800IAccountInfo acctInfo, CactTxnPost cactTxnPost) {
		
		RejectTxnJournalRptItem rejectTxnJournal = new RejectTxnJournalRptItem();
		rejectTxnJournal.org = cactTxnPost.getOrg();
		rejectTxnJournal.acctNo = cactTxnPost.getAcctSeq();
		rejectTxnJournal.businessType = cactTxnPost.getBusinessType();
		rejectTxnJournal.postingFlag = cactTxnPost.getPostingFlag();
		rejectTxnJournal.prePostingFlag = cactTxnPost.getPrePostingFlag();
		rejectTxnJournal.cardNo = cactTxnPost.getCardNo();
		rejectTxnJournal.txnCode = cactTxnPost.getPostCode();
		rejectTxnJournal.txnDate = cactTxnPost.getTxnDate();
		rejectTxnJournal.txnTime = cactTxnPost.getTxnTime();
		rejectTxnJournal.glPostAmt = cactTxnPost.getPostAmt();
		rejectTxnJournal.subAcctId = cactTxnPost.getSubAcctId();
		rejectTxnJournal.refNbr = cactTxnPost.getRefNbr();
		rejectTxnJournal.currCd = cactTxnPost.getPostCurrCd();
		rejectTxnJournal.acctBlockCd = acctInfo.getCactAccount().getBlockCode();
		rejectTxnJournal.acqBranchId = cactTxnPost.getAcqBranchId();
		rejectTxnJournal.txnShortDesc = cactTxnPost.getTxnShortDesc();
		
		acctInfo.getRejectTxnJournals().add(rejectTxnJournal);
	}
}
