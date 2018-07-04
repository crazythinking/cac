package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import net.engining.gm.infrastructure.enums.AuthType;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.param.model.enums.AccrualInterestType;
import net.engining.pcx.cc.param.model.enums.AccrualType;
import net.engining.pcx.cc.param.model.enums.CalcMethod;
import net.engining.pcx.cc.param.model.enums.ComputInteHT;
import net.engining.pcx.cc.param.model.enums.CycleStartDay;
import net.engining.pcx.cc.param.model.enums.DelqDayInd;
import net.engining.pcx.cc.param.model.enums.DelqTolInd;
import net.engining.pcx.cc.param.model.enums.DownpmtTolInd;
import net.engining.pcx.cc.param.model.enums.GenAcctMethod;
import net.engining.pcx.cc.param.model.enums.LoanFeeMethod;
import net.engining.pcx.cc.param.model.enums.ParamBaseType;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.param.model.enums.PnitType;
import net.engining.pcx.cc.param.model.enums.SysInternalAcctActionCd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.param.model.enums.TransformType;
import net.engining.pg.parameter.HasEffectiveDate;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 账户参数
 * 
 */
public class Account implements HasEffectiveDate, Serializable, Comparable<Account>{

	private static final long serialVersionUID = -5145883222989744588L;

//	基础参数-开始
	/**
	 * 账户属性ID
	 */
	@PropertyInfo(name="账户参数标识", length=30)
	public String paramId;

	
	/**
	 * 描述
	 */
	@PropertyInfo(name="描述", length=100)
	public String description;
	
	/**
	 * 是否有罚息 
	 * 
	 */
	@PropertyInfo(name="是否有罚息", length=2)
	public Boolean isPnit;
	
	/**
	 * 逾期还款罚息参数
	 * 
	 */
	@PropertyInfo(name="逾期还款罚息参数", length=2)
	public PnitType pnitType;
	
	
	/**
	 * 提前还款当期计息标准  M按月	  D按日
	 */
	@PropertyInfo(name="提前还款计息标准", length=2)
	public String advanceType;
	
	
	/**
	 * 记账结转方式(D-逐期结转)；按科目记账时形态转移类型
	 */
	@PropertyInfo(name="记账结转方式，按科目记账时形态转移类型", length=2)
	public TransformType carryType;
	
	
	/**
	 * 业务类型
	 */
	@PropertyInfo(name="业务类型", length=2)
	public BusinessType businessType;

	/**
	 * 币种
	 */
	@PropertyInfo(name="币种", length=3)
	public String currencyCode;
	
	/**
     * 锁定码控制参数
     * key: BlockCode对象中的blockCode(锁定码)
     */
    public Map<String, BlockCodeControl> blockcode;
    
    /**
     * 子账户计价参数
     * <p>key - 子账户类型key值{@link SubAcctType}</p>
     * value - 子账户参数key值 {@link SubAcct}
     */
    public Map<String, String> subAcctParam;
    
    @PropertyInfo(name="建账方式", length=1)
    public GenAcctMethod genAcctMethod;
    
    /**
     * 系统产生交易代码与入账交易代码对应
     * <p>key - 系统产生交易代码{@link SysTxnCd}</p>
     * value - 入账代码postCode的key值{@link String}
     */
    public Map<SysTxnCd, String> sysTxnCdMapping;
//    基础参数-结束
    
    /**
    	系统内部账户行为代码与内部账户入账交易代码映射
    key - 系统内部账户行为代码
    value - 内部账户入账代码
    */
    public Map<SysInternalAcctActionCd, List<String>> internalAcctPostMapping;
    
    
//	循环信用贷款、消费分期和一次性授信贷款共有的参数-开始
	/**
	 * 固定每次还款日
	 */
	@PropertyInfo(name="固定每次还款日", length=1)
    public Boolean isLockPaymentDay;
	
    /**
     * 到期还款日天数：对于在账单日（结息日）之后若干天
     */
    @PropertyInfo(name="到期还款日天数", length=2)
    public Integer pmtDueDays;

    /**
     * 到期还款宽限天数
     */
    @PropertyInfo(name="到期还款宽限天数", length=2)
    public Integer pmtGracePrd;

    /**
     * 全额还款宽限计算方式：
     */
    @PropertyInfo(name="全额还款宽限计算方式", length=1)
    public DownpmtTolInd downpmtTolInd;

	/**
     * 全额还款宽限比例
     * 如果存放比例值，该值为允许少还的比例，如0.01允许少还1%
     */
    @PropertyInfo(name="全额还款宽限比例", length=7, precision=4)
    public BigDecimal downpmtTolPerc;
    
    /**
     * 全额还款宽限金额
     */
    @PropertyInfo(name="全额还款宽限金额", length=15, precision=2)
    public BigDecimal downpmtTol;

    /**
     * 账龄提升日
     */
    @PropertyInfo(name="账龄提升日", length=1)
    public DelqDayInd delqDayInd;

    /**
     * 账龄提升宽限方式
     */
    @PropertyInfo(name="账龄提升宽限方式", length=1)
    public DelqTolInd delqTolInd;

    /**
     * 账龄宽限金额
     */
    @PropertyInfo(name="账龄宽限金额", length=15, precision=2)
    public BigDecimal delqTol;

    /**
     * 账龄宽限比例
     */
    @PropertyInfo(name="账龄宽限比例",  length=7, precision=4)
    public BigDecimal delqTolPerc;

    /**
     * 滞纳金
     */
    //@PropertyInfo(name="滞纳金参数")
    public LatePaymentCharge latePaymentCharge;
    
    /**
     * 超限费
     */
    //@PropertyInfo(name="超限费参数")
    public OverlimitCharge overlimitCharge;
    
    /**
     * 冲销顺序
     * key-账龄
     */
    public Map<String, List<SubAcctType>> paymentHier;
    
    @PropertyInfo(name="默认授信额度", length=8)
    public Integer defaultLimit;
    
    @PropertyInfo(name="默认授信方式", length=1)
    public AuthType defaultAuthType;
    
    @PropertyInfo(name="还款类型", length=3)
    public PaymentMethod paymentMethod;
    
    /**
     * 计息头尾规则
     */
    public ComputInteHT computInteHT;
    
    //分期本金除不尽的部分放在首期收取还是末期收取
    //@PropertyInfo(name="分期本金余数收取方式", length=2)
    //public LoanPrincipalRemainderMethod loanPrincipalRemainderMethod;   
    
//    循环信用贷款和一次性授信贷款共有的参数-结束
    
//    循环信用贷款独有参数-开始
    /**
     * 免出账单溢缴款最小金额
     */
    @PropertyInfo(name="免出账单溢缴款最小金额", length=15, precision=2)
    public BigDecimal crMaxbalNoStmt;

    /**
     * 免出账单最小借方金额
     */
    @PropertyInfo(name="免出账单最小借方金额", length=15, precision=2)
    public BigDecimal stmtMinBal;
//    循环信用贷款独有参数-结束
    
//    消费分期独有参数-开始
    /**
	 * 分期手续费收取方式
	 */
	@PropertyInfo(name="贷款手续费收取方式", length=1)
	public LoanFeeMethod loanFeeMethod;

	/**
	 * 分期手续费计算方式
	 */
	@PropertyInfo(name="贷款手续费计算方式", length=1)
	public CalcMethod loanFeeCalcMethod;

	/**
	 * 分期手续费金额
	 */
	@PropertyInfo(name="贷款手续费金额", length=15, precision=2)
	public BigDecimal feeAmount;
	
	/**
	 * 分期手续费比例
	 */
	@PropertyInfo(name="贷款手续费比例", length=7, precision=4)
	public BigDecimal feeRate;
//    消费分期独有参数-结束
    
//    一次性授信贷款、消费分期、借记活期、借记定期、智能存款共有参数-开始
    /**
     * 结息周期起始日计算方式
     * P-从建账日期作为第一个结息周期的开始日/Y-自然年的1月开始
     */
    @PropertyInfo(name="结息周期起始日类型", length=1)
    public CycleStartDay intSettleStartMethod;
    
    /**
     * 与结息周期乘数合并，形成结息周期长度
     * 如：6个月，intUnit = M, intUnitMult = 6
     */
    @PropertyInfo(name="结息周期计数单位", length=1)
    public Interval intUnit;
    
    /**
     * 与结息周期单位合并，形成结息周期长度
     * 如：每6个月期结一次息，intUnit = M, intUnitMult = 6
     */
    @PropertyInfo(name="结息周期计数乘数", length=4)
    public Integer intUnitMult;
    
    /**
     * 结息日，与结息周期乘数和单位配合使用。
     * 99表示当前周期的最后一天。1-29表示当前这个周期的某一天。
     * 例如：intSettleStartMethod = Y, intUnit = M, intUnitMult = 6, dIntSettleDay = 99
     *      表示6月30日、12月31日结息
     * 例如：intSettleStartMethod = P, intUnit = M, intUnitMult = 6, dIntSettleDay = 99
     *      计息日是2月15日开始，当intSettleStartMethod等于Y的时候dIntSettleDay不起作用，
     *      表示从2月15日开始，每6个月期结一次息。
     */
    @PropertyInfo(name="结息日", length=2)
    public Integer dIntSettleDay;
    
    /**
     * 结息总期数，如果intSettleFrequency=-1表示总期数没有终止。
     */
    @PropertyInfo(name="结息总期数", length=3)
    public Integer intSettleFrequency;
    
    /**
     * 首期天数调整
     * 结息周期计数单位设置成"日"时，该参数生效
     * false：首次结息日  = 建账日期 + 结息周期乘数 - 1
     * true：首次结息日  = 建账日期 + 结息周期乘数
     * default : false
     */
    @PropertyInfo(name="首期天数调整", length=1)
    public Boolean intFirstPeriodAdj;
    
//    一次性授信贷款、借记活期、借记定期、智能存款共有参数-结束
 
    // 智能存款使用参数-开始
    /**
     * 智能存款起存金额
     */
    @PropertyInfo(name="起存金额", length=15, precision=0)
	public BigDecimal minAmount;
    
    /**
     * 计息方式
     */
    @PropertyInfo(name="计息方式", length=1)
	public AccrualType accrualType;
    
    /**
     * 计提利率类型
     */
    @PropertyInfo(name="计提利率类型", length=1)
	public AccrualInterestType accrualInterestType;
    
    /**
     * 是否代扣利息税
     */
    @PropertyInfo(name="代扣利息税", length=1)
	public Boolean withHoldingInt;
    
    /**
     * 利息税入账交易代码
     */
    @PropertyInfo(name="利息税交易代码", length=8)
	public String intTaxPostCode;
    /**
     * 销项税税率参数码
     */
    public String TaxCode;
    /**
     * 是否出账单
     */
    @PropertyInfo(name="出账单", length=1)
	public Boolean isStmt;
    
//     智能存款使用参数-结束
    
    /**
	 * 利率参数有效类型
	 */
	@PropertyInfo(name="利率参数有效类型", length=5)
	public ParamBaseType intParamBaseType;
    
	@PropertyInfo(name = "生效日期")
	public Date effectiveDate;

	@Override
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	@Override
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Override
	public int compareTo(Account o) {
		
		ComparisonChain chain = ComparisonChain.start()
				.compare(intUnit, o.intUnit)
				.compare(intUnitMult, o.intUnitMult)
				.compare(intSettleFrequency, o.intSettleFrequency)
				.compare(paymentMethod, o.paymentMethod)
				.compare(pmtGracePrd, o.pmtGracePrd)
				.compare(advanceType, o.advanceType)
				.compare(pnitType, o.pnitType);
		
		MapDifference<String, String> diffs= Maps.difference(subAcctParam, o.subAcctParam);
		if(!diffs.areEqual()){
			return 1;
		}
		else{
			return chain.result();
		}
	}
    
	public static void main(String[] args){
		
		Account acct1 = new Account();
		acct1.paramId = "PI000000";
		acct1.description = "主账户活期";
		acct1.businessType = BusinessType.PI;
		acct1.currencyCode = "156";
		acct1.genAcctMethod = GenAcctMethod.A;
		acct1.blockcode = new HashMap<String, BlockCodeControl>();
		acct1.subAcctParam = new HashMap<String, String>();
		acct1.subAcctParam.put("PAYM", "PI000000PY");
		acct1.subAcctParam.put("LOAN", "LI000000PY");
		acct1.subAcctParam.put("INTE", "PI000000PB");
		acct1.intSettleStartMethod = CycleStartDay.Y;
		acct1.intUnit = Interval.M;
		acct1.intUnitMult = 3;
		acct1.dIntSettleDay = 21;
		acct1.intSettleFrequency = -1;
		acct1.sysTxnCdMapping = new HashMap<SysTxnCd, String>();
		acct1.sysTxnCdMapping.put(SysTxnCd.S40, "IB000023");
		acct1.sysTxnCdMapping.put(SysTxnCd.S41, "IB000025");
		acct1.sysTxnCdMapping.put(SysTxnCd.S37, "IB000009");
		acct1.sysTxnCdMapping.put(SysTxnCd.T01, "IB000048");
		acct1.sysTxnCdMapping.put(SysTxnCd.T02, "IB000012");
		acct1.sysTxnCdMapping.put(SysTxnCd.T03, "IB000005");
		acct1.sysTxnCdMapping.put(SysTxnCd.T04, "IB000017");
		acct1.sysTxnCdMapping.put(SysTxnCd.S38, "IB000017");
		acct1.sysTxnCdMapping.put(SysTxnCd.T99, "IB000017");
		acct1.withHoldingInt = true;
		acct1.intTaxPostCode = "IB000039";
		List<String> internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("DNCNY139099000000");
		acct1.internalAcctPostMapping = new HashMap<SysInternalAcctActionCd, List<String>>();
		acct1.internalAcctPostMapping.put(SysInternalAcctActionCd.S003, internalAcctPostCodes);
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("CNCNY261099000000");
		acct1.internalAcctPostMapping.put(SysInternalAcctActionCd.S002, internalAcctPostCodes);
		acct1.paymentMethod = PaymentMethod.IFP;
		acct1.pmtGracePrd = 0;
		acct1.advanceType = "a";
		acct1.pnitType = PnitType.B;
		
		Account acct2 = new Account();
		acct2.paramId = "PI000000";
		acct2.description = "主账户活期";
		acct2.businessType = BusinessType.PI;
		acct2.currencyCode = "156";
		acct2.genAcctMethod = GenAcctMethod.A;
		acct2.blockcode = new HashMap<String, BlockCodeControl>();
		acct2.subAcctParam = new HashMap<String, String>();
		acct2.subAcctParam.put("PAYM", "PI000000PY");
		acct2.subAcctParam.put("LOAN", "LI000000PY");
		acct2.subAcctParam.put("INTE", "PI000000PB");
		acct2.intSettleStartMethod = CycleStartDay.Y;
		acct2.intUnit = Interval.M;
		acct2.intUnitMult = 3;
		acct2.dIntSettleDay = 21;
		acct2.intSettleFrequency = -1;
		acct2.sysTxnCdMapping = new HashMap<SysTxnCd, String>();
		acct2.sysTxnCdMapping.put(SysTxnCd.S40, "IB000023");
		acct2.sysTxnCdMapping.put(SysTxnCd.S41, "IB000025");
		acct2.sysTxnCdMapping.put(SysTxnCd.S37, "IB000009");
		acct2.sysTxnCdMapping.put(SysTxnCd.T01, "IB000048");
		acct2.sysTxnCdMapping.put(SysTxnCd.T02, "IB000012");
		acct2.sysTxnCdMapping.put(SysTxnCd.T03, "IB000005");
		acct2.sysTxnCdMapping.put(SysTxnCd.T04, "IB000017");
		acct2.sysTxnCdMapping.put(SysTxnCd.S38, "IB000017");
		acct2.sysTxnCdMapping.put(SysTxnCd.T99, "IB000017");
		acct2.withHoldingInt = true;
		acct2.intTaxPostCode = "IB000039";
		List<String> internalAcctPostCodes2 = new ArrayList<String>();
		internalAcctPostCodes2.add("DNCNY139099000000");
		acct2.internalAcctPostMapping = new HashMap<SysInternalAcctActionCd, List<String>>();
		acct2.internalAcctPostMapping.put(SysInternalAcctActionCd.S003, internalAcctPostCodes);
		internalAcctPostCodes = new ArrayList<String>();
		internalAcctPostCodes.add("CNCNY261099000000");
		acct2.internalAcctPostMapping.put(SysInternalAcctActionCd.S002, internalAcctPostCodes);
		acct2.paymentMethod = PaymentMethod.IFP;
		acct2.pmtGracePrd = 0;
		acct2.advanceType = "a";
		acct2.pnitType = PnitType.B;
		
		System.out.println(acct1.compareTo(acct2));
	}
	
    //
    // 以下是废弃参数
    /**
     * 缺省授权允许超限比例
     */
    /*@PropertyInfo(name="默认允许超限比例", length=7, precision=4)
    public BigDecimal ovrlmtRate;*/
    /**
     * 缺省账单日
     */
   /* @PropertyInfo(name="缺省账单日", length=2)
    public Integer dfltCycleDay;*/
    
    /**
	 * 到期还款日类型
	 *//*
	@PropertyInfo(name="到期还款日类型", length=1)
    public PaymentDueDay paymentDueDay;
	
	*//**
     * 到期还款固定日：对于每月固定日期的情况
     * 01 - 28 ： 固定日期 
     * 99 ： 月末
     *//*
    @PropertyInfo(name="到期还款固定日 ", length=2)
    public Integer pmtDueDate;
    
    *//**
     * 到期还款短信/信函提前天数
     *//*
    @PropertyInfo(name="到期还款提醒提前天数", length=1)
    public Integer pmtDueLtrPrd;
    
    *//**
     * 约定还款日标识
     *//*
    @PropertyInfo(name="约定还款日标识", length=1)
    public DirectDbIndicator directDbInd;
    
    *//**
     * 约定还款提前天数
     *//*
    @PropertyInfo(name="约定还款提前天数", length=2)
    public Integer directDbDays;
    
    *//**
     * 约定还款固定日
     *//*
    @PropertyInfo(name="约定还款固定日", length=2)
    public Integer directDbDate;
    
    *//**
     * 拖欠短信/信函产生标识天数（拖欠之后第多少天产生）
     * 00 - 98 ： 实际天数
     * 99 ： 下个账单日产生
     *//*
    @PropertyInfo(name="拖欠通知延期天数", length=2)
    public Integer delqLtrPrd;

    *//**
     * 是否连续拖欠都输出信函
     *//*
    @PropertyInfo(name="连续拖欠输出信函", length=1)
    public Boolean ltrOnContDlq;
    
    *//**
     * 催收账龄阀值
     *//*
    @PropertyInfo(name="入催最小账龄", length=1)
    public String collOnAge;

    *//**
     * 超限催收标志
     * Y/N
     *//*
    @PropertyInfo(name="超限入催", length=1)
    public Boolean collOnOvrlmt;

    *//**
     * 首次还款拖欠催收标志
     * collect on first statment delinquncy
     *//*
    @PropertyInfo(name="首次还款拖欠入催", length=1)
    public Boolean collOnFsDlq;

    *//**
     * 入催最小金额阀值
     *//*
    @PropertyInfo(name="免催最大金额", length=15, precision=2)
    public BigDecimal collMinpmt;
    
    *//**
     * 账单周期乘数
     * CYCLE_BASE_MULT为2，
     * 表明每2个月组成一个账单周期
     *//*
    @PropertyInfo(name="账单周期乘数", length=1)
    public Integer cycleBaseMult;

    *//**
     * 临时额度失效提醒天数
     *//*
    @PropertyInfo(name="临时额度失效提醒天数", length=2)
    public Integer tlExpPrmptPrd;
    
    *//**
     * 缺省取现额度比例
     *//*
    @PropertyInfo(name="默认取现比例", length=7, precision=4)
    public BigDecimal cashLimitRate;

    *//**
     * 缺省额度内分期比例
     *//*
    @PropertyInfo(name="默认额度内分期比例", length=7, precision=4)
    public BigDecimal loanLimitRate;
     
	//信用卡业务的特定参数-开始
	*//**
	 * 支持多币种共享账户额度
	 *//*
	@PropertyInfo(name="支持多币种共享账户额度", length=1)
	public Boolean isMultiCurrency;
	
	*//**
	 * 支持多卡一账户
	 *//*
	@PropertyInfo(name="支持多卡一账户", length=1)
	public Boolean isMultiCard;*/
	//信用卡业务的特定参数-结束
}