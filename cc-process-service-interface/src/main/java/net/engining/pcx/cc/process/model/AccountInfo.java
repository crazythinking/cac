package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.StmtMediaType;


public class AccountInfo implements Serializable {

    private static final long serialVersionUID = -5729072520856254152L;
    /**
     * 账户ID
     */
    private Integer acctNo;
    /**
     * 客户ID
     */
    private String custId;
    /**
     * 账户参数代码
     */
    private String paramId;

    /**
     * 业务日期，如果是签约建账需要传入该日期
     */
    private Date businessDate;

    /**
     * 额度
     */
    private BigDecimal acctLimit;
    /**
     * 账单周期
     */
    private Integer billingCycle;
    /**
     * 所属分行
     */
    private String owningBranch;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 地址ID
     */
    private Integer addrId;
    /**
     * 账单类型
     */
    private StmtMediaType stmtMediaType;
    /**
     * 是否多币种
     */
    private Boolean isMultiCurr;

    /**
     * 贷款总期数
     */
    private Integer totalLoanPeriod;

    /**
     * 系统内自动扣款账号
     */
    private Integer autoPayAcctSeqInSystem;

    public Integer getAcctNo() {
        return acctNo;
    }

    public void setAcctNo(Integer acctNo) {
        this.acctNo = acctNo;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public BigDecimal getAcctLimit() {
        return acctLimit;
    }

    public void setAcctLimit(BigDecimal acctLimit) {
        this.acctLimit = acctLimit;
    }

    public Integer getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(Integer billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getOwningBranch() {
        return owningBranch;
    }

    public void setOwningBranch(String owningBranch) {
        this.owningBranch = owningBranch;
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

    public StmtMediaType getStmtMediaType() {
        return stmtMediaType;
    }

    public void setStmtMediaType(StmtMediaType stmtMediaType) {
        this.stmtMediaType = stmtMediaType;
    }

    public Boolean getIsMultiCurr() {
        return isMultiCurr;
    }

    public void setIsMultiCurr(Boolean isMultiCurr) {
        this.isMultiCurr = isMultiCurr;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public Integer getTotalLoanPeriod() {
        return totalLoanPeriod;
    }

    public void setTotalLoanPeriod(Integer totalLoanPeriod) {
        this.totalLoanPeriod = totalLoanPeriod;
    }

    public Integer getAutoPayAcctSeqInSystem() {
        return autoPayAcctSeqInSystem;
    }

    public void setAutoPayAcctSeqInSystem(Integer autoPayAcctSeqInSystem) {
        this.autoPayAcctSeqInSystem = autoPayAcctSeqInSystem;
    }
}
