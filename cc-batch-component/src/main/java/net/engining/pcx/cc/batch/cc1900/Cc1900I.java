package net.engining.pcx.cc.batch.cc1900;

import java.util.ArrayList;
import java.util.List;

import net.engining.pcx.cc.infrastructure.shared.model.CactStmtHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactTxnUnstmt;


public class Cc1900I {
	private CactStmtHst stmtHst;
	
	private List<CactTxnUnstmt> txnUnstmts = new ArrayList<CactTxnUnstmt>();

	public CactStmtHst getStmtHst() {
		return stmtHst;
	}

	public void setStmtHst(CactStmtHst stmtHst) {
		this.stmtHst = stmtHst;
	}

	public List<CactTxnUnstmt> getTxnUnstmts() {
		return txnUnstmts;
	}

	public void setTxnUnstmts(List<CactTxnUnstmt> txnUnstmts) {
		this.txnUnstmts = txnUnstmts;
	}
	
}
