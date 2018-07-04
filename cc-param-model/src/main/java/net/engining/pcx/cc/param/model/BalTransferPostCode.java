package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 账龄变化时发生子账户余额结转
 */
public class BalTransferPostCode implements Serializable {

	private static final long serialVersionUID = 7143761183356890512L;
    
    /**
     * 变更前账龄
     */
    @PropertyInfo(name="变化前账龄", length=1)
    public AgeGroupCd ageCdB4Changing;
    
    /**
     * 变更后账龄
     */
    @PropertyInfo(name="变化后账龄", length=1)
    public AgeGroupCd ageCdAfterChanging;
    
    /**
     * 子账户余额结转の交易代码
     */
    @PropertyInfo(name="子账户余额结转之交易代码", length=8)
    public String postCode;
    
    /**
     * 子账户利息计提结转の交易代码
     */
    @PropertyInfo(name="子账户利息计提结转之交易代码", length=8)
    public String postCode4IntAccrual;
    
    /**
     * 子账户罚息计提结转の交易代码
     */
    @PropertyInfo(name="子账户罚息计提结转之交易代码", length=8)
    public String postCode4IntPenaltyAccrual;
    
    /**
     * 子账户余额结转の内部账户入账代码
     */
    public List<String> internalAcctPostCodes;
    
    /**
     * 子账户利息计提结转の内部账户入账代码
     */
    public List<String> internalAcctPostCodes4IntAccrual;
    
    /**
     * 子账户罚息计提结转の内部账户入账代码
     */
    public List<String> internalAcctPostCodes4IntPenaltyAccrual;
    
    public String getKey() {
    	return key(ageCdB4Changing, ageCdAfterChanging);
    }
    
    public static String key(AgeGroupCd ageCdB4Changing, AgeGroupCd ageCdAfterChanging) {
    	return ageCdB4Changing + "|" + ageCdAfterChanging;
    }

}
