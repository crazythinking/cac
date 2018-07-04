package net.engining.pcx.cc.param.model;

import java.io.Serializable;
import java.util.List;

import net.engining.pg.support.meta.PropertyInfo;

public class ProductPost implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
     * 产品
     */
    @PropertyInfo(name="产品", length=8)
    public String productId;
    
    /**
     * 入账交易码
     */
    @PropertyInfo(name="入账交易码", length=8)
    public List<String> postList;

}
