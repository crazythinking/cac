SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS CRF_REPORT;
DROP TABLE IF EXISTS POLLING_TASK;
DROP TABLE IF EXISTS REPT_OVERDUE_INFO;
DROP TABLE IF EXISTS REPT_SUMMARY_LOAN_INFO;
DROP TABLE IF EXISTS RPT_NEW_ACCT_DAILY;
DROP TABLE IF EXISTS RPT_POSTING_DAILY;
DROP TABLE IF EXISTS TRANS_REPORT;




/* Create Tables */

-- CRF报表
CREATE TABLE CRF_REPORT
(
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	CRF_ID varchar(64) NOT NULL COMMENT '序号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	REPORT_DATE date COMMENT '报表日期',
	CLEAR_DATE date COMMENT '清算日期',
	ACCOUNTING_DATE date COMMENT '记账日期',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG varchar(1) COMMENT '表内外标志 : ///
A|表内
B|表外',
	SUBJECT_CD varchar(40) COMMENT '科目号',
	SUBJECT_NAME varchar(100) COMMENT '科目名称',
	PRIOR_PERIOD decimal(18,2) COMMENT '上期余额',
	CURRENT_BALANCE decimal(18,2) COMMENT '当前余额',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	TXN_DIRECTION char(1) COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection',
	AMOUNT_DEBIT_SIDE decimal(18,2) COMMENT '借方金额',
	AMOUNT_CREDIT_SIDE decimal(18,2) COMMENT '贷方金额',
	ASSIST_ACCOUNT mediumtext COMMENT '辅助核算项',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CRF_ID)
) COMMENT = 'CRF报表';


-- 报表查询、轮循任务表
CREATE TABLE POLLING_TASK
(
	ID int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	START_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	QUERY_CONDITION mediumtext NOT NULL COMMENT '查询条件',
	-- ///
	-- P|待处理
	-- B|处理中
	-- F|处理完成
	PROCESSING_STRUTS varchar(1) NOT NULL COMMENT '处理状态 : ///
P|待处理
B|处理中
F|处理完成',
	-- ///
	-- T|TRANS报表
	-- C|CRF报表
	REPORT_TYPE varchar(1) NOT NULL COMMENT '报表类型 : ///
T|TRANS报表
C|CRF报表',
	FILE_NAME varchar(50) COMMENT '文件名',
	RECORD_LOCATION varchar(50) COMMENT '文件存储位置',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ID)
) COMMENT = '报表查询、轮循任务表';


-- 每日贷款信息汇总
CREATE TABLE REPT_OVERDUE_INFO
(
	ID_SEQ int NOT NULL AUTO_INCREMENT COMMENT 'ID_SEQ',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	CUST_ID varchar(64) COMMENT '客户编号',
	ACCT_SEQ int COMMENT '账户编号',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PaymentMethod
	PAYMENT_METHOD char(3) COMMENT '还款方式 : ///
@net.engining.pcx.cc.param.model.enums.PaymentMethod',
	-- 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要
	AGE_CD char(1) COMMENT '账龄 : 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要',
	FIRST_OVERDUE_DATE date COMMENT '首次逾期日期',
	-- -1表示无穷大。
	CURRENT_LOAN_PERIOD int COMMENT '当前贷款期数 : -1表示无穷大。',
	-- -1表示无穷大。
	TOTAL_LOAN_PERIOD int COMMENT '总贷款期数 : -1表示无穷大。',
	-- 贷款总金额，只有当前贷款期数=0的时候才更新。
	TOTAL_LOAN_PRINCIPAL_AMT decimal(18,2) COMMENT '贷款总金额 : 贷款总金额，只有当前贷款期数=0的时候才更新。',
	-- 持卡人还清该欠款，则会对账户进行全额还款免息，该金额会显示在账单上，小于等于账户账单余额
	QUAL_GRACE_BAL decimal(18,2) COMMENT '全部应还款额 : 持卡人还清该欠款，则会对账户进行全额还款免息，该金额会显示在账单上，小于等于账户账单余额',
	-- 到期还款日余额
	PMT_DUE_DAY_BAL decimal(18,2) COMMENT '到期还款日余额 : 到期还款日余额',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ID_SEQ)
) COMMENT = '每日贷款信息汇总';


-- 每日贷款信息汇总报表
CREATE TABLE REPT_SUMMARY_LOAN_INFO
(
	ID_SEQ int NOT NULL AUTO_INCREMENT COMMENT 'ID_SEQ',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	TOP1 int COMMENT '等额本金笔数',
	TOP2 int COMMENT '一次还本付息笔数',
	TOP3 int COMMENT '分次付息一次还本笔数',
	TOP4 int COMMENT '等额本息笔数',
	TOP5 int COMMENT '利随本清笔数',
	TOP6 int COMMENT '1-6期笔数',
	TOP7 int COMMENT '7-12期笔数',
	TOP8 int COMMENT '13-24期笔数',
	TOP9 int COMMENT '24期以上笔数',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ID_SEQ)
) COMMENT = '每日贷款信息汇总报表';


-- 每日新户报表
CREATE TABLE RPT_NEW_ACCT_DAILY
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE char(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	-- -1表示无穷大。
	TOTAL_LOAN_PERIOD int COMMENT '总贷款期数 : -1表示无穷大。',
	-- 当前欠款（负值表示有溢缴款）
	CURR_BAL decimal(18,2) NOT NULL COMMENT '当前余额 : 当前欠款（负值表示有溢缴款）',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PaymentMethod
	PAYMENT_METHOD char(3) COMMENT '还款方式 : ///
@net.engining.pcx.cc.param.model.enums.PaymentMethod',
	-- 首个账单日
	FIRST_STMT_DATE date COMMENT '首个账单日期 : 首个账单日',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ)
) COMMENT = '每日新户报表';


-- 每日入账明细报表
CREATE TABLE RPT_POSTING_DAILY
(
	SEQ int NOT NULL COMMENT '序号',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	POST_CODE char(8) NOT NULL COMMENT '入账交易码',
	TXN_DESC varchar(400) COMMENT '交易描述',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	-- 清算货币码
	POST_CURR_CD char(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	ACCT_CURR_BAL decimal(18,2) COMMENT '账户当前余额',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ)
) COMMENT = '每日入账明细报表';


-- TRANS报表
CREATE TABLE TRANS_REPORT
(
	-- ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###
	TRANS_ID varchar(64) NOT NULL COMMENT '序号 : ###net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator###',
	ORG varchar(12) NOT NULL COMMENT '机构号',
	BRANCH_NO varchar(9) NOT NULL COMMENT '分行号',
	-- ///
	-- A|表内
	-- B|表外
	IN_OUT_FLAG varchar(1) COMMENT '表内外标志 : ///
A|表内
B|表外',
	TRADE_TYPE varchar(20) COMMENT '交易类型',
	TRANSACTION_SERIAL_NUMBER varchar(64) COMMENT '交易流水号',
	ACCOUNT_NUMBER varchar(64) COMMENT '记账流水号',
	EXTERNAL_FLOW_NUMBER varchar(64) COMMENT '外部流水号',
	IOUS_NUMBER varchar(20) COMMENT '借据号',
	ACCOUNTING_DATE date COMMENT '记账日期',
	REPORT_DATE date COMMENT '报表日期',
	CLEAR_DATE date COMMENT '清算日期',
	TRADE_DATE date COMMENT '交易日期',
	ACCOUNT_ABSTRACT varchar(80) COMMENT '记账摘要',
	SUBJECT_CD varchar(40) COMMENT '科目号',
	SUBJECT_NAME varchar(100) COMMENT '科目名称',
	AMOUNT_DEBIT_SIDE decimal(18,2) COMMENT '借方金额',
	AMOUNT_CREDIT_SIDE decimal(18,2) COMMENT '贷方金额',
	ASSIST_ACCOUNT mediumtext COMMENT '辅助核算项',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TRANS_ID)
) COMMENT = 'TRANS报表';



