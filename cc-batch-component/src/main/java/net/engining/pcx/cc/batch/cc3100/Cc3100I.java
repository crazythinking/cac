package net.engining.pcx.cc.batch.cc3100;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.Channel;
import net.engining.gm.infrastructure.enums.TxnType;
import net.engining.pcx.cc.infrastructure.shared.enums.ManualAuthFlagDef;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnStatusDef;
import net.engining.pg.support.cstruct.CChar;

/**
 * unmatchO
 * 
 * @author Bo
 * 
 */
public class Cc3100I {
	@CChar( value = 12, order = 100 )
	public String org;

	@CChar( value = 9, order = 200 )
	public Integer txnSqId;

	@CChar( value = 19, zeroPadding = true, order = 300 )
	public String cardGroupId;

	@CChar( value = 6, order = 400 )
	public String traceAuditNo;

	@CChar( value = 15, order = 500 )
	public BigDecimal txnAmt;

	@CChar( value = 3, order = 600 )
	public String txnCurrCd;

	@CChar( value = 6, order = 700 )
	public String authCode;

	@CChar( value = 40, order = 800 )
	public String acqNameAddr;

	@CChar( value = 15, precision = 2, order = 900 )
	public BigDecimal chbTxnAmt;

	@CChar( value = 3, order = 1000 )
	public String chbCurrCd;

	@CChar( value = 15, order = 1100 )
	public Channel channel;

	@CChar( value = 4, order = 1200 )
	public String mcc;

	@CChar( value = 9, order = 1300 )
	public String acqBranchId;

	@CChar( value = 11, order = 1400 )
	public String fwdInstId;

	@CChar( value = 10, order = 1500 )
	public String transmissionTimestamp;

	@CChar( value = 4, order = 1600 )
	public String settleDate;

	@CChar( value = 1, order = 1700 )
	public TxnType txnType;

	@CChar( value = 1, order = 1800 )
	public TxnStatusDef txnStatus;
	
	@CChar( value = 20, order = 1900 )
    private String txnCode;

	@CChar( value = 14, datePattern = "yyyyMMddHHmmss", order = 2000 )
	public Date logOlTime;

	@CChar( value = 8, datePattern = "yyyyMMdd", order = 2100 )
	public Date logBizDate;

	@CChar( value = 4, order = 2200 )
	public String mti;

	@CChar( value = 1, order = 2300 )
	public ManualAuthFlagDef manualAuthFlag;

	@CChar( value = 40, order = 2400 )
	public String operaId;

	@CChar( value = 6, order = 2500 )
	public String productCd;

	@CChar( value = 4, order = 2600 )
	public String finalReason;

	@CChar( value = 15, precision = 2, order = 2700 )
	public BigDecimal compAmt;
	
	@CChar( value = 40, order = 2800 )
	public String rejReason;

	@CChar( value = 2, order = 2900 )
	public String posConditionCd;

	@CChar( value = 2, order = 3000 )
	public String returnCd;

	@CChar( value = 15, order = 3100 )
	public String merchantCd;

	@CChar( value = 20, order = 3200 )
	public String operaTermId;

	@CChar( value = 15, order = 3300 )
	public BigDecimal transactionFee;
	
	@CChar ( value = 20, order = 3400 )
	public Integer acctNo;
	
	@CChar( value = 15, order = 3500 )
	public Integer custId;

	@CChar( value = 9, order = 3600 )
	public Integer jpaVersion;

	@CChar( value = 15, order = 3700 )
	public BigDecimal exchangeRate;
	
	@CChar ( value = 19, order = 3800 )
	public String cardNo;
	
}
