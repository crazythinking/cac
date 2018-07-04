package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.param.model.enums.BalanceDirection;
import net.engining.pcx.cc.param.model.enums.BalanceType;
import net.engining.pcx.cc.param.model.enums.IntAccumFrom;
import net.engining.pcx.cc.param.model.enums.InterestAccrualType;
import net.engining.pcx.cc.param.model.enums.SubAcctCategory;
import net.engining.pcx.cc.param.model.enums.TransMergeMethod;
import net.engining.pg.parameter.HasEffectiveDate;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 子账户参数
 */
public class SubAcct implements HasEffectiveDate, Serializable, Comparable<SubAcct>{

	private static final long serialVersionUID = -4258130560855171282L;

	/**
	 * 子帐户参数标识
	 * 
	 */
	@PropertyInfo(name="子帐户参数标识", length=30)
	public String subAcctId;

    /**
     * 子帐户类型
     */
    @PropertyInfo(name="子帐户类型", length=8)
    public String subAcctType;
    
    /**
     * 是否参与还款分配标志
     */
    @PropertyInfo(name="参与还款分配", length=1)
    public Boolean writeOffInd;

    /**
     * 子账户描述
     */
    @PropertyInfo(name="描述", length=100)
    public String description;
    
    /**
	 * 交易金额是否合并余额
	 * 替代 交易是否新建子账户
	 */
	@PropertyInfo(name="余额合并方式", length=1)
	public TransMergeMethod transMergeMethod;
	
    /**
     * 计划保存天数
     */
    @PropertyInfo(name="失效保留天数", length=3)
    public Integer planPurgeDays;
    
    /**
     * 核销转出交易代码
     */
    @PropertyInfo(name="核销转出交易代码", length=8)
    public String writeOffPostCode;
    
    /**
     * 利息入账交易代码
     */
    @PropertyInfo(name="利息入账交易代码", length=8)
    public String interestPostCode;
    
    /**
     * 罚息入账交易代码
     */
    @PropertyInfo(name="罚息入账交易代码", length=8)
    public String penalizedInterestPastDuePostCode;
    
    /**
     * 是否支持账单分期
     */
    @PropertyInfo(name="支持账单分期", length=1)
    public Boolean supportStmtLoan;
    
    /**
     * 余额所属余额类型
     */
    @PropertyInfo(name="余额类型", length=4)
    public BalanceType balanceType;
    
    /**
     *  余额方向，包含借、贷、双向
     */
    @PropertyInfo(name="余额方向", length=1)
    public BalanceDirection balanceDirection;
	
	/**
	 * 起息日类型
	 */
	@PropertyInfo(name="起息日类型", length=1)
	public IntAccumFrom intAccumFrom;
	
	/**
	 * 起息日延后天数
	 */
	@PropertyInfo(name="起息日延后天数", length=1)
	public Integer postponeDays; 
	
	/**
	 * 是否享受免息期
	 */
	@PropertyInfo(name="享受免息期", length=1)
	public Boolean intWaive;
	
	/**
	 * 是否计入全额应还款金额
	 */
	@PropertyInfo(name="计入全部应还款额", length=1)
	public Boolean graceQualify;
	
	/**
	 * 是否参与超限计算
	 */
	@PropertyInfo(name="参与超限计算", length=1)
	public Boolean overlimitQualify;
	
	/**
	 * 最小还款额计算比例
	 * key:账龄
	 */
	//@PropertyInfo(name="最小还款额计算比例", length=7, precision=4)
	public Map<Integer, BigDecimal> minPaymentRates = new HashMap<Integer, BigDecimal>();	
	
	/**
	 * 标明子账户是否属于分户
	 */
	@PropertyInfo(name="子账户类别", length=1)
	public SubAcctCategory subAcctCategory;
	
	/**
	 * 支持多个利率表
	 */
	//@PropertyInfo(name="利率表", length=20)
	public List<String> intTables;
	
	/**
	 * 罚息利率
	 */
	@PropertyInfo(name="罚息利率", length=20)
	public String penalizedInterestTable;
	
	/**
	 * 利息计提方式
	 */
	@PropertyInfo(name="利息计提方式", length=20)
	public Interval interestAccruedMethod;
	
	/**
     * 冲销交易代码
     */
    @PropertyInfo(name="冲销交易代码", length=8)
    public String depositPostCode;
    
    /**
    	发生还款冲销时，内部账户入账代码
    key - 账龄
    value - 内部账户入账代码
    */
    public Map<String, List <String>> depositInternalPostCode;
    
    /**
    	利息入账时，内部账户入账代码
    key - 账龄
    value - 内部账户入账代码
    */
    public Map<String, List <String>> interestPostingInternalPostCode;
    
    /**
	         罚息入账时，内部账户入账代码
    key - 账龄
    value - 内部账户入账代码
    */
    public Map<String, List <String>> interestPenaltyPostingInternalPostCode;
    
    /**
     * 余额结转规则
     * key - ageCdB4Changing + "|" + ageCdAfterChanging
     * value - 结转规则
     */
    public Map<String, BalTransferPostCode> balTransferMap;
    
	@PropertyInfo(name = "生效日期")
	public Date effectiveDate;
	
	
	@PropertyInfo(name = "利息计算方法", length = 20)
	public InterestAccrualType interestAccrualType;

	@Override
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	@Override
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Override
	public int compareTo(SubAcct o) {
		ComparisonChain chain = ComparisonChain.start()
				.compare(subAcctType, o.subAcctType)
				.compare(balanceType, o.balanceType)
				.compare(penalizedInterestTable, o.penalizedInterestTable);
		
		//返回set1中包含且set2中不包含的元素，所以必须再倒过来比一遍，才能确定是否相同
		Set<String> diffs1 = Sets.difference(Sets.newHashSet(intTables), Sets.newHashSet(o.intTables));
		Set<String> diffs2 = Sets.difference(Sets.newHashSet(o.intTables), Sets.newHashSet(intTables));
		
		if(diffs1.size() != 0 || diffs2.size() != 0) {
			return 1;
		}
		else{
			return chain.result();
		}
		
	}
	
	public static void main(String[] args){
		SubAcct sa1 = new SubAcct();
		sa1.subAcctId = "PLD00000IT";
		sa1.description = "贷款利息-子账户";
		sa1.subAcctType = "INTE";
		sa1.balanceDirection = BalanceDirection.D;
		sa1.balanceType = BalanceType.INTE;
		sa1.graceQualify = true;
		sa1.intAccumFrom = IntAccumFrom.P;
		sa1.interestPostCode = "IB000044";
		sa1.intTables = new ArrayList<String>();
		sa1.intTables.add("Z0003");
		sa1.intTables.add("Z0001");
		sa1.intWaive = false;
		sa1.minPaymentRates = new HashMap<Integer, BigDecimal>();
		sa1.minPaymentRates.put(0, BigDecimal.ONE);
		sa1.minPaymentRates.put(1, BigDecimal.ONE);
		sa1.minPaymentRates.put(2, BigDecimal.ONE);
		sa1.minPaymentRates.put(3, BigDecimal.ONE);
		sa1.minPaymentRates.put(4, BigDecimal.ONE);
		sa1.minPaymentRates.put(5, BigDecimal.ONE);
		sa1.minPaymentRates.put(6, BigDecimal.ONE);
		sa1.minPaymentRates.put(7, BigDecimal.ONE);
		sa1.minPaymentRates.put(8, BigDecimal.ONE);
		sa1.minPaymentRates.put(9, BigDecimal.ONE);
		sa1.overlimitQualify = true;
		sa1.penalizedInterestPastDuePostCode = "IB000045";
		sa1.penalizedInterestTable = "Z0001";
		sa1.planPurgeDays = 30;
		sa1.postponeDays = 0;
		sa1.supportStmtLoan = false;
		sa1.transMergeMethod = TransMergeMethod.A;
		sa1.writeOffInd = true;
		//sa.writeOffPostCode = "IB000028";
		sa1.interestAccruedMethod = Interval.D;
		sa1.depositPostCode = "IB000034";
		sa1.depositInternalPostCode = new HashMap<String, List<String>>();
		List<String> internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY2241.01.002.006.001.000000");
		internalAcctPostCodes.add("CNCNY1122.01.003.001.000000");
		sa1.depositInternalPostCode.put("0", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("1", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("2", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("3", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("4", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("5", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("6", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("7", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("8", internalAcctPostCodes);
		sa1.depositInternalPostCode.put("9", internalAcctPostCodes);
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY1122.01.003.001.000001");
		internalAcctPostCodes.add("CNCNY1132.03.000001");
		sa1.interestPenaltyPostingInternalPostCode = new HashMap<String, List <String>>();
		sa1.interestPenaltyPostingInternalPostCode.put("1", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("2", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("3", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("4", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("5", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("6", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("7", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("8", internalAcctPostCodes);
		sa1.interestPenaltyPostingInternalPostCode.put("9", internalAcctPostCodes);
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY1122.01.003.001.000000");
		internalAcctPostCodes.add("CNCNY1132.03.000000");
		sa1.interestPostingInternalPostCode = new HashMap<String, List <String>>();
		sa1.interestPostingInternalPostCode.put("0", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("1", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("2", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("3", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("4", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("5", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("6", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("7", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("8", internalAcctPostCodes);
		sa1.interestPostingInternalPostCode.put("9", internalAcctPostCodes);
		sa1.subAcctCategory = SubAcctCategory.S;

		SubAcct sa2 = new SubAcct();
		sa2.subAcctId = "PLD00000PI";
		sa2.description = "贷款罚息-子账户";
		sa2.subAcctType = "LABL";
		sa2.balanceDirection = BalanceDirection.D;
		sa2.balanceType = BalanceType.INTE;
		sa2.graceQualify = true;
		sa2.intAccumFrom = IntAccumFrom.P;
		sa2.interestPostCode = "IB000044";
		sa2.intTables = new ArrayList<String>();
		sa2.intTables.add("Z0001");
		sa2.intTables.add("Z0003");
		sa2.intWaive = false;
		sa2.minPaymentRates = new HashMap<Integer, BigDecimal>();
		sa2.minPaymentRates.put(0, BigDecimal.ONE);
		sa2.minPaymentRates.put(1, BigDecimal.ONE);
		sa2.minPaymentRates.put(2, BigDecimal.ONE);
		sa2.minPaymentRates.put(3, BigDecimal.ONE);
		sa2.minPaymentRates.put(4, BigDecimal.ONE);
		sa2.minPaymentRates.put(5, BigDecimal.ONE);
		sa2.minPaymentRates.put(6, BigDecimal.ONE);
		sa2.minPaymentRates.put(7, BigDecimal.ONE);
		sa2.minPaymentRates.put(8, BigDecimal.ONE);
		sa2.minPaymentRates.put(9, BigDecimal.ONE);
		sa2.overlimitQualify = true;
		sa2.penalizedInterestPastDuePostCode = "IB000045";
		sa2.penalizedInterestTable = "Z0001";
		sa2.planPurgeDays = 30;
		sa2.postponeDays = 0;
		sa2.supportStmtLoan = false;
		sa2.transMergeMethod = TransMergeMethod.A;
		sa2.writeOffInd = true;
		//sa.writeOffPostCode = "IB000028";
		sa2.interestAccruedMethod = Interval.D;
		sa2.depositPostCode = "IB000046";
		sa2.depositInternalPostCode = new HashMap<String, List<String>>();
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY2241.01.002.006.001.000000");
		internalAcctPostCodes.add("CNCNY1122.01.003.001.000001");
		sa2.depositInternalPostCode.put("0", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("1", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("2", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("3", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("4", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("5", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("6", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("7", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("8", internalAcctPostCodes);
		sa2.depositInternalPostCode.put("9", internalAcctPostCodes);
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY1122.01.003.001.000001");
		internalAcctPostCodes.add("CNCNY1132.03.000001");
		sa2.interestPenaltyPostingInternalPostCode = new HashMap<String, List <String>>();
		sa2.interestPenaltyPostingInternalPostCode.put("1", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("2", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("3", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("4", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("5", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("6", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("7", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("8", internalAcctPostCodes);
		sa2.interestPenaltyPostingInternalPostCode.put("9", internalAcctPostCodes);
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY1122.01.003.001.000000");
		internalAcctPostCodes.add("CNCNY1132.03.000000");
		sa2.interestPostingInternalPostCode = new HashMap<String, List <String>>();
		sa2.interestPostingInternalPostCode.put("0", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("1", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("2", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("3", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("4", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("5", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("6", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("7", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("8", internalAcctPostCodes);
		sa2.interestPostingInternalPostCode.put("9", internalAcctPostCodes);
		sa2.subAcctCategory = SubAcctCategory.S;
		
		System.out.println(sa1.compareTo(sa2));
	}
}