package com.framework.core.web.tree;

import java.util.List;

import com.framework.core.web.tree.constants.TreeTypeEnum;


/**
 * 树形缓存服务
 * @author zhangjun
 *
 */
public interface TreeCacheService {
	
//	/**
//	 * 获取Tree形2个节点之间的关系
//	 * @param treeTypeEnum
//	 * @param sourceNodeId 原节点
//	 * @param compareNodeId 比较的节点 
//	 * @return
//	 */
//	TreeCacheRelation getTreeNodeRelation(TreeTypeEnum treeTypeEnum,long sourceNodeId,long compareNodeId);
//	
	
	/**
	 * 获取所有的parent节点
	 * @param treeTypeEnum
	 * @param nodeId
	 * @return
	 */
	List<Long> getParentNodeIds(TreeTypeEnum treeTypeEnum,long nodeId);
	
	
	/**
	 * 获取所有的子节点
	 * @param treeTypeEnum
	 * @param nodeId
	 * @return
	 */
	List<Long> getAllSubNodeIds(TreeTypeEnum treeTypeEnum,long nodeId);
	
	

	
	/**
	 * 构建树形节点cache
	 * @param treeTypeEnum
	 */
	void buildTreeCache(TreeTypeEnum treeTypeEnum);
	
	
	/**
	 * 清理企业的树形缓存
	 */
	void clearTenantTreeCache(TreeTypeEnum treeTypeEnum);
	
	/**
	 * 查询节点层级信息
	 * 
	 * @param treeTypeEnum
	 * @param nodeId
	 * @return
	 */
	int queryNodeLv(TreeTypeEnum treeTypeEnum,long nodeId);
	
	
	
	/**
	 * 查询所有节点的子节点的合集
	 * @param treeTypeEnum
	 * @param queryIds
	 * @return
	 */
	List<Long> querySelfAndUnionAllSubNodeIds(TreeTypeEnum treeTypeEnum,List<Long> queryIds);
	
	
	
}