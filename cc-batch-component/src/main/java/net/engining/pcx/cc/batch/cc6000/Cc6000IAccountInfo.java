package net.engining.pcx.cc.batch.cc6000;

import java.util.ArrayList;
import java.util.List;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;

/**
 * 账户主表(cact_account)
 * 子账户表(cact_sub_acct)
 * 当日入账交易表(cact_TXN_POST)-清算交易表
 */
public class Cc6000IAccountInfo{

	/**
	 * 账户信息
	 */
	private CactAccount cactAccount;

	/**
	 * 子账户列表
	 */
	private List<CactSubAcct> cactSubAccts = new ArrayList<CactSubAcct>();
	
	/**
	 * 最小还款额历史
	 */
	private List<CactAgeDue> cactAgeDues = new ArrayList<CactAgeDue>();
	
	/**
	 * 账户对应的当日入账交易List
	 */
	private List<CactTxnPost> cactTxnPosts = new ArrayList<CactTxnPost>();

	public CactAccount getCactAccount() {
		return cactAccount;
	}

	public void setCactAccount(CactAccount cactAccount) {
		this.cactAccount = cactAccount;
	}

	public List<CactSubAcct> getCactSubAccts() {
		return cactSubAccts;
	}

	public void setCactSubAccts(List<CactSubAcct> cactSubAccts) {
		this.cactSubAccts = cactSubAccts;
	}

	public List<CactAgeDue> getCactAgeDues() {
		return cactAgeDues;
	}

	public void setCactAgeDues(List<CactAgeDue> cactAgeDues) {
		this.cactAgeDues = cactAgeDues;
	}

	public List<CactTxnPost> getCactTxnPosts() {
		return cactTxnPosts;
	}

	public void setCactTxnPosts(List<CactTxnPost> cactTxnPosts) {
		this.cactTxnPosts = cactTxnPosts;
	}
}