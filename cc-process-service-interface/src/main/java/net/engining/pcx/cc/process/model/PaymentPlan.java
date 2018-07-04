package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.engining.pcx.cc.param.model.enums.PaymentMethod;

/**
 * 还款计划
 * @author Ronny
 *
 */
public class PaymentPlan implements Serializable{
	  
	private static final long serialVersionUID = 4687280679133291248L;
	
	/**
	 * 产品名称
	 */
	private String productCode;
	/**
	 * 产品描述
	 */
	private String producDesc;
	/**
	 * 子产品名称
	 */
	private String prodAcctParamId;
	/**
	 * 子产品描述
	 */
	private String prodAcctParamDesc;
	/**
	 * 贷款账户编号
	 */
	private Integer acctSeq;

	/**
	 * 创建日期
	 */
	private Date createDate;
	
	/**
	 * 贷款日期
	 */
	private Date postDate;
	
	/**
	 * 贷款总期数
	 */
	private Integer totalLoanPeriod;

	/**
	 * 贷款总金额
	 */
	private BigDecimal totalLoanPrincipalAmt;
	
	/**
	 * 剩余总期数
	 */
	private Integer leftLoanPeriod;
	
	/**
	 * 剩余本金
	 */
	private BigDecimal leftLoanPrincipalAmt;
	
	/**
	 * 还款方式
	 */
	private PaymentMethod paymentMethod;
	  
	/**
	 * 年利率
	 */
	private BigDecimal yearRate;
	
	/**
	 * 还款明细表
	 */
	private List<PaymentPlanDetail> details;
	
	

	private Map <Integer ,PaymentPlanDetail> detailsMap ;
	
	
	
	public String getProducDesc() {
		return producDesc;
	}

	public void setProducDesc(String producDesc) {
		this.producDesc = producDesc;
	}

	public String getProdAcctParamId() {
		return prodAcctParamId;
	}

	public void setProdAcctParamId(String prodAcctParamId) {
		this.prodAcctParamId = prodAcctParamId;
	}

	public String getProdAcctParamDesc() {
		return prodAcctParamDesc;
	}

	public void setProdAcctParamDesc(String prodAcctParamDesc) {
		this.prodAcctParamDesc = prodAcctParamDesc;
	}

	public Map<Integer, PaymentPlanDetail> getDetailsMap() {
		return detailsMap;
	}

	public void setDetailsMap(Map<Integer, PaymentPlanDetail> detailsMap) {
		this.detailsMap = detailsMap;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getTotalLoanPeriod() {
		return totalLoanPeriod;
	}

	public void setTotalLoanPeriod(Integer totalLoanPeriod) {
		this.totalLoanPeriod = totalLoanPeriod;
	}

	public BigDecimal getTotalLoanPrincipalAmt() {
		return totalLoanPrincipalAmt;
	}

	public void setTotalLoanPrincipalAmt(BigDecimal totalLoanPrincipalAmt) {
		this.totalLoanPrincipalAmt = totalLoanPrincipalAmt;
	}

	public Integer getLeftLoanPeriod() {
		return leftLoanPeriod;
	}

	public void setLeftLoanPeriod(Integer leftLoanPeriod) {
		this.leftLoanPeriod = leftLoanPeriod;
	}

	public BigDecimal getLeftLoanPrincipalAmt() {
		return leftLoanPrincipalAmt;
	}

	public void setLeftLoanPrincipalAmt(BigDecimal leftLoanPrincipalAmt) {
		this.leftLoanPrincipalAmt = leftLoanPrincipalAmt;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getYearRate() {
		return yearRate;
	}

	public void setYearRate(BigDecimal yearRate) {
		this.yearRate = yearRate;
	}

	public List<PaymentPlanDetail> getDetails() {
		return details;
	}

	public void setDetails(List<PaymentPlanDetail> details) {
		this.details = details;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	 
	public Integer getAcctSeq() {
		return acctSeq;
	}

	public void setAcctSeq(Integer acctSeq) {
		this.acctSeq = acctSeq;
	}
	
	
	
}
