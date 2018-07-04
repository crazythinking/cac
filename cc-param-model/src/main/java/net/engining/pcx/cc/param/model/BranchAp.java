package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pg.support.meta.PropertyInfo;

public class BranchAp implements Serializable {

	private static final long serialVersionUID = 6740900744414903179L;

	@PropertyInfo(name="分行代码", length=9)
	public String branchNo;
	
	@PropertyInfo(name="描述", length=80)
    public String description;
}
