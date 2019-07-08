package net.engining.pcx.cc.process.service.support;

import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.process.service.account.PostDetail;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 通用逻辑工具类
 *
 * @author Eric Lu
 * @create 2019-07-07 12:07
 **/
public class CommonLogicUtils {

    public static PostDetail setupPostDetail4PostTxnTypeDef(BigDecimal amount, CactAccount cactAccount, String postCode,
                                                      Date now, PostTxnTypeDef postTxnTypeDef) {
        PostDetail detail = new PostDetail();
        detail.setTxnDate(now);
        detail.setTxnTime(now);
        detail.setPostTxnType(PostTxnTypeDef.M);
        detail.setPostCode(postCode);
        detail.setTxnAmt(amount);
        detail.setPostAmt(amount);
        detail.setTxnCurrCd(cactAccount.getCurrCd());
        detail.setPostCurrCd(cactAccount.getCurrCd());
        return detail;
    }
}
