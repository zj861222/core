package com.framework.core.web.tree.model;

import java.io.Serializable;

/**
 * 
 * @author zhangjun
 *
 */
public class CodedTreeData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3619051409851592685L;
	
	
	/**
	 * 节点id
	 */
	private long id;
	
	/**
	 * 节点父id
	 */
	private long parentId;
	
	/**
	 * 节点编码，
	 */
	private long nodeCode;
	
	/**
	 * 全局的节点编码
	 */
	private long globalNodeCode;
	
	/**
	 * 当前层数
	 */
	private int lv;
	
	
	/**
	 * 总层数
	 */
	private int maxLv;


	public long getId()
	{
		return id;
	}


	public void setId(long id)
	{
		this.id = id;
	}


	public long getParentId()
	{
		return parentId;
	}


	public void setParentId(long parentId)
	{
		this.parentId = parentId;
	}


	public long getNodeCode()
	{
		return nodeCode;
	}


	public void setNodeCode(long nodeCode)
	{
		this.nodeCode = nodeCode;
	}


	public long getGlobalNodeCode()
	{
		return globalNodeCode;
	}


	public void setGlobalNodeCode(long globalNodeCode)
	{
		this.globalNodeCode = globalNodeCode;
	}


	public int getLv()
	{
		return lv;
	}


	public void setLv(int lv)
	{
		this.lv = lv;
	}


	public int getMaxLv()
	{
		return maxLv;
	}


	public void setMaxLv(int maxLv)
	{
		this.maxLv = maxLv;
	}
	
	
	
	
	
}