package net.engining.pcx.cc.param.model;

import java.io.Serializable;

import net.engining.pg.support.meta.PropertyInfo;


/**
 * 账户状态锁定码
 * @author Ronny
 *
 */
public class BlockCode implements Serializable {

	private static final long serialVersionUID = -6443949660391506845L;

	/**
     * 0-9 : age
     * A-Z
     */
    @PropertyInfo(name="锁定码", length=1)
    public String blockCode;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;
    
    /**
     * 是否可人工编辑
     */
    @PropertyInfo(name="是否可人工编辑", length=1)
    public Boolean enableEdit;
}