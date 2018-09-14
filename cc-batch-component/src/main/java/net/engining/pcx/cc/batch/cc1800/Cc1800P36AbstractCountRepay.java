package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactEndChangeAcct;
import net.engining.pcx.cc.infrastructure.shared.model.QCactSubAcct;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.enums.BalanceType;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import net.engining.pcx.cc.process.service.PaymentPlanService;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewPostService;
import net.engining.pcx.cc.process.service.account.PostDetail;
import net.engining.pg.parameter.ParameterFacility;


public abstract class Cc1800P36AbstractCountRepay implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
		
	@Autowired
	private ParameterFacility parameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{new org.joda.time.LocalDate(jobParameters['batchDate'].time)}")
	private LocalDate batchDate;
	
	@Value("#{new java.util.Date(jobParameters['bizDate'].time)}")
	private Date businessDate;
	
	@Autowired
	private NewPostService newPostService;
	
	@Autowired
	private PaymentPlanService paymentPlanService;
	
	@Autowired
	private NewComputeService newComputeService;
	
	private boolean daily;
	
	protected Cc1800P36AbstractCountRepay(boolean daily)
	{
		this.daily = daily;
	}

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item)
	{
		Date now = new Date();
		// 迭代所有账户
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos: item.getAccountList().values())
		{
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos)
			{
				//在结息日，业务类型为CC或者BL的才计算应收本金
				//结息日
				LocalDate interestDate = new LocalDate(cc1800IAccountInfo.getCactAccount().getInterestDate());
				
				if(batchDate.isEqual(interestDate))
				{
					//业务类型是循环信用账户或者小额贷款，并且当前期数大于等于0,小于等于总期数
					if((cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.BL
							|| cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.CC)
						&& cc1800IAccountInfo.getCactAccount().getCurrentLoanPeriod()>= 0
						&& cc1800IAccountInfo.getCactAccount().getCurrentLoanPeriod() < cc1800IAccountInfo.getCactAccount().getTotalLoanPeriod()
//							|| cc1800IAccountInfo.getCactAccount().getBusinessType() == BusinessType.CL
							){
						
						BigDecimal repayAmt = countRepayAmt(cc1800IAccountInfo);
						//结转金额不为0，才入账。
						if ( repayAmt.compareTo(BigDecimal.ZERO) > 0 )
						{ 
							
							if( !isAlreadlyLbal(cc1800IAccountInfo) ) continue ;
							//转入贷款应还本金子账户
							AcctModel acctModel = cc1800IAccountInfo.getAcctModel();
							CactAccount acct = cc1800IAccountInfo.getCactAccount();
							Account acctParam = newComputeService.retrieveAccount(acct);
							Map<SysTxnCd, String> txnCdMap = acctParam.sysTxnCdMapping;
							PostDetail inDetail = new PostDetail();
							inDetail.setTxnDate(now);
							inDetail.setTxnTime(now);
							inDetail.setPostTxnType(PostTxnTypeDef.M);
							inDetail.setPostCode(txnCdMap.get(SysTxnCd.S19));
							inDetail.setTxnAmt(repayAmt);
							inDetail.setPostAmt(repayAmt);
							inDetail.setTxnCurrCd(acct.getCurrCd());
							inDetail.setPostCurrCd(acct.getCurrCd());
							
							newPostService.postToAccount(acctModel, batchDate, inDetail, false, 0);

							
							//从贷款剩余本金子账户转出
							// 取剩余本金子账户序号,填充至交易记录中
							PostCode outPostCode = parameterFacility.loadParameter(PostCode.class, txnCdMap.get(SysTxnCd.S18));
							PostDetail outDetail = new PostDetail();
							outDetail.setTxnDate(now);
							outDetail.setTxnTime(now);
							outDetail.setPostTxnType(PostTxnTypeDef.M);
							outDetail.setPostCode(outPostCode.postCode);
							outDetail.setTxnAmt(repayAmt);
							outDetail.setPostAmt(repayAmt);
							outDetail.setTxnCurrCd(acct.getCurrCd());
							outDetail.setPostCurrCd(acct.getCurrCd());
							
							Integer subAcctId = null; 
							for (CactSubAcct cactSubAcct : cc1800IAccountInfo.getCactSubAccts())
							{
								if (cactSubAcct.getSubAcctType().equals(outPostCode.subAcctType))
								{
									subAcctId = cactSubAcct.getSubAcctId();
								}
							}
							//肯定能找到
							newPostService.postToSubAccount(subAcctId, batchDate, outDetail, acctModel, false, 0);
						}
					}
				}
			}
		}
		return item;
	}
	
	/**
	 * 计算应还本金
	 * @param acctInfo
	 */
	private BigDecimal countRepayAmt(Cc1800IAccountInfo cc1800IAccountInfo){
		
		CactAccount acctInfo = cc1800IAccountInfo.getCactAccount();
		List<CactSubAcct> subAcctList = cc1800IAccountInfo.getCactSubAccts();
		
		//查询子账户类型为BalanceType.LOAN，用于获取月利率
		CactSubAcct subAcct = null;
		for(CactSubAcct sub : subAcctList){
			SubAcct subParam = newComputeService.retrieveSubAcct(sub, acctInfo);
			if(subParam.balanceType == BalanceType.LOAN){
				subAcct = sub;
				break;
			}		
		}
		if(subAcct == null)
			return BigDecimal.ZERO;//已还清
			//throw new IllegalArgumentException("无效的子账户");
		
		/*//获取账户参数
		Account acctParam = newComputeService.retrieveAccount(acctInfo);*/
		//获取利率参数
		InterestTable interestParam = newComputeService.retrieveInterestTable(subAcct.getInterestCode(), acctInfo);
		
		/*//获取月利率
		BigDecimal mRate = getRates(interestParam);*/
		BigDecimal repayAmt = BigDecimal.ZERO;
		
		//此处只处理按周期计息（非按日计息），这种情况不需要考虑算头不算尾，特别是当前周期的应还本金也需要计息，所以该步骤放在计息之后做。
		if ((interestParam.cycleBase == Interval.D && interestParam.cycleBaseMult == 1) == daily) {
			/*switch (acctParam.paymentMethod) {
			case MRT://等额本金-剩余靠后
				if (acctInfo.getCurrentLoanPeriod() != acctInfo.getTotalLoanPeriod() - 1){
					repayAmt = acctInfo.getTotalLoanPrincipalAmt().divide(new BigDecimal(acctInfo.getTotalLoanPeriod()), 2, RoundingMode.DOWN);
				}
				else{
					repayAmt = subAcct.getEndDayBal();
				}
				break;
			case MRF://等额本金-剩余靠前
				if (cc1800IAccountInfo.getCactAccount().getCurrentLoanPeriod() == 0){
					repayAmt = acctInfo.getTotalLoanPrincipalAmt()
							.subtract(acctInfo.getTotalLoanPrincipalAmt()
											.divide(BigDecimal.valueOf(acctInfo.getTotalLoanPeriod()), 2, RoundingMode.DOWN)
											.multiply(BigDecimal.valueOf(acctInfo.getTotalLoanPeriod() - 1)));
				}
				else{
					repayAmt = acctInfo.getTotalLoanPrincipalAmt().divide(new BigDecimal(acctInfo.getTotalLoanPeriod()), 2, RoundingMode.DOWN);
				}
				break;	
			case MSV://等额本息
				repayAmt = commonComputeClass.getMSVRepayAmt(acctInfo.getTotalLoanPrincipalAmt(), mRate, acctInfo.getTotalLoanPeriod());
				break;
			//一次还本付息、多次付息一次还本、利随本清，对于本金的计算都是一样的，应该是当前期数=总期数-1的时候才出本金。
			case OPT:
			case IFP:
				repayAmt = acctInfo.getTotalLoanPrincipalAmt();
				break;
			case IWP:
				if (acctInfo.getCurrentLoanPeriod() == acctInfo.getTotalLoanPeriod() - 1) {
					repayAmt = subAcct.getEndDayBal();
				}
				else {
					repayAmt = BigDecimal.ZERO;
				}	
				break;
			default:
				break;
			}*/
			BigDecimal notToPayLastAmt = BigDecimal.ZERO;
			PaymentPlan paymentPlan = paymentPlanService.searchPaymentPlan(acctInfo.getAcctSeq());
			for (PaymentPlanDetail detail : paymentPlan.getDetails()){
				if (detail.getLoanPeriod() >= acctInfo.getCurrentLoanPeriod() + 1 + 1 && detail.getLoanPeriod() <= acctInfo.getTotalLoanPeriod()) {
					notToPayLastAmt = notToPayLastAmt.add(detail.getPrincipalBal());
				}			
			}
			
			repayAmt = subAcct.getEndDayBal().subtract(notToPayLastAmt);
		}		
		return repayAmt;
	}
	
	/**
	 * 判断批量过程中有没有做还款,如果已经生成了本金余额成分,则不生成
	 * @param cc1800IAccountInfo
	 * @return
	 */
	private boolean isAlreadlyLbal(Cc1800IAccountInfo cc1800IAccountInfo){
		//判断是否是批量中产生的还款数据
		QCactEndChangeAcct qCactEndChangeAcct = QCactEndChangeAcct.cactEndChangeAcct;
		Long n1 = new JPAQueryFactory(em)
				.select(qCactEndChangeAcct.changeSeq)
				.from(qCactEndChangeAcct)
				.where(
						qCactEndChangeAcct.acctSeq.eq(cc1800IAccountInfo.getCactAccount().getAcctSeq()),
						qCactEndChangeAcct.txnDate.eq( businessDate )
						)
				.fetchCount();
		if(n1 == 0)  return true ;//没有产生数据，则返回为true,可以做本金余额成分生成
		
		//在批量的时候，到期日做还款，会导致余额成分多一笔，故在批量的时候判断当期到期本金的余额成分是否已经生成
		QCactSubAcct qCactSubAcct = QCactSubAcct.cactSubAcct;
		Long n2 = new JPAQueryFactory(em)
				.select(qCactSubAcct.subAcctId)
				.from(qCactSubAcct)
				.where(
						qCactSubAcct.acctSeq.eq(cc1800IAccountInfo.getCactAccount().getAcctSeq()),
						qCactSubAcct.subAcctType.eq("LBAL"),
						qCactSubAcct.stmtHist.eq(0)
						)
				.fetchCount();
		if(n2 == 0)  return true ;//没有产生数据，则返回为true,可以做本金余额成分生成
		
		return false ;
	}
}
