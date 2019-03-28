//package com.framework.core.web.tree.model;
//
//import com.framework.core.web.tree.utils.TreeNodeUtils;
//
///**
// * 
// * 树形缓存关系。
// * 
// * @author zhangjun
// *
// */
//public class TreeCacheRelation {
//	
//
//	/**
//	 * 源id
//	 */
//	private long sourceNodeId;
//	
//	/**
//	 * 源code
//	 */
//	private long sourceNodeCode;
//	
//	/**
//	 * 比较目标节点
//	 */
//	private long compareNodeId;
//	
//	/**
//	 * 比较目标的code
//	 */
//	private long compareNodeCode;
//	
//	
//	
//	
//	
//	public long getSourceNodeId()
//	{
//		return sourceNodeId;
//	}
//
//
//
//
//	public long getSourceNodeCode()
//	{
//		return sourceNodeCode;
//	}
//
//
//
//	public long getCompareNodeId()
//	{
//		return compareNodeId;
//	}
//
//
//
//	public long getCompareNodeCode()
//	{
//		return compareNodeCode;
//	}
//
//
//
//
//	public TreeCacheRelation(long sourceNodeId,long sourceNodeCode,long compareNodeId,long compareNodeCode) {
//		this.sourceNodeId = sourceNodeId;
//		this.sourceNodeCode = sourceNodeCode;
//		
//		this.compareNodeId = compareNodeId;
//		this.compareNodeCode = compareNodeCode;
//	}
//	
//	
//	/**
//	 * 判断source是否是compare节点的子节点.
//	 * 
//	 * @return
//	 */
//	public boolean isSubNode(){
//	
//		return TreeNodeUtils.isSubNode(sourceNodeCode, compareNodeCode);
//	}
//	
//	/**
//	 * 获取source节点的lv
//	 * @return
//	 */
//	public int getSourceNodeLv(){
//		
//		return TreeNodeUtils.getNodeCodeLv(sourceNodeCode);
//	}
//	
//	
//	/**
//	 * 获取目标节点的lv
//	 * @return
//	 */
//	public int getTargetNodeLv(){
//		
//		return TreeNodeUtils.getNodeCodeLv(compareNodeCode);
//
//	}
//
//
//
//}