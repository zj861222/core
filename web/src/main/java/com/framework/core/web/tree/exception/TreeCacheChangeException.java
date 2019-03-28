package com.framework.core.web.tree.exception;

import com.framework.core.web.tree.constants.TreeTypeEnum;

/**
 * 树形缓存变更异常
 * @author zhangjun
 *
 */
public class TreeCacheChangeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7032228010978281872L;
	
	
	public TreeCacheChangeException(TreeTypeEnum treeTypeEnum)
	{
		super("Tree Cache Change during the query,And previous data is not vaild .[Tree type]:"+treeTypeEnum.getTreeType());
	}

}