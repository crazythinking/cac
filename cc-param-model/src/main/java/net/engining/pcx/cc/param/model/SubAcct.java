package net.engining.pcx.cc.param.model;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.param.model.enums.*;
import net.engining.pg.parameter.HasEffectiveDate;
import net.engining.pg.support.meta.PropertyInfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

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
	@PropertyInfo(name="最小还款额计算比例")
	public Map<Integer, BigDecimal> minPaymentRates = new HashMap<Integer, BigDecimal>();	
	
	/**
	 * 标明子账户是否属于分户
	 */
	@PropertyInfo(name="子账户类别", length=1)
	public SubAcctCategory subAcctCategory;
	
	/**
	 * 支持多个利率表
	 */
	@PropertyInfo(name="利率表")
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
	
}