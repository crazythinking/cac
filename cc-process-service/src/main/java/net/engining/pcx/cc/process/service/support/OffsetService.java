package net.engining.pcx.cc.process.service.support;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.SubAcctType;
import net.engining.pcx.cc.param.model.enums.BalanceType;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.model.AcctModel;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pg.parameter.ParameterFacility;

@Service
public class OffsetService
{
	@Autowired
	private ParameterFacility parameterFacility;

	@Autowired
	private Provider7x24 provider7x24;

	/**
	 * 获取参数工具类
	 */
	@Autowired
	private ParameterFacility parameterCacheFacility;
	
	/**
	 * 账务处理通用业务组件
	 */
	@Autowired
	private NewComputeService newComputeService;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ApplicationContext ctx;

	/**
	 * 冲销子账户数据模型:
	 * 子账户账期(Integer)组合，账期相同的根据子账户类型(String)组合子账户(List<CactSubAcct>)
	 */

	/**
	 * 普通还款分配:生成待冲销账户的数据模型，执行还款分配处理方法，
	 * 把分配金额的余额更新到溢缴款子账户（depositCactSubAcct）的当前余额字段。
	 */
	public void offsetBalance(AcctModel model, CactSubAcct depositCactSubAcct, BigDecimal postAmount, String txnDetailSeq, TxnDetailType txnDetailType ,String txnType ) {
		CactAccount cactAccount = model.getCactAccount();
		List<CactSubAcct> subAccts = model.getCactSubAccts();
		
		// 根据账户属性ID，查找账户属性
		Account account = newComputeService.retrieveAccount(cactAccount);
		
		if (account.paymentHier == null)
		{
			//不配不处理冲销（比如活期账户）
			return;
		}
		//取当前账龄对应的子账户类型的冲销顺序
		List<SubAcctType> depositSubAcctTypeList = account.paymentHier.get(cactAccount.getAgeCd());
		
		//减免类交易特殊处理，只冲相应的余额成分
		if( "PnitReducation".equals(txnType) ) {
			List<SubAcctType> list = new ArrayList<SubAcctType>();
			
			SubAcctType sat = new SubAcctType();
			sat.subAcctType = "PNIT";
			sat.description = "罚息余额";
			list.add(sat);
			
			depositSubAcctTypeList=list ;
		}
		
		// 还款剩余金额
		BigDecimal assignBal = BigDecimal.ZERO;
		
		//冲销子账户数据模型:
		 // 子账户账期(Integer)组合，账期相同的根据子账户类型(String)组合子账户(List<CactSubAcct>)
		Map<Integer, Map<String, List<CactSubAcct>>> depositCactSubAcctMap = genDepositSubAcct(subAccts);
		
		// 取子账户的账期排序
		List<Integer> stmtList = sortDepositSubAcct(depositCactSubAcctMap);
		
		// 执行还款分配处理，需要还款的金额为当前存款余额、日终存款余额和交易入账金额中较小者
		BigDecimal offsetAmount = depositCactSubAcct.getCurrBal().negate();

		if (offsetAmount.compareTo(depositCactSubAcct.getEndDayBal().negate()) > 0)
		{
			offsetAmount = depositCactSubAcct.getEndDayBal().negate();
		}
		if (offsetAmount.compareTo(postAmount) > 0)
		{
			offsetAmount = postAmount;
		}
		
		if (offsetAmount.signum() > 0)
		{
			//最后还剩金额
			assignBal = payAssign(depositCactSubAcctMap, offsetAmount, stmtList, depositSubAcctTypeList, cactAccount, txnDetailSeq, txnDetailType, depositCactSubAcct);
	
			// 溢缴款入账处理，子账户减去冲销的差额
			provider7x24.increaseBalance(depositCactSubAcct, offsetAmount.subtract(assignBal));
		}
	}

	/**
	 * 冲销子账户数据结构：子账户账期(Integer)组合，账期相同的根据子账户类型(String)组合子账户(List<CactSubAcct>)。
	 * @param cactSubAccts 子账户数组
	 */
	private Map<Integer, Map<String, List<CactSubAcct>>> genDepositSubAcct(List<CactSubAcct> cactSubAccts){
		Map<Integer, Map<String, List<CactSubAcct>>> deposits= new HashMap<Integer, Map<String, List<CactSubAcct>>>();
		for(CactSubAcct sa : cactSubAccts){
			//为添加到冲销子账户的map数据结构里的子map数据结构而创造的临时对象，所以每次进入循环必须要初始化，列表也相同
			Map<String, List<CactSubAcct>> saMapTmp = new HashMap<String, List<CactSubAcct>>();
			List<CactSubAcct> saListTmp = new ArrayList<CactSubAcct>();
			/*先判断数据结构中是否包含该子账户的账期信息。
			1，如果包含当前子账户的账期信息，那么再判断是否还包含该子账户类型的信息，如果也包含，那么直接在子账户的List中追加当前子账户。
			                                  如果不包含该子账户类型，那么就新增该子账户类型和子账户信息列表的映射关系。
			2，如果不包含当前子账户的账期信息，直接新增该子账户账期和子账户类型和子账户的映射关系。*/
			if(deposits.containsKey(sa.getStmtHist())){
				if(deposits.get(sa.getStmtHist()).containsKey(sa.getSubAcctType())){
					deposits.get(sa.getStmtHist()).get(sa.getSubAcctType()).add(sa);
				}
				else{
					saListTmp.add(sa);
					deposits.get(sa.getStmtHist()).put(sa.getSubAcctType(), saListTmp);
				}
			}
			else{
				saListTmp.add(sa);
				saMapTmp.put(sa.getSubAcctType(), saListTmp);
				deposits.put(sa.getStmtHist(), saMapTmp);
			}
		}
		return deposits;
	}
	
	/**
	 * 按账期排序
	 * @param depositCactSubAcctMap
	 * @return
	 */
	private List<Integer> sortDepositSubAcct(Map<Integer, Map<String, List<CactSubAcct>>> depositCactSubAcctMap){
		
		List<Integer> subAcctPayments = new ArrayList<Integer>();
		Iterator<Integer> it = depositCactSubAcctMap.keySet().iterator();
		while( it.hasNext() ) {
			subAcctPayments.add( it.next() );
		}
	
		if( !subAcctPayments.isEmpty() ) {
			// 根据账期，逆序或者顺序排序
			Collections.sort( subAcctPayments, new Comparator<Integer>() { 
				public int compare(Integer n1, Integer n2) { 
//					return (n2.compareTo(n1)); 
					return (n1.compareTo(n2)); 
				} 
			}); 
			
			// 测试
//			for(Integer e : subAcctPayments) {   
//				System.out.println( e );   
//	        }
		}
		return subAcctPayments;
	}
	
	/**
	 * 执行还款分配处理：先按账期有大到小取出映射，
	 * 再从account参数中，按照账龄对应的冲销顺序，遍历子账户类型，取出子账户冲销。
	 * 如果为溢缴款计划，或“是否参与还款分配标志”为“否”，或者当前子账户余额小于0，则退出当前循环。
	 * 判断还款剩余金额是否大于0，如果大于0，还款剩余金额=分配金额-子账户当前余额,还入金额=子账户当前余额；
	 * 如果小于0，还入金额=分配金额，还款剩余金额=0。
	 * @param assignBal 分配金额
	 * @param stmt 子账户账期序列
	 * @param depositSubAcctTypeLis 子账户账户类型序列
	 * @return 最后剩余金额
	 */
	//FIXME: liyinxia 此处不应该按照账期来获取子账户的冲销顺序，可以采用增加参数控制冲销计算逻辑，是采用每个账龄对应的冲销顺序来冲销，还是采用最大账龄的冲销顺序冲销所有子账户。系统当前做法是按照第一种。
	//第一种：用最大账龄的冲销顺序冲销所有子账户，这种情况不需要考虑账期是否按月增加。
	//第二种：用冲销余额的时候同时冲销最小还款额和账龄，需要判定账龄是否发生变动，如果变动需要按照当前账龄的冲销顺序重置，这种情况需要考虑账期的是否按月增加，不能根据账期来获取冲销顺序。重点在于重置冲销顺序的处理。
	private BigDecimal payAssign(Map<Integer, Map<String, List<CactSubAcct>>> depositCactSubAcctMap ,BigDecimal assignBal, List<Integer> stmtList, List<SubAcctType> depositSubAcctTypeList, CactAccount cactAccount, String txnDetailSeq, TxnDetailType txnDetailType, final CactSubAcct depositCactSubAcct)
	{
		LocalDate currentDate = provider7x24.getCurrentDate();
		for (Integer stmt : stmtList)
		{
			if (assignBal.compareTo(BigDecimal.ZERO) == 0) {
				break;
			}
			for (SubAcctType subAcctType : depositSubAcctTypeList)
			{
				if (assignBal.compareTo(BigDecimal.ZERO) == 0) {
					break;
				}
				List<CactSubAcct> cactSubAcctList = depositCactSubAcctMap.get(stmt).get(subAcctType.subAcctType);
				if(cactSubAcctList == null) {
					continue;
				}
				for (CactSubAcct csa : cactSubAcctList)
				{
					if (assignBal.compareTo(BigDecimal.ZERO) == 0) {
						break;
					}
					// 取子账户对应的参数
					SubAcct subAcct = newComputeService.retrieveSubAcct(csa, cactAccount);
					// 如果为溢缴款子账户，或“是否参与还款分配标志”为“否”，子账户余额小于0，则退出
					if (subAcct.balanceType.equals(BalanceType.PAYM) || 
							!subAcct.writeOffInd || 
							provider7x24.getBalance(csa).signum() <= 0 )
					{
						continue;
					}
					
					// 还入金额
					BigDecimal pay = BigDecimal.ZERO;
					// 子账户余额
					BigDecimal bal = provider7x24.getBalance(csa);
					
					// 假如还款剩余金额大于零
					
					//取剩余金额与当前子账户余额小者
					if (assignBal.compareTo(bal) > 0)
					{
						pay = bal;
					}
					else
					{
						pay = assignBal;
					}
					
					assignBal = assignBal.subtract(pay);
					provider7x24.increaseBalance(csa, pay.negate());

					//发出冲销事件
					OffsetEvent offsetEvent = new OffsetEvent(this);
					offsetEvent.setAcctSeq(cactAccount.getAcctSeq());
					offsetEvent.setSubAcctId(csa.getSubAcctId());
					offsetEvent.setAmount(pay);
					offsetEvent.setTxnDetailSeq(txnDetailSeq);
					offsetEvent.setTxnDetailType(txnDetailType);
					offsetEvent.setPostDate(provider7x24.getCurrentDate());	//TODO review
					ctx.publishEvent(offsetEvent);
					
					//入账Memo交易
					PostCode postCode = parameterFacility.loadParameter(PostCode.class, subAcct.depositPostCode);
					CactTxnPost cactTxnPostIn = new CactTxnPost();
					cactTxnPostIn.setAcctCurrBal(cactAccount.getCurrBal());
					cactTxnPostIn.setAcctSeq(cactAccount.getAcctSeq());
					cactTxnPostIn.setAgeCdAfterPosting(cactAccount.getAgeCd());
					cactTxnPostIn.setAgeCdB4Posting(cactAccount.getAgeCd());
					cactTxnPostIn.setBusinessType(cactAccount.getBusinessType());
					cactTxnPostIn.setDbCrInd(postCode.processor.getTxnDirection());
					cactTxnPostIn.setOrg(cactAccount.getOrg());
					cactTxnPostIn.setPostAmt(pay);
					cactTxnPostIn.setPostCode(postCode.postCode);
					cactTxnPostIn.setPostCurrCd(cactAccount.getCurrCd());
					cactTxnPostIn.setPostDate(currentDate.toDate());
					cactTxnPostIn.setPostingFlag(PostingFlag.F00);
					cactTxnPostIn.setPostTxnType(PostTxnTypeDef.O);
					cactTxnPostIn.setStmtDate(cactAccount.getInterestDate());
					cactTxnPostIn.setSubAcctId(csa.getSubAcctId());
					cactTxnPostIn.setSubacctParamId(csa.getSubacctParamId());
					cactTxnPostIn.setTxnAmt(cactTxnPostIn.getPostAmt());
					cactTxnPostIn.setTxnCurrCd(cactTxnPostIn.getPostCurrCd());
					cactTxnPostIn.setTxnDate(currentDate.toDate());
					cactTxnPostIn.setTxnDesc(postCode.description);
					cactTxnPostIn.setTxnDetailSeq(txnDetailSeq);
					cactTxnPostIn.setTxnDetailType(txnDetailType);
					cactTxnPostIn.setTxnShortDesc(postCode.shortDesc);
					cactTxnPostIn.setTxnTime(new Date());
					cactTxnPostIn.setBizDate(provider7x24.getCurrentDate().toDate());
					cactTxnPostIn.fillDefaultValues();
					em.persist(cactTxnPostIn);
					
					Account accountParam = newComputeService.retrieveAccount(cactAccount);
					PostCode postCodeOut = parameterCacheFacility.loadParameter(PostCode.class, accountParam.sysTxnCdMapping.get(SysTxnCd.S37));
					CactTxnPost cactTxnPostOut = new CactTxnPost();
					cactTxnPostOut.setAcctCurrBal(cactAccount.getCurrBal());
					cactTxnPostOut.setAcctSeq(cactAccount.getAcctSeq());
					cactTxnPostOut.setAgeCdAfterPosting(cactAccount.getAgeCd());
					cactTxnPostOut.setAgeCdB4Posting(cactAccount.getAgeCd());
					cactTxnPostOut.setBusinessType(cactAccount.getBusinessType());
					cactTxnPostOut.setDbCrInd(postCodeOut.processor.getTxnDirection());
					cactTxnPostOut.setOrg(cactAccount.getOrg());
					cactTxnPostOut.setPostAmt(pay);
					cactTxnPostOut.setPostCode(postCodeOut.postCode);
					cactTxnPostOut.setPostCurrCd(cactAccount.getCurrCd());
					cactTxnPostOut.setPostDate(currentDate.toDate());
					cactTxnPostOut.setPostingFlag(PostingFlag.F00);
					cactTxnPostOut.setPostTxnType(PostTxnTypeDef.O);
					cactTxnPostOut.setStmtDate(cactAccount.getInterestDate());
					cactTxnPostOut.setSubAcctId(depositCactSubAcct.getSubAcctId());
					cactTxnPostOut.setSubacctParamId(depositCactSubAcct.getSubacctParamId());
					cactTxnPostOut.setTxnAmt(cactTxnPostOut.getPostAmt());
					cactTxnPostOut.setTxnCurrCd(cactTxnPostOut.getPostCurrCd());
					cactTxnPostOut.setTxnDate(currentDate.toDate());
					cactTxnPostOut.setTxnDesc(postCodeOut.description);
					cactTxnPostOut.setTxnDetailSeq(txnDetailSeq);
					cactTxnPostOut.setTxnDetailType(txnDetailType);
					cactTxnPostOut.setTxnShortDesc(postCodeOut.shortDesc);
					cactTxnPostOut.setTxnTime(new Date());
					cactTxnPostOut.setBizDate(provider7x24.getCurrentDate().toDate());
					cactTxnPostOut.fillDefaultValues();
					em.persist(cactTxnPostOut);
					
					/*
					 * 最小还款额,如果最小还款额小于还入金额，就把最小还款额清零。否则就拿最小还款额
					        减去还入金额
					*/
					if (csa.getTotDueAmt().compareTo(pay) < 0)
					{
						csa.setTotDueAmt(BigDecimal.ZERO);
					}
					else
					{
						csa.setTotDueAmt(csa.getTotDueAmt().subtract(pay));
					}
				}
			}
		}
		return assignBal;
    }
	
	/**
	 * 还款冲销最小还款额
	 * @param account 账户表
	 * @param cactAgeDues 账龄历史表
	 * @param amount 还款金额，正值
	 */
	public void offsetMinDue(AcctModel model, BigDecimal amount)
	{
		checkArgument(amount.signum() > 0);

		CactAccount cactAccount = model.getCactAccount();
		List<CactAgeDue> cactAgeDues = model.getCactAgeDues();
		
		// 未冲销还款金额
		BigDecimal leftAmount = amount;
		//待删除的账龄记录
		List<CactAgeDue> removeAgedus = new ArrayList<CactAgeDue>();
		//遍历存在所有账龄的最小还款额，在读取时已经按照时间顺序排序，最远的排在队列的前面
		for (CactAgeDue cactAgeDue : cactAgeDues)
		{
			//如果最小还款额大于还款金额
			if (cactAgeDue.getAgeDueAmt().compareTo(leftAmount) > 0)
			{
				cactAgeDue.setAgeDueAmt(cactAgeDue.getAgeDueAmt().subtract(leftAmount));
				cactAccount.setTotDueAmt(cactAccount.getTotDueAmt().subtract(leftAmount));
				break;
			}
			//如果最小还款额小于还款金额，就把该账龄的最小还款额记录删除
			else
			{
				leftAmount = leftAmount.subtract(cactAgeDue.getAgeDueAmt());
				removeAgedus.add(cactAgeDue);
//				cactAgeDues.remove(cactAgeDue);
				em.remove(cactAgeDue);
				cactAccount.setTotDueAmt(cactAccount.getTotDueAmt().subtract(cactAgeDue.getAgeDueAmt()));
//				cactAccount.setFirstOverdueDate(cactAgeDues.get(0).getGraceDate());
			}
			if (leftAmount.compareTo(BigDecimal.ZERO) == 0)
			{
				break;
			}
		}
		cactAgeDues.removeAll(removeAgedus);
		if(cactAgeDues.size() > 0){
			if (cactAccount.getFirstOverdueDate() == null || cactAccount.getFirstOverdueDate().before(cactAgeDues.get(0).getGraceDate())) {
				cactAccount.setFirstOverdueDate(cactAgeDues.get(0).getGraceDate());
			}
		}else {
			cactAccount.setFirstOverdueDate(null);
		}
		if (cactAccount.getTotDueAmt().compareTo(BigDecimal.ZERO) < 0) {
			cactAccount.setTotDueAmt(BigDecimal.ZERO);
		}
	}
}
