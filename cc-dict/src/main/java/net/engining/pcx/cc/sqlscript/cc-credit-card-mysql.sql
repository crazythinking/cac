SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS AUTH_CARD_LIMIT_OVERRIDE;
DROP TABLE IF EXISTS AUTH_TXN_HIST;
DROP TABLE IF EXISTS AUTH_UNMATCH;
DROP TABLE IF EXISTS AUTH_CARD_LIMIT;
DROP TABLE IF EXISTS AUTH_MERCHANT_TXN_CTRL;
DROP TABLE IF EXISTS AUTH_TRAN_ADJ_LOG;
DROP TABLE IF EXISTS AUTH_TXNTYPE_LIMIT;
DROP TABLE IF EXISTS CACT_AMORTIZATION;
DROP TABLE IF EXISTS CACT_LOAN_REG;
DROP TABLE IF EXISTS CACT_CARD;
DROP TABLE IF EXISTS CACT_CARD_GROUP;
DROP TABLE IF EXISTS CACT_CSSFEE_REG;
DROP TABLE IF EXISTS CACT_LOAN;
DROP TABLE IF EXISTS CACT_OPER_LOG;
DROP TABLE IF EXISTS CACT_ORG_STST;
DROP TABLE IF EXISTS CACT_POINT_ADJ_LOG;
DROP TABLE IF EXISTS CACT_POINT_REG;
DROP TABLE IF EXISTS CACT_REPAY_FEE_INFO;
DROP TABLE IF EXISTS CACT_REPRINT_REG;
DROP TABLE IF EXISTS CACT_STMT_HST;
DROP TABLE IF EXISTS CACT_TXN_UNSTMT;




/* Create Tables */

-- 逻辑卡额度表-授权
CREATE TABLE AUTH_CARD_LIMIT
(
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	CUST_ID varchar(64) NOT NULL COMMENT '客户编号',
	ACCT_NO int NOT NULL COMMENT '账号',
	CURR_CD char(3) NOT NULL COMMENT '币种',
	-- 全9 - 无限额；新建卡若外部未送入，则赋值为全9
	CYCLE_LIMIT decimal(18,2) COMMENT '消费周期限额 : 全9 - 无限额；新建卡若外部未送入，则赋值为全9',
	-- 全9 - 无限额；新建卡若外部未送入，则赋值为全9
	CYCLE_CASH_LIMIT decimal(18,2) COMMENT '取现周期限额 : 全9 - 无限额；新建卡若外部未送入，则赋值为全9',
	-- 全9 - 无限额；新建卡若外部未送入，则赋值为全9
	CYCLE_NET_LIMIT decimal(18,2) COMMENT '网上消费周期限额 : 全9 - 无限额；新建卡若外部未送入，则赋值为全9',
	-- 缺省赋值为全9
	TXN_LIMIT decimal(18,2) COMMENT '单笔限额 : 缺省赋值为全9',
	-- 全9 - 无限额；新建卡若外部未送入，则赋值为全9
	TXN_CASH_LIMIT decimal(18,2) COMMENT '取现单笔限额 : 全9 - 无限额；新建卡若外部未送入，则赋值为全9',
	-- 全9 - 无限额；新建卡若外部未送入，则赋值为全9
	TXN_NET_LIMIT decimal(18,2) COMMENT '网上消费单笔限额 : 全9 - 无限额；新建卡若外部未送入，则赋值为全9',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CARD_GROUP_ID)
) COMMENT = '逻辑卡额度表-授权';


-- 逻辑卡限额覆盖表
CREATE TABLE AUTH_CARD_LIMIT_OVERRIDE
(
	-- !!!java.lang.Boolean!!!
	DAY_ATM_OVERRIDE_IND bit(1) NOT NULL COMMENT 'ATM限制覆盖标志 : !!!java.lang.Boolean!!!',
	-- ATM限制覆盖起始日期
	DAY_ATM_OVERRIDE_START date NOT NULL COMMENT 'ATM覆盖起始日期 : ATM限制覆盖起始日期',
	-- ATM覆盖终止日期
	DAY_ATM_OVERRIDE_END date NOT NULL COMMENT 'ATM覆盖终止日期 : ATM覆盖终止日期',
	-- 来自银监会监管需要
	DAY_ATM_NBR_LIMIT int NOT NULL COMMENT '单日ATM取现限笔 : 来自银监会监管需要',
	-- 来自银监会监管需要
	DAY_ATM_AMT_LIMIT decimal(18,2) NOT NULL COMMENT '单日ATM取现限额 : 来自银监会监管需要',
	-- !!!java.lang.Boolean!!!
	DAY_RETAIL_OVERRIDE_IND bit(1) NOT NULL COMMENT '消费限制覆盖标志 : !!!java.lang.Boolean!!!',
	DAY_RETAIL_OVERRIDE_START date NOT NULL COMMENT '消费覆盖起始日期',
	DAY_RETAIL_OVERRIDE_END date NOT NULL COMMENT '消费覆盖终止日期',
	-- 金电需要
	DAY_RETAIL_NBR_LIMIT  int NOT NULL COMMENT '单日消费限笔 : 金电需要',
	-- 金电需要
	DAY_RETAIL_AMT_LIMIT  decimal(18,2) NOT NULL COMMENT '单日消费限额 : 金电需要',
	-- !!!java.lang.Boolean!!!
	DAY_CASH_OVERRIDE_IND bit(1) NOT NULL COMMENT '取现限制覆盖标志 : !!!java.lang.Boolean!!!',
	DAY_CASH_OVERRIDE_START date NOT NULL COMMENT '取现覆盖起始日期',
	DAY_CASH_OVERRIDE_END date NOT NULL COMMENT '取现覆盖终止日期',
	-- 金电需要
	DAY_CASH_NBR_LIMIT  int NOT NULL COMMENT '单日取现限笔 : 金电需要',
	-- 金电需要
	DAY_CASH_AMT_LIMIT  decimal(18,2) NOT NULL COMMENT '单日取现限额 : 金电需要',
	-- !!!java.lang.Boolean!!!
	DAY_XFROUT_OVERRIDE_IND bit(1) NOT NULL COMMENT '转出限制覆盖标志 : !!!java.lang.Boolean!!!',
	DAY_XFROUT_OVERRIDE_START date NOT NULL COMMENT '转出覆盖起始日期',
	DAY_XFROUT_OVERRIDE_END date NOT NULL COMMENT '转出覆盖终止日期',
	-- 金电需要
	DAY_XFROUT_NBR_LIMIT  int NOT NULL COMMENT '单日转出限笔 : 金电需要',
	-- 金电需要
	DAY_XFROUT_AMT_LIMIT  decimal(18,2) NOT NULL COMMENT '单日转出限额 : 金电需要',
	-- !!!java.lang.Boolean!!!
	DAY_CUPXB_ATM_OVERRIDE_IND bit(1) NOT NULL COMMENT '单日银联境外ATM限制覆盖标志 : !!!java.lang.Boolean!!!',
	DAY_CUPXB_ATM_OVERRIDE_START date NOT NULL COMMENT '单日银联境外ATM限制覆盖起始日期',
	DAY_CUPXB_ATM_OVERRIDE_END date NOT NULL COMMENT '单日银联境外ATM覆盖终止日期',
	DAY_CUPXB_ATM_AMT_LIMIT decimal(18,2) NOT NULL COMMENT '单日银联境外ATM取现限额',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	PRIMARY KEY (CARD_GROUP_ID)
) COMMENT = '逻辑卡限额覆盖表';


-- 商户授权交易控制表
CREATE TABLE AUTH_MERCHANT_TXN_CTRL
(
	SEQ_ID int NOT NULL COMMENT '编号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	MERCHANT_ID varchar(15) NOT NULL COMMENT '商户ID',
	MERCHANT_NAME varchar(255) NOT NULL COMMENT '商户名称',
	COUNTRY_CD char(3) NOT NULL COMMENT '国家代码',
	ACQ_INSTITUTION varchar(30) NOT NULL COMMENT '收单机构',
	MERCHANT_SHORT_NAME varchar(255) NOT NULL COMMENT '商户简称',
	MERCHANT_CLASS char(2) NOT NULL COMMENT '商户等级',
	MCC char(4) NOT NULL COMMENT '商户类别代码',
	MERCHANT_POSTCODE char(6) NOT NULL COMMENT '商户邮编',
	MERCHANT_ADDR varchar(255) NOT NULL COMMENT '商户地址',
	MERCHANT_TEL varchar(19) NOT NULL COMMENT '商户电话',
	MERCHANT_CONTACT varchar(255) NOT NULL COMMENT '商户联系人',
	-- !!!java.lang.Boolean!!!
	ACTIVATE_IND bit(1) NOT NULL COMMENT '是否已激活 : !!!java.lang.Boolean!!!',
	-- !!!java.lang.Boolean!!!
	FORCE_MOTO_RETAIL_CVV2_IND bit(1) NOT NULL COMMENT '是否强制检查moto交易的CVV2 : !!!java.lang.Boolean!!!',
	-- !!!java.lang.Boolean!!!
	SUPPORT_MOTO_POS_IND bit(1) NOT NULL COMMENT '是否支持MOTO消费交易 : !!!java.lang.Boolean!!!',
	-- !!!java.lang.Boolean!!!
	SUPPORT_EMOTO_IND bit(1) NOT NULL COMMENT '是否支持电子类消费交易 : !!!java.lang.Boolean!!!',
	-- !!!java.lang.Boolean!!!
	SUPPORT_LOAN_IND bit(1) NOT NULL COMMENT '是否支持分期消费交易 : !!!java.lang.Boolean!!!',
	-- !!!java.lang.Boolean!!!
	SUPPORT_SPECLOAN bit(1) NOT NULL COMMENT '是否支持大额分期消费交易 : !!!java.lang.Boolean!!!',
	-- !!!java.lang.Boolean!!!
	SUPPORT_CALC_FEE_O bit(1) NOT NULL COMMENT '是否支持联机计算手续费 : !!!java.lang.Boolean!!!',
	PRIMARY KEY (SEQ_ID),
	UNIQUE (ORG, MERCHANT_ID)
) COMMENT = '商户授权交易控制表';


-- 授权交易调整日志表
CREATE TABLE AUTH_TRAN_ADJ_LOG
(
	-- 操作序列号
	OPER_SEQ int NOT NULL AUTO_INCREMENT COMMENT '操作序列号 : 操作序列号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	OPER_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	OPERA_ID varchar(40) NOT NULL COMMENT '操作员ID',
	ACCT_NO int NOT NULL COMMENT '账号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	POST_CODE char(8) NOT NULL COMMENT '入账交易码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	CURR_CD char(3) NOT NULL COMMENT '币种',
	REF_NBR varchar(23) NOT NULL COMMENT '交易参考号',
	REMARK varchar(40) NOT NULL COMMENT '备注',
	AUTH_CODE char(6) NOT NULL COMMENT '授权码',
	MCC char(4) NOT NULL COMMENT '商户类别代码',
	PROCESS_CODE varchar(6) NOT NULL COMMENT '交易处理码-B003',
	B007 varchar(10) NOT NULL COMMENT '交易传输时间-B007',
	B011 char(6) NOT NULL COMMENT '系统跟踪号-B011',
	-- 受理机构标识码
	B032 varchar(15) NOT NULL COMMENT '受理机构标识码-B032 : 受理机构标识码',
	-- 受理机构名称、地址
	B033 varchar(40) NOT NULL COMMENT '受理机构名称地址-B033 : 受理机构名称、地址',
	MTI char(4) NOT NULL COMMENT '交易类型标识',
	-- !!!java.lang.Boolean!!!
	VOID_IND bit(1) NOT NULL COMMENT '是否已撤销 : !!!java.lang.Boolean!!!',
	VOID_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '撤销日期时间',
	VOID_REASON varchar(40) NOT NULL COMMENT '撤销原因',
	VOID_OPERATOR varchar(8) NOT NULL COMMENT '撤销操作员',
	MERCHANT_CD varchar(15) NOT NULL COMMENT '受卡方(商户)标识码-B042',
	RETURN_CD varchar(2) NOT NULL COMMENT '返回码-B039',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (OPER_SEQ)
) COMMENT = '授权交易调整日志表';


-- 卡片特定业务类型状态表
CREATE TABLE AUTH_TXNTYPE_LIMIT
(
	SEQ_ID int NOT NULL COMMENT '编号',
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	-- ///
	-- A01|有卡自助
	-- A02|无卡自助
	-- A03|互联网消费
	-- A04|moto
	-- A09|代收
	SPEC_TXN_TYPE varchar(3) NOT NULL COMMENT '特定业务类型 : ///
A01|有卡自助
A02|无卡自助
A03|互联网消费
A04|moto
A09|代收',
	-- !!!java.lang.Boolean!!!
	TXN_SUPPORT_INDICATOR bit(1) NOT NULL COMMENT '交易支持指示 : !!!java.lang.Boolean!!!',
	ATTACHED_DEVICE_NBR varchar(40) NOT NULL COMMENT '绑定设备号码',
	-- 缺省赋值为全9
	TXN_LIMIT decimal(18,2) NOT NULL COMMENT '单笔限额 : 缺省赋值为全9',
	DAY_AMT_LIMIT decimal(18,2) NOT NULL COMMENT '单日交易限额',
	DAY_NBR_LIMIT int NOT NULL COMMENT '单日交易限笔',
	CYCLE_TXN_AMT_LIMIT decimal(18,2) NOT NULL COMMENT '周期交易限额',
	CYCLE_TXN_NBR_LIMIT int NOT NULL COMMENT '周期交易限笔',
	-- 上次联机更新业务日期
	LAST_UPDATE_BIZ_DATE date NOT NULL COMMENT '上次联机更新业务日期 : 上次联机更新业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ_ID),
	UNIQUE (CARD_GROUP_ID, SPEC_TXN_TYPE)
) COMMENT = '卡片特定业务类型状态表';


-- 授权交易历史
CREATE TABLE AUTH_TXN_HIST
(
	ORG varchar(12) NOT NULL COMMENT '机构号',
	SEQ_ID int NOT NULL AUTO_INCREMENT COMMENT '编号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	ACCT_NO int NOT NULL COMMENT '账号',
	CUST_ID varchar(64) NOT NULL COMMENT '客户编号',
	TRACE_AUDIT_NO varchar(6) NOT NULL COMMENT '系统跟踪号',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	TXN_CURR_CD char(3) NOT NULL COMMENT '交易币种代码',
	AUTH_CODE char(6) COMMENT '授权码',
	-- 受卡点（商户）名称，地址
	MERCHANT_NAME_ADDR varchar(40) NOT NULL COMMENT '商户名称地址 : 受卡点（商户）名称，地址',
	CHB_TXN_AMT decimal(18,2) NOT NULL COMMENT '持卡人账户币种金额',
	CHB_CURR_CD char(3) NOT NULL COMMENT '持卡人账户币种',
	-- ///
	-- @net.engining.gm.infrastructure.enums.Channel
	CHANNEL varchar(15) NOT NULL COMMENT '交易渠道 : ///
@net.engining.gm.infrastructure.enums.Channel',
	MCC char(4) NOT NULL COMMENT '商户类别代码',
	-- 受理分行代码
	ACQ_BRANCH_ID varchar(11) NOT NULL COMMENT '受理分行代码 : 受理分行代码',
	FWD_INST_ID varchar(11) NOT NULL COMMENT '转发机构号',
	-- MMddhhmmss
	TRANSMISSION_TIMESTAMP varchar(10) NOT NULL COMMENT '传送日期 : MMddhhmmss',
	-- 清算日期，以卡管系统为准
	SETTLE_DATE date NOT NULL COMMENT '清算日期 : 清算日期，以卡管系统为准',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnType
	TXN_TYPE varchar(20) NOT NULL COMMENT '交易类型 : ///
@net.engining.gm.infrastructure.enums.TxnType',
	-- ///
	-- N|正常
	-- R|被冲正
	-- V|被撤销
	-- A|已确认完成
	-- D|拒绝
	TXN_STATUS char(1) NOT NULL COMMENT '交易状态 : ///
N|正常
R|被冲正
V|被撤销
A|已确认完成
D|拒绝',
	TXN_CODE varchar(20) NOT NULL COMMENT '交易代码',
	LOG_OL_TIME timestamp DEFAULT NOW() NOT NULL COMMENT 'LOG联机时间',
	LOG_BIZ_DATE date NOT NULL COMMENT '联机业务日期',
	OLD_TXN_CODE varchar(20) COMMENT '原交易代码',
	OLD_FWD_INST_ID varchar(11) COMMENT '原始转发机构号',
	OLD_ACQ_INST_ID varchar(11) COMMENT '原始受理机构号',
	OLD_TRANS_DATE date COMMENT '原始交易日期',
	-- for pre_auth
	OLD_TRACE_NO int COMMENT '原始系统跟踪号 : for pre_auth',
	OLD_TXN_AMT decimal(18,2) COMMENT '原始交易币种金额',
	OLD_SEQ_ID int COMMENT '原交易LOG键值',
	OLD_CHB_TXN_AMT decimal(18,2) COMMENT '原始入账币种金额',
	OLD_BIZ_DATE date COMMENT '原交易的联机业务处理日期',
	-- ///
	-- A|普通
	-- N|否
	-- F|强制
	-- D|借记调整
	-- C|贷记调整
	MANUAL_AUTH_FLAG char(1) NOT NULL COMMENT '人工授权标志 : ///
A|普通
N|否
F|强制
D|借记调整
C|贷记调整',
	OPERA_ID varchar(40) COMMENT '操作员ID',
	-- ///
	-- @net.engining.gm.infrastructure.enums.Brand
	BRAND char(1) NOT NULL COMMENT '品牌 : ///
@net.engining.gm.infrastructure.enums.Brand',
	PRODUCT_CD char(6) NOT NULL COMMENT '产品代码',
	FINAL_REASON char(5) COMMENT '授权原因',
	-- ///
	-- @net.engining.gm.infrastructure.enums.ActionCode
	FINAL_ACTION char(1) NOT NULL COMMENT '最终行动 : ///
@net.engining.gm.infrastructure.enums.ActionCode',
	-- 10个拒绝原因数组
	REJ_REASON varchar(40) COMMENT '拒绝原因 : 10个拒绝原因数组',
	POS_ENTRY_MODE_P1 char(3) COMMENT 'POS输入方式码',
	-- !!!java.lang.Boolean!!!
	-- 交易是否输入PIN，null表示未知
	POS_ENTRY_MODE_P2 bit(1) COMMENT 'POS输入方式码部分2 : !!!java.lang.Boolean!!!
交易是否输入PIN，null表示未知',
	POS_CONDITION_CD char(2) NOT NULL COMMENT '服务点条件码-B025',
	MERCHANT_CD varchar(15) NOT NULL COMMENT '受卡方(商户)标识码-B042',
	OPERA_TERM_ID varchar(20) NOT NULL COMMENT '操作用户终端编号',
	TRANSACTION_FEE decimal(18,2) NOT NULL COMMENT '手续费-B028',
	OVRDFT_AMT decimal(18,2) NOT NULL COMMENT '透支金额',
	-- 第一笔原始交易的交易编号
	ORG_SEQ_ID int NOT NULL COMMENT '初始交易编号 : 第一笔原始交易的交易编号',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- 外币交易发生时的结算汇率
	EXCHANGE_RATE decimal(9,6) COMMENT '外币汇率 : 外币交易发生时的结算汇率',
	PRIMARY KEY (SEQ_ID)
) COMMENT = '授权交易历史';


-- 未匹配交易授权(预授权)流水表
CREATE TABLE AUTH_UNMATCH
(
	-- 对应auth_txn_hist中的交易编号
	TXN_SEQ_ID int NOT NULL COMMENT '交易编号 : 对应auth_txn_hist中的交易编号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	TRACE_AUDIT_NO varchar(6) NOT NULL COMMENT '系统跟踪号',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	TXN_CURR_CD char(3) NOT NULL COMMENT '交易币种代码',
	AUTH_CODE char(6) COMMENT '授权码',
	-- 受卡点（商户）名称，地址
	MERCHANT_NAME_ADDR varchar(40) NOT NULL COMMENT '商户名称地址 : 受卡点（商户）名称，地址',
	CHB_TXN_AMT decimal(18,2) NOT NULL COMMENT '持卡人账户币种金额',
	CHB_CURR_CD char(3) NOT NULL COMMENT '持卡人账户币种',
	-- ///
	-- @net.engining.gm.infrastructure.enums.Channel
	CHANNEL varchar(15) NOT NULL COMMENT '交易渠道 : ///
@net.engining.gm.infrastructure.enums.Channel',
	MCC char(4) NOT NULL COMMENT '商户类别代码',
	-- 受理分行代码
	ACQ_BRANCH_ID varchar(11) NOT NULL COMMENT '受理分行代码 : 受理分行代码',
	FWD_INST_ID varchar(11) NOT NULL COMMENT '转发机构号',
	-- MMddhhmmss
	TRANSMISSION_TIMESTAMP varchar(10) NOT NULL COMMENT '传送日期 : MMddhhmmss',
	-- 清算日期，以卡管系统为准
	SETTLE_DATE date NOT NULL COMMENT '清算日期 : 清算日期，以卡管系统为准',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnType
	TXN_TYPE varchar(20) NOT NULL COMMENT '交易类型 : ///
@net.engining.gm.infrastructure.enums.TxnType',
	-- ///
	-- N|正常
	-- R|被冲正
	-- V|被撤销
	-- A|已确认完成
	-- D|拒绝
	TXN_STATUS char(1) NOT NULL COMMENT '交易状态 : ///
N|正常
R|被冲正
V|被撤销
A|已确认完成
D|拒绝',
	TXN_CODE varchar(20) NOT NULL COMMENT '交易代码',
	LOG_OL_TIME timestamp DEFAULT NOW() NOT NULL COMMENT 'LOG联机时间',
	LOG_BIZ_DATE date NOT NULL COMMENT '联机业务日期',
	-- ///
	-- A|普通
	-- N|否
	-- F|强制
	-- D|借记调整
	-- C|贷记调整
	MANUAL_AUTH_FLAG char(1) NOT NULL COMMENT '人工授权标志 : ///
A|普通
N|否
F|强制
D|借记调整
C|贷记调整',
	OPERA_ID varchar(40) COMMENT '操作员ID',
	PRODUCT_CD char(6) NOT NULL COMMENT '产品代码',
	FINAL_REASON char(5) COMMENT '授权原因',
	COMP_AMT decimal(18,2) NOT NULL COMMENT '最终授权金额',
	-- 10个拒绝原因数组
	REJ_REASON varchar(40) COMMENT '拒绝原因 : 10个拒绝原因数组',
	POS_CONDITION_CD char(2) NOT NULL COMMENT '服务点条件码-B025',
	MERCHANT_CD varchar(15) NOT NULL COMMENT '受卡方(商户)标识码-B042',
	OPERA_TERM_ID varchar(20) NOT NULL COMMENT '操作用户终端编号',
	TRANSACTION_FEE decimal(18,2) NOT NULL COMMENT '手续费-B028',
	CUST_ID varchar(64) NOT NULL COMMENT '客户编号',
	ACCT_NO int NOT NULL COMMENT '账号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	-- 外币交易发生时的结算汇率
	EXCHANGE_RATE decimal(9,6) COMMENT '外币汇率 : 外币交易发生时的结算汇率',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ_ID)
) COMMENT = '未匹配交易授权(预授权)流水表';


-- 交易分期还款计划表
CREATE TABLE CACT_AMORTIZATION
(
	LOAN_ID int NOT NULL COMMENT '分期计划ID',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	TERM_NO int NOT NULL COMMENT '期数',
	-- 当前本金余额
	PRINCIPAL_BAL decimal(18,2) NOT NULL COMMENT '本金余额 : 当前本金余额',
	INTEREST_AMT decimal(18,2) NOT NULL COMMENT '利息金额',
	FEE_AMT decimal(18,2) NOT NULL COMMENT '费用金额',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (LOAN_ID)
) COMMENT = '交易分期还款计划表';


-- 卡号表
CREATE TABLE CACT_CARD
(
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	-- 逻辑卡号
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号 : 逻辑卡号',
	ACCT_NO int NOT NULL COMMENT '账号',
	CUST_ID varchar(64) NOT NULL COMMENT '客户编号',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CARD_NO, BUSINESS_TYPE)
) COMMENT = '卡号表';


-- 卡组信息表
CREATE TABLE CACT_CARD_GROUP
(
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	PRODUCT_CD char(6) NOT NULL COMMENT '产品代码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BscSuppInd
	BSC_SUPP_IND char(1) NOT NULL COMMENT '主附卡指示 : ///
@net.engining.gm.infrastructure.enums.BscSuppInd',
	-- 对应主卡的卡群组号
	MAIN_CARD_GROUP_ID varchar(19) COMMENT '主卡卡群组号 : 对应主卡的卡群组号',
	OWNING_BRANCH varchar(9) NOT NULL COMMENT '发卡网点',
	SETUP_DATE date NOT NULL COMMENT '创建日期',
	-- 卡片账户置上待销卡销户锁定码的日期
	-- 在销卡时置上销卡日期，在销卡撤销时清零销卡日期
	CANCEL_DATE date COMMENT '销卡销户日期 : 卡片账户置上待销卡销户锁定码的日期
在销卡时置上销卡日期，在销卡撤销时清零销卡日期',
	CTD_RETAIL_AMT decimal(18,2) NOT NULL COMMENT '当期消费金额',
	CTD_CASH_AMT decimal(18,2) NOT NULL COMMENT '当期取现金额',
	CTD_NET_RETAIL_AMT decimal(18,2) NOT NULL COMMENT '当期网银交易金额',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CARD_GROUP_ID)
) COMMENT = '卡组信息表';


-- 客服费通知表 : 拒绝重入账交易临时表
CREATE TABLE CACT_CSSFEE_REG
(
	CSSFEE_TXN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '客服费交易流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	SERVICE_NBR char(6) NOT NULL COMMENT '客服交易编号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	-- 请求日期时间
	REQUEST_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '请求日期时间 : 请求日期时间',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CSSFEE_TXN_SEQ)
) COMMENT = '客服费通知表 : 拒绝重入账交易临时表';


-- 交易分期信息表
CREATE TABLE CACT_LOAN
(
	LOAN_ID int NOT NULL AUTO_INCREMENT COMMENT '分期计划ID',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	REF_NBR varchar(23) NOT NULL COMMENT '交易参考号',
	-- 主卡逻辑卡号
	LOGICAL_CARD_NO varchar(19) NOT NULL COMMENT '逻辑卡号 : 主卡逻辑卡号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	ACCT_NO int NOT NULL COMMENT '账号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD char(3) NOT NULL COMMENT '币种',
	REGISTER_DATE date NOT NULL COMMENT '分期注册日期',
	-- 请求日期时间
	REQUEST_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '请求日期时间 : 请求日期时间',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.LoanType
	LOAN_TYPE char(1) NOT NULL COMMENT '分期类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.LoanType',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus
	LOAN_STATUS char(1) NOT NULL COMMENT '分期状态 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus
	-- 
	LAST_LOAN_STATUS char(1) NOT NULL COMMENT '分期上次状态 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus
',
	LOAN_INIT_TERM int NOT NULL COMMENT '分期总期数',
	CURR_TERM int NOT NULL COMMENT '当前期数',
	REMAIN_TERM int NOT NULL COMMENT '剩余期数',
	LOAN_INIT_PRIN decimal(18,2) NOT NULL COMMENT '分期总本金',
	LOAN_FIXED_PMT_PRIN decimal(18,2) NOT NULL COMMENT '分期每期应还本金',
	LOAN_FIRST_TERM_PRIN decimal(18,2) NOT NULL COMMENT '分期首期应还本金',
	LOAN_FINAL_TERM_PRIN decimal(18,2) NOT NULL COMMENT '分期末期应还本金',
	LOAN_INIT_FEE1 decimal(18,2) NOT NULL COMMENT '分期总手续费',
	LOAN_FIXED_FEE1 decimal(18,2) NOT NULL COMMENT '分期每期手续费',
	LOAN_FIRST_TERM_FEE1 decimal(18,2) NOT NULL COMMENT '分期首期手续费',
	LOAN_FINAL_TERM_FEE1 decimal(18,2) NOT NULL COMMENT '分期末期手续费',
	UNEARNED_PRIN decimal(18,2) NOT NULL COMMENT '未出账单的本金',
	UNEARNED_FEE1 decimal(18,2) NOT NULL COMMENT '未出账单手续费',
	ACTIVATE_DATE date NOT NULL COMMENT '激活日期',
	PAID_OUT_DATE date NOT NULL COMMENT '还清日期',
	TERMINATE_DATE date NOT NULL COMMENT '提前终止日期',
	-- ///
	-- V|持卡人主动终止（volunteer）
	-- M|银行业务人员手工终止（manual）
	-- D|逾期自动终止（delinquncy）
	TERMINATE_REASON_CD char(1) NOT NULL COMMENT '分期终止原因代码 : ///
V|持卡人主动终止（volunteer）
M|银行业务人员手工终止（manual）
D|逾期自动终止（delinquncy）',
	PRIN_PAID decimal(18,2) NOT NULL COMMENT '已偿还本金',
	INT_PAID decimal(18,2) NOT NULL COMMENT '已偿还利息',
	FEE_PAID decimal(18,2) NOT NULL COMMENT '已偿还费用',
	LOAN_CURR_BAL decimal(18,2) NOT NULL COMMENT '分期当前总余额',
	LOAN_BAL_XFROUT decimal(18,2) NOT NULL COMMENT '分期未到期余额',
	LOAN_BAL_XFRIN decimal(18,2) NOT NULL COMMENT '分期已出账单余额',
	LOAN_PRIN_XFROUT decimal(18,2) NOT NULL COMMENT '分期未到期本金',
	LOAN_PRIN_XFRIN decimal(18,2) NOT NULL COMMENT '分期已出账单本金',
	LOAN_FEE1_XFROUT decimal(18,2) NOT NULL COMMENT '分期未到期手续费',
	LOAN_FEE1_XFRIN decimal(18,2) NOT NULL COMMENT '分期已出账单手续费',
	OLD_TXN_AMT decimal(18,2) NOT NULL COMMENT '原始交易币种金额',
	OLD_TRANS_DATE date NOT NULL COMMENT '原始交易日期',
	ORIG_AUTH_CODE char(6) NOT NULL COMMENT '原始交易授权码',
	LOAN_CODE varchar(4) NOT NULL COMMENT '分期计划代码',
	REGISTER_ID int NOT NULL COMMENT '分期申请顺序号',
	LOAN_PRIN_SUB_ACCT_ID int COMMENT '分期本金子账户序号',
	LOAN_FEE_SUB_ACCT_ID int COMMENT '分期费用子账户序号',
	LOAN_INT_SUB_ACCT_ID int COMMENT '分期利息子账户序号',
	MERCHANT_ID varchar(15) COMMENT '商户ID',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (LOAN_ID)
) COMMENT = '交易分期信息表';


-- 交易分期注册表
CREATE TABLE CACT_LOAN_REG
(
	REGISTER_ID int NOT NULL AUTO_INCREMENT COMMENT '分期申请顺序号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	ACCT_NO int NOT NULL COMMENT '账号',
	CURR_CD char(3) NOT NULL COMMENT '币种',
	REGISTER_DATE date NOT NULL COMMENT '分期注册日期',
	-- 请求日期时间
	REQUEST_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '请求日期时间 : 请求日期时间',
	REF_NBR varchar(23) NOT NULL COMMENT '交易参考号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.LoanType
	LOAN_TYPE char(1) NOT NULL COMMENT '分期类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.LoanType',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus
	LOAN_STATUS char(1) NOT NULL COMMENT '分期状态 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus',
	MERCHANT_ID varchar(15) COMMENT '商户ID',
	LOAN_INIT_TERM int NOT NULL COMMENT '分期总期数',
	LOAN_INIT_PRIN decimal(18,2) NOT NULL COMMENT '分期总本金',
	LOAN_FIXED_PMT_PRIN decimal(18,2) NOT NULL COMMENT '分期每期应还本金',
	LOAN_FIRST_TERM_PRIN decimal(18,2) NOT NULL COMMENT '分期首期应还本金',
	LOAN_FINAL_TERM_PRIN decimal(18,2) NOT NULL COMMENT '分期末期应还本金',
	LOAN_INIT_FEE1 decimal(18,2) NOT NULL COMMENT '分期总手续费',
	LOAN_FIXED_FEE1 decimal(18,2) NOT NULL COMMENT '分期每期手续费',
	LOAN_FIRST_TERM_FEE1 decimal(18,2) NOT NULL COMMENT '分期首期手续费',
	LOAN_FINAL_TERM_FEE1 decimal(18,2) NOT NULL COMMENT '分期末期手续费',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.LoanFeeMethod
	LOAN_FEE_METHOD char(1) NOT NULL COMMENT '分期手续费收取方式 : ///
@net.engining.pcx.cc.param.model.enums.LoanFeeMethod',
	OLD_TXN_AMT decimal(18,2) NOT NULL COMMENT '原始交易币种金额',
	OLD_TRANS_DATE date NOT NULL COMMENT '原始交易日期',
	ORIG_AUTH_CODE char(6) NOT NULL COMMENT '原始交易授权码',
	-- 受理机构终端标识码
	ACQ_TERMINAL_ID varchar(8) COMMENT '受理机构终端标识码 : 受理机构终端标识码',
	LOAN_CODE varchar(4) NOT NULL COMMENT '分期计划代码',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (REGISTER_ID)
) COMMENT = '交易分期注册表';


-- 操作日志表 : 内管系统操作日志表
CREATE TABLE CACT_OPER_LOG
(
	ORG varchar(12) NOT NULL COMMENT '机构号',
	-- 操作序列号
	OPER_SEQ int NOT NULL AUTO_INCREMENT COMMENT '操作序列号 : 操作序列号',
	OPERA_ID varchar(40) NOT NULL COMMENT '操作员ID',
	OPER_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	BRANCH_ID varchar(9) NOT NULL COMMENT '分支行号',
	SERVICE_CODE char(4) NOT NULL COMMENT '服务代码',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	CUST_ID varchar(64) NOT NULL COMMENT '客户编号',
	RELATED_KEY varchar(50) NOT NULL COMMENT '关联键值',
	RELATED_DESC varchar(100) NOT NULL COMMENT '关联描述',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (OPER_SEQ)
) COMMENT = '操作日志表 : 内管系统操作日志表';


-- 机构统计信息表 : 用联机服务来实现，在内存中统计，不落实体表
CREATE TABLE CACT_ORG_STST
(
	ORG_SEQ_ID int NOT NULL COMMENT '机构编号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	LOG_BIZ_DATE date NOT NULL COMMENT '联机业务日期',
	LAST_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔交易时间',
	LAST_SUCC_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔成功交易时间',
	LAST_CUP_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔CUP渠道交易时间',
	LAST_BANK_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔行内交易时间',
	LAST_VISA_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔VISA交易时间',
	LAST_MC_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔MC交易时间',
	LAST_JCB_TXN_TIMESTAMP timestamp DEFAULT NOW() NOT NULL COMMENT '最后一笔JCB交易时间',
	TODAY_TXN_CNT int NOT NULL COMMENT '今日交易笔数',
	TODAY_SUCC_TXN_CNT int NOT NULL COMMENT '今日成功交易笔数',
	TODAY_REVERSAL_CNT int NOT NULL COMMENT '今日冲正交易笔数',
	TODAY_RETAIL_CNT int NOT NULL COMMENT '今日消费笔数',
	TODAY_CASH_CNT int NOT NULL COMMENT '今日取现交易笔数',
	TODAY_LOAN_CNT int NOT NULL COMMENT '今日分期交易笔数',
	TODAY_REDEMP_CNT int NOT NULL COMMENT '今日积分交易笔数',
	TODAY_IC_TSF_CNT int NOT NULL COMMENT '今日圈存交易笔数',
	TODAY_BILPMT_CNT int NOT NULL COMMENT '今日代扣笔数',
	TODAY_PAYMENT_CNT int NOT NULL COMMENT '今日存款笔数',
	TODAY_TSFIN_CNT int NOT NULL COMMENT '今日转入笔数',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ORG_SEQ_ID),
	UNIQUE (ORG)
) COMMENT = '机构统计信息表 : 用联机服务来实现，在内存中统计，不落实体表';


-- 卡积分调整日志表
CREATE TABLE CACT_POINT_ADJ_LOG
(
	-- 操作序列号
	OPER_SEQ int NOT NULL AUTO_INCREMENT COMMENT '操作序列号 : 操作序列号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	OPER_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	OPERA_ID varchar(40) NOT NULL COMMENT '操作员ID',
	ACCT_NO int NOT NULL COMMENT '账号',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD char(3) COMMENT '币种',
	-- ///
	-- I|积分增加
	-- A|积分调整
	-- D|积分兑换
	-- 
	ADJ_IND char(1) NOT NULL COMMENT '调整标志 : ///
I|积分增加
A|积分调整
D|积分兑换
',
	POINT decimal(13) NOT NULL COMMENT '积分数值',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (OPER_SEQ)
) COMMENT = '卡积分调整日志表';


-- 卡积分调整通知表 : 交易流水历史 - 在入账的同时写该表
CREATE TABLE CACT_POINT_REG
(
	-- 如果一个账户需要补打多期账单，则需要生成多笔补打申请流水，每笔流水分配一个流水号
	REPRINT_SEQ int NOT NULL AUTO_INCREMENT COMMENT '补打账单流水号 : 如果一个账户需要补打多期账单，则需要生成多笔补打申请流水，每笔流水分配一个流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	ACCT_NO int NOT NULL COMMENT '账号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD char(3) NOT NULL COMMENT '币种',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	-- 请求日期时间
	REQUEST_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '请求日期时间 : 请求日期时间',
	-- ///
	-- M|金融交易
	-- P|积分交易
	-- O|MEMO类交易
	-- INTEACC|利息计提
	-- PINTACC|罚息计提
	-- TRANSFO|余额成份结转
	-- BATREPA|批量还款
	-- LoanTally|贷款发放记账
	-- GrantTally|授额提降额记账
	-- FeeTally|费用收取记账
	-- RepTally|还款记账
	-- RefTally|退款记账
	POST_TXN_TYPE varchar(12) NOT NULL COMMENT '入账交易类型 : ///
M|金融交易
P|积分交易
O|MEMO类交易
INTEACC|利息计提
PINTACC|罚息计提
TRANSFO|余额成份结转
BATREPA|批量还款
LoanTally|贷款发放记账
GrantTally|授额提降额记账
FeeTally|费用收取记账
RepTally|还款记账
RefTally|退款记账',
	-- ///
	-- I|积分增加
	-- A|积分调整
	-- D|积分兑换
	-- 
	ADJ_IND char(1) NOT NULL COMMENT '调整标志 : ///
I|积分增加
A|积分调整
D|积分兑换
',
	POINT decimal(13) NOT NULL COMMENT '积分数值',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (REPRINT_SEQ)
) COMMENT = '卡积分调整通知表 : 交易流水历史 - 在入账的同时写该表';


-- 分期手续费收取情况表
CREATE TABLE CACT_REPAY_FEE_INFO
(
	SEQ_ID int NOT NULL AUTO_INCREMENT COMMENT 'SEQ_ID',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	REPAY_PERIOD int COMMENT '还款期数',
	-- IB_ONLINE_JOURNEY
	TXN_SEQ char(100) COMMENT '交易流水号 : IB_ONLINE_JOURNEY',
	FEE_AMOUNT decimal(18,2) COMMENT '未还手续费金额',
	CREATE_DATE date COMMENT '创建日期',
	POST_DATE date COMMENT '入账日期',
	PRINCIPAL decimal(18,2) COMMENT '未还本金',
	REPAY_FEE_DESC char(20) COMMENT '分期手续费描述',
	PRIMARY KEY (SEQ_ID)
) COMMENT = '分期手续费收取情况表';


-- 补打账单通知表 : 交易流水历史 - 在入账的同时写该表
CREATE TABLE CACT_REPRINT_REG
(
	-- 如果一个账户需要补打多期账单，则需要生成多笔补打申请流水，每笔流水分配一个流水号
	REPRINT_SEQ int NOT NULL AUTO_INCREMENT COMMENT '补打账单流水号 : 如果一个账户需要补打多期账单，则需要生成多笔补打申请流水，每笔流水分配一个流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	ACCT_NO int NOT NULL COMMENT '账号',
	STMT_DATE date NOT NULL COMMENT '账单日期',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	-- 请求日期时间
	REQUEST_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '请求日期时间 : 请求日期时间',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (REPRINT_SEQ)
) COMMENT = '补打账单通知表 : 交易流水历史 - 在入账的同时写该表';


-- 账户账单历史表
CREATE TABLE CACT_STMT_HST
(
	STMT_SEQ int NOT NULL AUTO_INCREMENT COMMENT '账单历史编号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	STMT_DATE date NOT NULL COMMENT '账单日期',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	-- 客户姓名 - 冗余字段，从客户表复制过来，这样批量处理时可以不用关联客户信息
	NAME varchar(80) COMMENT '姓名 : 客户姓名 - 冗余字段，从客户表复制过来，这样批量处理时可以不用关联客户信息',
	PMT_DUE_DATE date NOT NULL COMMENT '到期还款日期',
	CURR_CD char(3) NOT NULL COMMENT '币种',
	-- !!!java.lang.Boolean!!!
	STMT_FLAG bit(1) NOT NULL COMMENT '账单标志 : !!!java.lang.Boolean!!!',
	ACCT_LIMIT decimal(18,2) COMMENT '账户额度',
	TEMP_LIMIT decimal(18,2) COMMENT '临时额度',
	TEMP_LIMIT_BEGIN_DATE date COMMENT '临时额度开始日期',
	TEMP_LIMIT_END_DATE date COMMENT '临时额度结束日期',
	-- 上个账单日
	LAST_STMT_DATE date COMMENT '上个账单日期 : 上个账单日',
	STMT_BEG_BAL decimal(18,2) COMMENT '上期账单余额',
	STMT_CURR_BAL decimal(18,2) NOT NULL COMMENT '当期账单余额',
	-- 持卡人还清该欠款，则会对账户进行全额还款免息，该金额会显示在账单上，小于等于账户账单余额
	QUAL_GRACE_BAL decimal(18,2) NOT NULL COMMENT '全部应还款额 : 持卡人还清该欠款，则会对账户进行全额还款免息，该金额会显示在账单上，小于等于账户账单余额',
	TOT_DUE_AMT decimal(18,2) NOT NULL COMMENT '最小还款额',
	CTD_CASH_AMT decimal(18,2) COMMENT '当期取现金额',
	-- 本账期取现笔数
	CTD_CASH_CNT int COMMENT '当期取现笔数 : 本账期取现笔数',
	CTD_RETAIL_AMT decimal(18,2) COMMENT '当期消费金额',
	-- 本账期消费笔数
	CTD_RETAIL_CNT int COMMENT '当期消费笔数 : 本账期消费笔数',
	-- 本账期还款金额
	CTD_PAYMENT_AMT decimal(18,2) COMMENT '当期还款金额 : 本账期还款金额',
	-- 本账期还款笔数
	CTD_PAYMENT_CNT int COMMENT '当期还款笔数 : 本账期还款笔数',
	-- 本账期借记调整金额
	CTD_DB_ADJ_AMT decimal(18,2) COMMENT '当期借记调整金额 : 本账期借记调整金额',
	-- 本账期借记调整笔数
	CTD_DB_ADJ_CNT int COMMENT '当期借记调整笔数 : 本账期借记调整笔数',
	-- 本账期贷记调整金额
	CTD_CR_ADJ_AMT decimal(18,2) COMMENT '当期贷记调整金额 : 本账期贷记调整金额',
	-- 本账期贷记调整笔数
	CTD_CR_ADJ_CNT int COMMENT '当期贷记调整笔数 : 本账期贷记调整笔数',
	-- 本账期费用金额
	CTD_FEE_AMT decimal(18,2) COMMENT '当期费用金额 : 本账期费用金额',
	-- 本账期费用笔数
	CTD_FEE_CNT int COMMENT '当期费用笔数 : 本账期费用笔数',
	-- 本账期退货金额
	CTD_REFUND_AMT decimal(18,2) COMMENT '当期退货金额 : 本账期退货金额',
	-- 本账期退货笔数
	CTD_REFUND_CNT int COMMENT '当期退货笔数 : 本账期退货笔数',
	-- 当期借记金额，统计当期新增的借记交易金额
	CTD_AMT_DB decimal(18,2) COMMENT '当期借记金额 : 当期借记金额，统计当期新增的借记交易金额',
	-- 当期借记交易笔数
	CTD_NBR_DB int COMMENT '当期借记交易笔数 : 当期借记交易笔数',
	-- 当期贷记金额
	CTD_AMT_CR decimal(18,2) COMMENT '当期贷记金额 : 当期贷记金额',
	-- 当期贷记交易笔数
	CTD_NBR_CR int COMMENT '当期贷记交易笔数 : 当期贷记交易笔数',
	-- 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要
	AGE_CD char(1) COMMENT '账龄 : 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要',
	-- !!!java.lang.Boolean!!!
	GRACE_DAYS_FULL_IND bit(1) NOT NULL COMMENT '是否已全额还款 : !!!java.lang.Boolean!!!',
	-- 超限日期
	OVRLMT_DATE date COMMENT '超限日期 : 超限日期',
	BLOCK_CODE varchar(27) COMMENT '锁定码',
	-- 取值为M,F时账单系统需要将本外币账单接口合并生成实体账单
	-- ///
	-- N|不还款
	-- M|最小额还款
	-- F|全额还款
	DUAL_BILLING_FLAG char(1) COMMENT '本币溢缴款还外币指示 : 取值为M,F时账单系统需要将本外币账单接口合并生成实体账单
///
N|不还款
M|最小额还款
F|全额还款',
	-- 取现额度比例（占用账户信用额度）
	CASH_LIMIT_RT decimal(5,2) COMMENT '取现额度比例 : 取现额度比例（占用账户信用额度）',
	-- 额度内分期额度比例
	LOAN_LIMIT_RT decimal(5,2) COMMENT '额度内分期额度比例 : 额度内分期额度比例',
	-- 账单日生成，账龄提升日转移至账龄0的最小还款额中
	CURR_DUE_AMT decimal(18,2) NOT NULL COMMENT '当期最小还款额 : 账单日生成，账龄提升日转移至账龄0的最小还款额中',
	-- 上个月最小还款额
	PAST_DUE_AMT1 decimal(18,2) COMMENT '上个月最小还款额 : 上个月最小还款额',
	-- 上2个月最小还款额
	PAST_DUE_AMT2 decimal(18,2) COMMENT '上2个月最小还款额 : 上2个月最小还款额',
	-- 上3个月最小还款额
	PAST_DUE_AMT3 decimal(18,2) COMMENT '上3个月最小还款额 : 上3个月最小还款额',
	-- 上4个月最小还款额
	AGE4_DUE_AMT decimal(18,2) COMMENT '账龄4最小还款额 : 上4个月最小还款额',
	-- 上5个月最小还款额
	PAST_DUE_AMT5 decimal(18,2) COMMENT '上5个月最小还款额 : 上5个月最小还款额',
	-- 上6个月最小还款额
	PAST_DUE_AMT6 decimal(18,2) COMMENT '上6个月最小还款额 : 上6个月最小还款额',
	-- 上7个月最小还款额
	PAST_DUE_AMT7 decimal(18,2) COMMENT '上7个月最小还款额 : 上7个月最小还款额',
	-- 上8个月最小还款额
	PAST_DUE_AMT8 decimal(18,2) COMMENT '上8个月最小还款额 : 上8个月最小还款额',
	EMAIL varchar(80) COMMENT '电子邮箱',
	-- ///
	-- M|男
	-- F|女
	GENDER char(1) COMMENT '性别 : ///
M|男
F|女',
	MOBILE_NO varchar(20) COMMENT '移动电话',
	STMT_ADDR_ID int COMMENT '账单寄送地址编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.StmtMediaType
	STMT_MEDIA_TYPE char(1) COMMENT '账单介质类型 : ///
@net.engining.gm.infrastructure.enums.StmtMediaType',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (STMT_SEQ),
	UNIQUE (STMT_DATE)
) COMMENT = '账户账单历史表';


-- 未出账单交易历史表 : 未出账单交易历史表
CREATE TABLE CACT_TXN_UNSTMT
(
	TXN_SEQ int NOT NULL COMMENT '交易流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CARD_NO varchar(19) COMMENT '卡号',
	CARD_GROUP_ID varchar(19) COMMENT '卡群组号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	TXN_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '交易时间',
	POST_CODE char(8) NOT NULL COMMENT '入账交易码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	POST_DATE date NOT NULL COMMENT '入账日期',
	AUTH_CODE char(6) COMMENT '授权码',
	TXN_CURR_CD char(3) NOT NULL COMMENT '交易币种代码',
	-- 清算货币码
	POST_CURR_CD char(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	REF_NBR varchar(23) COMMENT '交易参考号',
	TXN_DESC varchar(400) NOT NULL COMMENT '交易描述',
	-- 用于账单交易描述显示
	TXN_SHORT_DESC varchar(40) NOT NULL COMMENT '账单交易描述 : 用于账单交易描述显示',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	POSTING_FLAG char(3) NOT NULL COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	PRE_POSTING_FLAG char(3) COMMENT '往日入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- 受理分行代码
	ACQ_BRANCH_ID varchar(11) COMMENT '受理分行代码 : 受理分行代码',
	-- 受理机构终端标识码
	ACQ_TERMINAL_ID varchar(8) COMMENT '受理机构终端标识码 : 受理机构终端标识码',
	-- 受理机构标识码
	ACQ_ACCEPTOR_ID varchar(15) COMMENT '受卡方标识码 : 受理机构标识码',
	-- 受卡点（商户）名称，地址
	MERCHANT_NAME_ADDR varchar(40) COMMENT '商户名称地址 : 受卡点（商户）名称，地址',
	MCC char(4) COMMENT '商户类别代码',
	STMT_DATE date COMMENT '账单日期',
	SUB_ACCT_ID int COMMENT '子账户序号',
	-- 子账户计价规则参数编号
	SUBACCT_PARAM_ID varchar(30) COMMENT '子账户参数序号 : 子账户计价规则参数编号',
	-- 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_B4_POSTING char(1) COMMENT '入账前账龄 : 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	-- 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_AFTER_POSTING char(1) COMMENT '入账后账龄 : 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	ACCT_CURR_BAL decimal(18,2) COMMENT '账户当前余额',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- 供查询交易使用。
	TXN_TYPE varchar(30) COMMENT '本地交易类型 : 供查询交易使用。',
	-- 供查询交易使用。
	OPP_ACCT varchar(80) COMMENT '交易对手方账号 : 供查询交易使用。',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ)
) COMMENT = '未出账单交易历史表 : 未出账单交易历史表';



