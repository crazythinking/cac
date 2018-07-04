package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pg.support.meta.PropertyInfo;

public class TxnPostCode implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@PropertyInfo(name="外部交易码", length=40)
	public SysTxnCd txnCd;
	
	@PropertyInfo(name="ruzhang交易码", length=40)
	public String postCode;
	
	public String getKey() {
    	return key(txnCd, postCode);
    }
    
    public static String key(SysTxnCd txnCd, String postCode) {
    	return txnCd + "|" + postCode;
    }

}
