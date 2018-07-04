package net.engining.pcx.cc.batch.cc2100;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.file.model.DdSucessMessInterfaceItem;
import net.engining.pcx.cc.infrastructure.shared.enums.MessageCategory;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnHst;
import net.engining.pcx.cc.param.model.SysTxnCdMapping;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pg.parameter.ParameterFacility;

/**
 * CMD300-账单交易生成还款短信文件接口
 * <p>
 * 1. 生成约定还款成功短信接口记录(DD_SUCESS_MESS_INTERFACE)
 * <p>
 * 2. 生成还款成功短信接口记录
 * @author heyu.wang
 * 
 */
@Service
@Scope("step")
public class Cc2100P00 implements ItemProcessor<CactTxnHst, DdSucessMessInterfaceItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private ParameterFacility unifiedParameter;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public DdSucessMessInterfaceItem process(CactTxnHst txnHst) throws Exception {
		SysTxnCdMapping ddTxnCdMapping = unifiedParameter.getParameter(SysTxnCdMapping.class, SysTxnCd.S11.toString());
		logger.debug("交易代码：" + txnHst.getPostCode());
		
		if (ddTxnCdMapping.postCode.equals(txnHst.getPostCode())) { // 约定还款成功
			CactAccount account = em.find(CactAccount.class, txnHst.getAcctSeq());
			return createDdSucessMessInterfaceItem(txnHst, account);
		} 

		return null;
	}

	/**
	 * 创建约定还款成功短信接口内容
	 * 
	 * @param txnPost
	 * @param tmAccount
	 * @return
	 */
	private DdSucessMessInterfaceItem createDdSucessMessInterfaceItem(CactTxnHst txnPost, CactAccount account) {
		assert account != null;

		DdSucessMessInterfaceItem ddSucessMessInterfaceItem = new DdSucessMessInterfaceItem();
		ddSucessMessInterfaceItem.org = txnPost.getOrg();
		ddSucessMessInterfaceItem.txnAmt = txnPost.getTxnAmt();
		ddSucessMessInterfaceItem.txnDate = txnPost.getTxnDate();
		ddSucessMessInterfaceItem.category = MessageCategory.O07;
//		ddSucessMessInterfaceItem.email =  account.getEmail();
		
		return ddSucessMessInterfaceItem;
	}
}
