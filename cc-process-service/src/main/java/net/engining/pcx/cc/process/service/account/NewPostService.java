package net.engining.pcx.cc.process.service.account;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.BalanceDirection;
import net.engining.pcx.cc.param.model.enums.TransMergeMethod;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.support.OffsetService;
import net.engining.pcx.cc.process.service.support.PostPostEvent;
import net.engining.pcx.cc.process.service.support.PrePostEvent;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.OrganizationContextHolder;
import net.engining.pg.parameter.ParameterFacility;

@Service
public class NewPostService
{
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private ParameterFacility parameterFacility;

	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	private OffsetService offsetService;
	
	@Autowired
	private NewAgeService newAgeService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Transactional
	public boolean postToSubAccount(int subAcctId, LocalDate postDate, PostDetail detail, boolean offset, int stmtHist)
	{
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, subAcctId);
		
		AcctModel model = loadAcctModel(cactSubAcct.getAcctSeq());
		
		return postToSubAccount(subAcctId, postDate, detail, model, offset, stmtHist);
	}
	
	/**
	 * 入账到指定subAcctId
	 * @param subAcctId 子账户号
	 * @param detail 交易明细信息
	 * @param model 账户模型上下文
	 * @param offset 是否冲销
	 * @param stmtHist 子账户账期
	 */
	@Transactional
	public boolean postToSubAccount(int subAcctId, LocalDate postDate, PostDetail detail, AcctModel model, boolean offset, int stmtHist)
	{
		checkNotNull(subAcctId);
		checkNotNull(detail);
		
		BigDecimal txnAmount = detail.getTxnAmt().setScale(newComputeService.getBalanceScale(), RoundingMode.HALF_UP);
		BigDecimal postAmount = detail.getPostAmt().setScale(newComputeService.getBalanceScale(), RoundingMode.HALF_UP);
		
		if (txnAmount.compareTo(detail.getTxnAmt()) != 0)
		{
			logger.warn("子账户[{}]入账，txnAmt在进行舍入后与原值不同，可能会造成问题: {} -> {} ", subAcctId, txnAmount, detail.getTxnAmt());
			assert false;	//避免生产断批
		}

		if (postAmount.compareTo(detail.getPostAmt()) != 0)
		{
			logger.warn("子账户[{}]入账，postAmt在进行舍入后与原值不同，可能会造成问题: {} -> {} ", subAcctId, postAmount, detail.getPostAmt());
			assert false;	//避免生产断批
		}
		
		//入账前事件
		PrePostEvent prePostEvent = new PrePostEvent(this);
		prePostEvent.setSubAcctId(subAcctId);
		prePostEvent.setDetail(detail);
		prePostEvent.setModel(model);
		prePostEvent.setOffset(offset);
		prePostEvent.setStmtHist(stmtHist);
		ctx.publishEvent(prePostEvent);
		
		PostCode postCode = parameterFacility.loadParameter(PostCode.class, detail.getPostCode());
		CactSubAcct cactSubAcct = em.find(CactSubAcct.class, subAcctId);
		CactAccount cactAccount = model.getCactAccount();
		checkNotNull(cactSubAcct, "子账户[%s]找不到", subAcctId);
		
		//建立交易记录
		CactTxnPost cactTxnPost = new CactTxnPost();
		cactTxnPost.setOrg(OrganizationContextHolder.getCurrentOrganizationId());
		cactTxnPost.setAcctSeq(cactAccount.getAcctSeq());
		cactTxnPost.setBusinessType(cactAccount.getBusinessType());
		cactTxnPost.setTxnDate(detail.getTxnDate());
		cactTxnPost.setTxnTime(detail.getTxnTime());
		cactTxnPost.setPostTxnType(detail.getPostTxnType());
		cactTxnPost.setPostCode(detail.getPostCode());
		cactTxnPost.setDbCrInd(postCode.processor.getTxnDirection());
		cactTxnPost.setTxnAmt(txnAmount);
		cactTxnPost.setPostAmt(postAmount);
		cactTxnPost.setPostDate(postDate.toDate());
		cactTxnPost.setTxnCurrCd(detail.getTxnCurrCd());
		cactTxnPost.setPostCurrCd(detail.getTxnCurrCd());
		cactTxnPost.setTxnDesc(StringUtils.defaultIfBlank(detail.getTxnDesc(), postCode.description));
		cactTxnPost.setTxnShortDesc(StringUtils.defaultIfBlank(detail.getTxnShortDesc(), postCode.shortDesc));
		cactTxnPost.setPostingFlag(PostingFlag.FFF);
		cactTxnPost.setTxnDetailSeq(detail.getTxnDetailSeq());
		cactTxnPost.setTxnDetailType(detail.getTxnDetailType());
		cactTxnPost.setSubacctParamId(cactSubAcct.getSubacctParamId());
		cactTxnPost.setSubAcctId(subAcctId);
		cactTxnPost.setAgeCdB4Posting(cactAccount.getAgeCd()); //入账前账龄
		cactTxnPost.setTxnType(detail.getTxnType());
		cactTxnPost.setOppAcct(detail.getOppAcct());
		cactTxnPost.setLastUpdateDate(new Date());
		em.persist(cactTxnPost);
		
		// TODO review
		if (model.getCactTxnPosts() != null)
		{
			model.getCactTxnPosts().add(cactTxnPost);
		}
		
		// 00：成功入账
		cactTxnPost.setPostingFlag(PostingFlag.F00);
		// 计算交易的账单日期
		cactTxnPost.setStmtDate(cactAccount.getInterestDate());
		
		// MEMO交易不执行入账逻辑
		if (TxnDirection.O == cactTxnPost.getDbCrInd()) 
			return true;

		//具体入账
		SubAcct subAcct =  newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);

		BigDecimal deltaAmount;
		if(TxnDirection.C.equals(cactTxnPost.getDbCrInd()))
		{
			deltaAmount = postAmount.negate();
		}
		else
		{
			deltaAmount = postAmount;
		}
		
		//余额发生变动时还是以当前余额为准，不使用7x24中的余额
		BigDecimal amt = cactSubAcct.getCurrBal().add(deltaAmount);
		
		if (amt.signum() > 0 && subAcct.balanceDirection == BalanceDirection.C ||
			(amt.signum() < 0 && subAcct.balanceDirection == BalanceDirection.D))
		{
			// 余额符号与余额方向不相同则挂账
			// TODO:挂账
			cactTxnPost.setPostingFlag(PostingFlag.F97);
			logger.warn("交易挂账，余额符号与余额方向不相同:subAcctId[{}], stmtHist[{}], offset[{}], detail[{}]", subAcctId, stmtHist, offset,  ReflectionToStringBuilder.toString(detail));
			return false;
		}
		else
		{
			provider7x24.increaseBalance(cactSubAcct, deltaAmount);
			//更新账户余额
			cactAccount.setCurrBal(cactAccount.getCurrBal().add(deltaAmount));

		}
		cactTxnPost.setAcctCurrBal(cactAccount.getCurrBal());

		if (logger.isDebugEnabled()) {
			logger.debug("金融交易,批量入账:PostAmt[{}],CurrBal[{}]", postAmount, cactSubAcct.getAcctSeq());
		}
		
		if (offset)
		{
			if (cactTxnPost.getDbCrInd() == TxnDirection.C)
			{
				cactAccount.setQualGraceBal(cactAccount.getQualGraceBal().subtract(postAmount));
				if (cactAccount.getQualGraceBal().signum() < 0) {
					cactAccount.setQualGraceBal(BigDecimal.ZERO);
				}

				if (provider7x24.shouldDeferOffset())
				{
					//入账交易结果标志记为"FTD|成功入账，延时待冲销"。
					cactTxnPost.setPostingFlag(PostingFlag.FTD);
				}
				else
				{
					// 还款冲销
					offsetService.offsetBalance(model, cactSubAcct, detail.getPostAmt(), detail.getTxnDetailSeq(), detail.getTxnDetailType() ,detail.getTxnType() );
					// 还款冲销最小还款额
					offsetService.offsetMinDue(model, postAmount);

					// 更新账龄
					newAgeService.updateAgeCode(model, detail.getTxnDetailSeq(), detail.getTxnDetailType());

					// 更新首次逾期日期
					if (cactAccount.getAgeCd().equals("C") || cactAccount.getAgeCd().equals("0"))
					{
						cactAccount.setFirstOverdueDate(null);
					}
				}
			}
		}
		//更新账户层统计量
		updatingAccountStatistics(cactTxnPost, cactAccount, cactSubAcct);
		
		//更新入账后账龄
		cactTxnPost.setAgeCdAfterPosting(cactAccount.getAgeCd());
		
		//入账后事件
		PostPostEvent postPostEvent = new PostPostEvent(this);
		postPostEvent.setAcctModel(model);
		postPostEvent.setSubAcctId(subAcctId);
		postPostEvent.setPostCode(detail.getPostCode());
		postPostEvent.setPostAmount(postAmount);
		postPostEvent.setPostDate(postDate);
		postPostEvent.setDetail(detail);
		postPostEvent.setOffset(offset);
		postPostEvent.setStmtHist(stmtHist);
		postPostEvent.setTxnPostSeq(cactTxnPost.getTxnSeq());
		ctx.publishEvent(postPostEvent);
		return true;
	}

	
	@Transactional
	public boolean postToAccount(int acctSeq, LocalDate postDate, PostDetail detail, boolean offset, int stmtHist)
	{
		AcctModel model = loadAcctModel(acctSeq);
	
		return postToAccount(model, postDate, detail, offset, stmtHist);
	}
	/**
	 * 根据acctSeq和detail.postCode入账
	 * @param detail
	 */
	@Transactional
	public boolean postToAccount(AcctModel targetModel, LocalDate postDate, PostDetail detail, boolean offset, int stmtHist)
	{
		PostCode postCode = parameterFacility.loadParameter(PostCode.class, detail.getPostCode());

		//先找或建子账户
		
		CactSubAcct cactSubAcct = null;
		
		Account account = newComputeService.retrieveAccount(targetModel.getCactAccount());
		
		// 循环所有现有子账户,查找是否已经存在当期的交易应入账的子账户记录
		// 确定子账户参数ID
		SubAcct subAcct = newComputeService.retrieveSubAcct(account.subAcctParam.get(postCode.subAcctType), targetModel.getCactAccount());

		for (CactSubAcct sa : targetModel.getCactSubAccts())
		{
			if (sa.getSubacctParamId().equals(subAcct.subAcctId) && sa.getStmtHist() == stmtHist)
			{
				if (subAcct.transMergeMethod == TransMergeMethod.A || 
					(subAcct.transMergeMethod == TransMergeMethod.D && sa.getSetupDate().equals(provider7x24.getCurrentDate()))) 
				{
					cactSubAcct = sa;
					break;
				}
			}
		}
		
		if (cactSubAcct == null)
		{
			//没找到则新建
			CactAccount cactAccount = targetModel.getCactAccount();
			cactSubAcct = new CactSubAcct();
			cactSubAcct.setOrg(cactAccount.getOrg());
			cactSubAcct.setSubAcctType(subAcct.subAcctType);
			cactSubAcct.setAcctSeq(cactAccount.getAcctSeq());
			cactSubAcct.setBusinessType(cactAccount.getBusinessType());
			cactSubAcct.setSubacctParamId(subAcct.subAcctId);
			cactSubAcct.setStmtHist(stmtHist);
			//已删除期末余额（endBal）
			//先把期初余额初始化为零，因为这里没有子账户组的概念，考虑到冲销能够找到最早的子账户，
			//所以在确定期初余额的时候，需要累积该账户的所有子账户中paramid相同并且multSaleInd为true的期末当期余额。
			
			//除了这些子账户，paramid相同并且multSaleInd为false认为是单笔交易对应的子账户，这类子账户是不会再有新的交易
			//追加上去的，所以不需要根据这类子账户再创建出新的子账户。
			//paramid不相同的子账户无需累积，直接跳过就行了。
			cactSubAcct.setBeginBal(BigDecimal.ZERO);
			/*for (CactSubAcct sa : cactSubAccts){
				if (subAcct.subAcctId.equals(sa.getParamId()) && subAcct.multSaleInd)
				{
					cactSubAcct.setBeginBal( cactSubAcct.getBeginBal().add( sa.getEndBal()) );
				}
			}*/
			cactSubAcct.setCurrBal(BigDecimal.ZERO);
			cactSubAcct.setEndDayBal(BigDecimal.ZERO);
			cactSubAcct.setCurrCd(cactAccount.getCurrCd());
			cactSubAcct.setTotDueAmt(BigDecimal.ZERO);
			cactSubAcct.setIntPending(BigDecimal.ZERO);
			cactSubAcct.setIntReceivable(BigDecimal.ZERO);
			cactSubAcct.setSetupDate(postDate.toDate());//先建账，再起息。
			cactSubAcct.setLastUpdateDate(new Date());
			cactSubAcct.setIntAccrual(BigDecimal.ZERO);
			cactSubAcct.setEndDayBeforeBal(BigDecimal.ZERO);
			cactSubAcct.setPenalizedAmt(BigDecimal.ZERO);
			cactSubAcct.setIntPenaltyAccrual(BigDecimal.ZERO);
			cactSubAcct.setPenalizedInterestCode(subAcct.penalizedInterestTable);
			cactSubAcct.setAddupAmt(BigDecimal.ZERO);
			cactSubAcct.setBizDate(provider7x24.getCurrentDate().toDate());
			cactSubAcct.fillDefaultValues();
			em.persist(cactSubAcct);
			targetModel.getCactSubAccts().add(cactSubAcct);
		}
		
		return postToSubAccount(cactSubAcct.getSubAcctId(), postDate, detail, targetModel, offset, stmtHist);
	}
	
	private void updatingAccountStatistics(CactTxnPost cactTxnPost, CactAccount cactAccount, CactSubAcct cactSubAcct)
	{
		
		SubAcct subAcct = newComputeService.retrieveSubAcct(cactSubAcct, cactAccount);
		switch(subAcct.balanceType)
		{
		case LOAN:
			//贷款当前期数=0，设置贷款总金额
			if (cactAccount.getCurrentLoanPeriod() == 0 && cactTxnPost.getDbCrInd() == TxnDirection.D)
			{
				if (cactAccount.getTotalLoanPrincipalAmt() == null )
				{
					cactAccount.setTotalLoanPrincipalAmt(BigDecimal.ZERO);
				}
				cactAccount.setTotalLoanPrincipalAmt(cactAccount.getTotalLoanPrincipalAmt().add(cactTxnPost.getPostAmt()));
			}
			break;
		default:
			break;
		}
		
	}
	
	public AcctModel loadAcctModel(int acctSeq)
	{
		AcctModel model = new AcctModel();
		model.setCactAccount(em.find(CactAccount.class, acctSeq));
		
		QCactSubAcct qSubAcct = QCactSubAcct.cactSubAcct;
		List<CactSubAcct> subAccts = new JPAQueryFactory(em)
				.select(qSubAcct)
				.from(qSubAcct).where(qSubAcct.acctSeq.eq(acctSeq))
				.fetch();
		model.setCactSubAccts(subAccts);
		
		QCactAgeDue qAgeDue = QCactAgeDue.cactAgeDue;
		List<CactAgeDue> ageDues = new JPAQueryFactory(em)
				.select(qAgeDue)
				.from(qAgeDue).where(qAgeDue.acctSeq.eq(acctSeq))
				.orderBy(qAgeDue.seq.asc())
				.fetch();
		model.setCactAgeDues(ageDues);
		
		return model;
	}
}
