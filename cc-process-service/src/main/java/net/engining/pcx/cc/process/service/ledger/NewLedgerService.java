package net.engining.pcx.cc.process.service.ledger;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.ApGlTxn;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.enums.PostGlInd;
import net.engining.pcx.cc.process.service.account.NewAgeService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.Provider4Organization;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 总账处理服务
 * 
 * @author luxue
 *
 */
@Service
public class NewLedgerService {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ParameterFacility parameterFacility;

	@Autowired
	private NewAgeService newAgeService;

	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	Provider4Organization provider4Organization;
	
	private static String currency = "156";
	/**
	 * 入总账交易流水
	 * 
	 * @param acctSeq
	 *            账户流水号
	 * @param postCode
	 *            交易码
	 * @param postAmount
	 *            交易金额
	 * @param postDate
	 *            交易日期
	 * @param txnDetailSeq
	 *            相应的业务表的主键
	 * @param txnDetailType
	 *            交易流水对应的业务明细类型，对应到相应的业务表
	 * @param newAgeGroupCd
	 *            账龄组
	 */
	@Transactional
	public void postLedger(int acctSeq, String postCode, BigDecimal postAmount, LocalDate postDate, String txnDetailSeq,
			TxnDetailType txnDetailType, AgeGroupCd newAgeGroupCd) {
		checkArgument(postAmount.signum() >= 0);

		if (postAmount.signum() == 0) {
			// 0金额不处理
			return;
		}

		// 加载数据
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);

		PostCode postCodeParam = parameterFacility.loadParameter(PostCode.class, postCode);

		postLedger(cactAccount, postCodeParam, postAmount, postDate, txnDetailSeq, txnDetailType, postDate, postDate,
				newAgeGroupCd);
	}

	/**
	 * 入总账交易流水
	 * 
	 * @param acctSeq
	 * @param postCode
	 * @param postAmount
	 * @param postDate
	 * @param txnDetailSeq
	 * @param txnDetailType
	 */
	@Transactional
	public void postLedger(int acctSeq, String postCode, BigDecimal postAmount, LocalDate postDate, String txnDetailSeq,
			TxnDetailType txnDetailType) {
		checkArgument(postAmount.signum() > 0);

		// if (postAmount.signum() == 0)
		// {
		// //0金额不处理
		// return;
		// }

		// 加载数据
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);

		PostCode postCodeParam = parameterFacility.loadParameter(PostCode.class, postCode);

		postLedger(cactAccount, postCodeParam, postAmount, postDate, txnDetailSeq, txnDetailType, postDate, postDate,
				newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()));

	}

	/**
	 * 入总账交易流水
	 * @param acctSeq
	 * @param postCode
	 * @param postAmount
	 * @param postDate
	 * @param txnDetailSeq
	 * @param txnDetailType
	 * @param clearDate
	 * @param transDate
	 * @param newAgeGroupCd
	 */
	@Transactional
	public void postLedger(int acctSeq, String postCode, BigDecimal postAmount, LocalDate postDate, String txnDetailSeq,
			TxnDetailType txnDetailType, LocalDate clearDate, LocalDate transDate, AgeGroupCd newAgeGroupCd) {
		checkArgument(postAmount.signum() > 0);
		CactAccount cactAccount = em.find(CactAccount.class, acctSeq);
		PostCode postCodeParam = parameterFacility.loadParameter(PostCode.class, postCode);
		postLedger(cactAccount, postCodeParam, postAmount, postDate, txnDetailSeq, txnDetailType, clearDate, transDate,
				newAgeService.calcAgeGroupCd(cactAccount.getAgeCd()));
	}

	/**
	 * 入总账交易流水
	 * 
	 * @param cactAccount
	 * @param postCodeParam
	 * @param postAmount
	 * @param postDate
	 * @param txnDetailSeq
	 * @param txnDetailType
	 * @param newAgeGroupCd
	 */
	@Transactional
	public void postLedger(CactAccount cactAccount, PostCode postCodeParam, BigDecimal postAmount, LocalDate postDate,
			String txnDetailSeq, TxnDetailType txnDetailType, LocalDate clearDate, LocalDate transDate,
			AgeGroupCd newAgeGroupCd) {
		ApGlTxn apGltxn = new ApGlTxn();
		apGltxn.setOrg(cactAccount.getOrg());
		apGltxn.setBranchNo(cactAccount.getBranchNo());
		apGltxn.setAcctSeq(cactAccount.getAcctSeq());
		apGltxn.setCurrCd(cactAccount.getCurrCd());
		apGltxn.setPostCode(postCodeParam.postCode);
		apGltxn.setPostDesc(postCodeParam.shortDesc);
		apGltxn.setTxnDirection(postCodeParam.processor.getTxnDirection());
		apGltxn.setPostDate(postDate.toDate());
		apGltxn.setPostAmount(postAmount.setScale(2, RoundingMode.HALF_UP));
		if (cactAccount.getBlockCode() != null && cactAccount.getBlockCode().contains("W")) {
			apGltxn.setPostGlInd(PostGlInd.Writeoff);
		} else {
			apGltxn.setPostGlInd(PostGlInd.Normal);
		}
		apGltxn.setOwingBranch(cactAccount.getOwningBranch());
		apGltxn.setAcqBranch(cactAccount.getBranchNo());
		apGltxn.setAgeGroupCd(newAgeGroupCd);
		apGltxn.setTxnDetailType(txnDetailType);
		apGltxn.setTxnDetailSeq(txnDetailSeq);
		apGltxn.setPostType(PostTypeDef.SYSM);
		apGltxn.setAccountDesc(postCodeParam.description);
		apGltxn.setClearDate(clearDate.toDate());
		apGltxn.setTransDate(transDate.toDate());
		apGltxn.setBizDate(provider7x24.getCurrentDate().toDate());
		apGltxn.fillDefaultValues();
		em.persist(apGltxn);
	}


	/**
	 * 日终批量记账 当日总账交易流水
	 * 
	 * @param acctSeq
	 * @param postCode
	 * @param postAmount
	 * @param postDate
	 * @param clearDate
	 * @param trdate
	 * @param txnDetailType
	 * @param txnDetailSeq
	 */
	@Transactional
	public void postLedger(Integer acctSeq, String postCode, BigDecimal postAmount, Date postDate, Date clearDate,
			Date trdate, TxnDetailType txnDetailType, String txnDetailSeq) {
		PostCode postCodeParam = parameterFacility.loadParameter(PostCode.class, postCode);
		ApGlTxn apGltxn = new ApGlTxn();
		apGltxn.setOrg(provider4Organization.getCurrentOrganizationId());
		apGltxn.setBranchNo(provider4Organization.getCurrentOrganizationId());
		apGltxn.setCurrCd(currency);
		apGltxn.setAcctSeq(acctSeq);
		apGltxn.setPostCode(postCodeParam.postCode);
		apGltxn.setPostDesc(postCodeParam.description);
		apGltxn.setPostDate(postDate);
		apGltxn.setPostType(PostTypeDef.SYSM);
		apGltxn.setTransDate(trdate);
		apGltxn.setClearDate(clearDate);
		apGltxn.setTxnDetailType(txnDetailType);
		apGltxn.setTxnDetailSeq(txnDetailSeq);
		apGltxn.setBizDate(postDate);
		apGltxn.setPostAmount(postAmount);
		apGltxn.setPostGlInd(PostGlInd.Normal);
		apGltxn.fillDefaultValues();
		em.persist(apGltxn);
		
	}

}
