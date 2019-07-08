package net.engining.pcx.cc.process.service.account.date;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.enums.PaymentMethod;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;
import net.engining.pg.support.utils.DateUtilsExt;
import net.engining.pg.support.utils.ValidateUtilExt;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * 还款日相关计算服务
 *
 * @author Eric Lu
 * @create 2019-07-06 11:34
 **/
@Service
public class PaymentDateCalculationService implements PaymentDateCalculationServiceInter {

    @Autowired
    Provider7x24 provider7x24;

    @Autowired
    NewComputeService newComputeService;

    /**
     * 获取下一期的还款日
     *
     */
    @Override
    public Date getNextPaymentDay(CactAccount cactAccount) {
        // 获取账户参数
        Account account = newComputeService.retrieveAccount(cactAccount);

        // 下一期的还款日
        Date paymentDay = caculatePaymentDate(account.intUnit, account.intFirstPeriodAdj, account.paymentMethod,
                cactAccount.getStartDate(), account.lockPaymentDay, account.fixedPmtDay, account.intUnitMult, account.pmtDueDays, cactAccount.getCurrentLoanPeriod());

        return paymentDay;
    }

    /**
     * 计算并设置还款计划明细相应期数的到期还款日期
     *
     * @param interval          还款周期间隔单位
     * @param intFirstPeriodAdj 首期天数是否调整
     * @param paymentMethod     还款方式
     * @param startDate         入账交易日期
     * @param mult              周期乘数
     * @param pmtDueDays        到期还款延后天数
     * @param i                 要计算的期数：从0开始，0代表第一期
     * @param detail            还款计划明细对象
     * @return
     */
    @Override
    public PaymentPlanDetail setupPaymentNatureDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                                    Date startDate, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail) {
        return setupPaymentNatureDate(interval, intFirstPeriodAdj, paymentMethod, startDate, 0, mult, pmtDueDays, i, detail);
    }

    /**
     * 计算并设置还款计划明细相应期数的到期还款自然日期 - 针对固定还款日
     *
     * @param interval          还款周期间隔单位
     * @param intFirstPeriodAdj 首期天数是否调整
     * @param paymentMethod     还款方式
     * @param startDate         开始计息自然日
     * @param fixedDay          固定还款日
     * @param mult              周期乘数
     * @param pmtDueDays        到期还款延后天数
     * @param i                 要计算的期数：从0开始，0代表第一期
     * @param detail            还款计划明细对象
     * @return
     */
    @Override
    public PaymentPlanDetail setupPaymentNatureDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                                    Date startDate, int fixedDay, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail) {
        Date date = caculatePaymentDate(interval, intFirstPeriodAdj, paymentMethod, startDate, true, fixedDay, mult, pmtDueDays, i);
        detail.setPaymentNatureDate(date);
        return detail;
    }

    /**
     * 计算并设置还款计划明细相应期数的到期还款日期
     *
     * @param interval          还款周期间隔单位
     * @param intFirstPeriodAdj 首期天数是否调整
     * @param paymentMethod     还款方式
     * @param postDate          入账交易日期
     * @param mult              周期乘数
     * @param pmtDueDays        到期还款延后天数
     * @param i                 要计算的期数：从0开始，0代表第一期
     * @param detail            还款计划明细对象
     * @return
     */
    @Override
    public PaymentPlanDetail setupPaymentDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                              Date postDate, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail) {
        return setupPaymentDate(interval, intFirstPeriodAdj, paymentMethod, postDate, 0, mult, pmtDueDays, i, detail);
    }

    /**
     * 计算并设置还款计划明细相应期数的到期还款日期 - 针对固定还款日
     *
     * @param interval          还款周期间隔单位
     * @param intFirstPeriodAdj 首期天数是否调整
     * @param paymentMethod     还款方式
     * @param postDate          入账交易日期
     * @param fixedDay          固定还款日
     * @param mult              周期乘数
     * @param pmtDueDays        到期还款延后天数
     * @param i                 要计算的期数：从0开始，0代表第一期
     * @param detail            还款计划明细对象
     * @return
     */
    @Override
    public PaymentPlanDetail setupPaymentDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                              Date postDate, int fixedDay, Integer mult, int pmtDueDays, int i, PaymentPlanDetail detail) {
        Date date = caculatePaymentDate(interval, intFirstPeriodAdj, paymentMethod, postDate, true, fixedDay, mult, pmtDueDays, i);
        detail.setPaymentDate(date);
        return detail;
    }

    /**
     * 根据系统业务日期计算与当前自然日的偏移量
     *
     * @param bizDate
     * @param interval
     * @return n days or n week or n month or n year
     */
    @Override
    public int getOffset4BizDate2NatureDate(LocalDate bizDate, Interval interval) {
        int offset = 0;
        Period period = null;
        LocalDate natureDate = new LocalDate(DateUtilsExt.truncate(new Date(), Calendar.DATE));
        if (ValidateUtilExt.isNullOrEmpty(bizDate)) {
            bizDate = provider7x24.getCurrentDate();
        }
        switch (interval) {
            case D:
                //bizDate大于natureDate为负数，反之正数
                period = new Period(bizDate, natureDate, PeriodType.days());
                offset = period.getDays();
                break;
            case W:
                //bizDate大于natureDate为负数，反之正数
                period = new Period(bizDate, natureDate, PeriodType.weeks());
                offset = period.getWeeks();
                break;
            case M:
                //bizDate大于natureDate为负数，反之正数
                period = new Period(bizDate, natureDate, PeriodType.months());
                offset = period.getMonths();
                break;
            case Y:
                //bizDate大于natureDate为负数，反之正数
                period = new Period(bizDate, natureDate, PeriodType.years());
                offset = period.getYears();
                break;
            default:
                throw new ErrorMessageException(ErrorCode.Restricted, String.format("not support %s", interval));
        }

        return offset;
    }

    /**
     * 计算并设置还款计划明细相应期数的到期还款日期
     *
     * @param interval          还款周期间隔单位
     * @param intFirstPeriodAdj 首期天数是否调整
     * @param paymentMethod     还款方式
     * @param startDate         起息日期
     * @param lockPaymentDay    是否固定还款日
     * @param fixedDay          固定还款日
     * @param mult              周期乘数
     * @param pmtDueDays        到期还款延后天数
     * @param i                 要计算的期数：从0开始，0代表第一期
     * @return
     */
    private Date caculatePaymentDate(Interval interval, Boolean intFirstPeriodAdj, PaymentMethod paymentMethod,
                                     Date startDate, Boolean lockPaymentDay, int fixedDay, Integer mult, int pmtDueDays, int i) {
        Date date;
        switch (interval) {
            case D:
                if (intFirstPeriodAdj != null && intFirstPeriodAdj) {
                    date = DateUtils.addDays(DateUtils.addDays(startDate, mult * (i + 1)), pmtDueDays);
                } else {
                    date = DateUtils.addDays(DateUtils.addDays(DateUtils.addDays(startDate, 0), mult * (i + 1)), pmtDueDays);
                }
                break;
            case W:
                date = DateUtils.addDays(DateUtils.addDays(startDate, mult * (i + 1) * 7), pmtDueDays);
                break;
            case M:
                // 按月支持分期的还款方式都可以支持固定日还款, 且固定还款日忽略还款延迟日参数
                if (lockPaymentDay) {
                    date = caculate4FixedPaymentDate(fixedDay, startDate, mult, i);
                } else {
                    date = DateUtils.addDays(DateUtils.addMonths(startDate, mult * (i + 1)), pmtDueDays);
                }
                break;
            case Y:
                date = DateUtils.addDays(DateUtils.addYears(startDate, mult * (i + 1)), pmtDueDays);
                break;
            default:
                throw new ErrorMessageException(ErrorCode.Restricted, String.format("not support %s", interval));
        }
        return date;
    }

    /**
     * 计算固定日还款情况下的还款日期，逻辑规定不满1个月靠后处理：
     * 假设每月固定5日还款<br>
     * 如果起息日 startDate=1月1日，小于等于固定日, 那么第一次还款应该为2月5日；
     * 如果起息日 startDate=1月10日，大于固定日, 那么第一次还款应该为3月5日；
     *
     * @param fixedDay  固定还款日
     * @param startDate 起息日
     * @param mult      周期乘数
     * @param i         要计算的期数：从0开始，0代表第一期
     * @return
     */
    private Date caculate4FixedPaymentDate(int fixedDay, Date startDate, Integer mult, int i) {
        Date date;
        if (fixedDay == 0 || fixedDay > MAX_FIXDAY) {
            throw new IllegalArgumentException("固定还款日参数不可为0 或大于31");
        }

        //根据固定还款日计算方式，以28日作为分界，因为大小月的原因，业务上规定28日之后都以28日作为还款日
        fixedDay = fixedDay >= 28 ? 28 : fixedDay;
        //计算入账日对应的LocalDate
        java.time.LocalDate startLocalDate = dateToLocalDate(startDate);
        //计算固定还款日相对于入账日所在月对应的LocalDate
        java.time.LocalDate fixedlocalDate = java.time.LocalDate.of(startLocalDate.getYear(), startLocalDate.getMonth(), fixedDay);

        //小于等于，加1个周期
        if (startLocalDate.getDayOfMonth() <= fixedDay) {
            date = localDateToDate(fixedlocalDate.plusMonths(mult * (i + 1)));
        }
        //大于，加2个周期
        else {
            date = localDateToDate(fixedlocalDate.plusMonths(mult * (i + 2)));
        }

        return date;
    }

    static void main(String[] args) {
        PaymentDateCalculationService paymentDateCalculationService = new PaymentDateCalculationService();
        //放款日大于固定日
        Date date = paymentDateCalculationService.caculate4FixedPaymentDate(2, new Date(), 1, 0);
        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd"));
        date = paymentDateCalculationService.caculate4FixedPaymentDate(2, new Date(), 1, 1);
        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd"));
        date = paymentDateCalculationService.caculate4FixedPaymentDate(2, new Date(), 1, 2);
        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd"));

        //放款日小于固定日
        date = paymentDateCalculationService.caculate4FixedPaymentDate(10, new Date(), 1, 0);
        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd"));
        date = paymentDateCalculationService.caculate4FixedPaymentDate(10, new Date(), 1, 1);
        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd"));
        date = paymentDateCalculationService.caculate4FixedPaymentDate(10, new Date(), 1, 2);
        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd"));
    }

    /**
     * java.time.LocalDate --> java.util.Date
     */
    private static Date localDateToDate(java.time.LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        Date date = Date.from(instant);
        return date;
    }

    /**
     * java.util.Date --> java.time.LocalDate
     */
    private static java.time.LocalDate dateToLocalDate(java.util.Date date) {
        if (date instanceof java.sql.Date){
            return ((java.sql.Date) date).toLocalDate();
        }
        else {
            Instant instant = date.toInstant();
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            java.time.LocalDate localDate = localDateTime.toLocalDate();
            return localDate;
        }

    }

}
