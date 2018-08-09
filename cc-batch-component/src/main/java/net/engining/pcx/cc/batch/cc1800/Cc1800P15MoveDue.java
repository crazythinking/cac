package net.engining.pcx.cc.batch.cc1800;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.BusinessType;

/**
 * 最小还款额账单日移位
 * @author Ronny
 *
 */
@Service
@StepScope
public class Cc1800P15MoveDue implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		for (List<Cc1800IAccountInfo> cc1800IAccountInfos : item.getAccountList().values()) {
			for (Cc1800IAccountInfo cc1800IAccountInfo : cc1800IAccountInfos){
				moveDue(cc1800IAccountInfo);
			}
		}
		return item;
	}

	private Cc1800IAccountInfo moveDue(Cc1800IAccountInfo item) throws Exception {
		logger.debug("最小还款额账单日移位处理Process started! AccountNo:[" + item.getCactAccount().getAcctNo() + "] BusinessType:[" + item.getCactAccount().getBusinessType()
				+ "] ");

		if (item.getCactAccount().getBusinessType() != BusinessType.CC
				&& item.getCactAccount().getBusinessType() != BusinessType.BL) {
			return item;
		}
		
		// 判断批量日是否等于账单日
//		if ( batchDate.compareTo( item.getCactAccount().getInterestDate() ) == 0 )
//			cc1800UComputeDueAndAgeCode.moveDueAtStmtDay(item.getCactAccount());
		
		return item;
	}

}
