package net.engining.pcx.cc.batch.cc1900;

import java.io.Serializable;
import java.util.Date;

public class CactStmtKey implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer acctSeq;

    private String currCd;

    private Date stmtDate;

    public CactStmtKey() {
        
    }

    public CactStmtKey(Integer acctSeq, String currCd, Date stmtDate) {
        this.acctSeq = acctSeq;
        this.currCd = currCd;
        this.stmtDate = stmtDate;
    }

	public Integer getAcctSeq() {
		return acctSeq;
	}

	public void setAcctSeq(Integer acctSeq) {
		this.acctSeq = acctSeq;
	}

	public String getCurrCd() {
		return currCd;
	}

	public void setCurrCd(String currCd) {
		this.currCd = currCd;
	}

	public Date getStmtDate() {
		return stmtDate;
	}

	public void setStmtDate(Date stmtDate) {
		this.stmtDate = stmtDate;
	}
    
}
