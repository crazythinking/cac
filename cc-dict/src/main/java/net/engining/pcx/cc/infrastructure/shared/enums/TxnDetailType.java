package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"A|账户表",
	"O|联机交易流水",
	"P|客户账入账交易流水",
	"I|内部账户批量入账交易流水",
	"L|内部账户联机入账交易流水",
	"C|总账交易流水",
	"J|内部账户调账交易流水",
	"G|会计科目调账交易流水",
	"R|会计分录流水",
	"S|数据迁移临时流水表",
	"T|当日辅助核算拆分表",
	"U|预留4",
	"V|预留5",
	"W|预留6",
	"X|预留7",
	"Y|预留8",
	"Z|预留9"
})

public enum TxnDetailType {
	/**
	 *	账户表CactAccount
	 */
	A("账户表"),
	/**
	 *	联机交易流水 
	 */
	O("联机交易流水"),
	/**
	 *	客户账入账交易流水CactTxnHst
	 */
	P("客户账入账交易流水"),
	/**
	 * 	内部账户批量入账交易流水CactInternalTxnPostHst(因为CactInternalTxnPost4Batch会清空，所以历史表里面会留存流水表中的序号)
	 */
	I("内部账户批量入账交易流水"),
	/**
	 * 	内部账户联机入账交易流水CactInternalTxnPostHst(因为CactInternalTxnPost4Online会清空，所以历史表里面会留存流水表中的序号)
	 */
	L("内部账户联机入账交易流水"),
	/**
	 * 	总账交易流水ApGltxnHst
	 */
	C("总账交易流水"),
	/**
	 * 	调整内部账户余额操作历史CactAdjIntrAcctOprHst
	 */
	J("内部账户调账交易流水"),
	/**
	 * 	会计科目调账交易流水CactAdjLedgerAcctOprHst
	 */
	G("会计科目调账交易流水"),
	
	/**
	 * 会计分录流水
	 */
	R("会计分录流水"),
	
	/**
	 * 数据迁移临时流水表
	 */
	S("数据迁移临时流水表"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	T("预留3"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	U("预留4"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	V("预留5"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	W("预留6"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	X("预留7"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	Y("预留8"),
	
	/**
	 * 预留给客户化项目的实施使用
	 */
	Z("预留9");

	private String description;

	private TxnDetailType(String description){
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
