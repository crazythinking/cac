SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS AP_INTERNAL_GLTXN;
DROP TABLE IF EXISTS AP_INTERNAL_SUBJECT_SUM;
DROP TABLE IF EXISTS AP_INTERNAL_SUBJECT_SUM_HST;
DROP TABLE IF EXISTS CACT_ADJ_INTR_ACCT_OPR_HST;
DROP TABLE IF EXISTS CACT_INTERNAL_ACCT;
DROP TABLE IF EXISTS CACT_INTERNAL_ACCT_HST;
DROP TABLE IF EXISTS CACT_INTERNAL_TXN_POST_HST;
DROP TABLE IF EXISTS CACT_INTRNL_TXN_POST_BT;
DROP TABLE IF EXISTS CACT_INTRNL_TXN_POST_OL;
DROP TABLE IF EXISTS CACT_INTRNL_TXN_POST_SUM;
DROP TABLE IF EXISTS CACT_INTRNL_TXN_POST_SUM_HST;




/* Create Tables */

-- 内部户产生会计分录待入总账流水
CREATE TABLE AP_INTERNAL_GLTXN
(
	SEQ_ID int NOT NULL AUTO_INCREMENT COMMENT '序列号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	POST_AMOUNT decimal(15,2) COMMENT '入账金额',
	CURR_CD varchar(3) COMMENT '币种',
	DBSUBJECT_CD varchar(40) COMMENT '借方科目号',
	CRSUBJECT_CD varchar(40) COMMENT '贷方科目号',
	TXN_DESC varchar(400) COMMENT '交易描述',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	TXN_DATE date COMMENT '交易日期',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ_ID)
) COMMENT = '内部户产生会计分录待入总账流水';


-- 内部帐对总账科目分录入账汇总表
CREATE TABLE AP_INTERNAL_SUBJECT_SUM
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
) COMMENT = '内部帐对总账科目分录入账汇总表';


-- 内部帐对总账科目分录入账汇总历史表
CREATE TABLE AP_INTERNAL_SUBJECT_SUM_HST
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
	CR_COUNT int COMMENT '贷方发生笔数',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '内部帐对总账科目分录入账汇总历史表';


-- 调整内部账户余额操作历史
CREATE TABLE CACT_ADJ_INTR_ACCT_OPR_HST
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	SUBJECT_CD varchar(40) NOT NULL COMMENT '科目号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	INTERNAL_ACCT_ID varchar(30) COMMENT '内部账户号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) NOT NULL COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	CURR_CD varchar(3) COMMENT '币种',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	TXN_DESC varchar(400) COMMENT '交易描述',
	EVENT_ID int NOT NULL COMMENT '事件号',
	OPERA_ID varchar(40) NOT NULL COMMENT '操作员ID',
	OPER_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	CHECKER_ID varchar(40) COMMENT '复核员ID',
	PROC_DATE date NOT NULL COMMENT '处理日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ)
) COMMENT = '调整内部账户余额操作历史';


-- 内部账户表
CREATE TABLE CACT_INTERNAL_ACCT
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) COMMENT '内部账户号',
	INTERNAL_ACCT_NAME varchar(100) COMMENT '内部户名称',
	SUBJECT_CD varchar(40) COMMENT '科目号',
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
	LAST_DB_BAL decimal(18,2) COMMENT '昨日借记余额',
	LAST_CR_BAL decimal(18,2) COMMENT '昨日贷记余额',
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
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '内部账户表';


-- 内部账户历史表
CREATE TABLE CACT_INTERNAL_ACCT_HST
(
	SEQ int NOT NULL COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) NOT NULL COMMENT '内部账户号',
	INTERNAL_ACCT_NAME varchar(100) COMMENT '内部户名称',
	SUBJECT_CD varchar(40) COMMENT '科目号',
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
	LAST_DB_BAL decimal(18,2) COMMENT '昨日借记余额',
	LAST_CR_BAL decimal(18,2) COMMENT '昨日贷记余额',
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
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '内部账户历史表';


-- 内部账户入账历史表
CREATE TABLE CACT_INTERNAL_TXN_POST_HST
(
	TXN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '交易流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) NOT NULL COMMENT '内部账户号',
	INTERNAL_ACCT_POST_CODE varchar(20) COMMENT '内部账户入账代码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	-- 清算货币码
	POST_CURR_CD char(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	POST_DATE date NOT NULL COMMENT '入账日期',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct
	POSTING_FLAG char(3) NOT NULL COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) NOT NULL COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	TXN_POST_SEQ int NOT NULL COMMENT '内部账户入账流水表序号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_POST_TYPE char(1) NOT NULL COMMENT '内部账户入账流水表类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ)
) COMMENT = '内部账户入账历史表';


-- 内部账入账交易流水表（批量时使用）
CREATE TABLE CACT_INTRNL_TXN_POST_BT
(
	TXN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '交易流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) COMMENT '内部账户号',
	INTERNAL_ACCT_POST_CODE varchar(20) NOT NULL COMMENT '内部账户入账代码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	-- 清算货币码
	POST_CURR_CD char(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	POST_DATE date NOT NULL COMMENT '入账日期',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct
	POSTING_FLAG char(3) NOT NULL COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) NOT NULL COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ)
) COMMENT = '内部账入账交易流水表（批量时使用）';


-- 内部账入账交易流水表（联机时使用）
CREATE TABLE CACT_INTRNL_TXN_POST_OL
(
	TXN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '交易流水号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) COMMENT '内部账户号',
	INTERNAL_ACCT_POST_CODE varchar(20) COMMENT '内部账户入账代码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	-- 清算货币码
	POST_CURR_CD char(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	POST_DATE date NOT NULL COMMENT '入账日期',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct
	POSTING_FLAG char(3) NOT NULL COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.RedBlueInd
	RED_BLUE_IND char(1) NOT NULL COMMENT '红蓝字标识 : ///
@net.engining.pcx.cc.param.model.enums.RedBlueInd',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ)
) COMMENT = '内部账入账交易流水表（联机时使用）';


-- 内部账户入账汇总表
CREATE TABLE CACT_INTRNL_TXN_POST_SUM
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) NOT NULL COMMENT '内部账户号',
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
) COMMENT = '内部账户入账汇总表';


-- 内部账户入账汇总历史表
CREATE TABLE CACT_INTRNL_TXN_POST_SUM_HST
(
	SEQ int NOT NULL COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INTERNAL_ACCT_ID varchar(30) COMMENT '内部账户号',
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
) COMMENT = '内部账户入账汇总历史表';



