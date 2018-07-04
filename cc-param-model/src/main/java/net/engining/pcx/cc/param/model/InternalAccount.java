package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pcx.cc.param.model.enums.AccountCategory;
import net.engining.pcx.cc.param.model.enums.InternalAccountStatus;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 内部账户定义
 * 
 * @author heyu.wang
 *
 */
public class InternalAccount implements Serializable {


	private static final long serialVersionUID = 7720389304308164241L;

	/**
	 * 内部账户编号
	 */
	@PropertyInfo(name="内部账户编号", length=30)
	public String internalAccountId;
	
	/**
	 * 描述
	 */
	@PropertyInfo(name="描述", length=100)
	public String desc;
	
	/**
     * 所属科目号
     */
	@PropertyInfo(name="所属科目号", length=40)
    public String subjectCd;
    
    /**
     * 状态
     */
	@PropertyInfo(name="状态", length=10)
    public InternalAccountStatus status;
	
	/**
     * 账户类别
     */
	@PropertyInfo(name="账户类别", length=10)
	public AccountCategory accountCategory;
}
