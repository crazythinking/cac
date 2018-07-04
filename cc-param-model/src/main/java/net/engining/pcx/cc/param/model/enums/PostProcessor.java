package net.engining.pcx.cc.param.model.enums;

import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({ "DN|借记正常(借贷方向：借)", "CN|贷记正常(借贷方向：贷)", "CT|贷记转出(借贷方向：贷)" })
public enum PostProcessor {
	/** 借记正常 */
	DN(TxnDirection.D),

	/** 贷记正常 */
	CN(TxnDirection.C),

	/** 贷记转出 */
	CT(TxnDirection.C);

	private TxnDirection txnDirection;

	private PostProcessor(TxnDirection txnDirection) {
		this.txnDirection = txnDirection;
	}

	public TxnDirection getTxnDirection() {
		return txnDirection;
	}
}
