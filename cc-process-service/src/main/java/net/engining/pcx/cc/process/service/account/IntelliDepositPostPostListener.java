package net.engining.pcx.cc.process.service.account;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.process.service.account.NewInterestService.InterestCycleRestMethod;
import net.engining.pcx.cc.process.service.support.PostPostEvent;
import net.engining.pg.parameter.ParameterFacility;

/**
 * CactTxnPost当日入账交易表，产生入账后的入账事件监听；<br>
 * 该监听只处理智能存款业务，用于触发其借贷标志为借方的时候，进行计息
 * @author luxue
 *
 */
@Service
public class IntelliDepositPostPostListener implements ApplicationListener<PostPostEvent>
{
	@Autowired
	private ParameterFacility parameterFacility;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private NewComputeService newComputeService;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;

	@Override
	public void onApplicationEvent(PostPostEvent event)
	{
		PostDetail detail = event.getDetail();
		
		CactAccount cactAccount = event.getAcctModel().getCactAccount();
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, event.getSubAcctId());
		Account account = newComputeService.retrieveAccount(cactAccount);
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
		PostCode postCode = parameterFacility.loadParameter(PostCode.class, detail.getPostCode());
		//如果业务类型为智能存款，借贷标志位借记，则触发计息
		
		if(cactSubAcct.getBusinessType() == BusinessType.ID && 
			postCode.processor.getTxnDirection() == TxnDirection.D)
		{
			LocalDate endDate = new LocalDate(systemStatusFacility.getSystemStatus().processDate);	//计息计到最近一次批量对应的日期

			List<InterestTable> tables = newInterestService.retrieveInterestTable(cactAccount, cactSubAcct, account, subAcct, endDate);
			
			BigDecimal interest = newInterestService.calcInterest(
					new LocalDate(cactAccount.getSetupDate()),		// 从建账日，即存款日开始计
					endDate.plusDays(1),							// 开区间，计到当endDate
					event.getPostAmount(),							// 计息金额
					tables,
					newComputeService.getInterestRateScale(),
					InterestCycleRestMethod.Daily,					// 剩余周期按日息计算
					detail.getAcctBal() == null ? event.getPostAmount() 
							: detail.getAcctBal());					// 靠档金额，如不指定则默认是计息金额
			if (interest != null)
			{
				//计息成功则更新到 receivable 上
				cactSubAcct.setIntReceivable(interest);
			}
		}
	}
}
