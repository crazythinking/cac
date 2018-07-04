package net.engining.pcx.cc.batch.cc1900;

import java.util.ArrayList;
import java.util.List;

import net.engining.pcx.cc.file.model.StmtInterfaceItem;
import net.engining.pcx.cc.file.model.StmtMsgInterfaceItem;
import net.engining.pcx.cc.file.model.StmttxnInterfaceItem;

/**
 * 账单功能输出项
 * 
 * @author heyu.wang
 * 
 */
public class StmtInfoItem {
	/**
	 * 实体帐单交易接口文件内容列表
	 */
	private List<StmttxnInterfaceItem> stmttxnInterfaceItems = new ArrayList<StmttxnInterfaceItem>();

	/**
	 * 实体账单汇总接口文件内容
	 */
	private List<StmtInterfaceItem> stmtInterfaceItems = new ArrayList<StmtInterfaceItem>();
	
	/**
	 * 账单提醒短信
	 */
	private List<StmtMsgInterfaceItem> stmtMsgInterfaceItems = new ArrayList<StmtMsgInterfaceItem>();

	public List<StmttxnInterfaceItem> getStmttxnInterfaceItems() {
		return stmttxnInterfaceItems;
	}

	public void setStmttxnInterfaceItems(List<StmttxnInterfaceItem> stmttxnInterfaceItems) {
		this.stmttxnInterfaceItems = stmttxnInterfaceItems;
	}

	public List<StmtInterfaceItem> getStmtInterfaceItems() {
		return stmtInterfaceItems;
	}

	public void setStmtInterfaceItems(List<StmtInterfaceItem> stmtInterfaceItems) {
		this.stmtInterfaceItems = stmtInterfaceItems;
	}

	public List<StmtMsgInterfaceItem> getStmtMsgInterfaceItems() {
		return stmtMsgInterfaceItems;
	}

	public void setStmtMsgInterfaceItems(List<StmtMsgInterfaceItem> stmtMsgInterfaceItems) {
		this.stmtMsgInterfaceItems = stmtMsgInterfaceItems;
	}

}
