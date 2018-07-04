package net.engining.pcx.cc.process.service.common;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.param.model.enums.UnmatchStatus;
import net.engining.pcx.cc.process.model.AuthorizeInfo;

@Component
public class AuthorizeMatch {
	
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @PersistenceContext
	private EntityManager em;
	
	/**
	 * 授权匹配处理
	 * @param txnPost 匹配交易
	 * @param unmatchs 未达授权交易
	 * @param unmatchStates 授权匹配状态
	 */
	public void authorizeMatch(CactTxnPost txnPost, List<AuthorizeInfo> authorizeInfos) {
		if (logger.isDebugEnabled()) {
			logger.debug("授权匹配-清算信息:PostAmt["+txnPost.getPostAmt()
					+"],AuthCode["+txnPost.getAuthCode()
					+"],CardNo["+txnPost.getCardNo()
					+"],DbCrInd["+txnPost.getDbCrInd()
					+"],PostTxnType["+txnPost.getPostTxnType()
					+"]");
		}
		// MEMO类交易，或积分交易不做匹配
		if (TxnDirection.O == txnPost.getDbCrInd() || PostTxnTypeDef.P == txnPost.getPostTxnType()) return;
		// 交易中，授权码为NULL则不做匹配
		if (txnPost.getAuthCode() == null) return;
		// 交易中，借贷标识为NULL则不做匹配
		if (txnPost.getDbCrInd() == null) return;
		// 授权匹配逻辑:
		for (AuthorizeInfo ai : authorizeInfos) {
			if (logger.isDebugEnabled()) {
				logger.debug("授权匹配-授权信息:Org["+ai.getAuthUnmatch().getOrg()
						+"],CardGroupId["+ai.getAuthUnmatch().getCardGroupId()
						+"],ChbTxnAmt["+ai.getAuthUnmatch().getChbTxnAmt()
						+"],AuthCode["+ai.getAuthUnmatch().getAuthCode()
						+"],CardNo["+ai.getAuthUnmatch().getCardNo()
						+"]");
			}
			// 标记授权匹配交易的匹配状态，U-未匹配
			//if (unmatchStates.size() == i) unmatchStates.add(UnmatchStatus.U);
			// 成功匹配
			if (ai.getUnmatchStatus() == UnmatchStatus.M) continue;
			// 卡号相等
			if (!txnPost.getCardNo().equals(ai.getAuthUnmatch().getCardNo())) continue;
			// 授权码相等
			if (!txnPost.getAuthCode().equals(ai.getAuthUnmatch().getAuthCode())) continue;

			//FIXME: 获取授权信息需要产品层提供接口，或者说授权匹配是否还需要放在账务中处理。
			/*ProductCc productC =  parameterCacheFacility.getParameter(ProductCc.class, txnPost.getProductCd());
			// 交易金额在授权金额的80%-120%之间
			if (!(txnPost.getTxnAmt().abs().compareTo(ai.getAuthUnmatch().getTxnAmt().abs()
						.multiply(BigDecimal.ONE.subtract(productC.athMatchTolRt))) >= 0
					&& txnPost.getTxnAmt().abs().compareTo(ai.getAuthUnmatch().getTxnAmt().abs()
						.multiply(BigDecimal.ONE.add(productC.athMatchTolRt))) <= 0)) continue;*/
			// 匹配成功，M-成功匹配，直接从auth_unmatch表中直接删除这笔已经匹配的记录。
			ai.setUnmatchStatus(UnmatchStatus.M);
		    em.remove(ai.getAuthUnmatch());
			return;
		}
	}
}