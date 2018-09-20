SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Indexes */

DROP INDEX INX_IOU_NO ON CACT_ACCOUNT_ADDI;
DROP INDEX INX_CUST_ID ON CACT_ACCOUNT_NO;



/* Drop Tables */

DROP TABLE IF EXISTS CACT_ACCOUNT_ADDI;
DROP TABLE IF EXISTS CACT_ADJ_CUST_ACCT_OPR_HST;
DROP TABLE IF EXISTS CACT_AGE_DUE;
DROP TABLE IF EXISTS CACT_END_CHANGE_ACCT;
DROP TABLE IF EXISTS CACT_LOAN_PAYMENT_DETAIL;
DROP TABLE IF EXISTS CACT_LOAN_PAYMENT_PLAN;
DROP TABLE IF EXISTS CACT_TXN_POST;
DROP TABLE IF EXISTS CACT_SUB_ACCT;
DROP TABLE IF EXISTS CACT_ACCOUNT;
DROP TABLE IF EXISTS CACT_ACCOUNT_NO;
DROP TABLE IF EXISTS CACT_ACCOUNT_TEM;
DROP TABLE IF EXISTS CACT_CANCEL_REG;
DROP TABLE IF EXISTS CACT_OPER_AUTH;
DROP TABLE IF EXISTS CACT_SEQ_ERROR;
DROP TABLE IF EXISTS CACT_TXN_HST;
DROP TABLE IF EXISTS CACT_TXN_REJECT;




/* Create Tables */

-- 账户信息表
CREATE TABLE CACT_ACCOUNT
(
	ACCT_SEQ int NOT NULL AUTO_INCREMENT COMMENT '账户编号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_NO int COMMENT '账号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD varchar(3) NOT NULL COMMENT '币种',
	ACCT_LIMIT decimal(18,2) COMMENT '账户额度',
	-- 当前欠款（负值表示有溢缴款）
	CURR_BAL decimal(18,2) NOT NULL COMMENT '当前余额 : 当前欠款（负值表示有溢缴款）',
	BEGIN_BAL decimal(18,2) NOT NULL COMMENT '期初余额',
	-- 到期还款日余额
	PMT_DUE_DAY_BAL decimal(18,2) NOT NULL COMMENT '到期还款日余额 : 到期还款日余额',
	-- 持卡人还清该欠款，则会对账户进行全额还款免息，该金额会显示在账单上，小于等于账户账单余额
	QUAL_GRACE_BAL decimal(18,2) NOT NULL COMMENT '全部应还款额 : 持卡人还清该欠款，则会对账户进行全额还款免息，该金额会显示在账单上，小于等于账户账单余额',
	-- !!!java.lang.Boolean!!!
	GRACE_DAYS_FULL_IND varchar(5) NOT NULL COMMENT '是否已全额还款 : !!!java.lang.Boolean!!!',
	-- 用户设定的循环信用账户的结息日。
	BILLING_CYCLE varchar(2) NOT NULL COMMENT '账单周期 : 用户设定的循环信用账户的结息日。',
	BLOCK_CODE varchar(27) COMMENT '锁定码',
	-- 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要
	AGE_CD char(1) COMMENT '账龄 : 实体账单不需要显示此域（但可以用作账单拖欠信息提醒）， 联机账单需要',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.DDType
	DD_IND char(1) COMMENT '约定还款类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.DDType',
	-- 约定还款扣款账户银行名称
	DD_BANK_NAME varchar(80) COMMENT '约定还款银行名称 : 约定还款扣款账户银行名称',
	DD_BANK_BRANCH varchar(9) COMMENT '约定还款开户行号',
	-- 约定还款扣款账号
	DD_BANK_ACCT_NO varchar(40) COMMENT '约定还款扣款账号 : 约定还款扣款账号',
	-- 约定还款扣款账户姓名
	DD_BANK_ACCT_NAME varchar(80) COMMENT '约定还款扣款账户姓名 : 约定还款扣款账户姓名',
	-- 上期约定还款金额
	LAST_DD_AMT decimal(18,2) COMMENT '上期约定还款金额 : 上期约定还款金额',
	-- 上期约定还款日期
	LAST_DD_DATE date COMMENT '上期约定还款日期 : 上期约定还款日期',
	-- 上笔还款金额
	LAST_PMT_AMT decimal(18,2) COMMENT '上笔还款金额 : 上笔还款金额',
	LAST_PMT_DATE date COMMENT '上一还款日期',
	-- 上个到期还款日
	LAST_PMT_DUE_DATE date COMMENT '上个到期还款日期 : 上个到期还款日',
	-- 上次账龄提升日，可以用来代替DELQ DAYS的计算
	LAST_AGING_DATE date COMMENT '上个账龄提升日期 : 上次账龄提升日，可以用来代替DELQ DAYS的计算',
	PMT_DUE_DATE date COMMENT '到期还款日期',
	-- 约定还款扣款文件生成日期
	DD_DATE date COMMENT '约定还款日期 : 约定还款扣款文件生成日期',
	-- 宽限日
	GRACE_DATE date COMMENT '宽限日期 : 宽限日',
	-- 最终销户日期，指账户closed
	CLOSED_DATE date COMMENT '最终销户日期 : 最终销户日期，指账户closed',
	-- 首个账单日
	FIRST_STMT_DATE date COMMENT '首个账单日期 : 首个账单日',
	-- 卡片账户置上待销卡销户锁定码的日期
	-- 在销卡时置上销卡日期，在销卡撤销时清零销卡日期
	CANCEL_DATE date COMMENT '销卡销户日期 : 卡片账户置上待销卡销户锁定码的日期
在销卡时置上销卡日期，在销卡撤销时清零销卡日期',
	-- 转呆账日期
	CHARGE_OFF_DATE date COMMENT '转呆账日期 : 转呆账日期',
	TOT_DUE_AMT decimal(18,2) NOT NULL COMMENT '最小还款额',
	-- 每个月一个值,循环存放
	-- 取值为net.engining.pcx.cc.infrastructure.shared.enums.PaymentStatus
	PAYMENT_HIST varchar(24) NOT NULL COMMENT '还款历史信息 : 每个月一个值,循环存放
取值为net.engining.pcx.cc.infrastructure.shared.enums.PaymentStatus',
	-- !!!java.lang.Boolean!!!
	WAIVE_LATEFEE_IND varchar(5) NOT NULL COMMENT '是否免除滞纳金 : !!!java.lang.Boolean!!!',
	CUST_ID varchar(64) NOT NULL COMMENT '客户编号',
	OWNING_BRANCH varchar(9) NOT NULL COMMENT '发卡网点',
	-- -1表示无穷大。
	CURRENT_LOAN_PERIOD int NOT NULL COMMENT '当前贷款期数 : -1表示无穷大。',
	-- -1表示无穷大。
	TOTAL_LOAN_PERIOD int NOT NULL COMMENT '总贷款期数 : -1表示无穷大。',
	FIRST_OVERDUE_DATE date COMMENT '首次逾期日期',
	ACCT_PARAM_ID varchar(30) NOT NULL COMMENT '账户参数代码',
	INTEREST_DATE date COMMENT '结息日期',
	LAST_INTEREST_DATE date COMMENT '上次结息日期',
	-- 本账期存款(还款)金额
	CTD_PAYMENT_AMT decimal(18,2) NOT NULL COMMENT '当期存款(还款)金额 : 本账期存款(还款)金额',
	-- 本账期贷记调整金额
	CTD_CR_ADJ_AMT decimal(18,2) NOT NULL COMMENT '当期贷记调整金额 : 本账期贷记调整金额',
	-- 本账期退货金额
	CTD_REFUND_AMT decimal(18,2) NOT NULL COMMENT '当期退货金额 : 本账期退货金额',
	-- 贷款总金额，只有当前贷款期数=0的时候才更新。
	TOTAL_LOAN_PRINCIPAL_AMT decimal(18,2) COMMENT '贷款总金额 : 贷款总金额，只有当前贷款期数=0的时候才更新。',
	-- 在系统内，贷款账户自动扣款的活期账户，优先级最高。
	AUTO_PAY_ACCT_SEQ_IN_SYSTEM int COMMENT '系统内自动扣款账号 : 在系统内，贷款账户自动扣款的活期账户，优先级最高。',
	START_DATE date COMMENT '账户创建自然日',
	SETUP_DATE date NOT NULL COMMENT '建账业务日',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ACCT_SEQ)
) COMMENT = '账户信息表';


-- 账户附加信息表 : 用于产生唯一账号
CREATE TABLE CACT_ACCOUNT_ADDI
(
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	IOU_NO varchar(36) COMMENT '借据号',
	PRODUCT_NO varchar(64) COMMENT '产品标识号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ACCT_SEQ)
) COMMENT = '账户附加信息表 : 用于产生唯一账号';


-- 账号表 : 用于产生唯一账号
CREATE TABLE CACT_ACCOUNT_NO
(
	ACCT_NO int NOT NULL AUTO_INCREMENT COMMENT '账号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	CUST_ID varchar(64) COMMENT '客户编号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ACCT_NO)
) COMMENT = '账号表 : 用于产生唯一账号';


-- 账户临时表
CREATE TABLE CACT_ACCOUNT_TEM
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	DATA mediumtext COMMENT '存储数据',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '账户临时表';


-- 客户账调账历史
CREATE TABLE CACT_ADJ_CUST_ACCT_OPR_HST
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	CARD_NO varchar(19) COMMENT '卡号(E账号)',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	SUB_ACCT_ID int NOT NULL COMMENT '子账户序号',
	-- 对应参数子账户类型
	SUB_ACCT_TYPE varchar(6) COMMENT '子账户类型 : 对应参数子账户类型',
	POST_CODE varchar(8) COMMENT '入账交易码',
	TXN_AMT decimal(18,2) COMMENT '交易金额',
	OPERA_ID varchar(64) NOT NULL COMMENT '操作员号',
	CHECKER_ID varchar(40) COMMENT '复核员号',
	OPER_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	PROC_DATE date NOT NULL COMMENT '处理日期',
	TXN_DESC varchar(400) COMMENT '交易描述',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ)
) COMMENT = '客户账调账历史';


-- 账龄信息表
CREATE TABLE CACT_AGE_DUE
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	AGE_DUE_AMT decimal(18,2) NOT NULL COMMENT '账龄最小还款额',
	-- 宽限日
	GRACE_DATE date NOT NULL COMMENT '宽限日期 : 宽限日',
	PERIOD int NOT NULL COMMENT '期数',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ)
) COMMENT = '账龄信息表';


-- 销户(销卡)任务表
CREATE TABLE CACT_CANCEL_REG
(
	REQUEST_SEQ int NOT NULL AUTO_INCREMENT COMMENT '请求序列号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_NO int NOT NULL COMMENT '账号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD varchar(3) NOT NULL COMMENT '币种',
	CARD_NO varchar(19) NOT NULL COMMENT '卡号(E账号)',
	CARD_GROUP_ID varchar(19) NOT NULL COMMENT '卡群组号',
	-- ///
	-- A|销卡请求
	-- B|销卡撤销请求
	-- C|销户请求
	-- D|销户撤销请求
	REQUEST_TYPE char(1) NOT NULL COMMENT '请求类型 : ///
A|销卡请求
B|销卡撤销请求
C|销户请求
D|销户撤销请求',
	-- 请求日期时间
	REQUEST_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '请求日期时间 : 请求日期时间',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	PRIMARY KEY (REQUEST_SEQ)
) COMMENT = '销户(销卡)任务表';


-- 账户变更信息表
CREATE TABLE CACT_END_CHANGE_ACCT
(
	CHANGE_SEQ int NOT NULL AUTO_INCREMENT COMMENT '流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	SUB_ACCT_ID int NOT NULL COMMENT '子账户序号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CHANGE_SEQ)
) COMMENT = '账户变更信息表';


-- 静态还款计划表明细
CREATE TABLE CACT_LOAN_PAYMENT_DETAIL
(
	SEQ_ID int NOT NULL AUTO_INCREMENT COMMENT '编号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	PLAN_SEQ int NOT NULL COMMENT '还款计划序号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	LOAN_PERIOD int COMMENT '还款期数',
	-- 相对于系统BIZ_DATE产生的还款日，便于系统内按日期比较计算
	PAYMENT_DATE date COMMENT '还款业务日 : 相对于系统BIZ_DATE产生的还款日，便于系统内按日期比较计算',
	-- 对外显示的还款日期
	PAYMENT_NATURE_DATE date COMMENT '还款自然日 : 对外显示的还款日期',
	-- 当前本金余额
	PRINCIPAL_BAL decimal(18,2) COMMENT '本金余额 : 当前本金余额',
	INTEREST_AMT decimal(18,2) COMMENT '利息金额',
	FEE_AMT decimal(18,2) COMMENT '费用金额',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ_ID)
) COMMENT = '静态还款计划表明细';


-- 静态还款计划表
CREATE TABLE CACT_LOAN_PAYMENT_PLAN
(
	PLAN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '还款计划序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	ACCT_PARAM_ID varchar(30) COMMENT '账户参数代码',
	CUST_ID varchar(64) COMMENT '客户编号',
	POST_DATE date NOT NULL COMMENT '入账日期',
	-- -1表示无穷大。
	TOTAL_LOAN_PERIOD int NOT NULL COMMENT '总贷款期数 : -1表示无穷大。',
	LEFT_LOAN_PERIOD int NOT NULL COMMENT '剩余贷款期数',
	-- 贷款总金额，只有当前贷款期数=0的时候才更新。
	TOTAL_LOAN_PRINCIPAL_AMT decimal(18,2) NOT NULL COMMENT '贷款总金额 : 贷款总金额，只有当前贷款期数=0的时候才更新。',
	LEFT_LOAN_PRINCIPAL_AMT decimal(18,2) NOT NULL COMMENT '剩余贷款本金',
	-- ///
	-- @net.engining.pcx.cc.param.model.enums.PaymentMethod
	PAYMENT_METHOD char(3) NOT NULL COMMENT '还款方式 : ///
@net.engining.pcx.cc.param.model.enums.PaymentMethod',
	YEAR_RATE decimal(9,6) NOT NULL COMMENT '年利率',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	PRIMARY KEY (PLAN_SEQ)
) COMMENT = '静态还款计划表';


-- 操作员授权表
CREATE TABLE CACT_OPER_AUTH
(
	OPER_AUTH_SEQ int NOT NULL COMMENT '操作员授权编号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	OPERA_ID varchar(64) COMMENT '操作员号',
	ADJ_TXN_AMT_MAX decimal(18,2) NOT NULL COMMENT '账务调整金额上限',
	ADJ_POINT_MAX decimal(13) NOT NULL COMMENT '积分调整上限',
	ADJ_CREDIT_LIMIT_MAX decimal(18,2) NOT NULL COMMENT '信用额度调整上限',
	UPD_OPER_ID varchar(40) NOT NULL COMMENT '更新用户ID',
	OPER_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '操作时间',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (OPER_AUTH_SEQ)
) COMMENT = '操作员授权表';


-- 流水差错表
CREATE TABLE CACT_SEQ_ERROR
(
	TXN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '交易流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
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
	PRIMARY KEY (TXN_SEQ),
	UNIQUE (TXN_SEQ)
) COMMENT = '流水差错表';


-- 子账户(余额成分)表
CREATE TABLE CACT_SUB_ACCT
(
	SUB_ACCT_ID int NOT NULL AUTO_INCREMENT COMMENT '子账户序号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	-- 对应参数子账户类型
	SUB_ACCT_TYPE varchar(6) NOT NULL COMMENT '子账户类型 : 对应参数子账户类型',
	-- 子账户计价规则参数编号
	SUBACCT_PARAM_ID varchar(30) NOT NULL COMMENT '子账户参数序号 : 子账户计价规则参数编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CURR_CD varchar(3) NOT NULL COMMENT '币种',
	-- 当期为0，第一次入账单为1，以后每个账单日增加1
	STMT_HIST int NOT NULL COMMENT '账期 : 当期为0，第一次入账单为1，以后每个账单日增加1',
	BEGIN_BAL decimal(18,2) NOT NULL COMMENT '期初余额',
	-- 当前欠款（负值表示有溢缴款）
	CURR_BAL decimal(18,2) NOT NULL COMMENT '当前余额 : 当前欠款（负值表示有溢缴款）',
	TOT_DUE_AMT decimal(18,2) NOT NULL COMMENT '最小还款额',
	INT_PENDING decimal(18,6) NOT NULL COMMENT '未决利息',
	INT_RECEIVABLE decimal(18,6) NOT NULL COMMENT '应收利息',
	INTEREST_CODE varchar(20) COMMENT '利率表标识',
	LAST_COMPUTING_INTEREST_DATE date COMMENT '上次计息日期',
	LAST_ACCRUAL_INTEREST_DATE date COMMENT '上次利息计提日期',
	-- 出给会计总账的实际值是保留2位小数点，所以系统里以实际值保存，避免经多次累积后造成误差。
	INT_ACCRUAL decimal(18,2) NOT NULL COMMENT '利息计提 : 出给会计总账的实际值是保留2位小数点，所以系统里以实际值保存，避免经多次累积后造成误差。',
	PENALIZED_INTEREST_CODE varchar(20) COMMENT '罚息利率表标识',
	LAST_PENALIZED_INTEREST_DATE date COMMENT '上次罚息计息日期',
	END_DAY_BAL decimal(18,2) COMMENT '批量前余额',
	END_DAY_BEFORE_BAL decimal(18,2) COMMENT '日终余额',
	PENALIZED_AMT decimal(18,6) COMMENT '罚息余额',
	INT_PENALTY_ACCRUAL decimal(18,2) COMMENT '罚息计提',
	LAST_ACCRUALINTEPENALTY_DATE date COMMENT '上次罚息计提日期',
	ADDUP_AMT decimal(18,2) COMMENT '当日积数',
	SETUP_DATE date NOT NULL COMMENT '建账业务日',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SUB_ACCT_ID)
) COMMENT = '子账户(余额成分)表';


-- 入账交易历史表 : 交易流水历史 - 在入账的同时写该表
CREATE TABLE CACT_TXN_HST
(
	TXN_SEQ int NOT NULL COMMENT '交易流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int COMMENT '账户编号',
	SUB_ACCT_ID int COMMENT '子账户序号',
	-- 子账户计价规则参数编号
	SUBACCT_PARAM_ID varchar(30) COMMENT '子账户参数序号 : 子账户计价规则参数编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) NOT NULL COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CARD_NO varchar(19) COMMENT '卡号(E账号)',
	CARD_GROUP_ID varchar(19) COMMENT '卡群组号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	TXN_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '交易时间',
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
	POST_CODE varchar(8) NOT NULL COMMENT '入账交易码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	POST_DATE date NOT NULL COMMENT '入账日期',
	AUTH_CODE varchar(6) COMMENT '授权码',
	TXN_CURR_CD varchar(3) NOT NULL COMMENT '交易币种代码',
	-- 清算货币码
	POST_CURR_CD varchar(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	REF_NBR varchar(23) COMMENT '交易参考号',
	TXN_DESC varchar(400) COMMENT '交易描述',
	-- 用于账单交易描述显示
	TXN_SHORT_DESC varchar(40) COMMENT '账单交易描述 : 用于账单交易描述显示',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	POSTING_FLAG varchar(3) NOT NULL COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	PRE_POSTING_FLAG varchar(3) COMMENT '往日入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- 受理分行代码
	ACQ_BRANCH_ID varchar(11) COMMENT '受理分行代码 : 受理分行代码',
	-- 受理机构终端标识码
	ACQ_TERMINAL_ID varchar(8) COMMENT '受理机构终端标识码 : 受理机构终端标识码',
	-- 受理机构标识码
	ACQ_ACCEPTOR_ID varchar(15) COMMENT '受卡方标识码 : 受理机构标识码',
	-- 受卡点（商户）名称，地址
	MERCHANT_NAME_ADDR varchar(40) COMMENT '商户名称地址 : 受卡点（商户）名称，地址',
	MCC varchar(4) COMMENT '商户类别代码',
	STMT_DATE date COMMENT '账单日期',
	-- 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_B4_POSTING char(1) COMMENT '入账前账龄 : 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	-- 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_AFTER_POSTING char(1) COMMENT '入账后账龄 : 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	ACCT_CURR_BAL decimal(18,2) COMMENT '账户当前余额',
	-- 对应参数子账户类型
	SUB_ACCT_TYPE varchar(6) COMMENT '子账户类型 : 对应参数子账户类型',
	-- ///
	-- S|短期
	-- L|中长期
	TERMS_TYPE char(1) COMMENT '期限类型 : ///
S|短期
L|中长期',
	-- ///
	-- LoanSv|贷款手续费
	-- Overdue|滞纳金
	FEE_TYPE varchar(10) COMMENT '费种 : ///
LoanSv|贷款手续费
Overdue|滞纳金',
	IS_CUN_LOAN boolean COMMENT '联合贷标志',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- 供查询交易使用。
	TXN_TYPE varchar(30) COMMENT '本地交易类型 : 供查询交易使用。',
	-- 供查询交易使用。
	OPP_ACCT varchar(80) COMMENT '交易对手方账号 : 供查询交易使用。',
	REQUEST_DATA mediumtext COMMENT '请求数据',
	SV_PR_ID varchar(64) COMMENT '服务提供系统标识',
	CHANNE_ID varchar(64) COMMENT '渠道ID',
	ASYN_IND varchar(64) COMMENT '异步接口标识',
	-- ///
	-- S|已入总账
	-- F|未入总账
	INTO_GL_STATUS varchar(1) COMMENT '入总账状态 : ///
S|已入总账
F|未入总账',
	-- ///
	-- UNCHK|未对账
	-- ADD|补记
	-- SUB|冲减
	-- RIGHT|正常
	CHECK_ACCOUNT_STATUS varchar(6) COMMENT '对账状态 : ///
UNCHK|未对账
ADD|补记
SUB|冲减
RIGHT|正常',
	CLEAR_DATE date COMMENT '清算日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ),
	UNIQUE (TXN_SEQ)
) COMMENT = '入账交易历史表 : 交易流水历史 - 在入账的同时写该表';


-- 当日入账交易表 : 当日入账交易表
CREATE TABLE CACT_TXN_POST
(
	TXN_SEQ int NOT NULL AUTO_INCREMENT COMMENT '交易流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int COMMENT '账户编号',
	SUB_ACCT_ID int COMMENT '子账户序号',
	-- 子账户计价规则参数编号
	SUBACCT_PARAM_ID varchar(30) COMMENT '子账户参数序号 : 子账户计价规则参数编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CARD_NO varchar(19) COMMENT '卡号(E账号)',
	CARD_GROUP_ID varchar(19) COMMENT '卡群组号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	TXN_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '交易时间',
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
	POST_CODE varchar(8) NOT NULL COMMENT '入账交易码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	POST_DATE date NOT NULL COMMENT '入账日期',
	AUTH_CODE varchar(6) COMMENT '授权码',
	TXN_CURR_CD varchar(3) NOT NULL COMMENT '交易币种代码',
	-- 清算货币码
	POST_CURR_CD varchar(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	REF_NBR varchar(23) COMMENT '交易参考号',
	TXN_DESC varchar(400) COMMENT '交易描述',
	-- 用于账单交易描述显示
	TXN_SHORT_DESC varchar(40) COMMENT '账单交易描述 : 用于账单交易描述显示',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	POSTING_FLAG varchar(3) NOT NULL COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	PRE_POSTING_FLAG varchar(3) COMMENT '往日入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- 受理分行代码
	ACQ_BRANCH_ID varchar(11) COMMENT '受理分行代码 : 受理分行代码',
	-- 受理机构终端标识码
	ACQ_TERMINAL_ID varchar(8) COMMENT '受理机构终端标识码 : 受理机构终端标识码',
	-- 受理机构标识码
	ACQ_ACCEPTOR_ID varchar(15) COMMENT '受卡方标识码 : 受理机构标识码',
	-- 受卡点（商户）名称，地址
	MERCHANT_NAME_ADDR varchar(40) COMMENT '商户名称地址 : 受卡点（商户）名称，地址',
	MCC varchar(4) COMMENT '商户类别代码',
	STMT_DATE date COMMENT '账单日期',
	-- 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_B4_POSTING char(1) COMMENT '入账前账龄 : 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	-- 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_AFTER_POSTING char(1) COMMENT '入账后账龄 : 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	ACCT_CURR_BAL decimal(18,2) COMMENT '账户当前余额',
	-- 对应参数子账户类型
	SUB_ACCT_TYPE varchar(6) COMMENT '子账户类型 : 对应参数子账户类型',
	-- ///
	-- S|短期
	-- L|中长期
	TERMS_TYPE char(1) COMMENT '期限类型 : ///
S|短期
L|中长期',
	-- ///
	-- LoanSv|贷款手续费
	-- Overdue|滞纳金
	FEE_TYPE varchar(10) COMMENT '费种 : ///
LoanSv|贷款手续费
Overdue|滞纳金',
	IS_CUN_LOAN boolean COMMENT '联合贷标志',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	-- 供查询交易使用。
	TXN_TYPE varchar(30) COMMENT '本地交易类型 : 供查询交易使用。',
	-- 供查询交易使用。
	OPP_ACCT varchar(80) COMMENT '交易对手方账号 : 供查询交易使用。',
	REQUEST_DATA mediumtext COMMENT '请求数据',
	SV_PR_ID varchar(64) COMMENT '服务提供系统标识',
	CHANNE_ID varchar(64) COMMENT '渠道ID',
	ASYN_IND varchar(64) COMMENT '异步接口标识',
	-- ///
	-- S|已入总账
	-- F|未入总账
	INTO_GL_STATUS varchar(1) COMMENT '入总账状态 : ///
S|已入总账
F|未入总账',
	-- ///
	-- UNCHK|未对账
	-- ADD|补记
	-- SUB|冲减
	-- RIGHT|正常
	CHECK_ACCOUNT_STATUS varchar(6) COMMENT '对账状态 : ///
UNCHK|未对账
ADD|补记
SUB|冲减
RIGHT|正常',
	CLEAR_DATE date COMMENT '清算日期',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ)
) COMMENT = '当日入账交易表 : 当日入账交易表';


-- 挂账交易历史表 : 挂账交易历史表，当日入账交易表中交易状态非正常的交易移入该历史表
CREATE TABLE CACT_TXN_REJECT
(
	TXN_SEQ int NOT NULL COMMENT '交易流水号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	ACCT_SEQ int NOT NULL COMMENT '账户编号',
	SUB_ACCT_ID int COMMENT '子账户序号',
	-- 子账户计价规则参数编号
	SUBACCT_PARAM_ID varchar(30) COMMENT '子账户参数序号 : 子账户计价规则参数编号',
	-- ///
	-- @net.engining.gm.infrastructure.enums.BusinessType
	BUSINESS_TYPE varchar(2) COMMENT '业务类型 : ///
@net.engining.gm.infrastructure.enums.BusinessType',
	CARD_NO varchar(19) COMMENT '卡号(E账号)',
	CARD_GROUP_ID varchar(19) COMMENT '卡群组号',
	-- 逻辑卡主卡卡号
	BSC_LOGICCARD_NO varchar(19) COMMENT '逻辑卡主卡卡号 : 逻辑卡主卡卡号',
	TXN_DATE date NOT NULL COMMENT '交易日期',
	TXN_TIME timestamp DEFAULT NOW() NOT NULL COMMENT '交易时间',
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
	POST_CODE varchar(8) NOT NULL COMMENT '入账交易码',
	-- ///
	-- @net.engining.gm.infrastructure.enums.TxnDirection
	-- 
	DB_CR_IND char(1) NOT NULL COMMENT '借贷标志 : ///
@net.engining.gm.infrastructure.enums.TxnDirection
',
	TXN_AMT decimal(18,2) NOT NULL COMMENT '交易金额',
	POST_AMT decimal(18,2) NOT NULL COMMENT '入账金额',
	POST_DATE date NOT NULL COMMENT '入账日期',
	AUTH_CODE varchar(6) COMMENT '授权码',
	TXN_CURR_CD varchar(3) NOT NULL COMMENT '交易币种代码',
	-- 清算货币码
	POST_CURR_CD varchar(3) NOT NULL COMMENT '入账币种代码 : 清算货币码',
	REF_NBR varchar(23) COMMENT '交易参考号',
	TXN_DESC varchar(400) COMMENT '交易描述',
	-- 用于账单交易描述显示
	TXN_SHORT_DESC varchar(40) COMMENT '账单交易描述 : 用于账单交易描述显示',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	POSTING_FLAG varchar(3) COMMENT '入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag
	PRE_POSTING_FLAG varchar(3) COMMENT '往日入账结果标示码 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag',
	-- 公司卡还款金额
	REL_PMT_AMT decimal(18,2) COMMENT '公司卡还款金额 : 公司卡还款金额',
	-- 受理分行代码
	ACQ_BRANCH_ID varchar(11) COMMENT '受理分行代码 : 受理分行代码',
	-- 受理机构终端标识码
	ACQ_TERMINAL_ID varchar(8) COMMENT '受理机构终端标识码 : 受理机构终端标识码',
	-- 受理机构标识码
	ACQ_ACCEPTOR_ID varchar(15) COMMENT '受卡方标识码 : 受理机构标识码',
	-- 受卡点（商户）名称，地址
	MERCHANT_NAME_ADDR varchar(40) COMMENT '商户名称地址 : 受卡点（商户）名称，地址',
	MCC varchar(4) COMMENT '商户类别代码',
	STMT_DATE date COMMENT '账单日期',
	-- 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_B4_POSTING char(1) COMMENT '入账前账龄 : 入账前，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	-- 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。
	AGE_CD_AFTER_POSTING char(1) COMMENT '入账后账龄 : 入账成功后，当前账户的账龄，用于账龄发生变化时会计科目的结转。',
	TXN_DETAIL_SEQ varchar(64) COMMENT '来源交易流水号',
	-- ///
	-- @net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType
	TXN_DETAIL_TYPE char(1) COMMENT '来源交易流水类型 : ///
@net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType',
	ACCT_CURR_BAL decimal(18,2) COMMENT '账户当前余额',
	-- 供查询交易使用。
	TXN_TYPE varchar(30) COMMENT '本地交易类型 : 供查询交易使用。',
	-- 供查询交易使用。
	OPP_ACCT varchar(80) COMMENT '交易对手方账号 : 供查询交易使用。',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (TXN_SEQ)
) COMMENT = '挂账交易历史表 : 挂账交易历史表，当日入账交易表中交易状态非正常的交易移入该历史表';



/* Create Indexes */

CREATE INDEX INX_IOU_NO ON CACT_ACCOUNT_ADDI (IOU_NO ASC);
CREATE INDEX INX_CUST_ID ON CACT_ACCOUNT_NO (CUST_ID DESC);



