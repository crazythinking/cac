package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * @about 自助類型 非自助/无卡自助/有卡自助
 * @author guopy
 * @date 2012-8-10 上午11:28:54
 */
@EnumInfo({ "NoSelfService|非自助", "NoCardSelfService|无卡自助","ACardSelfService|有卡自助" })
/**
 * 自助類型
 */
public enum AutoType {
	NoSelfService, NoCardSelfService, ACardSelfService
}