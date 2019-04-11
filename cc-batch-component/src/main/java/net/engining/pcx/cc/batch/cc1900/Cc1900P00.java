package net.engining.pcx.cc.batch.cc1900;

import net.engining.pcx.cc.batch.common.PrintStmtUtils;
import net.engining.pcx.cc.file.model.StmtMsgInterfaceItem;
import net.engining.pcx.cc.infrastructure.shared.enums.MessageCategory;
import net.engining.pcx.cc.infrastructure.shared.model.CactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnUnstmt;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.OrganizationContextHolder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * CMD300-账单交易生成
 * <p>
 * 生成实体账单当期交易接口文件（在账单日时生成、过滤掉MEMO交易）
 * <p>
 * 输入：账单日为当前批量日期的账单汇总信息，和未出账单交易信息
 * <p>
 * 输出：账单交易接口文件列表
 * 
 * @author heyu.wang
 * 
 */
@Service
@Scope("step")
public class Cc1900P00 implements ItemProcessor<Cc1900I, StmtInfoItem> {
	// 积分交易是否出账单: 暂时先出

	@Autowired
	private PrintStmtUtils printStmtUtils;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private ParameterFacility unifiedParameter;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public StmtInfoItem process(Cc1900I item) throws Exception {
		OrganizationContextHolder.setCurrentOrganizationId( item.getStmtHst().getOrg() );
		
		StmtInfoItem outItem = new StmtInfoItem();
		
		// 账单提醒短信
		outItem.getStmtMsgInterfaceItems().add(createStmtMsg(item.getStmtHst()));
		
		// 判断是否出账单
		if (item.getStmtHst().getStmtFlag()) {
			
			outItem.getStmtInterfaceItems().add(printStmtUtils.createStmtItem(item.getStmtHst()));
			
			for (CactTxnUnstmt txnUnstmt : item.getTxnUnstmts()) { // 循环账户的未出账单交易
				// 输出到实体账单交易当期接口文件
				PostCode postCode = unifiedParameter.getParameter(PostCode.class, txnUnstmt.getPostCode());
				if (postCode.stmtInd) { // 非memo交易，需要输出账单
					outItem.getStmttxnInterfaceItems().add(printStmtUtils.createStmttxnItem(txnUnstmt));
				}
				
				// 清理未出账单交易历史表TM_TXN_UNSTMT
				em.remove(txnUnstmt);
			}
		} 
		
		return outItem;
	}

	/**
	 * 创建账单提醒短信内容
	 * 
	 * @param stmtHst
	 * @return
	 */
	private StmtMsgInterfaceItem createStmtMsg(CactStmtHst stmtHst) {
		StmtMsgInterfaceItem item = new StmtMsgInterfaceItem();
		
		item.category = MessageCategory.O01;
		item.currencyCd = stmtHst.getCurrCd();
		item.custName = stmtHst.getName();
		item.due = stmtHst.getTotDueAmt();
		item.email = stmtHst.getEmail();
		item.gender = stmtHst.getGender();
		item.graceBalance = stmtHst.getQualGraceBal();
		item.mobileNo = stmtHst.getMobileNo();
		item.org = stmtHst.getOrg();
		item.paymentDate = stmtHst.getPmtDueDate();
		item.stmtDate = stmtHst.getStmtDate();
		
		return item;
	}
}
