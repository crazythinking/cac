package net.engining.pcx.cc.process.service.account.date;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 结息相关计算服务
 *
 * @author Eric Lu
 * @create 2019-07-06 17:51
 **/
@Service
public class SettleInterestDateCalculationService {

    /**
     * 获取当前结息日通用逻辑
     * @param cactAccount
     * @return
     */
    public static Date getInterestDateCommon(CactAccount cactAccount){
        return cactAccount.getInterestDate();
    }

    /**
     * 获取上次结息日通用逻辑
     * @param cactAccount
     * @return
     */
    public static Date getLastInterestDateCommon(CactAccount cactAccount){
        return cactAccount.getLastInterestDate();
    }
}
