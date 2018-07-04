package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 验证类型
 * @author Ronny
 *
 */
@EnumInfo({
	"CardNotMotoVerifyCvv2|无卡消费posmoto手工类强制cvv2",
	"CardNotElectronVerifyCvv2|无卡消费posmoto电子类强制cvv2",
	"IcArqcVerify|ic卡arqc验证",
	"ManualAuthVerifyCvv2|人工授权交易是否强制验证cvv2",
	"CashVerify|取现强制验密",
	"AtmVerify|ATM交易强制验密",
	"InstalmentExpenseVerify|分期消费强制验密",
	"CardNotExpenseElectronVerifyPassword|无卡消费电子类强制验密"
})
public enum VerifyEnum {
	/**
	 * 无卡消费posmoto手工类强制cvv2
	 */
	CardNotMotoVerifyCvv2,
	/**
	 * 无卡消费posmoto电子类强制cvv2
	 */
	CardNotElectronVerifyCvv2,
	/**
	 * ic卡arqc验证
	 */
	IcArqcVerify,
	/**
	 * 人工授权交易是否强制验证cvv2
	 */
	ManualAuthVerifyCvv2,
	
	CashVerify,
	AtmVerify,
	InstalmentExpenseVerify,
	CardNotExpenseElectronVerifyPassword
}