SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Indexes */

DROP INDEX INX_TXN_DETAIL_SEQ ON AP_GL_TXN;
DROP INDEX  INX_SUB_ASS_VALUE ON AP_GL_VOL_DTL_ASS_SUM_HST;



/* Drop Tables */

DROP TABLE IF EXISTS AP_GL;
DROP TABLE IF EXISTS AP_GL_BAL;
DROP TABLE IF EXISTS AP_GL_BAL_HST;
DROP TABLE IF EXISTS AP_GL_TXN;
DROP TABLE IF EXISTS AP_GL_TXN_HST;
DROP TABLE IF EXISTS AP_GL_VOL_DTL;
DROP TABLE IF EXISTS AP_GL_VOL_DTL_ASS;
DROP TABLE IF EXISTS AP_GL_VOL_DTL_ASS_HST;
DROP TABLE IF EXISTS AP_GL_VOL_DTL_ASS_SUM;
DROP TABLE IF EXISTS AP_GL_VOL_DTL_ASS_SUM_HST;
DROP TABLE IF EXISTS AP_GL_VOL_DTL_HST;
DROP TABLE IF EXISTS AP_SUBJECT_SUMMARY;
DROP TABLE IF EXISTS AP_SUBJECT_SUMMARY_HST;
DROP TABLE IF EXISTS CACT_ADJ_LEDGER_ACCT_OPR_HST;
DROP TABLE IF EXISTS CACT_TXN_GL;
DROP TABLE IF EXISTS GL_TRANS_OPR_HST;




/* Create Tables */

-- 总账文件表(供年终结转使用)(废弃)
CREATE TABLE AP_GL
(
	AP_GL_ID int NOT NULL AUTO_INCREMENT COMMENT 'AP_GL_ID',
	S varchar(40) COMMENT 'S',
	BUSINESS_UNIT varchar(40) COMMENT 'BUSINESS_UNIT',
	LEDGER varchar(40) COMMENT 'LEDGER',
	ACCOUNT varchar(40) COMMENT 'ACCOUNT',
	DEPARTMENT varchar(40) COMMENT 'DEPARTMENT',
	OPERATING_UNIT varchar(40) COMMENT 'OPERATING_UNIT',
	FUND_CODE varchar(40) COMMENT 'FUND_CODE',
	TRANSACTION_CURRENCY_CODE varchar(40) COMMENT 'TRANSACTION_CURRENCY_CODE',
	FISCAL_YEAR varchar(40) COMMENT 'FISCAL_YEAR',
	ACCOUNTING_PERIOD varchar(40) COMMENT 'ACCOUNTING_PERIOD',
	POSTED_TOTAL_AMOUNT varchar(40) COMMENT 'POSTED_TOTAL_AMOUNT',
	POSTED_BASE_CURRENCY_AMOUNT varchar(40) COMMENT 'POSTED_BASE_CURRENCY_AMOUNT',
	POSTED_TRANSACTION_AMOUNT varchar(40) COMMENT 'POSTED_TRANSACTION_AMOUNT',
	POSTED_TOTAL_DEBIT_AMOUNT varchar(40) COMMENT 'POSTED_TOTAL_DEBIT_AMOUNT',
	POSTED_TOTAL_CREDIT_AMOUNT varchar(40) COMMENT 'POSTED_TOTAL_CREDIT_AMOUNT',
	POSTED_TRANSACTION_DEBIT_AMOUNT varchar(40) COMMENT 'POSTED_TRANSACTION_DEBIT_AMOUNT',
	POSTED_TRANSACTION_CREDIT_AMOUNT varchar(40) COMMENT 'POSTED_TRANSACTION_CREDIT_AMOUNT',
	TRANSACTION_DATE varchar(40) COMMENT 'TRANSACTION_DATE',
	BRANCH_NO varchar(9) COMMENT 'BRANCH_NO',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT 'LAST_UPDATE_DATE : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT 'BIZ_DATE',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	ORG varchar(12) COMMENT '机构号',
	PRIMARY KEY (AP_GL_ID)
) COMMENT = '总账文件表(供年终结转使用)(废弃)';


-- 当日总账表
CREATE TABLE AP_GL_BAL
(
	ID int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.SubjectType
	SUBJECT_TYPE char(1) COMMENT '科目类型 : ///
@net.engining.pcx.cc.param.model.enums.SubjectType',
	SUBJECT_NAME varchar(100) COMMENT '科目名称',
	DB_BAL decimal(18,2) NOT NULL COMMENT '借方余额',
	CR_BAL decimal(18,2) NOT NULL COMMENT '贷方余额',
	DB_AMT decimal(18,2) NOT NULL COMMENT '借方发生额',
	CR_AMT decimal(18,2) NOT NULL COMMENT '贷方发生额',
	DB_COUNT int NOT NULL COMMENT '借方发生笔数',
	CR_COUNT int NOT NULL COMMENT '贷方发生笔数',
	LAST_DB_BAL decimal(18,2) NOT NULL COMMENT '昨日借记余额',
	LAST_CR_BAL decimal(18,2) NOT NULL COMMENT '昨日贷记余额',
	MTD_DB_AMT decimal(18,2) NOT NULL COMMENT '本月借方发生额',
	MTD_CR_AMT decimal(18,2) NOT NULL COMMENT '本月贷方发生额',
	LAST_MTH_DB_BAL decimal(18,2) NOT NULL COMMENT '上月借记余额',
	LAST_MTH_CR_BAL decimal(18,2) NOT NULL COMMENT '上月贷记余额',
	QTD_DB_AMT decimal(18,2) NOT NULL COMMENT '本季度借记发生额',
	QTD_CR_AMT decimal(18,2) NOT NULL COMMENT '本季度贷记发生额',
	LAST_QTR_DB_BAL decimal(18,2) NOT NULL COMMENT '上季度借记余额',
	LAST_QTR_CR_BAL decimal(18,2) NOT NULL COMMENT '上季度贷记余额',
	YTD_DB_AMT decimal(18,2) NOT NULL COMMENT '本年借记发生额',
	YTD_CR_AMT decimal(18,2) NOT NULL COMMENT '本年贷记发生额',
	LAST_YR_DB_BAL decimal(18,2) NOT NULL COMMENT '上年借记余额',
	LAST_YR_CR_BAL decimal(18,2) NOT NULL COMMENT '上年贷记余额',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	-- ///
	-- S|一级
	-- L|末级
	-- O|其他
	SUBJECT_LEVEL varchar(10) COMMENT '科目层级 : ///
S|一级
L|末级
O|其他',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ID),
	UNIQUE (ID),
	UNIQUE (ORG, SUBJECT_CD, BRANCH_NO)
) COMMENT = '当日总账表';


-- 总账历史表
CREATE TABLE AP_GL_BAL_HST
(
	ID int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.SubjectType
	SUBJECT_TYPE char(1) COMMENT '科目类型 : ///
@net.engining.pcx.cc.param.model.enums.SubjectType',
	SUBJECT_NAME varchar(100) COMMENT '科目名称',
	DB_BAL decimal(18,2) NOT NULL COMMENT '借方余额',
	CR_BAL decimal(18,2) NOT NULL COMMENT '贷方余额',
	DB_AMT decimal(18,2) NOT NULL COMMENT '借方发生额',
	CR_AMT decimal(18,2) NOT NULL COMMENT '贷方发生额',
	DB_COUNT int NOT NULL COMMENT '借方发生笔数',
	CR_COUNT int NOT NULL COMMENT '贷方发生笔数',
	LAST_DB_BAL decimal(18,2) NOT NULL COMMENT '昨日借记余额',
	LAST_CR_BAL decimal(18,2) NOT NULL COMMENT '昨日贷记余额',
	MTD_DB_AMT decimal(18,2) NOT NULL COMMENT '本月借方发生额',
	MTD_CR_AMT decimal(18,2) NOT NULL COMMENT '本月贷方发生额',
	LAST_MTH_DB_BAL decimal(18,2) NOT NULL COMMENT '上月借记余额',
	LAST_MTH_CR_BAL decimal(18,2) NOT NULL COMMENT '上月贷记余额',
	QTD_DB_AMT decimal(18,2) NOT NULL COMMENT '本季度借记发生额',
	QTD_CR_AMT decimal(18,2) NOT NULL COMMENT '本季度贷记发生额',
	LAST_QTR_DB_BAL decimal(18,2) NOT NULL COMMENT '上季度借记余额',
	LAST_QTR_CR_BAL decimal(18,2) NOT NULL COMMENT '上季度贷记余额',
	YTD_DB_AMT decimal(18,2) NOT NULL COMMENT '本年借记发生额',
	YTD_CR_AMT decimal(18,2) NOT NULL COMMENT '本年贷记发生额',
	LAST_YR_DB_BAL decimal(18,2) NOT NULL COMMENT '上年借记余额',
	LAST_YR_CR_BAL decimal(18,2) NOT NULL COMMENT '上年贷记余额',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	-- ///
	-- S|一级
	-- L|末级
	-- O|其他
	SUBJECT_LEVEL varchar(10) COMMENT '科目层级 : ///
S|一级
L|末级
O|其他',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ID),
	UNIQUE (ID)
) COMMENT = '总账历史表';


-- 当日总账交易流水
CREATE TABLE AP_GL_TXN
(
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	GLT_SEQ varchar(64) NOT NULL COMMENT '总账交易流水号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int COMMENT '账户编号',
	CURR_CD varchar(3) COMMENT '币种',
	POST_CODE varchar(20) NOT NULL COMMENT '入账代码',
	POST_DESC varchar(80) COMMENT '入账描述',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	POST_DATE date COMMENT '入账日期',
	POST_AMOUNT decimal(15,2) NOT NULL COMMENT '入账金额',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PostGlInd
	POST_GL_IND varchar(12) COMMENT '总账入账方式 : ///
@net.engining.pcx.cc.param.model.enums.PostGlInd',
	OWING_BRANCH varchar(9) COMMENT '所属分支行',
	ACQ_BRANCH varchar(9) COMMENT '受理所属分支行',
	-- ///
	-- @net.engining.gm.infrastructure.enums.AgeGroupCd
	AGE_GROUP_CD varchar(15) COMMENT '账龄组代码 : ///
@net.engining.gm.infrastructure.enums.AgeGroupCd',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	-- ///
	-- 
	-- MANU|手工记账
	-- 
	-- SYSM|系统记账
	POST_TYPE varchar(5) COMMENT '记账类型 : ///

MANU|手工记账

SYSM|系统记账',
	ACCOUNT_DESC varchar(80) COMMENT '记账说明',
	CLEAR_DATE date COMMENT '清算日期',
	TRANS_DATE date COMMENT '交易日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (GLT_SEQ)
) COMMENT = '当日总账交易流水';


-- 总账交易流水历史表
CREATE TABLE AP_GL_TXN_HST
(
	GLT_SEQ varchar(64) NOT NULL COMMENT '总账交易流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	CURR_CD varchar(3) NOT NULL COMMENT '币种',
	POST_CODE varchar(20) NOT NULL COMMENT '入账代码',
	POST_DESC varchar(80) COMMENT '入账描述',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	POST_DATE date COMMENT '入账日期',
	POST_AMOUNT decimal(15,2) NOT NULL COMMENT '入账金额',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PostGlInd
	POST_GL_IND varchar(12) COMMENT '总账入账方式 : ///
@net.engining.pcx.cc.param.model.enums.PostGlInd',
	OWING_BRANCH varchar(9) COMMENT '所属分支行',
	ACQ_BRANCH varchar(9) COMMENT '受理所属分支行',
	-- ///
	-- @net.engining.gm.infrastructure.enums.AgeGroupCd
	AGE_GROUP_CD varchar(15) COMMENT '账龄组代码 : ///
@net.engining.gm.infrastructure.enums.AgeGroupCd',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	-- ///
	-- 
	-- MANU|手工记账
	-- 
	-- SYSM|系统记账
	POST_TYPE varchar(5) COMMENT '记账类型 : ///

MANU|手工记账

SYSM|系统记账',
	ACCOUNT_DESC varchar(80) COMMENT '记账说明',
	CLEAR_DATE date COMMENT '清算日期',
	TRANS_DATE date COMMENT '交易日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (GLT_SEQ)
) COMMENT = '总账交易流水历史表';


-- 会计分录拆分交易流水表
CREATE TABLE AP_GL_VOL_DTL
(
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	GLV_SEQ varchar(64) NOT NULL COMMENT '分录流水号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	VOL_DT date COMMENT '记账日期',
	BRANCH varchar(40) COMMENT '记账机构',
	TXN_BRCD varchar(40) COMMENT '交易机构',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PostGlInd
	POST_GL_IND varchar(12) COMMENT '总账入账方式 : ///
@net.engining.pcx.cc.param.model.enums.PostGlInd',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	CURR_CD varchar(3) COMMENT '币种',
	DBSUBJECT_CD varchar(40) COMMENT '借方科目号',
	CRSUBJECT_CD varchar(40) COMMENT '贷方科目号',
	SUBJ_AMOUNT decimal(17,4) COMMENT '金额',
	VOL_SEQ int COMMENT '分录序号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	VOL_DESC varchar(80) COMMENT '分录摘要',
	REF_NO varchar(9) COMMENT '关联参考号',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	TRANS_DATE date COMMENT '交易日期',
	ASSIST_ACCOUNT_DATA mediumtext COMMENT '辅助核算项数据',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (GLV_SEQ)
) COMMENT = '会计分录拆分交易流水表';


-- 当日辅助核算拆分表
CREATE TABLE AP_GL_VOL_DTL_ASS
(
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	ASS_SEQ varchar(64) NOT NULL COMMENT '辅助拆分流水号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType
	ASSIST_TYPE varchar(10) COMMENT '辅助核算类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType',
	ASSIST_ACCOUNT_VALUE varchar(20) COMMENT '辅助核算项值',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	VOL_DT date COMMENT '记账日期',
	BRANCH varchar(40) COMMENT '记账机构',
	TXN_BRCD varchar(40) COMMENT '交易机构',
	CURR_CD varchar(3) COMMENT '币种',
	SUBJ_AMOUNT decimal(17,4) COMMENT '金额',
	VOL_SEQ int COMMENT '分录序号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	VOL_DESC varchar(80) COMMENT '分录摘要',
	REF_NO varchar(9) COMMENT '关联参考号',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	TRANS_DATE date COMMENT '交易日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	PRIMARY KEY (ASS_SEQ),
	UNIQUE (ASS_SEQ)
) COMMENT = '当日辅助核算拆分表';


-- 辅助核算拆分历史表
CREATE TABLE AP_GL_VOL_DTL_ASS_HST
(
	ASS_SEQ varchar(64) NOT NULL COMMENT '辅助拆分流水号',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType
	ASSIST_TYPE varchar(10) COMMENT '辅助核算类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType',
	ASSIST_ACCOUNT_VALUE varchar(20) COMMENT '辅助核算项值',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ORG varchar(12) COMMENT '机构号',
	VOL_DT date COMMENT '记账日期',
	BRANCH varchar(40) COMMENT '记账机构',
	TXN_BRCD varchar(40) COMMENT '交易机构',
	CURR_CD varchar(3) COMMENT '币种',
	SUBJ_AMOUNT decimal(17,4) COMMENT '金额',
	VOL_SEQ int COMMENT '分录序号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	VOL_DESC varchar(80) COMMENT '分录摘要',
	REF_NO varchar(9) COMMENT '关联参考号',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	TRANS_DATE date COMMENT '交易日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	PRIMARY KEY (ASS_SEQ),
	UNIQUE (ASS_SEQ)
) COMMENT = '辅助核算拆分历史表';


-- 当日辅助核算汇总表
CREATE TABLE AP_GL_VOL_DTL_ASS_SUM
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	ASS_SUM_SEQ varchar(64) NOT NULL COMMENT '辅助核算汇总流水号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType
	ASSIST_TYPE varchar(10) NOT NULL COMMENT '辅助核算类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType',
	ASSIST_ACCOUNT_VALUE varchar(20) NOT NULL COMMENT '辅助核算项值',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	VOL_DT date NOT NULL COMMENT '记账日期',
	DB_BAL decimal(18,4) NOT NULL COMMENT '借方余额',
	CR_BAL decimal(18,4) NOT NULL COMMENT '贷方余额',
	DB_AMT decimal(18,4) NOT NULL COMMENT '借方发生额',
	CR_AMT decimal(18,4) NOT NULL COMMENT '贷方发生额',
	DB_COUNT int NOT NULL COMMENT '借方发生笔数',
	CR_COUNT int NOT NULL COMMENT '贷方发生笔数',
	LAST_DB_BAL decimal(18,4) NOT NULL COMMENT '昨日借记余额',
	LAST_CR_BAL decimal(18,4) NOT NULL COMMENT '昨日贷记余额',
	MTD_DB_AMT decimal(18,4) NOT NULL COMMENT '本月借方发生额',
	MTD_CR_AMT decimal(18,4) NOT NULL COMMENT '本月贷方发生额',
	LAST_MTH_DB_BAL decimal(18,4) NOT NULL COMMENT '上月借记余额',
	LAST_MTH_CR_BAL decimal(18,4) NOT NULL COMMENT '上月贷记余额',
	QTD_DB_AMT decimal(18,4) NOT NULL COMMENT '本季度借记发生额',
	QTD_CR_AMT decimal(18,4) NOT NULL COMMENT '本季度贷记发生额',
	LAST_QTR_DB_BAL decimal(18,4) NOT NULL COMMENT '上季度借记余额',
	LAST_QTR_CR_BAL decimal(18,4) NOT NULL COMMENT '上季度贷记余额',
	YTD_DB_AMT decimal(18,4) NOT NULL COMMENT '本年借记发生额',
	YTD_CR_AMT decimal(18,4) NOT NULL COMMENT '本年贷记发生额',
	LAST_YR_DB_BAL decimal(18,4) NOT NULL COMMENT '上年借记余额',
	LAST_YR_CR_BAL decimal(18,4) NOT NULL COMMENT '上年贷记余额',
	PROC_DATE date NOT NULL COMMENT '处理日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '当日辅助核算汇总表';


-- 辅助核算汇总历史表
CREATE TABLE AP_GL_VOL_DTL_ASS_SUM_HST
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	ASS_SUM_SEQ varchar(64) NOT NULL COMMENT '辅助核算汇总流水号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType
	ASSIST_TYPE varchar(10) NOT NULL COMMENT '辅助核算类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.AssistAccountingType',
	ASSIST_ACCOUNT_VALUE varchar(20) NOT NULL COMMENT '辅助核算项值',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	VOL_DT date NOT NULL COMMENT '记账日期',
	DB_BAL decimal(18,4) NOT NULL COMMENT '借方余额',
	CR_BAL decimal(18,4) NOT NULL COMMENT '贷方余额',
	DB_AMT decimal(18,4) NOT NULL COMMENT '借方发生额',
	CR_AMT decimal(18,4) NOT NULL COMMENT '贷方发生额',
	DB_COUNT int NOT NULL COMMENT '借方发生笔数',
	CR_COUNT int NOT NULL COMMENT '贷方发生笔数',
	LAST_DB_BAL decimal(18,4) NOT NULL COMMENT '昨日借记余额',
	LAST_CR_BAL decimal(18,4) NOT NULL COMMENT '昨日贷记余额',
	MTD_DB_AMT decimal(18,4) NOT NULL COMMENT '本月借方发生额',
	MTD_CR_AMT decimal(18,4) NOT NULL COMMENT '本月贷方发生额',
	LAST_MTH_DB_BAL decimal(18,4) NOT NULL COMMENT '上月借记余额',
	LAST_MTH_CR_BAL decimal(18,4) NOT NULL COMMENT '上月贷记余额',
	QTD_DB_AMT decimal(18,4) NOT NULL COMMENT '本季度借记发生额',
	QTD_CR_AMT decimal(18,4) NOT NULL COMMENT '本季度贷记发生额',
	LAST_QTR_DB_BAL decimal(18,4) NOT NULL COMMENT '上季度借记余额',
	LAST_QTR_CR_BAL decimal(18,4) NOT NULL COMMENT '上季度贷记余额',
	YTD_DB_AMT decimal(18,4) NOT NULL COMMENT '本年借记发生额',
	YTD_CR_AMT decimal(18,4) NOT NULL COMMENT '本年贷记发生额',
	LAST_YR_DB_BAL decimal(18,4) NOT NULL COMMENT '上年借记余额',
	LAST_YR_CR_BAL decimal(18,4) NOT NULL COMMENT '上年贷记余额',
	PROC_DATE date NOT NULL COMMENT '处理日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '辅助核算汇总历史表';


-- 会计分录拆分交易流水历史表
CREATE TABLE AP_GL_VOL_DTL_HST
(
	GLV_SEQ varchar(64) NOT NULL COMMENT '分录流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	VOL_DT date COMMENT '记账日期',
	BRANCH varchar(40) COMMENT '记账机构',
	TXN_BRCD varchar(40) COMMENT '交易机构',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PostGlInd
	POST_GL_IND varchar(12) COMMENT '总账入账方式 : ///
@net.engining.pcx.cc.param.model.enums.PostGlInd',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	CURR_CD varchar(3) COMMENT '币种',
	DBSUBJECT_CD varchar(40) COMMENT '借方科目号',
	CRSUBJECT_CD varchar(40) COMMENT '贷方科目号',
	SUBJ_AMOUNT decimal(17,4) COMMENT '金额',
	VOL_SEQ int COMMENT '分录序号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	VOL_DESC varchar(80) COMMENT '分录摘要',
	REF_NO varchar(9) COMMENT '关联参考号',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	TRANS_DATE date COMMENT '交易日期',
	ASSIST_ACCOUNT_DATA mediumtext COMMENT '辅助核算项数据',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (GLV_SEQ)
) COMMENT = '会计分录拆分交易流水历史表';


-- 当日科目分录入账汇总表
CREATE TABLE AP_SUBJECT_SUMMARY
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	SUBJECT_CD varchar(40) COMMENT '科目号',
	OWING_BRANCH varchar(9) COMMENT '所属分支行',
	DB_AMT decimal(18,2) COMMENT '借方发生额',
	CR_AMT decimal(18,2) COMMENT '贷方发生额',
	DB_BAL decimal(18,2) COMMENT '借方余额',
	CR_BAL decimal(18,2) COMMENT '贷方余额',
	DB_COUNT int COMMENT '借方发生笔数',
	CR_COUNT int COMMENT '贷方发生笔数',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '当日科目分录入账汇总表';


-- 科目分录入账汇总历史表
CREATE TABLE AP_SUBJECT_SUMMARY_HST
(
	SEQ int NOT NULL COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	SUBJECT_CD varchar(40) COMMENT '科目号',
	OWING_BRANCH varchar(9) COMMENT '所属分支行',
	DB_AMT decimal(18,2) COMMENT '借方发生额',
	CR_AMT decimal(18,2) COMMENT '贷方发生额',
	DB_BAL decimal(18,2) COMMENT '借方余额',
	CR_BAL decimal(18,2) COMMENT '贷方余额',
	DB_COUNT int COMMENT '借方发生笔数',
	CR_COUNT int NOT NULL COMMENT '贷方发生笔数',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '科目分录入账汇总历史表';


-- 调整会计科目操作历史
CREATE TABLE CACT_ADJ_LEDGER_ACCT_OPR_HST
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	CURR_CD varchar(3) NOT NULL COMMENT '币种',
	TXN_DESC varchar(400) COMMENT '交易描述',
	-- ///
	-- A|打印凭证
	-- B|其他
	EVENT_ID char(1) COMMENT '事件号 : ///
A|打印凭证
B|其他',
	OPERA_ID varchar(40) NOT NULL COMMENT '操作员ID',
	OPER_DATE date NOT NULL COMMENT '操作日期',
	CHECK_DATE date NOT NULL COMMENT '复核日期',
	CHECKER_ID varchar(40) COMMENT '复核员ID',
	DBSUBJECT_CD varchar(40) COMMENT '借方科目号',
	CRSUBJECT_CD varchar(40) COMMENT '贷方科目号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) NOT NULL COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ)
) COMMENT = '调整会计科目操作历史';


-- 待送总账(用友)交易表(废弃) : 逻辑卡/介质对照表，只用于批量接口程序查介质卡号确定逻辑卡号。
-- 每天批量开始dro
CREATE TABLE CACT_TXN_GL
(
	TXN_SEQ int NOT NULL COMMENT '交易流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_NO int NOT NULL COMMENT '账号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD varchar(3) NOT NULL COMMENT '币种',
	-- 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要
	AGE_CD char(1) NOT NULL COMMENT '账龄 : 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	POST_DATE date NOT NULL COMMENT '入账日期',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	OWNING_BRANCH varchar(9) NOT NULL COMMENT '发卡网点',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PostGlInd
	POST_GL_IND varchar(12) COMMENT '总账入账方式 : ///
@net.engining.pcx.cc.param.model.enums.PostGlInd',
	POST_CODE varchar(20) COMMENT '入账代码',
	PRIMARY KEY (TXN_SEQ),
	UNIQUE (TXN_SEQ)
) COMMENT = '待送总账(用友)交易表(废弃) : 逻辑卡/介质对照表，只用于批量接口程序查介质卡号确定逻辑卡号。
每天批量开始dro';


-- 总账交易操作历史表
CREATE TABLE GL_TRANS_OPR_HST
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	TXN_AMT decimal(18,2) COMMENT '交易金额',
	POST_DESC varchar(80) COMMENT '入账描述',
	CURR_CD varchar(3) COMMENT '币种',
	ACCOUNT_DESC varchar(80) COMMENT '记账说明',
	-- ///
	-- A|打印凭证
	-- B|其他
	EVENT_ID char(1) COMMENT '事件号 : ///
A|打印凭证
B|其他',
	OPERA_ID varchar(40) COMMENT '操作员ID',
	OPER_DATE date COMMENT '操作日期',
	CHECK_DATE date COMMENT '复核日期',
	CHECKER_ID varchar(40) COMMENT '复核员ID',
	-- ///
	-- A|已复核
	-- B|未复核
	-- C|拒绝
	CHECK_FLAG char(1) COMMENT '复核标志 : ///
A|已复核
B|未复核
C|拒绝',
	REFUSE_REASON varchar(80) COMMENT '拒绝原因',
	PRINT_VOUCHER_COUNT int COMMENT '打印凭证次数',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG char(1) COMMENT '表内表外标志 : ///
A|表内
B|表外',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	PRIMARY KEY (SEQ)
) COMMENT = '总账交易操作历史表';



/* Create Indexes */

CREATE INDEX INX_TXN_DETAIL_SEQ ON AP_GL_TXN (TXN_DETAIL_SEQ ASC);
CREATE INDEX  INX_SUB_ASS_VALUE ON AP_GL_VOL_DTL_ASS_SUM_HST (SUBJECT_CD DESC, ASSIST_TYPE DESC, ASSIST_ACCOUNT_VALUE DESC);



