package com.framework.core.web.tree.model;

/**
 * 树形数据基类
 * @author zhangjun
 *
 */
public class TreeData
{

	/**
	 * 顶层根节点 父节点id
	 */
	public static final Long ROOT_REAL_PARENT_Id = -999L;
	
	
	public static final Long ROOT_PARENT_Id = -1L;

	/**
	 * 节点id
	 */
	private Long id;

	/**
	 * 父节点id
	 */
	private Long parentId;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Long getParentId()
	{
		return parentId;
	}

	public void setParentId(Long parentId)
	{
		this.parentId = parentId;
	}

}