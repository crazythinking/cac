package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import net.engining.pcx.cc.param.model.enums.AmtDbCrFlag;
import net.engining.pcx.cc.param.model.enums.BalDbCrFlag;
import net.engining.pcx.cc.param.model.enums.SubjectType;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 银行会计科目
 * 
 * @author heyu.wang
 *
 */
public class Subject implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * 科目编号
     */
    @PropertyInfo(name="科目编号", length=40)
    public String subjectCd;

    /**
     * 科目名称
     */
    @PropertyInfo(name="科目名称", length=100)
    public String name;
    
    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=300)
    public String description;

    /**
     * 余额方向
     * D - 借方余额
     * C - 贷方余额
     * B - 按轧差金额
     * D - 双向余额
     */
    @PropertyInfo(name="余额方向", length=1)
    public BalDbCrFlag balDbCrFlag;

    /**
     * 交易允许发生方向
     * D - 只允许借记发生额
     * C - 只允许贷记发生额
     * B - 双向发生额
     */
    @PropertyInfo(name="交易允许发生方向", length=1)
    public AmtDbCrFlag amtDbCrFlag;

    /**
     * 科目类型
     * A - 资产类
     * B - 负债类
     * C - 损益类
     * D - 共同类
     * E - 所有者权益
     * F - 表外类(账户呆账类)
     */
    @PropertyInfo(name="科目类型", length=1)
    public SubjectType type;
    
    /**
     * 币种
     */
    @PropertyInfo(name="币种", length=3)
    public String currCd;
    
    /**
     * 父科目编号
     */
    @PropertyInfo(name="父科目编号", length=40)
    public String parentSubjectCd;
    
    /**
     * 科目是否启用
     */
    @PropertyInfo(name="科目是否启用", length=1)
    public Boolean enable;
    
    /**
     * 科目表中的编号，一般用于保存行内的科目号，以便以后按照行内的科目表出会计流水
     */
    @PropertyInfo(name="总账科目号", length=40)
    public String subjectCode;
    
    /**
     * 是否允许透支
     */
    @PropertyInfo(name="是否允许透支", length=1)
    public Boolean isOverdraft;
    
    /**
     * 科目层级
     */
    @PropertyInfo(name="科目层级", length=2)
    public String subjectHierarchy;
    
    /**
     * 是否末级
     */
    @PropertyInfo(name="是否末级", length=1)
    public Boolean isLast;
    
    /**
     * 辅助核算项
     */
    @PropertyInfo(name="辅助核算项", length=100)
    public List<String> auxiliaryAssist;
    
    /**
     * 是否参与对账
     */
    @PropertyInfo(name="是否参与对账", length=1)
    public Boolean isAccount;
}
