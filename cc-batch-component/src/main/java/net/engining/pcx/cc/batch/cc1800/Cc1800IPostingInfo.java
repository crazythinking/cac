package net.engining.pcx.cc.batch.cc1800;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.engining.pcx.cc.process.model.AcctModel;

/**
 * cc1800 核心入账相关信息模型<br>
 * 主要包含：<br>
 * AcctNo => List<Cc1800IAccountInfo> 账号相关的所有入账信息
 * acctSeq => AcctModel 账户号对应的账户信息模型
 * 
 * @author luxue
 *
 */
public class Cc1800IPostingInfo {
	/**
	 * Key:Integer-AcctNo;
	 */
	private Map< Integer, List<Cc1800IAccountInfo>> accountList = new HashMap<Integer, List<Cc1800IAccountInfo>>();

	private String org;
	
	private String custId;
	
	/**
	 * acctSeq => AcctModel的映射
	 */
	private Map<Integer, AcctModel> acctModelMap = Maps.newHashMap();
	
	private Map<Integer, Object> customizerInfoMap;
	
	
	public Map<Integer, Object> getCustomizerInfoMap() {
		return customizerInfoMap;
	}

	public void setCustomizerInfoMap(Map<Integer, Object> customizerInfoMap) {
		this.customizerInfoMap = customizerInfoMap;
	}

	public Map<Integer, List<Cc1800IAccountInfo>> getAccountList() {
		return accountList;
	}

	public void setAccountList(Map<Integer, List<Cc1800IAccountInfo>> accountList) {
		this.accountList = accountList;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}
	
	public void setCustId(String custId) {
		this.custId = custId;
	}
	
	public String getCustId() {
		return custId;
	}

	public Map<Integer, AcctModel> getAcctModelMap()
	{
		return acctModelMap;
	}

	public void setAcctModelMap(Map<Integer, AcctModel> acctModelMap)
	{
		this.acctModelMap = acctModelMap;
	}
}
