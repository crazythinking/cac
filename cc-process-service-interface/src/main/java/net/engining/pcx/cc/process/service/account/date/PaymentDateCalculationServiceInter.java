package net.engining.pcx.cc.process.service.account.date;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.LocalDate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

public interface PaymentDateCalculationServiceInter {
    /**
     * 固定日最大值
     */
    int MAX_FIXDAY = 31;

    Date getNextPaymentDay(CactAccount cactAccount);

    PaymentPlanDetail setupPaymentNatureDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                             Date startDate, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail);

    PaymentPlanDetail setupPaymentNatureDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                             Date startDate, int fixedDay, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail);

    PaymentPlanDetail setupPaymentDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                       Date postDate, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail);

    PaymentPlanDetail setupPaymentDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                       Date postDate, int fixedDay, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail);

    int getOffset4BizDate2NatureDate(LocalDate bizDate, Interval interval);
}
