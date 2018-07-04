package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.engining.gm.infrastructure.enums.AgeGroupCd;
import net.engining.gm.infrastructure.enums.BusinessType;
import net.engining.pcx.cc.param.model.enums.Deadline;
import net.engining.pcx.cc.param.model.enums.FeeRace;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 
 * 交易会计科目关系 //TODO 该类的命名不合理，其实是PostCode与会计分录拆分规则；
 * 
 * @author heyu.wang
 *
 */
public class TxnSubjectParam implements Serializable {


	private static final long serialVersionUID = -281783322018251260L;

	@PropertyInfo(name="入账交易代码", length=8)
	public String txnCd;
	
	@PropertyInfo(name="账龄组代码", length=20)
	public AgeGroupCd ageGroupCd;
	
	@PropertyInfo(name="子账户类型", length=8)
	public String subAcctType;
	
	@PropertyInfo(name="期限", length=1)
	public Deadline deadline;
	
	@PropertyInfo(name="费种", length=30)
	public FeeRace feeRace;
	/**
	 * 是否联合贷
	 */
	@PropertyInfo(name="联合贷", length=1)
	public Boolean isCunLoan;
    
    /**
	 * 业务类型
	 */
	@PropertyInfo(name="业务类型", length=2)
	public BusinessType businessType; 
	
	@PropertyInfo(name="分录列表", length=2)
    public List<TxnSubjectMapping> entryList;

	@JsonIgnore
	public String getKey() {
    	return key(txnCd, ageGroupCd, businessType);
    }

    public static String key(String txnCd, AgeGroupCd ageGroupCd, BusinessType businessType) {
    	return txnCd + "|" + ageGroupCd + "|" + businessType;
    }
}
