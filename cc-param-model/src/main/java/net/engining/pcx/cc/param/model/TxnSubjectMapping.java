package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.param.model.enums.SubjectAmtType;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 
 * 交易会计科目关系
 * 
 * @author heyu.wang
 *
 */
public class TxnSubjectMapping implements Serializable {

	private static final long serialVersionUID = -4709315751231231827L;


	/**
     * 金额类型
     */
    @PropertyInfo(name="金额类型", length=20)
    public SubjectAmtType amtType;
	/**
     * 正常交易-借方科目
     */
    @PropertyInfo(name="正常交易-借方科目", length=40)
    public String ntDbSubjectCd;

    /**
     * 正常交易-借方科目
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd ntDbRedFlag;

    /**
     * 正常交易-贷方科目
     */
    @PropertyInfo(name="正常交易-贷方科目", length=40)
    public String ntCrSubjectCd;

    /**
     * 正常交易-贷方科目
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd ntCrRedFlag;

    /**
     * 正常交易-借方科目-表外
     */
    @PropertyInfo(name="正常交易-借方表外科目-表外", length=40)
    public String ntDbSubjectCdOs;

    /**
     * 正常交易-借方科目-表外
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd ntDbRedFlagOs;

    /**
     * 正常交易-贷方科目-表外
     */
    @PropertyInfo(name="正常交易-贷方科目-表外", length=40)
    public String ntCrSubjectCdOs;

    /**
     * 正常交易-贷方科目-表外
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd ntCrRedFlagOs;
    
    /**
     * 挂账交易-借方科目
     */
    @PropertyInfo(name="挂账交易-借方科目", length=40)
    public String stDbSubjectCd;

    /**
     * 挂账交易-借方科目
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd stDbRedFlag;

    /**
     * 挂账交易-贷方科目
     */
    @PropertyInfo(name="挂账交易-贷方科目", length=40)
    public String stCrSubjectCd;

    /**
     * 挂账交易-贷方科目
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd stCrRedFlag;

    /**
     * 核销交易-借方科目
     */
    @PropertyInfo(name="核销交易-借方科目", length=40)
    public String woDbSubjectCd;

    /**
     * 核销交易-借方科目
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd woDbRedFlag;

    /**
     * 核销交易-贷方科目
     */
    @PropertyInfo(name="核销交易-贷方科目", length=40)
    public String woCrSubjectCd;
    

    /**
     * 核销交易-贷方科目
     * 红蓝字标识
     */
    @PropertyInfo(name="///", length=1)
    public RedBlueInd woCrRedFlag;
    
    /**
	 * 业务类型
	 */
	@PropertyInfo(name="业务类型", length=2)
	public BusinessType businessType;

}
