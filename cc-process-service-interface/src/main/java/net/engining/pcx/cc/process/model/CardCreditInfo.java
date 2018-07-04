package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.math.BigDecimal;

import net.engining.gm.infrastructure.enums.StmtMediaType;

/**
 * 卡片使用基本信息
 * @author Ronny
 *
 */
public class CardCreditInfo implements Serializable{

	private static final long serialVersionUID = 3048923618408592086L;

	/**
	 * 信用额度
	 */
	private BigDecimal creditLimit;
	
	/**
	 * 币种
	 * 用于信用额度/周期限额
	 */
	private String currencyCode;
	
	/**
	 * 周期限额
	 */
	private BigDecimal cycleLimit;
	
	/**
	 * 周期取现限额
	 */
	private BigDecimal cycleCashLimit;
	
	/**
	 * 网银周期交易限额
	 */
	private BigDecimal cycleNetLimit;
	
	/**
	 * 单笔交易限额
	 */
	private BigDecimal txnLimit;
	
	/**
	 * 单笔取现交易限额
	 */
	private BigDecimal txnCashLimit;

	/**
	 * 网银单笔交易限额
	 */
	private BigDecimal txnNetLimit;

	/**
	 * 账单介质类型标志
	 */
	private StmtMediaType stmtMediaType;

	/**
	 * 账单地址编号
	 */
	private Integer addressId;
	
	/**
	 * 账单地址：省
	 */
	private String state;
	
	/**
	 * 账单地址：市
	 */
	private String city;
	
	/**
	 * 账单地址：区县
	 */
	private String district;
	
	/**
	 * 账单地址：邮政编码
	 */
	private String zip;
	
	/**
	 * 账单地址：地址
	 */
	private String address;
	
	/**
	 * 所属分支行
	 */
	private String owningBranch;

	/**
	 * 邮件地址
	 */
	private String email;
	
	/**
	 * 账单地址编号
	 */
	private Integer addrId;
	
	public String getOwningBranch() {
		return owningBranch;
	}

	public void setOwningBranch(String owningBranch) {
		this.owningBranch = owningBranch;
	}

	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(BigDecimal creditLimit) {
		this.creditLimit = creditLimit;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getCycleLimit() {
		return cycleLimit;
	}

	public void setCycleLimit(BigDecimal cycleLimit) {
		this.cycleLimit = cycleLimit;
	}

	public BigDecimal getCycleCashLimit() {
		return cycleCashLimit;
	}

	public void setCycleCashLimit(BigDecimal cycleCashLimit) {
		this.cycleCashLimit = cycleCashLimit;
	}

	public BigDecimal getCycleNetLimit() {
		return cycleNetLimit;
	}

	public void setCycleNetLimit(BigDecimal cycleNetLimit) {
		this.cycleNetLimit = cycleNetLimit;
	}

	public BigDecimal getTxnLimit() {
		return txnLimit;
	}

	public void setTxnLimit(BigDecimal txnLimit) {
		this.txnLimit = txnLimit;
	}

	public BigDecimal getTxnCashLimit() {
		return txnCashLimit;
	}

	public void setTxnCashLimit(BigDecimal txnCashLimit) {
		this.txnCashLimit = txnCashLimit;
	}

	public BigDecimal getTxnNetLimit() {
		return txnNetLimit;
	}

	public void setTxnNetLimit(BigDecimal txnNetLimit) {
		this.txnNetLimit = txnNetLimit;
	}

	public StmtMediaType getStmtMediaType() {
		return stmtMediaType;
	}

	public void setStmtMediaType(StmtMediaType stmtMediaType) {
		this.stmtMediaType = stmtMediaType;
	}

	public Integer getAddressId() {
		return addressId;
	}

	public void setAddressId(Integer addressId) {
		this.addressId = addressId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getAddrId() {
		return addrId;
	}

	public void setAddrId(Integer addrId) {
		this.addrId = addrId;
	}
	
	
}
