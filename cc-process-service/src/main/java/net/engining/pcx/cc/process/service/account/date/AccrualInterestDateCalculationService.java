package net.engining.pcx.cc.process.service.account.date;

import net.engining.gm.infrastructure.enums.Interval;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pcx.cc.param.model.SubAcctType;
import net.engining.pcx.cc.param.model.enums.ComputInte4HeadTail;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 记息日相关计算服务；
 * 包括开始计息日与结束计息日的计算；
 * 包括应收利息、计提利息、补计息计算操作时获取日期的逻辑；
 *
 * @author Eric Lu
 * @create 2019-07-07 13:02
 **/
@Service
public class AccrualInterestDateCalculationService {

    @Autowired
    private Provider7x24 provider7x24;

    @Autowired
    private PostInDateCalculationService postInDateCalculationService;

    /**
     * 基于某个时间点计算计息的截止日期
     *
     * @param subAcctType
     *          余额成分类型
     * @param lastPaymentPlanDate
     *          还款计划最后一期的还款日
     * @param someDate
     * @param cycleBase
     *          计息周期单位
     * @param cycleBaseMult
     *          计息周期乘数
     *
     * @return
     */
    public LocalDate calcEndDate(String subAcctType, Date lastPaymentPlanDate, LocalDate someDate, Interval cycleBase, Integer cycleBaseMult) {

        //当前业务日期是否最后一期到期日
        LocalDate curDate = provider7x24.getCurrentDate();
        //当前业务日期是否最后一期的还款日
        boolean lastDay = lastPaymentPlanDate.compareTo(curDate.toDate()) == 0;

        //是否按日计息
        boolean calcIntByDay = cycleBase == Interval.D && cycleBaseMult == 1;

        // 如果当前期数在最后一期之前，还款日当天的利息在还款日当天收掉(因为到期日当天还款，日终批量还是会在当期计一天利息)，最后一期中还款的话当日就不收利息，其实是没有结清。
        // 这段逻辑依赖于批量顺序，本金先计息，再结息的情况下，LOAN类型子账户从创建日起每日计息；LBAL类型子账户创建日不计息，之后每天计息；
        // 因此算头不算尾时，LOAN不需要（在未跑批的情况下）预修当前业务日所属的利息；但LBAL则需要；
        //是否需要日期+1
        boolean plusOneDay4LoanOrLbal = (SubAcctType.DefaultSubAcctType.LBAL.toString().equals(subAcctType)
                || (SubAcctType.DefaultSubAcctType.LOAN.toString().equals(subAcctType) && !lastDay))
                && calcIntByDay;

        if (plusOneDay4LoanOrLbal) {
            someDate = someDate.plusDays(1);
        }

        return someDate;
    }

    /**
     * 计算当前余额成分的起息日；
     * 在余额成分上次计息日期为空时，根据起息日类型确定第一次起息日；
     * 但对于“LBAL”，需要再加1天，因为产生该子账户的时候就是结息日，已经计息，所以开始计息日是下一日；
     * 其他情况只要上次计息日期+1
     *
     * @param subAcctType
     *          余额成分类型
     * @param cactAccount
     * @param subAcct
     * @param account
     * @param lastInterestDate
     *          上次计息日期
     *
     * @return
     */
    public LocalDate calcStartDate(Date lastInterestDate, String subAcctType, CactAccount cactAccount, SubAcct subAcct, Account account) {
        LocalDate startDate;
        //建账时上次计息日必然为空
        if (lastInterestDate == null) {
            // 余额成分的建账日
            // TODO cactAccount.getFirstStmtDate()定义不正确，应该为账单日，并添加上次账单日
            startDate = postInDateCalculationService.calcSetupDate4NewSubAcct(cactAccount.getFirstStmtDate(), subAcct.intAccumFrom,
                        account.postponeDays, provider7x24.getCurrentDate().toDate(), subAcct.subAcctType);

            // LBAL子账户修利息的时候只修当天就可以了，setUpDate的时候不用计息,导致lastComputingInterestDate是null
            if (SubAcctType.DefaultSubAcctType.LBAL.toString().equals(subAcctType)) {
                startDate = startDate.plusDays(1);
            }

            // 对于LOAN子账户，存在不计头的情况
            boolean flag = SubAcctType.DefaultSubAcctType.LOAN.toString().equals(subAcctType)
                           && (ComputInte4HeadTail.NHNT.equals(account.computInte4HeadTail)
                               || ComputInte4HeadTail.NHYT.equals(account.computInte4HeadTail));
            if (flag) {
                startDate = startDate.plusDays(1);
            }
        }
        else {
            startDate = new LocalDate(lastInterestDate).plusDays(1);
        }
        return startDate;
    }

}
