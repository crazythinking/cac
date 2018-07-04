package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 资料变动类-M开头
 * 联机动账类-T开头
 * 额度管理类-L开头
 * 分期类-P开头
 * 卡片管理类-C开头
 * 征审-S开头
 * 其它类-O开头
 * 
 * @author licj
 */
@EnumInfo({
	"M01|预留电话变更提醒",
	"M02|预留地址变更提醒",
	"M03|预留问题答案变更提醒",
	"M04|支付密码错误超限提醒",
	"M05|支付密码成功修改提醒",
	"M06|查询密码重置提醒",
	"M07|凭密标志变更提醒",
	"M08|支付密码解锁提醒",
	"M09|卡片冻结解除提醒",
	"M10|重打密码函提醒",
	"M11|卡片寄送地址变更提醒",
	"M12|账单寄送地址变更提醒",
	"M13|预留联系人信息变更提醒",
	"M14|账单介质类型变更提醒",
	"M15|取现额度设置提醒",
	"M16|分期额度设置提醒",
	"M17|激活成功",
	"M18|查询密码锁定重置提醒",
	"M19|Email变更提醒",
	"T01|联机正向交易通知",
	"T02|商户分期付款（暂不支持）",
	"T03|联机撤销交易通知",
	"T04|联机冲正交易通知",
	"T05|联机撤销的冲正交易通知",
	"L01|临时额度调整成功",
	"L02|永久额度调整成功",
	"L03|额度调整拒绝",
	"L04|卡片限额调整成功",
	"P01|分期付款申请成功",
	"P02|分期付款展期成功",
	"P03|分期付款取消",
	"P04|分期付款中止",
	"O01|账单提醒",
	"O02|绑定约定还款",
	"O03|拖欠催款",
	"O04|到期换卡",
	"O06|约定还款失败",
	"O07|约定还款成功",
	"O09|建客建账建卡成功短信",
	"W05|审批补件短信"
})
public enum MessageCategory
{
	/**
	 * 预留电话变更提醒
	 */
	M01("phoneType|电话号码类型", "oldPhone|旧电话号码", "newPhone|新电话号码"),
	
	/**
	 * 预留地址变更提醒
	 */
	M02("addressType|地址类型", "newAddress|新地址"),
	
	/**
	 * 预留问题答案变更提醒
	 */
	M03,
	
	/**
	 * 支付密码错误超限提醒
	 */
	M04,
	
	/**
	 * 支付密码成功修改提醒
	 */
	M05,
	
	/**
	 * 查询密码重置提醒 
	 */
	M06,
	
	/**
	 * 凭密标志变更提醒
	 */
	M07,
	
	/**
	 * 支付密码解锁提醒
	 */
	M08,
	
	/**
	 * 卡片冻结解除提醒
	 */
	M09,
	
	/**
	 * 重打密码函提醒
	 */
	M10,
	
	/**
	 * 卡片寄送地址变更提醒
	 */
	M11("oldAddressType|原地址类型", "newAddressType|新地址类型"),
	
	/**
	 * 账单寄送地址变更提醒
	 */
	M12("oldAddressType|原地址类型", "newAddressType|新地址类型"),
	
	/**
	 * 预留联系人信息变更提醒
	 */
	M13("contactName|联系人姓名", "relationship|与联系人关系"),
	
	/**
	 * 账单介质类型变更提醒
	 */
	M14("oldMediaType|原账单介质类型", "newMediaType|新账单介质类型"),
	
	/**
	 * 取现额度设置提醒
	 */
	M15("cashLimitRt|取现额度比率", "currencyCd|货币代码"),
	
	/**
	 * 分期额度设置提醒
	 */
	M16("loanLimitRt|分期额度比率", "currencyCd|货币代码"),
	
	/**
	 * 激活成功
	 */
	M17("posPinVerifyInd|消费凭密标识"),
	
	/**
	 * 查询密码锁定重置提醒
	 */
	M18,
	/**
	 * Email变更提醒
	 */
	M19("oldEmail|原Email地址", "newEmail|新Email地址"),
	
	/**
	 * 账单日变更提醒
	 */
	M20("oldBillingCycle|原账单日", "newBillingCycle|新账单日"),
	
	/**
	 * 联机正向交易通知
	 */
	T01("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	/**
	 * 商户分期付款（暂不支持）
	 */
	T02,
	
	/**
	 * 联机撤销交易通知
	 */
	T03("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	/**
	 * 联机冲正交易通知
	 */
	T04("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	 /**
     * 联机撤销的冲正交易通知
     */
    T05("currencyCd|货币代码", "transType|交易类别", "amount|交易金额", "otb|可用余额", "otbCash|可用取现余额"),
	
	/**
	 * 临时额度调整成功
	 */
	L01("currencyCd|货币代码", "creditLimit|调整后信用额度", "expireDate|临额到期日"),
	
	/**
	 * 永久额度调整成功
	 */
	L02("currencyCd|货币代码", "creditLimit|调整后信用额度"),
	
	/**
	 * 额度调整拒绝
	 */
	L03("currencyCd|货币代码", "creditLimit|当前信用额度"),
	
	/**
	 * 卡片限额调整成功
	 */
	L04("localCycleLimit|本币限额", "dualCycleLimit|外币限额"),
	
	/**
	 * 分期付款申请成功
	 */
	P01("loanType|分期类型", "amt|分期金额", "term|分期期数", "loanFee|总手续费", "nextPayment|下期还款金额", "loanFixedFee1|每期手续费"),
	
	/**
	 * 分期付款展期成功
	 */
	P02("loanType|分期类型", "amt|分期金额", "term|分期期数", "loanFee|总手续费", "nextPayment|下期还款金额", "loanFixedFee1|每期手续费"),
	
	/**
	 * 分期付款取消
	 */
	P03("loanType|分期类型", "amt|分期金额", "loanFee|手续费"),
	
	/**
	 * 分期付款中止
	 */
	P04("loanType|分期类型", "amt|分期金额", "loanFee|手续费"),
	
	/**
	 * 账单提醒
	 */
	O01("currencyCd|货币代码", "stmtDate|账单日期", "graceBalance|全额应还款额", "due|最小还款额", "paymentDate|到期还款日"),
	
	/**
	 * 绑定约定还款
	 */
	O02("ddInd|约定还款类型", "nextStmtDate|下次账单日"),
	
	/**
	 * 拖欠催款
	 */
	O03("currencyCd|货币代码", "stmtDate|账单日期", "graceBalance|全额应还款额", "due|最小还款额", "paymentDate|到期还款日"),
	
	/**
	 * 到期换卡
	 */
	O04("expiryDate|新卡有效期", "bsFlag|主附卡标识", "cardFetchType|领卡方式"),
	
	/**
	 * 约定还款失败
	 */
	O06("ddReturnCode|还款失败原因"),
	
	/**
	 * 约定还款成功
	 */
	O07("txnAmt|约定还款金额", "txnDate|约定还款日期"),
	
	/**
	 * 建客建账建卡成功
	 */
	O09("creditLimit|信用额度", "dualCreditLimit|外币信用额度"),
	
	/**
	 * 审批补件类型
	 */
	S05("applyPatchBoltType|补件类型");
	
	private String variables[];

	/**
	 * @param variables 以"|"分隔的一组字符串，写明该类型支持/需要提供的变量列表
	 */
	private MessageCategory(String ... variables){
		this.variables = variables;
	}

	public String[] getVariables() {
		return variables;
	}
}