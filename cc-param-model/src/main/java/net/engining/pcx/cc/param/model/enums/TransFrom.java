package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * @about 交易发起方式
 * @author guopy
 * @date 2012-8-12 下午11:30:37
 */
@EnumInfo({ "N|未知", "P|现场", "S|自助", "O|联机代理", "B|批量代理" })
/**
 * 交易发起方式
 */
public enum TransFrom {
	N, P, S, O, B
};