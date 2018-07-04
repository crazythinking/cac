package net.engining.pcx.cc.batch.cc1800;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


/**
 * 计算应还本金（按周期计息的贷款，非按日计息）
 *
 */
@Service
@Scope("step")
public class Cc1800P369CountRepayAmt4Cycle extends Cc1800P36AbstractCountRepay {

	protected Cc1800P369CountRepayAmt4Cycle()
	{
		super(false);
	}
}
