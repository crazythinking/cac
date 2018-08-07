/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pg.parameter.ParameterFacility;

/**
 * @author zhengpy
 * 收取超限费
 */
@Service
@StepScope
public class Cc1800P28GenerateOverLimitFee implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {

	Logger logger = LoggerFactory.getLogger(Cc1800P28GenerateOverLimitFee.class);
	
	@Autowired
	private ParameterFacility ParameterFacility;
	
	@Autowired
	private SystemStatusFacility statusFacility;
	
	/**
	 * 账务处理通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute;
	
	/**
	 * 锁定码处理业务组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Override
	public Cc1800IPostingInfo process( Cc1800IPostingInfo item ) throws Exception {

		//FIXME: 超限计算在产品层，暂时删除。
//		SystemStatus systemStatus = statusFacility.getSystemStatus();
//		Date processDate = systemStatus.processDate;
//		Date lastProcessDate = systemStatus.lastProcessDate;
//
//		OrganizationInfo org = ParameterFacility.getParameter( OrganizationInfo.class, ParameterFacility.UNIQUE_PARAM_KEY );
//		
//		Set<?> set = item.getAccountList().entrySet();
//		Iterator<?> i = set.iterator();
//		while(i.hasNext()) {
//			Map.Entry<Integer, List<Cc1800IAccountInfo>> accountInfoEntry = (Map.Entry<Integer, List<Cc1800IAccountInfo>>)i.next();
//
//			// 计算超限金额
//			BigDecimal overAmt = BigDecimal.ZERO;
//			
//			// 多币种庄户，需更新本币账户超限信息；
//			CactAccount cactAcct = null;
//			
//			List<CactTxnPost> txnPosts = null;
//			
//			List<CactSubAcct> subAccts = null;
//			
//			// 计算超限金额
//			overAmt = commonCompute.computeOverLimitAmt( accountInfoEntry.getValue() );
//
//			for( Cc1800IAccountInfo acctInfo : accountInfoEntry.getValue() ) {
//				
//				if (logger.isDebugEnabled()) {
//					logger.debug("收取超限费：Org["+acctInfo.getCactAccount().getOrg()
//							+"],AcctNo["+acctInfo.getCactAccount().getAcctNo()
//							+"],BlockCode["+acctInfo.getCactAccount().getBlockCode()
//							+"],WaiveOvlfeeInd["+acctInfo.getCactAccount().getWaiveOvlfeeInd()
//							+"]");
//				}
//				// 如果超限金额小于等于零，则将账户的超限日期更新为空
//				if (overAmt.compareTo(BigDecimal.ZERO) <= 0) {
//					acctInfo.getCactAccount().setOvrlmtDate(null);
//				}
//
//				// 获取账户参数
//				Account account = commonCompute.retrieveAccount(acctInfo.getCactAccount());
//				
//				// 检查账户锁定码免除超限费标志
//				if( blockCodeUtils.getMergedOvrlmtFeeWaiveInd( acctInfo.getCactAccount().getBlockCode(), account ) ) break;
//
//				// 检查账户超限费免除标示
//				if( acctInfo.getCactAccount().getWaiveOvlfeeInd() ) break;
//				
//
//				//判断账户是否包含本币币种
//
//				if( org.baseCurrencyCode.equals( acctInfo.getCactAccount().getCurrCd() ) ) {
//					cactAcct = acctInfo.getCactAccount();
//					txnPosts = acctInfo.getCactTxnPosts();
//					subAccts = acctInfo.getCactSubAccts();
//					break;
//				}
//				
//				// 没有本币账户，取第一条账户作为本币结算账户
//				cactAcct = accountInfoEntry.getValue().get(0).getCactAccount();
//				txnPosts = accountInfoEntry.getValue().get(0).getCactTxnPosts();
//				subAccts = accountInfoEntry.getValue().get(0).getCactSubAccts();
//
//			}
//
//			// 当期最高超限金额
//			if ( overAmt.compareTo( cactAcct.getCtdHiOvrlmtAmt()) > 0) cactAcct.setCtdHiOvrlmtAmt(overAmt);
//		
//			// 产品参数
//			Account account = commonCompute.retrieveAccount(acctInfo.getCactAccount());;
//				
//			// 超限当日取超限费
//			if (ChargeDateInd.P.equals( account.overlimitCharge.chargeDateInd ) ) {
//				// 需要在维护主表时，判断是否超限，已超限直接返回；否则，不超限的情况下，在后面将账户的超限日期更新为空
//				if ( cactAcct.getOvrlmtDate() != null) continue;
//			}
//			// 账单日收取超限费
//			else if (ChargeDateInd.C.equals(account.overlimitCharge.chargeDateInd)) {
//				// 下一账单日=当前批量日期，或上一批量日期<下一账单日<当前批量日期，否则退出
//				if ( lastProcessDate.compareTo( cactAcct.getNextStmtDate() ) < 0 && processDate.compareTo( cactAcct.getNextStmtDate() ) < 0 ) continue;
//				// 超限费计算方法
//				switch ( account.overlimitCharge.calcInd ) {
//					case P: break;
//					case H: overAmt = cactAcct.getCtdHiOvrlmtAmt(); break;
//					default: throw new IllegalArgumentException("卡产品参数中，超限费计算方法：["+account.overlimitCharge.calcInd+"]不存在!");
//				}
//			} 
//			else throw new IllegalArgumentException("卡产品参数中，超限费收取日期：["+account.overlimitCharge.chargeDateInd+"]不存在!");
//	
//			if (logger.isDebugEnabled()) {
//				logger.debug("超限费计算:chargeDateInd["+account.overlimitCharge.chargeDateInd
//						+"],OvrlmtDate["+cactAcct.getOvrlmtDate()
//						+"],NextStmtDate["+cactAcct.getNextStmtDate()
//						+"],overAmt["+overAmt
//						+"],CtdHiOvrlmtAmt["+cactAcct.getCtdHiOvrlmtAmt()
//						+"],YtdOvrlmtFeeAmt["+cactAcct.getYtdOvrlmtFeeAmt()
//						+"],YtdOvrlmtFeeCnt["+cactAcct.getYtdOvrlmtFeeCnt()
//						+"]");
//			}
//			// 如果超限金额大于零，则调用交易生成和入账逻辑
//			if (overAmt.compareTo(BigDecimal.ZERO) > 0) {
//				// 生成一笔超限费交易，并入账
//				CactTxnPost txnPost = cc1800UGenerator.generateOverLimitFee(txnPosts, subAccts, cactAcct, overAmt, processDate);
//				// 超限日期
//				if ( cactAcct.getOvrlmtDate() == null ) cactAcct.setOvrlmtDate( processDate );
//				
//				if (txnPost == null) continue;
//	
//				// 本年超限费收取金额
//				cactAcct.setYtdOvrlmtFeeAmt(txnPost.getPostAmt().add(cactAcct.getYtdOvrlmtFeeAmt()));
//				// 本年超限费收取笔数
//				cactAcct.setYtdOvrlmtFeeCnt(cactAcct.getYtdOvrlmtFeeCnt() + 1);
//				// 生成超限账户报表记录
////				this.addOverLimitAccount(cactAcct, overAmt, txnPost.getPostAmt(), commonCompute.getCurrCreditLimit(cactAcct, processDate));
//			}
//		}
//		
		return item;
	}
	
	/**
	 * 超限账户报表
	 * @param acctInfo
	 * @param overAmt 超限部分金额
	 * @param overLimitFee 超限费
	 * @param currCreditLimit 当前账户有效额度
	 */
	public void addOverLimitAccount(Cc1800IAccountInfo acctInfo, BigDecimal overAmt, BigDecimal overLimitFee, BigDecimal currCreditLimit) {
		
		// FIXME：确定报表目录后，再实现
//		OverLimitAccountRptItem overLimitAccount = new OverLimitAccountRptItem();
//		overLimitAccount.org = acctInfo.getCactAccount().getOrg();
//		overLimitAccount.acctNo = acctInfo.getCactAccount().getAcctNo();
//		overLimitAccount.acctType = acctInfo.getCactAccount().getAcctType();
//		overLimitAccount.defaultLogicalCardNo = acctInfo.getCactAccount().getDefaultLogicalCardNo();
//		overLimitAccount.currBal = acctInfo.getCactAccount().getCurrBal();
//		overLimitAccount.overLimitAmt = overAmt;
//		overLimitAccount.overLimitFee = overLimitFee;
//		overLimitAccount.currCreditLimit = currCreditLimit;
//		overLimitAccount.CurrencyCode = acctInfo.getCactAccount().getAcctType().getCurrencyCode();
//		
//		acctInfo.getOverLimitAccounts().add(overLimitAccount);
	}
}
