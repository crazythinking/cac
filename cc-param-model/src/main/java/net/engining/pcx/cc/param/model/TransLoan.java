package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import net.engining.pg.support.meta.PropertyInfo;

/**
 * 交易分期活动
 * @author Ronny
 *
 */
public class TransLoan implements Serializable {

	private static final long serialVersionUID = -5815152000763337849L;

	/**
     * 分期代码
     */
    @PropertyInfo(name="活动代码", length=8)
	public String activityCode;
	
    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;
	
    /**
     * 分期中止账龄
     */
    @PropertyInfo(name="中止账龄", length=1)
    public String terminateAgeCd;
    
    /**
     * 受支持的交易代码
     * {@link PostCode}
     */
    public List<String> postCodeList;
    
    /**
     * 支持的还款计划列表
     * Key - 分期期数
     * Value - 还款计划表
     */
    public List<LoanPayment> loanPaymentList;
    
    /**
     * 支持的卡产品列表
     * String - 卡产品编号
     */
    public List<String> productList;
}
