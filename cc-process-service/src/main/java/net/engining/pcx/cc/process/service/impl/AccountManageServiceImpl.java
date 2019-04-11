package net.engining.pcx.cc.process.service.impl;

import com.google.common.base.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;
import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccountNo;
import net.engining.pcx.cc.infrastructure.shared.model.QCactAccountNo;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.process.model.AccountInfo;
import net.engining.pcx.cc.process.service.AccountManageService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.OrganizationContextHolder;
import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class AccountManageServiceImpl implements AccountManageService{

	@Autowired
	private ParameterFacility paramFacility;
	
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private Provider7x24 provider7x24;
	
	@Autowired
	private NewComputeService newComputeService;
			
	/**
	 * 获取账单日期
	 * 缺省实现取账户参数中的缺省账单日
	 * 客户化时可覆盖该方法实现多种账单日生成逻辑
	 * @param product
	 * @return
	 */
//	protected String genStmtCycleDay(ProductCc product){
//		// TODO 二期考虑多重账单日和账单日平衡的问题
//		return StringUtils.leftPad(product.dfltCycleDay.toString(), 2, '0');
//	}
//	protected String genStmtCycleDay(int  acctNo){
//		CactAccount acct = em.find(CactAccount.class, acctNo);
//		return String.valueOf(paramFacility.getParameter(Account.class, acct.getParamId()).dfltCycleDay);
//	}
	
//	protected Date genNextStmtDateAtInit(Integer stmtDay, Date processDate){
//		Date firstStmtDay = null;
//		if (stmtDay == 99) {
//			firstStmtDay = DateUtils.addMonths(processDate, 1);
//			firstStmtDay = DateUtils.setDays(firstStmtDay, 1);
//			firstStmtDay = DateUtils.addDays(firstStmtDay, -1);
//		} else {
//			firstStmtDay = DateUtils.setDays(processDate, stmtDay);
//		}		
//		while (firstStmtDay.compareTo(processDate) <= 0){
//			firstStmtDay = DateUtils.addMonths(firstStmtDay, 1);
//		}
//		return firstStmtDay;
//	}

	/**
	 * 建账
	 * <ol>
	 *   <li>创建一条CactAccount账户记录。</li>
	 *   <li>判断建账信息里的业务日期是否为空，如果不为空，账户的创建日期就取建账信息里的业务日期，否则就取批量日期。</li>
	 *   <li>判断建账信息里的账户额度是否为空，如果不为空，账户额度就取建账信息里的账户额度，否则就取账户参数里的默认额度，如果默认额度是空，就填0。</li>
	 *   <li>判断总期数是否为-1，如果是，当前期数填-1，否则，当前期数填0。</li>
	 * </ol>
	 * @param acctInfo 建账信息
	 * @return accountSeq 账户序号
	 */
	@Override
	public int createAccount(AccountInfo acctInfo) {
//		SystemStatus system = systemStatusFacility.getSystemStatus();
		CactAccount acct = new CactAccount();
		acct.setAcctNo(acctInfo.getAcctNo());
		acct.setAcctParamId(acctInfo.getParamId());
		acct.setCustId(acctInfo.getCustId());
		acct.setOrg(OrganizationContextHolder.getCurrentOrganizationId());
//		if(acctInfo.getBusinessDate() != null)
//			acct.setSetupDate(acctInfo.getBusinessDate());
//		else
		//fix bug: 建账日期用系统BizDate
		acct.setSetupDate(provider7x24.getCurrentDate().toDate());
		//账户创建自然日用当前系统自然日期
		acct.setStartDate(new Date());
		
		//获取账户参数
		Account acctParam = paramFacility.loadParameter(Account.class, acct.getAcctParamId(), provider7x24.getCurrentDate().toDate());
		acct.setBusinessType(acctParam.businessType);
		acct.setCurrCd(acctParam.currencyCode);
		acct.setGraceDaysFullInd(true);
		acct.setAgeCd("0");
		
		if(acctInfo.getAcctLimit() != null){
			acct.setAcctLimit(acctInfo.getAcctLimit());
		}else{
			acct.setAcctLimit(acctParam.defaultLimit == null ? BigDecimal.ZERO : new BigDecimal(acctParam.defaultLimit));
		}
		acct.setOwningBranch(acctInfo.getOwningBranch());
		
		if (acctInfo.getTotalLoanPeriod() != null) {
			acct.setTotalLoanPeriod(acctInfo.getTotalLoanPeriod());
		}
		else {
			acct.setTotalLoanPeriod(acctParam.intSettleFrequency);
		}
		
		if(-1 == acct.getTotalLoanPeriod())
			acct.setCurrentLoanPeriod(-1);
		else
			acct.setCurrentLoanPeriod(0);
		
		if (acctInfo.getAutoPayAcctSeqInSystem() != null) {
			CactAccount cactAccount = em.find(CactAccount.class, acctInfo.getAutoPayAcctSeqInSystem());
			if (cactAccount == null) {
				throw new IllegalArgumentException("设置系统内自动还款的活期账户[" + acctInfo.getAutoPayAcctSeqInSystem()
						+ "]没有找到.");
			}
			
			if (!cactAccount.getCustId().equals(acctInfo.getCustId())) {
				throw new IllegalArgumentException("设置系统内自动还款的活期账户[" + acctInfo.getAutoPayAcctSeqInSystem()
						+ "和当前创建的贷款账户不属于同一客户.");
			}
			else {
				acct.setAutoPayAcctSeqInSystem(acctInfo.getAutoPayAcctSeqInSystem());
			}	
		}

		Date interestStartDate = null;
		if (acctParam.intUnit == Interval.D && (acctParam.intFirstPeriodAdj == null || !acctParam.intFirstPeriodAdj)) {
			interestStartDate = DateUtils.addDays(acct.getSetupDate(), -1);
		}
		else {
			interestStartDate = acct.getSetupDate();
		}
		Date interestDate = newComputeService.getNextInterstDate(acct, interestStartDate, acctParam, acct.getBillingCycle());
		acct.setInterestDate(interestDate);
		acct.setFirstStmtDate(interestDate);
		acct.setBeginBal(BigDecimal.ZERO);
		acct.setCtdCrAdjAmt(BigDecimal.ZERO);
		acct.setCtdPaymentAmt(BigDecimal.ZERO);
		acct.setCtdRefundAmt(BigDecimal.ZERO);
		acct.setCurrBal(BigDecimal.ZERO);
		acct.setPmtDueDayBal(BigDecimal.ZERO);
		acct.setQualGraceBal(BigDecimal.ZERO);
		acct.setTotDueAmt(BigDecimal.ZERO);
		
		// FIXME 修正
		acct.setGraceDaysFullInd(true);
		acct.setOwningBranch(OrganizationContextHolder.getCurrentOrganizationId());//FIXME 增加按所属分支行
//		acct.setPaymentHist(" ");
//		acct.setBillingCycle(" "); //oracle不允许在nullable的字段里插入空值，所以改为空格 2014.12.17
		acct.setWaiveLatefeeInd(false);
		acct.setBizDate(provider7x24.getCurrentDate().toDate());
		acct.fillDefaultValues();
		
		em.persist(acct);
		return acct.getAcctSeq();
	}
	
	/**
	 * 根据客户号创建账号AccountNo
	 * <ol>
	 *   <li>根据客户号创建一个CactAccountNo记录，并返回账号AccountNo。</li>
	 * </ol>
	 * @param custId 客户号
	 * @return accountNo 账号
	 */
	@Override
	public int createAccountNo(String custId) {
		
		CactAccountNo acctNo = new CactAccountNo();
		acctNo.setOrg(OrganizationContextHolder.getCurrentOrganizationId());
		acctNo.setCustId(custId);
		acctNo.setBizDate(provider7x24.getCurrentDate().toDate());
		acctNo.fillDefaultValues();
		em.persist(acctNo);
		return acctNo.getAcctNo();
	}

	/**
	 * 根据客户号查询账号AccountNo
	 * <ol>
	 *   <li>根据客户号返回一个AccountNo，没有返回空。</li>
	 * </ol>
	 * @param custId 客户号
	 * @return accountNo 若账号存在就返回，否则返回空。
	 */
	@Override
	public Integer queryAccountNo(String custId) {
		QCactAccountNo qCactAccountNo = QCactAccountNo.cactAccountNo;
		Integer acctNo = new JPAQueryFactory(em)
				.select(qCactAccountNo.acctNo)
				.from(qCactAccountNo)
				.where(qCactAccountNo.custId.eq(custId))
				.fetchOne();
		
		if(Optional.fromNullable(acctNo).isPresent()){
			return acctNo;
		}
		else{
			throw new ErrorMessageException(ErrorCode.Null,String.format("客户号：%s，无法找到对应账号", custId));
		}
			
	}
}
