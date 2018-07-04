package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.gm.infrastructure.enums.CardClass;
import net.engining.gm.infrastructure.enums.CustServiceEvent;
import net.engining.gm.param.model.Product;
import net.engining.pg.support.meta.PropertyInfo;
import net.engining.pg.support.meta.ReferEnums;

/**
 * 卡产品贷记属性参数
 * 如果是账户级的参数，需要通过账户信息中的product_cd来查找产品参数，然后取对应的账户级参数
 * 如果不是账户级参数，需要通过卡片所属产品的product_cd来查找产品参数，然后取对应的参数
 */
public class ProductCc extends Product implements Serializable {

	private static final long serialVersionUID = 8168965691382009925L;

	/**
	 * 卡片等级
	 */
	@PropertyInfo(name="卡片等级", length=1)
    public CardClass cardClass;
    /**
     * 受支持账户参数
     * key1 - 业务类型(普通贷记、大额分期等等)
     * key2 - 货币代码
     * value - 账户参数
     */
    public Map<BusinessType, Map<String, Account>> accountParams;
    
    /**
     * 授权匹配宽限比例
     */
    @PropertyInfo(name="授权匹配宽限比例", length=7, precision=4)
    public BigDecimal athMatchTolRt;    

    /**
     * 预授权完成宽限比例
     */
    @PropertyInfo(name="预授权完成宽限比例", length=7, precision=4)
    public BigDecimal preathCompTolRt;

    /**
     * 未匹配授权额度冻结天数(账户级参数)
     */
    @PropertyInfo(name="未匹配授权额度冻结天数", length=3)
    public Integer unmatchRtnPrd;

    /**
     * 缺省账单日
     */
    @PropertyInfo(name="缺省账单日", length=2)
    public Integer dfltCycleDay;

    /**
     * OTB是否包含未匹配还款(账户级参数)
     */
    @PropertyInfo(name="可用额度包含未匹配还款", length=1)
    public Boolean otbInclCrath;

    /**
     * OTB是否包含溢缴款(账户级参数)
     */
    @PropertyInfo(name="可用额度包含溢缴款", length=1)
    public Boolean otbInclCrbal;

    /**
     * OTB是否包含争议金额(账户级参数)
     */
    @PropertyInfo(name="可用额度包含争议金额", length=1)
    public Boolean otbInclDspt;

    /**
     * OTB是否包含分期付款(账户级参数)
     * Y/N
     */
    @PropertyInfo(name="可用额度包含分期付款", length=1)
    public Boolean otbInclFrzn;

    /**
     * 取现可用额度是否包含未匹配还款(账户级参数)
     */
    @PropertyInfo(name="取现额度包含未匹配还款", length=1)
    public Boolean cotbInclCrath;

    /**
     * 取现可用额度是否包含溢缴款(账户级参数)
     */
    @PropertyInfo(name="取现额度包含溢缴款", length=1)
    public Boolean cotbInclCrbal;

    /**
     * 分期标识
     * 该产品是否支持分期付款
     */
    @PropertyInfo(name="支持分期付款", length=1)
    public Boolean loanSuppInd;
    
    /**
     * 专项分期标识
     * 该产品是否支持专项分期
     */
    @PropertyInfo(name="支持专项分期", length=1)
    public Boolean specLoanSuppInd;
    
    /**
     * 客服费用收取指示
     * key-客服服务代码
     */
    @ReferEnums(CustServiceEvent.class)
    public Map<CustServiceEvent, CustomerServiceFee> customerServiceFee;
    
    /**
     * 交易费用清单
     * key - 入账交易代码(PostCode主键)
     * value - 费用收取清单
     */
    public Map<String, List<TxnFee>> txnFeeList;
    
    /**
     * 每日ATM取现额度
     */
    public BigDecimal dailyAtmLimit;

    /**
     * 超限宽限金额(账户级参数)
     */
    public BigDecimal overlmtFee;
    
}