package net.engining.pcx.cc.file.model;

import java.math.BigDecimal;
import java.util.Date;

import net.engining.gm.infrastructure.enums.StmtMediaType;
import net.engining.pcx.cc.infrastructure.shared.enums.DualBillingInd;
import net.engining.pcx.cc.infrastructure.shared.enums.GenderDef;
import net.engining.pg.support.cstruct.CChar;

/**
 * 实体账单当期汇总信息接口文件正文(STMT_INTERFACE)，参考《D-C01-CPS01-BRD-贷记卡核心系统账务模块数据格式-V006.xlsx》
 * 
 * @author heyu.wang
 * 
 */
public class StmtInterfaceItem {
	
	@CChar( value = 12, order = 100 )
    public String org;

    @CChar( value = 8, order = 200 )
    public Integer acctNo;

    @CChar( value = 8, datePattern = "yyyyMMdd", order = 300 )
    public Date stmtDate;

    @CChar( value = 80, order = 400 )
    public String name;

    @CChar( value = 1, order = 500 )
    public GenderDef gender;

    @CChar( value = 1, order = 600 )
    public StmtMediaType stmtMediaType;

    @CChar( value = 19, order = 700 )
    public String defaltLogicalCardNo;

    @CChar( value = 8, datePattern = "yyyyMMdd", order = 800 )
    public Date pmtDueDate;

    @CChar( value = 3, order = 900 )
    public String currCd;

    @CChar( value = 13, precision = 0, order = 1000 )
    public BigDecimal creditLimit;

    @CChar( value = 13, precision = 0, order = 1100 )
    public BigDecimal tempLimit;

    @CChar( value = 8, datePattern = "yyyyMMdd", order = 1200 )
    public Date tempLimitBeginDate;

    @CChar( value = 8, datePattern = "yyyyMMdd", order = 1300 )
    public Date tempLimitEndDate;

    @CChar( value = 15, precision = 2, order = 1400 )
    public BigDecimal stmtBegBal;

    @CChar( value = 15, precision = 2, order = 1500 )
    public BigDecimal ctdAmtDb;

    @CChar( value = 15, precision = 2, order = 1600 )
    public BigDecimal ctdPaymentAmt;

    @CChar( value = 15, precision = 2, order = 1700 )
    public BigDecimal qualGraceBal;

    @CChar( value = 15, precision = 2, order = 1800 )
    public BigDecimal stmtCurrBal;

    @CChar( value = 15, precision = 2, order = 1900 )
    public BigDecimal totDueAmt;

    @CChar( value = 13, precision = 0, order = 2000 )
    public BigDecimal pointBeginBal;

    @CChar( value = 13, precision = 0, order = 2100 )
    public BigDecimal ctdEarnedPoints;

    @CChar( value = 13, precision = 0, order = 2200 )
    public BigDecimal ctdAdjPoints;

    @CChar( value = 13, precision = 0, order = 2300 )
    public BigDecimal ctdDisbPoints;

    @CChar( value = 13, precision = 0, order = 2400 )
    public BigDecimal pointBal;

    @CChar( value = 1, order = 2500 )
    public String ageCd;

    @CChar( value = 20, order = 2600 )
    public String mobileNo;

    @CChar( value = 80, order = 2700 )
    public String email;

    @CChar( value = 1, order = 2800 )
    public DualBillingInd dualBillingFlag;
}
