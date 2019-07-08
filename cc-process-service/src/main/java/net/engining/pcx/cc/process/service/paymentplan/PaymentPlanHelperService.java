package net.engining.pcx.cc.process.service.paymentplan;

import com.querydsl.jpa.impl.JPAQueryFactory;
import net.engining.pcx.cc.infrastructure.shared.model.CactLoanPaymentDetail;
import net.engining.pcx.cc.infrastructure.shared.model.CactLoanPaymentPlan;
import net.engining.pcx.cc.infrastructure.shared.model.QCactLoanPaymentDetail;
import net.engining.pcx.cc.infrastructure.shared.model.QCactLoanPaymentPlan;
import net.engining.pcx.cc.process.model.PaymentPlan;
import net.engining.pcx.cc.process.model.PaymentPlanDetail;
import net.engining.pg.support.utils.ValidateUtilExt;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

/**
 * 还款计划辅助逻辑服务
 *
 * @author Eric Lu
 * @create 2019-07-07 20:04
 **/
@Service
public class PaymentPlanHelperService {

    @PersistenceContext
    private EntityManager em;

    /**
     * 获取cc完整的静态的还款计划及其明细
     *
     * @param acctSeq
     * @return
     */
    public PaymentPlan getPaymentPlan(Integer acctSeq) {
        PaymentPlan paymentPlan = new PaymentPlan();

        QCactLoanPaymentPlan qCactLoanPaymentPlan = QCactLoanPaymentPlan.cactLoanPaymentPlan;
        CactLoanPaymentPlan cactLoanPaymentPlan = new JPAQueryFactory(em)
                .selectFrom(qCactLoanPaymentPlan)
                .where(qCactLoanPaymentPlan.acctSeq.eq(acctSeq))
                .fetchOne();

        if (cactLoanPaymentPlan != null) {
            paymentPlan.setAcctSeq(cactLoanPaymentPlan.getAcctSeq());
            paymentPlan.setProdAcctParamId(cactLoanPaymentPlan.getAcctParamId());
            paymentPlan.setPaymentMethod(cactLoanPaymentPlan.getPaymentMethod());
            paymentPlan.setCreateDate(cactLoanPaymentPlan.getSetupDate());
            paymentPlan.setTotalLoanPeriod(cactLoanPaymentPlan.getTotalLoanPeriod());
            paymentPlan.setTotalLoanPrincipalAmt(cactLoanPaymentPlan.getTotalLoanPrincipalAmt());
            paymentPlan.setYearRate(cactLoanPaymentPlan.getYearRate());
            paymentPlan.setLeftLoanPeriod(cactLoanPaymentPlan.getLeftLoanPeriod());
            paymentPlan.setLeftLoanPrincipalAmt(cactLoanPaymentPlan.getLeftLoanPrincipalAmt());
            paymentPlan.setPostDate(cactLoanPaymentPlan.getPostDate());
        }
        else {
            return null;
        }

        QCactLoanPaymentDetail qCactLoanPaymentDetail = QCactLoanPaymentDetail.cactLoanPaymentDetail;
        List<CactLoanPaymentDetail> cactLoanPaymentDetails = new JPAQueryFactory(em)
                .selectFrom(qCactLoanPaymentDetail)
                .where(qCactLoanPaymentDetail.planSeq.eq(cactLoanPaymentPlan.getPlanSeq()))
                .fetch();

        if (cactLoanPaymentDetails != null) {
            List<PaymentPlanDetail> details = new ArrayList<>(cactLoanPaymentDetails.size());
            Map<Integer, PaymentPlanDetail> detailsMap = new HashMap<>(cactLoanPaymentDetails.size());
            for (CactLoanPaymentDetail cactLoanPaymentDetail : cactLoanPaymentDetails) {
                PaymentPlanDetail paymentPlanDetail = new PaymentPlanDetail();
                paymentPlanDetail.setLoanPeriod(cactLoanPaymentDetail.getLoanPeriod());
                paymentPlanDetail.setPaymentDate(cactLoanPaymentDetail.getPaymentDate());
                paymentPlanDetail.setPaymentNatureDate(cactLoanPaymentDetail.getPaymentNatureDate());
                paymentPlanDetail.setFeeAmt(cactLoanPaymentDetail.getFeeAmt());
                paymentPlanDetail.setInterestAmt(cactLoanPaymentDetail.getInterestAmt());
                paymentPlanDetail.setPrincipalBal(cactLoanPaymentDetail.getPrincipalBal());
                details.add(paymentPlanDetail);
                detailsMap.put(paymentPlanDetail.getLoanPeriod(), paymentPlanDetail);
            }
            paymentPlan.setDetails(details);
            paymentPlan.setDetailsMap(detailsMap);
        }
        return paymentPlan;
    }

    public Date getLastPaymentPlanDate(PaymentPlan paymentPlan){
        return paymentPlan.getDetailsMap().get(paymentPlan.getTotalLoanPeriod()).getPaymentDate();
    }

    public Date getLastPaymentPlanDate(Integer acctSeq){
        QCactLoanPaymentDetail qCactLoanPaymentDetail = QCactLoanPaymentDetail.cactLoanPaymentDetail;
        List<CactLoanPaymentDetail> cactLoanPaymentDetails = new JPAQueryFactory(em)
                .selectFrom(qCactLoanPaymentDetail)
                .where(qCactLoanPaymentDetail.acctSeq.eq(acctSeq))
                .orderBy(qCactLoanPaymentDetail.loanPeriod.desc())
                .fetch();
        if (ValidateUtilExt.isNotNullOrEmpty(cactLoanPaymentDetails)){
            return cactLoanPaymentDetails.get(0).getPaymentDate();
        }

        return null;
    }
}
