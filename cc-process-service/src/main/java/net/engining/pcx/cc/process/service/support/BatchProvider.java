package net.engining.pcx.cc.process.service.support;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.param.model.SystemStatus;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pg.support.utils.DateUtilsExt;
import net.engining.pg.support.utils.ValidateUtilExt;

public class BatchProvider implements Provider7x24
{
	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@Override
	public LocalDate getCurrentDate()
	{
		SystemStatus status = systemStatusFacility.getSystemStatus();
		switch (status.systemStatus)
		{
		case B:
			return new LocalDate(status.processDate);
		case N:
			return new LocalDate(status.businessDate);
		default:
			throw new IllegalArgumentException("should not be here");
		}
	}

	@Override
	public BigDecimal getBalance(CactSubAcct cactSubAcct)
	{
		//批量时使用日终余额
		return cactSubAcct.getEndDayBal();
	}

	@Override
	public void increaseBalance(CactSubAcct cactSubAcct, BigDecimal balanceDelta)
	{
		cactSubAcct.setCurrBal(cactSubAcct.getCurrBal().add(balanceDelta));
		cactSubAcct.setEndDayBal(cactSubAcct.getEndDayBal().add(balanceDelta));
	}

	@Override
	public boolean shouldDeferOffset()
	{
		//批量时始终不做延迟冲销
		return false;
	}

	@Override
	public boolean allowRaiseAge()
	{
		return true;
	}

	@Override
	public boolean isInternalAccountAsBatch()
	{
		SystemStatus status = systemStatusFacility.getSystemStatus();
		switch (status.systemStatus)
		{
		case B:
			return true;
		case N:
			return false;
		default:
			throw new IllegalArgumentException("should not be here");
		}
	}

	@Override
	public boolean shouldDeferPenaltySettle()
	{
		return shouldDeferOffset();
	}

}
