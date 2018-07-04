package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 授权决定
 */
@EnumInfo({
	"A|批准",
	"D|拒绝",
	"P|没收",
	"C|电话联系"
})

public enum AuthAction {
	/**
	 * 批准
	 */
	A("批准", "4"), 
	/**
	 * 拒绝
	 */
	D("拒绝", "3"),
	/**
	 * 没收
	 */
	P("没收", "1"),
	/**
	 * 照会(电话联系)
	 */
	C("电话联系", "2");
	
	private String  actionDesc;
	
	private String  actionFlag;
	
	private AuthAction(String actionDesc, String actionFlag)
	{
		this.actionDesc = actionDesc;
		this.actionFlag = actionFlag;
	}

	public String getActionDesc() {
		return actionDesc;
	}

	public String getActionFlag() {
		return actionFlag;
	}
}
