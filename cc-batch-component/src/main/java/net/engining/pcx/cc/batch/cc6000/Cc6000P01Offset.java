package net.engining.pcx.cc.batch.cc6000;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewInterestService;
import net.engining.pcx.cc.process.service.common.CreateSubAccount;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;
import net.engining.pcx.cc.process.service.support.OffsetService;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 延迟冲销处理
 * @author yinxia
 * 
 */
@Service
@Scope("step")
public class Cc6000P01Offset implements ItemProcessor<Cc6000IAccountInfo, Cc6000IAccountInfo> {
	
	/**
	 * 批量处理公共组件
	 */
	@Autowired
	private ParameterFacility parameterFacility;
	
	/**
	 * 创建子账户
	 */
	@Autowired
	private CreateSubAccount createSubAccount;
	
	/**
	 * 入账通用业务组件
	 */
	@Autowired
	private NewComputeService commonCompute ;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;

	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 最小还款额及账龄计算业务组件
	 */
	@Autowired
	private UComputeDueAndAgeCode uComputeDueAndAgeCode;
	
	@Autowired
	private NewInterestService newInterestService;
	
	@Autowired
	private OffsetService offsetService;
	
	@Autowired
	private NewAgeService newAgeService;

	@Override
	public Cc6000IAccountInfo process(Cc6000IAccountInfo item) throws Exception {
		
		LocalDate businessDate = new LocalDate(systemStatusFacility.getSystemStatus().businessDate);
		
		boolean penaltyProcessed = false;
		AcctModel model = new AcctModel();
		model.setCactAccount(item.getCactAccount());
		model.setCactSubAccts(item.getCactSubAccts());
		model.setCactAgeDues(item.getCactAgeDues());
		for (CactTxnPost cactTxnPost : item.getCactTxnPosts())
		{
			if (!penaltyProcessed)
			{
				newInterestService.settlePenalty(model, model, businessDate, cactTxnPost.getTxnDetailSeq(), cactTxnPost.getTxnDetailType());
				penaltyProcessed = true;
			}

			CactAccount cactAccount = model.getCactAccount();
			CactSubAcct cactSubAcct = em.find(CactSubAcct.class, cactTxnPost.getSubAcctId());
			// 还款冲销
			offsetService.offsetBalance(model, cactSubAcct, cactTxnPost.getPostAmt(), cactTxnPost.getTxnDetailSeq(), cactTxnPost.getTxnDetailType() ,"");
			// 还款冲销最小还款额
			offsetService.offsetMinDue(model, cactTxnPost.getPostAmt());

			// 更新账龄
			newAgeService.updateAgeCode(model, cactTxnPost.getTxnDetailSeq(), cactTxnPost.getTxnDetailType());

			// 更新首次逾期日期
			if (cactAccount.getAgeCd().equals("C") || cactAccount.getAgeCd().equals("0"))
			{
				cactAccount.setFirstOverdueDate(null);
			}
			cactTxnPost.setPostingFlag(PostingFlag.F00);
			cactTxnPost.setLastUpdateDate(new Date());
		}
		
		return item;
	}
	
}
