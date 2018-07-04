package net.engining.pcx.cc.process.model;

import java.io.Serializable;

import net.engining.pcx.cc.infrastructure.shared.model.AuthUnmatch;
import net.engining.pcx.cc.param.model.enums.UnmatchStatus;

public class AuthorizeInfo implements Serializable{

	private static final long serialVersionUID = -528679559567905927L;

	private AuthUnmatch authUnmatch;
	
	private UnmatchStatus unmatchStatus;
	
	public AuthUnmatch getAuthUnmatch() {
		return authUnmatch;
	}

	public void setAuthUnmatch(AuthUnmatch authUnmatch) {
		this.authUnmatch = authUnmatch;
	}

	public UnmatchStatus getUnmatchStatus() {
		return unmatchStatus;
	}

	public void setUnmatchStatus(UnmatchStatus unmatchStatus) {
		this.unmatchStatus = unmatchStatus;
	}
}
