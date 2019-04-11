package net.engining.pcx.cc.batch.cc1400;

import com.querydsl.jpa.impl.JPAQueryFactory;
import net.engining.gm.infrastructure.enums.AcctCloseReason;
import net.engining.gm.param.model.CreditParameter;
import net.engining.pcx.cc.file.model.AcctCloseRptItem;
import net.engining.pcx.cc.file.model.CancelRptItem;
import net.engining.pcx.cc.infrastructure.shared.enums.RequestTypeDef;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactCancelReg;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.QCactCard;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.OrganizationContextHolder;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 销卡销户及关闭账户批处理
 * 
 * @author yinxia
 *
 */
@Service
@Scope("step")
public class Cc1400P00 implements ItemProcessor<CactCancelReg, I4001OutputItem> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String P_CODE = "P";
	private static final String C_CODE = "C";

	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 锁定码处理业务组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	@Autowired
	private NewComputeService newComputeService;
	
	/**
	 * 获取参数
	 */
	@Autowired
	private ParameterFacility parameterFacility;
	
	/**
	 * 获取客户信息
	 */
//	@Autowired
//	private CiCustomerQuery ciCustomerQuery;

	@Override
	public I4001OutputItem process(CactCancelReg cancel) throws Exception 
	{
		try
		{
			I4001OutputItem output = new I4001OutputItem();
	
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrganizationId(cancel.getOrg());
			
			//销户
			if(DateUtils.truncatedCompareTo(batchDate, cancel.getBizDate(), Calendar.DATE) == 0)
			{
				QCactAccount qCactAccount = QCactAccount.cactAccount;
				switch (cancel.getRequestType())
				{
					case C:
						//销户撤销户通过账户获取客户信息
						CactAccount cactAccount = new JPAQueryFactory(em)
							.select(qCactAccount).from(qCactAccount)
							.where(
									qCactAccount.acctNo.eq(cancel.getAcctNo()),
									qCactAccount.businessType.eq(cancel.getBusinessType()))
							.fetchOne();
						
						//生成销卡销户报表
						output.setCancelRptItem(createCancelResponseItem(cancel, cactAccount));
						
						//保留销户记录,待关闭账户
						break;
						
					//撤销户
					case D:
						//销户撤销户通过账户获取客户信息
						CactAccount cactAccounta = new JPAQueryFactory(em)
							.select(qCactAccount)
							.from(qCactAccount)
							.where(
									qCactAccount.acctNo.eq(cancel.getAcctNo()),
									qCactAccount.businessType.eq(cancel.getBusinessType()))
							.fetchOne();
						
						//生成销卡销户报表
						output.setCancelRptItem(createCancelResponseItem(cancel, cactAccounta));
						
						//移除撤销户记录
						em.remove(cancel);
						break;
					
					default: throw new IllegalArgumentException("销户请求类型错误" + cancel.getRequestType().toString());
				}
			}
			
			//关闭账户, 封锁码C + 预销户满N天(包含N天)
			CreditParameter creditParameter = parameterFacility.getParameter(CreditParameter.class, ParameterFacility.UNIQUE_PARAM_KEY);
			if(cancel.getRequestType() == RequestTypeDef.C
					&& DateUtils.truncatedCompareTo(batchDate, DateUtils.addDays(cancel.getBizDate(), creditParameter.daysBeforeClose), Calendar.DATE) >= 0)
			{
				List<CactAccount> accList = new ArrayList<CactAccount>();
				
				Boolean balEqualZeroFlag = Boolean.TRUE;
				QCactAccount qCactAccount = QCactAccount.cactAccount;
				//计算本币账户及外币账户的3项余额(当前余额)
				List<CactAccount> accts = new JPAQueryFactory(em).select(qCactAccount).from(qCactAccount).where(qCactAccount.acctNo.eq(cancel.getAcctNo())).fetch();
				for(CactAccount acct : accts){
		
					//本外币账户
					accList.add(acct);
					
					//每个账户余额都是零
					if (!acct.getCurrBal().equals(BigDecimal.ZERO)){
						balEqualZeroFlag = Boolean.FALSE;
					}
					//是有效记录; 账户档销户日期为空则已销户撤销, 账户档销户日期与记录业务日期不等则非最后一次销户操作 
					if(acct.getCancelDate() != null && DateUtils.truncatedCompareTo(cancel.getBizDate(), acct.getCancelDate(), Calendar.DATE) != 0){
						em.remove(cancel);
						return output;
					}
				}
				
				//若本币账户3项余额 + 外币账户3项余额 = 0，则关闭账户
				if(balEqualZeroFlag.equals(Boolean.TRUE))
				{
					for(CactAccount acc : accList)
					{
						// 产品参数
						Account account = newComputeService.retrieveAccount(acc);
						//更新账户
						acc.setBlockCode(blockCodeUtils.removeBlockCode(acc.getBlockCode(), C_CODE, account));
						acc.setBlockCode(blockCodeUtils.addBlockCode(acc.getBlockCode(), P_CODE, account));
						acc.setClosedDate(batchDate);
						//保存
						em.merge(acc);
						
						//生成关闭账户报表
						output.setAcctCloseRptItem(createAcctCloseRptItem(acc, AcctCloseReason.R00));
					}
					//移除当前成功关闭账户记录
					em.remove(cancel);
				}
				//若不为0，关闭账户失败
				else
				{
					for(CactAccount acc : accList)
					{
						//返回原因码
						AcctCloseReason reason = getAcctCloseReason(acc);
						//生成关闭账户报表
						reason = AcctCloseReason.R00.equals(reason)?AcctCloseReason.R99:reason;
						output.setAcctCloseRptItem(createAcctCloseRptItem(acc, reason));
					}
				}
			}
			return output;
			
		} catch (Exception e) {
			logger.error("销卡销户及关闭账户异常,账号{}", cancel.getAcctNo());
			logger.error("销卡销户及关闭账户异常{}", e);
			throw e;
		}
		
	}


	/**
	 * 生成销卡销户报表
	 * @param cancel
	 * @param cactAccount
	 * @return
	 */
	private CancelRptItem createCancelResponseItem(CactCancelReg cancel, CactAccount cactAccount) {
		
		CancelRptItem cancelRptItem = new CancelRptItem();
		
		cancelRptItem.org = cancel.getOrg();
		cancelRptItem.acctNo = cancel.getAcctNo();
		cancelRptItem.businessType = cancel.getBusinessType();
		cancelRptItem.logicCardNo = cancel.getCardGroupId();
		cancelRptItem.requestType = cancel.getRequestType();
		cancelRptItem.appDate = cancel.getBizDate();
		
//		CiCustomer ciCustomer = ciCustomerQuery.getCustomerByCustId(cactAccount.getCustId());
//		cancelRptItem.name = ciCustomer.getName();
//		cancelRptItem.mobile = ciCustomer.getMobileNo();
		
		return cancelRptItem;
	}


	/**
	 * 关闭账户条件校验
	 * @param cactAccount
	 * @return
	 */
	private AcctCloseReason getAcctCloseReason(CactAccount cactAccount) {
		
		//当前余额不为0
		if(cactAccount.getCurrBal().compareTo(BigDecimal.ZERO) != 0)
		{
			return AcctCloseReason.R01;
		}
		
		return AcctCloseReason.R00;
	}


	/**
	 * 生成关闭账户报表
	 * @param acct
	 * @param reason
	 * @return
	 */
	private AcctCloseRptItem createAcctCloseRptItem(CactAccount acct, AcctCloseReason reason) 
	{
		AcctCloseRptItem acctCloseRptItem = new AcctCloseRptItem();
		QCactCard qCactCard = QCactCard.cactCard;
		String cardGroupId = new JPAQueryFactory(em).select(qCactCard.cardGroupId).from(qCactCard).where(qCactCard.acctNo.eq(acct.getAcctNo())).fetchOne();
		acctCloseRptItem.org = acct.getOrg();
		acctCloseRptItem.acctNo = acct.getAcctNo();
		acctCloseRptItem.businessType = acct.getBusinessType();
		acctCloseRptItem.cancelDate = acct.getCancelDate();
		acctCloseRptItem.closedDate = acct.getClosedDate();
		acctCloseRptItem.blockCode = acct.getBlockCode();
		acctCloseRptItem.reason = reason;
		acctCloseRptItem.currBal = acct.getCurrBal();
		acctCloseRptItem.defaultLogicalCardNo = cardGroupId;
		acctCloseRptItem.currencyCode = acct.getCurrCd();
		
		return acctCloseRptItem;
	}

}
