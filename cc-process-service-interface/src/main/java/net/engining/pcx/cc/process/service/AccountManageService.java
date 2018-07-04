package net.engining.pcx.cc.process.service;

import net.engining.pcx.cc.process.model.AccountInfo;

/**
 * 账号管理服务
 * @author Ronny
 *
 */
public interface AccountManageService {	
	/**
	 * 查询accoutNo是否存在
	 * @param custId 客户号
	 * @return accoutNo 存在就返回accountNO,否则返回空。
	 */
	public Integer queryAccountNo(String custId);
	
	/**
	 * 产品建账接口
	 * @param custId 客户号
	 * @return accoutNo
	 */
	public int createAccountNo(String custId);
	
	/**
	 * 产品建账接口
	 * @param acctInfo ，必填字段acctNo, custId和 paramId,businessDate在交易建账的时候需要传入
	 * @return accountSeq
	 */
	public int createAccount(AccountInfo acctInfo);
}
