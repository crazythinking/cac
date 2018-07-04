package net.engining.pcx.cc.param.model.enums;

import net.engining.pg.support.meta.EnumInfo;

/**
 * 授权验证行动
 * @author Ronny
 *
 */
@EnumInfo({
	"Must|必有必验",
	"Exist|有则验" 
	 
})	
public enum AuthVerifyAction {
	 
	/**
	 * 必有必验
	 */
	Must, 
	/**
	 * 有则验
	 */
	Exist 
}
