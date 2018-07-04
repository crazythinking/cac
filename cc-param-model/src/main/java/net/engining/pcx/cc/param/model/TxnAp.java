package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pg.support.meta.PropertyInfo;

public class TxnAp implements Serializable {

	private static final long serialVersionUID = -1156638511007058598L;

	@PropertyInfo(name="交易代码", length=8)
	public String txnCd;
	
	@PropertyInfo(name="描述", length=80)
    public String description;
}
