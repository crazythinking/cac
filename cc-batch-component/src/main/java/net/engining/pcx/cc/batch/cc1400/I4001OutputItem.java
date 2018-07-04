package net.engining.pcx.cc.batch.cc1400;

import net.engining.pcx.cc.file.model.AcctCloseRptItem;
import net.engining.pcx.cc.file.model.CancelRptItem;

/**
 * 销卡销户及关闭账户报表
 * 
 * @author yinxia
 *
 */
public class I4001OutputItem {

	/**
	 * 销卡销户送报表接口
	 */
	private CancelRptItem cancelRptItem;
	
	/**
	 * 关闭账户送报表接口 
	 */
	private AcctCloseRptItem acctCloseRptItem;

	
	public CancelRptItem getCancelRptItem() {
		return cancelRptItem;
	}

	public AcctCloseRptItem getAcctCloseRptItem() {
		return acctCloseRptItem;
	}

	public void setCancelRptItem(CancelRptItem cancelRptItem) {
		this.cancelRptItem = cancelRptItem;
	}

	public void setAcctCloseRptItem(AcctCloseRptItem acctCloseRptItem) {
		this.acctCloseRptItem = acctCloseRptItem;
	}
	
}
