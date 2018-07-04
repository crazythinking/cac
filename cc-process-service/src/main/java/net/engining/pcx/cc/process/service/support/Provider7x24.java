package net.engining.pcx.cc.process.service.support;

import java.math.BigDecimal;

import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;

import org.joda.time.LocalDate;

public interface Provider7x24
{
	/**
	 * 获取系统当前业务日期
	 * @return
	 */
	LocalDate getCurrentDate();
	
	/**
	 * 用于7x24小时支持下取余额
	 * @param cactSubAcct
	 * @return
	 */
	BigDecimal getBalance(CactSubAcct cactSubAcct);
	
	void increaseBalance(CactSubAcct cactSubAcct, BigDecimal balanceDelta);
	
	boolean shouldDeferOffset();
	
	/**
	 * 是否允许降账龄
	 * @return
	 */
	boolean allowRaiseAge();
	
	/**
	 * 内部户入账时是否以批量方式处理
	 * @return
	 */
	boolean isInternalAccountAsBatch();
	
	/**
	 * 是否需要延迟结罚息
	 * @return
	 */
	boolean shouldDeferPenaltySettle();
}
