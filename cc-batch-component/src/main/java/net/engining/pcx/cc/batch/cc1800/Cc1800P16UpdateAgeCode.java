/**
 * 
 */
package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.enums.DelqDayInd;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;

/**
 * 更新账龄处理
 * @author linwk
 *
 */
@Service
@Scope("step")
public class Cc1800P16UpdateAgeCode implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 入账处理通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;
	
	/**
	 * FIXME 上次处理日期
	 */
	private Date lastProcessDate;
	
	@Autowired
	private SystemStatusFacility facility;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@PostConstruct
	private void init()
	{
		lastProcessDate = facility.getSystemStatus().lastProcessDate;
	}
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) {
		
		for (List<Cc1800IAccountInfo> infos : item.getAccountList().values())
		{
			for (Cc1800IAccountInfo info : infos)
			{
				if (logger.isDebugEnabled()) {
					logger.debug("更新账龄处理：Org["+info.getCactAccount().getOrg()
							+"],BusinessType["+info.getCactAccount().getBusinessType()
							+"],AcctNo["+info.getCactAccount().getAcctNo()
							+"],BatchDate["+batchDate
							+"]");
				}
				Date processDate = batchDate;
		
				// 账户属性参数
				Account account = commonCompute.retrieveAccount(info.getCactAccount());
				logger.debug("账龄提升参数:[" + account.delqDayInd + "]");
				
				if (account.businessType != BusinessType.CC && account.businessType != BusinessType.BL) {
					continue;
				}
				
				//宽限日更新首次逾期日期，如果首次逾期日期是空，并且账龄大于0（并且不是C），没有还清最小还款额，设置首次逾期日期。		
				if (processDate.equals(info.getCactAccount().getGraceDate())
						&& info.getCactAccount().getTotDueAmt().compareTo(BigDecimal.ZERO) > 0
						&& info.getCactAccount().getFirstOverdueDate() == null 
						&& (info.getCactAccount().getAgeCd().equals("0") || info.getCactAccount().getAgeCd().equals("C"))) {
					info.getCactAccount().setFirstOverdueDate(info.getCactAccount().getPmtDueDate());
				}
				// 根据账户属性参数设定的账龄提升日期进行计算
				if (
						// 溢缴款每日检查账龄
						info.getCactAccount().getAgeCd().equals("C")
						||
						(
							//以下是虚拟信用卡业务类型的账龄判断
							info.getCactAccount().getBusinessType().equals(BusinessType.CC) &&
							(
								// 到期还款日提升
								(account.delqDayInd == DelqDayInd.P && processDate.compareTo( info.getCactAccount().getPmtDueDate() ) >= 0 && lastProcessDate.compareTo( info.getCactAccount().getPmtDueDate() ) < 0 )
								// 宽限日提升
								|| (account.delqDayInd == DelqDayInd.G && info.getCactAccount().getGraceDate() != null && processDate.compareTo( info.getCactAccount().getGraceDate() ) >= 0 && lastProcessDate.compareTo( info.getCactAccount().getGraceDate() ) < 0 )
								// 账单日提升
								|| (account.delqDayInd == DelqDayInd.C && processDate.compareTo( info.getCactAccount().getInterestDate() ) >= 0 && lastProcessDate.compareTo( info.getCactAccount().getInterestDate() ) < 0 )
							)
						)
						||
						(
							//以下是小额贷款账户和消费分期的账龄判断
							(info.getCactAccount().getBusinessType().equals(BusinessType.BL) 
//									|| info.getCactAccount().getBusinessType().equals(BusinessType.CL)
									)
							&&
							//每30天提升
							info.getCactAccount().getFirstOverdueDate() != null
						)
					) {
						logger.debug("开始处理更新账龄");
						
						newAgeService.updateAgeCode(info.getAcctModel(), info.getCactAccount().getAcctSeq().toString(), TxnDetailType.A);
//						cc1800UComputeDueAndAgeCode.updateAgeCode(info.getCactAccount(), info.getCactSubAccts(), processDate, info.getGlTxnItems(), null, null, true);
					}
				
				if (account.delqDayInd != DelqDayInd.P 
						&& account.delqDayInd != DelqDayInd.G
						&& account.delqDayInd != DelqDayInd.C)
				{
					throw new IllegalArgumentException("账龄提升日期类型不正确");
				}
						
				logger.debug("更新账龄处理Process finished! AccountNo:[" + info.getCactAccount().getAcctNo() + "] BusinessType:[" 
						+ info.getCactAccount().getBusinessType() + "] ");
			}
		}
		return item;
	}

}
