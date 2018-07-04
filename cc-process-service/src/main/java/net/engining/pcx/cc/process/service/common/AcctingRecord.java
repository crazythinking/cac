package net.engining.pcx.cc.process.service.common;

import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.param.model.enums.DbCrInd;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;

public class AcctingRecord {
	
	/**
	 * 科目
	 */
	private Subject subject;
	
	/**
	 * 借贷记方向
	 */
	private DbCrInd dbCrInd;
	
	/**
	 * 红蓝字方向
	 */
	private RedBlueInd redBlueInd;

	public AcctingRecord(Subject subject, DbCrInd dbCrInd, RedBlueInd redBlueInd) {
		super();
		this.subject = subject;
		this.dbCrInd = dbCrInd;
		this.redBlueInd = redBlueInd;
	}

	public DbCrInd getDbCrInd() {
		return dbCrInd;
	}

	public void setDbCrInd(DbCrInd dbCrInd) {
		this.dbCrInd = dbCrInd;
	}

	public RedBlueInd getRedBlueInd() {
		return redBlueInd;
	}

	public void setRedBlueInd(RedBlueInd redBlueInd) {
		this.redBlueInd = redBlueInd;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}
}
