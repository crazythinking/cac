package net.engining.pcx.cc.process.service.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.BlockCodeControl;
import net.engining.pcx.cc.param.model.enums.PaymentCalcMethod;
import net.engining.pcx.cc.param.model.enums.PostAvailiableInd;

@Service
public class BlockCodeUtils {

	private static final String AGE_CD_LIST = "123456789";
	
	public PostAvailiableInd getFirstByPriority(String blockcodes, Account account)
	{
		//	判断锁定码列表是否为空
		if (blockcodes == null){
			return null;
		}
		
		//	校验锁定码
		List<BlockCodeControl> list = validate(blockcodes, account);
		
		//	锁定码列表
		//List<BlockCodeControl> list = new ArrayList<BlockCodeControl>(account.blockcode.values());
		
		//	判断列表是否为空
		if (list.isEmpty() || list.size() == 0)
			//	列表为空，返回null
			return null;
		else
		{
			List<PostAvailiableInd> availiableIndList = new ArrayList<PostAvailiableInd>();
			for (BlockCodeControl bc : list) {
				availiableIndList.add(bc.postInd);
			}
			
			//	有锁定码，排序，返回优先级最高的blockCode
			Collections.sort(availiableIndList);
			return availiableIndList.get(0);
		}
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除交易费标志
	 * 任意一个锁定码对应的是否免除交易费标志为true,则返回true
	 * 所有锁定码对应的免除交易费标志为false，返回false
	 * @param blockCodes
	 * @return false-不减免交易费 true-减免交易费
	 */
	public Boolean getMergedTxnFeeWaiveInd(String blockcodes, Account account)
	{
		Boolean txnFeeWaiveInd = false;
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				txnFeeWaiveInd |= code.txnFeeWaiveInd;
			}
		}
		/*for (BlockCodeControl code : account.blockcode.values())
		{
			txnFeeWaiveInd |= code.txnFeeWaiveInd;
		}*/
		
		return txnFeeWaiveInd;
	}
	
	/**
	 * 验证锁定码是否在参数中已设置
	 * @param blockcodes
	 */
	private List<BlockCodeControl> validate(String blockcodes, Account account)
	{	
		return validate(blockcodes, account.blockcode);
	}
	
	/**
	 * 验证锁定码是否在参数中已设置
	 * @param blockcodes
	 */
	private List<BlockCodeControl> validate(String blockCodes, Map<String, BlockCodeControl> availabledBlockCodes) {
		List<BlockCodeControl> list = new ArrayList<BlockCodeControl>();
		if (blockCodes != null)
		{
			for (Character c : blockCodes.toCharArray())
			{
				if (!availabledBlockCodes.containsKey(c.toString()))
				{
					throw new IllegalArgumentException("锁定码:[" + c.toString() + "] 在参数配置中不存在");
				}else{
					list.add(availabledBlockCodes.get(c.toString()));
				}
			}
		}
		
		return list;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除超限费标志
	 * 任意一个锁定码对应的是否免除超限费标志为true,则返回true
	 * 所有锁定码对应的是否免除超限费标志为false，返回false
	 * @param blockCodes
	 * @return false-收取超限费 true-免受超限费
	 */
	public Boolean getMergedOvrlmtFeeWaiveInd(String blockcodes, Account account) {
		
		Boolean ovrlmtFeeWaiveInd = false;
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				ovrlmtFeeWaiveInd |= code.ovrlmtFeeWaiveInd;
			}
		}
		/*for (BlockCodeControl code : account.blockcode.values())
		{
			ovrlmtFeeWaiveInd |= code.ovrlmtFeeWaiveInd;
		}*/
		
		return ovrlmtFeeWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否进行日常利息累积标志
	 * 任意一个锁定码对应的否进行日常利息累积标志为false,则返回false
	 * 所有锁定码对应的日常利息累积标志为true，返回true
	 * @param blockCodes
	 * @return false-不累积利息 true-累积利息
	 */
	public Boolean getMergedIntAccuralInd(String blockcodes, Account account)
	{
		Boolean intAccuralInd = true;
		
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				intAccuralInd &= code.intAccuralInd;
			}
		}
		
		return intAccuralInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除滞纳金标志
	 * 任意一个锁定码对应的是否免除滞纳金标志为true,则返回true
	 * 所有锁定码对应的是否免除滞纳金标志为false，返回false
	 * @param blockCodes
	 * @return false-收取滞纳金 true-免收滞纳金
	 */
	public Boolean getMergedLateFeeWaiveInd(String blockcodes, Account account)
	{
		Boolean lateFeeWaiveInd = false;
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				lateFeeWaiveInd |= code.lateFeeWaiveInd;
			}
		}
		/*for (BlockCodeControl code : account.blockcode.values())
		{
			lateFeeWaiveInd |= code.lateFeeWaiveInd;
		}*/
		
		return lateFeeWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否利息减免标志
	 * 任意一个锁定码对应的是否计息标志为true,则返回true
	 * 所有锁定码对应的是否计息标志为false，返回false
	 * @param blockCodes
	 * @return true-减免利息，不入账 false-不减免利息，需入账
	 */
	public Boolean getMergedIntWaiveInd(String blockcodes, Account account)
	{
		Boolean intWaiveInd = false;
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				intWaiveInd |= code.intWaiveInd;
			}
		}
		/*for (BlockCodeControl code : (account.blockcode.values()))
		{
			intWaiveInd |= code.intWaiveInd;
		}*/
		
		return intWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取入催标志
	 * 任意一个锁定码对应的入催标志为true,则返回true
	 * 所有锁定码对应的入催标志为false，返回false
	 * @param blockCodes
	 * @return false-不入催 true-入催
	 */
	public Boolean getMergedCollectionInd(String blockcodes, Account account)
	{
		Boolean collectionInd = false;
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				collectionInd |= code.collectionInd;
			}
		}
		/*for (BlockCodeControl code : account.blockcode.values())
		{
			collectionInd |= code.collectionInd;
		}*/
		
		return collectionInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取还款处理方式
	 * 任意一个锁定码对应的还款处理方式标志为B时，返回B,否则返回N
	 * @param blockCodes
	 * @return
	 */
	public PaymentCalcMethod getMergedPaymentInd(String blockcodes, Account account)
	{
		PaymentCalcMethod paymentInd = PaymentCalcMethod.N;
		//	TODO 判定S锁定码
		if (blockcodes != null) {
			for (char c : blockcodes.toCharArray())
			{
				BlockCodeControl code = account.blockcode.get(String.valueOf(c));
				if (code.paymentInd == PaymentCalcMethod.B)
					paymentInd = PaymentCalcMethod.B;
			}
		}
		/*for (BlockCodeControl code : account.blockcode.values())
		{
			if (code.paymentInd == PaymentCalcMethod.B)
				paymentInd = PaymentCalcMethod.B;
		}*/
		
		return paymentInd;
	}

/**
 * 在现有的锁定码列表中增加一个锁定码
 * 对于账龄锁定码，增加一个账龄锁定码会移除其它账龄锁定码
 * @param blockcodes 原锁定码列表
 * @param newCode 增加的锁定码
 * @return
 */
public String addBlockCode(String blockcodes, String newCode, Account account)
{
	return addBlockCode(blockcodes, newCode, account.blockcode);
}

/**
 * 在现有的锁定码列表中增加一个锁定码
 * 对于账龄锁定码，增加一个账龄锁定码会移除其它账龄锁定码
 * @param blockcodes 原锁定码列表
 * @param newCode 增加的锁定码
 * @return
 */
public String addBlockCode(String blockcodes, String newCode, Map<String, BlockCodeControl> availabledBlockCodes)
{
	if (newCode == null)
		return blockcodes;
	
	//	校验锁定码
	validate(blockcodes, availabledBlockCodes);

	validate(newCode, availabledBlockCodes);

	if (blockcodes == null)
		blockcodes = "";
	
	//	判断锁定码列表是否包含新锁定码
	if (StringUtils.containsAny(blockcodes, newCode))
		//	已包含，直接返回
		return blockcodes;
	else
	{
		//	判断锁定码是否是账龄新锁定码
		if (StringUtils.containsAny(AGE_CD_LIST, newCode))
		{
			//	新锁定码是账龄锁定码
			
			//	移除旧锁定码
			for (int i = 0; i< AGE_CD_LIST.length(); i ++)
			{
				blockcodes = StringUtils.remove(blockcodes, AGE_CD_LIST.charAt(i));
			}
			//	添加新锁定码
			return blockcodes.concat(newCode);
		}
		else
		{
			//	新锁定码不是账龄锁定码
			return blockcodes.concat(newCode);
		}
	}
			
}

/**
 * 移除一个锁定码
 * @param blockcodes
 * @param removeCode
 * @return
 */
public String removeBlockCode(String blockcodes, String removeCode, Account account)
{
	return removeBlockCode(blockcodes, removeCode, account.blockcode);
}

/**
 * 移除一个锁定码
 * @param blockcodes
 * @param removeCode
 * @return
 */
public String removeBlockCode(String blockcodes, String removeCode, Map<String, BlockCodeControl> availabledBlockCodes)
{
	//	校验锁定码
	validate(blockcodes, availabledBlockCodes);

	validate(removeCode, availabledBlockCodes);
	if (blockcodes == null){
		return null;
	}
	if (removeCode == null){
		return blockcodes;
	}
	String bl = blockcodes;
	for (char c : removeCode.toCharArray())
	{
		bl = StringUtils.remove(bl, c);
	}
	return bl;
}

/**
 * 根据帐户上的锁定码列表，获取是否输出账单标志
 * 任意一个锁定码对应的是否输出账单标志为false,则返回false
 * 所有锁定码对应的是否输出账单标志为true，返回true
 * @param blockCodes
 * @return false-不出账单 true-出账单
 */
public Boolean getMergedStmtInd(String blockcodes, Account account)
{
	Boolean stmtInd = true;
	if (blockcodes != null) {
		for (char c : blockcodes.toCharArray())
		{
			BlockCodeControl code = account.blockcode.get(String.valueOf(c));
			stmtInd &= code.stmtInd;
		}
	}
	/*for (BlockCodeControl code : account.blockcode.values())
	{
		stmtInd &= code.stmtInd;
	}*/
	
	return stmtInd;
}

/**
 * 比较两个blockcode是否相等
 * @param blockcode1
 * @param blockcode2
 * @return
 */
public Boolean isEquals(String blockcode1, String blockcode2){
	// 两个输入值都为null时返回true
	if (blockcode1 == null && blockcode2 == null)
		return true;
	// 两个输入值都不为null时进行比较
	else if (blockcode1 != null && blockcode2 != null ){
		List<String> list1 = new ArrayList<String>();
		for (char c : blockcode1.toCharArray()){
			list1.add(String.valueOf(c));
		}
		Collections.sort(list1);
		
		List<String> list2 = new ArrayList<String>();
		for (char c : blockcode2.toCharArray()){
			list2.add(String.valueOf(c));
		}
		Collections.sort(list2);
		return list1.equals(list2); 
	}
	// 一个值为null 一个值不为null时返回false;
	return false;
}

/**
 * 交易允许返回False
 * @param txnCode
 * @param blockcodes
 * @param paramId
 * @return false-交易允许 true-交易拒绝
 */
public Boolean getTransControl(String txnCode, String blockcodes, Account acct){
	if(blockcodes != null){
		char[] blockcodeChar = blockcodes.toCharArray();
		for(char blockcode : blockcodeChar){
			BlockCodeControl bc = acct.blockcode.get(String.valueOf(blockcode));
			if(bc != null && bc.transList !=null && bc.transList.contains(txnCode)){
				return Boolean.TRUE;
			}
		}	
	}
	
	return Boolean.FALSE;
}


/**
 * 根据帐户上的锁定码列表，获取是否允许账务核销
 * 任意一个锁定码对应的是否允许账务核销标志为true,则返回true
 * 所有锁定码对应的是否允许账务核销标志为false，返回false
 * @param blockCodes
 * @return false-不减免交易费 true-减免交易费
 */
public Boolean isWriteOff(String blockcodes, Account account){
	Boolean canWriteOff = false;
	if (blockcodes != null) {
		for (char c : blockcodes.toCharArray())
		{
			BlockCodeControl code = account.blockcode.get(String.valueOf(c));
			canWriteOff |= code.canWriteOff;
		}
	}
	
	return canWriteOff;
}
}