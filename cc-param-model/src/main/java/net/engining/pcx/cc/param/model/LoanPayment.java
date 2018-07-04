package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import net.engining.pcx.cc.param.model.enums.CalcMethod;
import net.engining.pcx.cc.param.model.enums.DistributeMethod;
import net.engining.pcx.cc.param.model.enums.LoanFeeMethod;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 还款计划定义
 * @author Ronny
 *
 */
public class LoanPayment implements Serializable{

	private static final long serialVersionUID = -6116365921885550191L;

	/**
	 * 还款计划代码
	 */
	@PropertyInfo(name="还款计划代码", length=8)
	public String code;
	
	/**
	 * 描述
	 */
	@PropertyInfo(name="描述", length=80)
	public String description;
	
	/**
	 * 期数
	 */
	@PropertyInfo(name="期数", length=2)
	public Integer term;
	
	/**
	 * 最小分期金额
	 */
	@PropertyInfo(name="最小分期金额", length=15, precision=2)
	public BigDecimal minAmount;
	
	/**
	 * 最大允许分期金额
	 */
	@PropertyInfo(name="最大允许分期金额", length=15, precision=2)
	public BigDecimal maxAmount;
	
	/**
	 * 分期手续费收取方式
	 */
	@PropertyInfo(name="分期手续费收取方式", length=1)
	public LoanFeeMethod loanFeeMethod;

	/**
	 * 分期手续费计算方式
	 */
	@PropertyInfo(name="分期手续费计算方式", length=1)
	public CalcMethod loanFeeCalcMethod;

	/**
	 * 分期手续费金额
	 */
	@PropertyInfo(name="分期手续费金额", length=15, precision=2)
	public BigDecimal feeAmount;
	
	/**
	 * 分期手续费比例
	 */
	@PropertyInfo(name="分期手续费比例", length=7, precision=4)
	public BigDecimal feeRate;
	
	/**
	 * 是否允许展期
	 */
	@PropertyInfo(name="允许展期", length=1)
	public Boolean rescheduleInd;
	
	/**
	 * 展期手续费收取方式
	 */
	@PropertyInfo(name="展期手续费收取方式", length=1)
	public LoanFeeMethod rescheduleFeeMethod;

	/**
	 * 展期手续费计算方式
	 */
	@PropertyInfo(name="展期手续费计算方式", length=1)
	public CalcMethod rescheduleCalcMethod;

	/**
	 * 展期手续费金额
	 */
	@PropertyInfo(name="展期手续费金额", length=15, precision=2)
	public BigDecimal rescheduleFeeAmount;
	
	/**
	 * 展期手续费比例
	 */
	@PropertyInfo(name="展期手续费比例", length=7, precision=4)
	public BigDecimal rescheduleFeeRate;
	
	/**
	 * 本金分配方式
	 */
	@PropertyInfo(name="本金分配方式", length=1)
	public DistributeMethod distributeMethod;
	
	/**
	 * 本金分配方式中, 分配表计算比例
	 */
	@PropertyInfo(name="分配表计算比例", length=5, precision=2)
	public List<BigDecimal> distributeRateList;
}
