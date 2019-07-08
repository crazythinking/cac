package net.engining.pcx.cc.process.service.account.date;

import net.engining.pcx.cc.param.model.SubAcctType;
import net.engining.pcx.cc.param.model.enums.IntAccumFrom;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 记账日、建账日相关计算服务；
 * 记账日、建账日都是基于系统业务日期的；
 *
 * @author Eric Lu
 * @create 2019-07-07 13:30
 **/
@Service
public class PostInDateCalculationService {

    /**
     * 计算余额成分的建账业务日，注意只适用于余额成分建账时：
     * 从业务场景上讲，延迟起息通常只存在与贷款类业务，首次建账时用到，因此只有针对Loan类型的余额成分需要+起息日延后天数
     *
     * @param billingDate
     *          账单日
     * @param intAccumFrom
     * @param postponeDays
     * @param postDate
     * @param subAcctType
     * @return
     */
    public LocalDate calcSetupDate4NewSubAcct(Date billingDate, IntAccumFrom intAccumFrom, Integer postponeDays,
                                              Date postDate, String subAcctType) {
        LocalDate setupDate;
        if (SubAcctType.DefaultSubAcctType.LOAN.toString().equals(subAcctType)){
            switch (intAccumFrom) {
                // 取当前账单日。这类余额肯定存在上一账单日数据
                case C:
                    setupDate = new LocalDate(billingDate).plusDays(postponeDays);
                    break;
                case P:
                    setupDate = new LocalDate(postDate).plusDays(postponeDays);
                    break;
                default:
                    throw new IllegalArgumentException("should not be here");
            }
        }
        else {
            switch (intAccumFrom) {
                // 取当前账单日。这类余额肯定存在上一账单日数据
                case C:
                    setupDate = new LocalDate(billingDate);
                    break;
                case P:
                    setupDate = new LocalDate(postDate);
                    break;
                default:
                    throw new IllegalArgumentException("should not be here");
            }
        }
        return setupDate;
    }
}
