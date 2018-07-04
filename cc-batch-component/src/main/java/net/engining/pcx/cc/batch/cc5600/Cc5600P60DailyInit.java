package net.engining.pcx.cc.batch.cc5600;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pg.support.utils.DateUtilsExt;

/**
 * 总账每日清零
 * @author binarier
 *
 */
public class Cc5600P60DailyInit implements ItemProcessor<ApGlBal, ApGlBal> {
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Override
	public ApGlBal process(ApGlBal item) throws Exception {
		item.setLastDbBal(item.getDbBal());
		item.setLastCrBal(item.getCrBal());
		
		Calendar cl = Calendar.getInstance();
		cl.setTime(batchDate);
		cl.add(Calendar.DAY_OF_MONTH, 1);
		
//		SystemStatus systemStatus = systemStatusFacility.getSystemStatus();
		
		if (DateUtilsExt.isFirstDayOfMonth(cl.getTime())) {
			item.setLastMthDbBal(item.getDbBal());
			item.setLastMthCrBal(item.getCrBal());

			if (DateUtilsExt.isFirstDayOfQuarter(cl.getTime())) {
				item.setLastQtrDbBal(item.getDbBal());
				item.setLastQtrCrBal(item.getCrBal());

				if (DateUtilsExt.isFirstDayOfYear(cl.getTime())) {
					item.setLastYrDbBal(item.getDbBal());
					item.setLastYrCrBal(item.getCrBal());
				}
			}
		}
		item.setDbAmt(BigDecimal.ZERO);
		item.setDbCount(0);
		item.setCrAmt(BigDecimal.ZERO);
		item.setCrCount(0);

		return item;
	}
}