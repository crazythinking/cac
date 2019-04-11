package net.engining.pcx.cc.process.service.impl;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostBt;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostOl;
import net.engining.pcx.cc.param.model.InternalAcctPostCode;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.OrganizationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 内部账户记账处理服务；<br>
 * 
 * 记录内部账户交易流水 <br> //TODO 后面要把其他相关处理逻辑移过来重构，如InternalTxnPostTask内的记账逻辑
 *
 * 
 * @author luxue
 *
 */
@Service
public class InternalAccountService{

	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ParameterFacility parameterFacility;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private Provider7x24 provider7x24;

	/**
	 * 按内部账户交易码记流水；根据是否批量标志，记录不同的内部账户交易流水表；
	 * @param internalAccountPostCode
	 * @param amount
	 * @param currencyCode
	 * @param txnDetailSeq
	 * @param txnDetailType
	 * @param postDate 入账日期，因为入账日期可能是批处理日的下一日，所以将该字段设置为参数由外部传入
	 */
	@Transactional
	public void postByCode(String internalAccountPostCode, BigDecimal amount, String currencyCode, String txnDetailSeq, TxnDetailType txnDetailType, Date postDate)
	{
		if (amount.signum() == 0)
		{
			return;
		}
		
		InternalAcctPostCode code =  parameterFacility.getParameter(InternalAcctPostCode.class, internalAccountPostCode);
		
		if (provider7x24.isInternalAccountAsBatch())
		{
			logger.debug("处理的txnDetailSeq,进入批量表");
			CactIntrnlTxnPostBt txnPost4Batch = new CactIntrnlTxnPostBt();
			txnPost4Batch.setOrg(OrganizationContextHolder.getCurrentOrganizationId());
			txnPost4Batch.setDbCrInd(code.processor.getTxnDirection());
			txnPost4Batch.setInternalAcctId(code.internalAccountId);
			txnPost4Batch.setInternalAcctPostCode(internalAccountPostCode);
			txnPost4Batch.setPostAmt(amount);
			txnPost4Batch.setPostCurrCd(currencyCode); 
			txnPost4Batch.setPostDate(postDate);
			txnPost4Batch.setPostingFlag(PostingFlag4InternalAcct.FFF);
			txnPost4Batch.setTxnDetailSeq(txnDetailSeq);//来源交易流水号
			txnPost4Batch.setTxnDetailType(txnDetailType);//来源交易流水类型
			txnPost4Batch.setRedBlueInd(code.redBlueInd);
			txnPost4Batch.setBizDate(provider7x24.getCurrentDate().toDate());
			txnPost4Batch.fillDefaultValues();
			em.persist(txnPost4Batch);
		}
		else
		{	
			logger.debug("处理的txnDetailSeq为{},进入联机表", txnDetailSeq);
			CactIntrnlTxnPostOl txnPost4Online = new CactIntrnlTxnPostOl();
			txnPost4Online.setOrg(OrganizationContextHolder.getCurrentOrganizationId());
			txnPost4Online.setInternalAcctId(code.internalAccountId);//内部账号
			txnPost4Online.setInternalAcctPostCode(internalAccountPostCode);//内部帐交易代码
			txnPost4Online.setDbCrInd(code.processor.getTxnDirection());//借贷标志
			txnPost4Online.setPostAmt(amount);//入账金额
			txnPost4Online.setPostCurrCd(currencyCode);//入账币种
			txnPost4Online.setPostDate(postDate);//入账日期
			txnPost4Online.setPostingFlag(PostingFlag4InternalAcct.FFF);//入账结果标示码
			txnPost4Online.setTxnDetailSeq(txnDetailSeq);//来源交易流水号
			txnPost4Online.setTxnDetailType(txnDetailType);//来源交易流水类型
			txnPost4Online.setRedBlueInd(code.redBlueInd);
			txnPost4Online.setBizDate(provider7x24.getCurrentDate().toDate());
			txnPost4Online.fillDefaultValues();
			em.persist(txnPost4Online);
		}
	}
	
	/**
	 * 
	 * @param internalAccountPostCodes
	 * @param amount
	 * @param currencyCode
	 * @param txnDetailSeq
	 * @param txnDetailType
	 * @param postDate
	 */
	public void postByCode(List<String> internalAccountPostCodes, BigDecimal amount, String currencyCode, String txnDetailSeq, TxnDetailType txnDetailType, Date postDate)
	{
		if (internalAccountPostCodes != null)
		{
			for(String internalPostCode : internalAccountPostCodes)
			{
				postByCode(internalPostCode, amount, currencyCode, txnDetailSeq, txnDetailType, postDate);
			}
		}
		
	}
	/**
	 * 按内部账户交易码记流水(按联机日期记账)
	 * @param internalAccountPostCode
	 * @param amount
	 * @param currencyCode
	 * @param txnDetailSeq
	 * @param txnDetailType
	 * @param isBatchPostInvoke
	 */
	@Transactional
	public void postByCode(String internalAccountPostCode, BigDecimal amount, String currencyCode, String txnDetailSeq, TxnDetailType txnDetailType, boolean isBatchPostInvoke)
	{
		postByCode(internalAccountPostCode, amount, currencyCode, txnDetailSeq, txnDetailType, systemStatusFacility.getSystemStatus().businessDate);
	}

	/**
	 * 按照指定方式入内部账户入账流水（页面调账）
	 * @param internalAccountId
	 * @param dbCrInd
	 * @param redBlueInd
	 * @param amount
	 * @param currencyCode
	 * @param processDate
	 * @param txnDetailSeq
	 * @param txnDetailType
	 */
	public void postByData(String internalAccountId, TxnDirection dbCrInd, RedBlueInd redBlueInd, BigDecimal amount, String currencyCode,Date processDate, String txnDetailSeq, TxnDetailType txnDetailType)
	{
		CactIntrnlTxnPostOl txnPost4Online = new CactIntrnlTxnPostOl();
		txnPost4Online.setOrg(OrganizationContextHolder.getCurrentOrganizationId());
		txnPost4Online.setInternalAcctId(internalAccountId);//内部账号
		txnPost4Online.setDbCrInd(dbCrInd);//借贷标志
		txnPost4Online.setPostAmt(amount);//入账金额
		txnPost4Online.setPostCurrCd(currencyCode);//入账币种
		txnPost4Online.setPostDate(processDate);//入账日期
		txnPost4Online.setPostingFlag(PostingFlag4InternalAcct.FFF);//入账结果标示码
		txnPost4Online.setTxnDetailSeq(txnDetailSeq);//来源交易流水号
		txnPost4Online.setTxnDetailType(txnDetailType);//来源交易流水类型
		txnPost4Online.setRedBlueInd(redBlueInd);
		txnPost4Online.setLastUpdateDate(new Date());
		txnPost4Online.setBizDate(provider7x24.getCurrentDate().toDate());
		txnPost4Online.fillDefaultValues();
		em.persist(txnPost4Online);
	}
}
