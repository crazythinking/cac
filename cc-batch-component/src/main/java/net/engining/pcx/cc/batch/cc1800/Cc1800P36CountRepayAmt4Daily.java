package net.engining.pcx.cc.batch.cc1800;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Service;


/**
 * 计算应还本金（按日计息的贷款）
 *
 */
@Service
@StepScope
public class Cc1800P36CountRepayAmt4Daily extends Cc1800P36AbstractCountRepay {

	protected Cc1800P36CountRepayAmt4Daily()
	{
		super(true);
	}

}
