package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 授权匹配状态
 * 
 * @author zhengpy & heyu.wang
 * 
 */
@EnumInfo({ "M|成功匹配", "U|未匹配", "E|过期授权" })
public enum UnmatchStatus {
	/**
	 * 成功匹配
	 */
	M,
	/**
	 * 未匹配
	 */
	U,
	/**
	 * 过期授权
	 */
	E
}