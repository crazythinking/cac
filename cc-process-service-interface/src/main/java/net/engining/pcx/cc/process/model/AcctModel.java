package net.engining.pcx.cc.process.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.infrastructure.shared.model.CactSubAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnPost;

public class AcctModel implements Serializable {

	private static final long serialVersionUID = 5102389584999977862L;

	private CactAccount cactAccount;
	
	private List<CactSubAcct> cactSubAccts = new ArrayList<CactSubAcct>();
	
	private List<CactAgeDue> cactAgeDues;
	
	private List<CactTxnPost> cactTxnPosts;

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

	public List<CactAgeDue> getCactAgeDues()
	{
		return cactAgeDues;
	}

	public void setCactAgeDues(List<CactAgeDue> cactAgeDues)
	{
		this.cactAgeDues = cactAgeDues;
	}

	public List<CactTxnPost> getCactTxnPosts()
	{
		return cactTxnPosts;
	}

	public void setCactTxnPosts(List<CactTxnPost> cactTxnPosts)
	{
		this.cactTxnPosts = cactTxnPosts;
	}
}
