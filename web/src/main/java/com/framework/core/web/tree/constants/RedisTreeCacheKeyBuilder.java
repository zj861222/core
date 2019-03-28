package com.framework.core.web.tree.constants;




/**
 * 树形缓存redis key构建
 * @author zhangjun
 *
 */
public class RedisTreeCacheKeyBuilder {
	
	
	/**
	 * hash结构的key
	 * @param treeTypeEnum
	 * @return
	 */
	public static String getTreeCacheMapKey(TreeTypeEnum treeTypeEnum)
	{

		return "appsvr:tc:hash:" + treeTypeEnum.getTreeType() ;
	}
	
	
	/**
	 * id作为hashkey
	 * @param id
	 * @return
	 */
	public static  String buildIdHashKey(long id)
	{

		return "id:" + id;
	}
	
	
	/**
	 * node ide作为hashkey
	 * 
	 * @param nodeId
	 * @return
	 */
	public static String buildNodeCodeHashKey(long nodeId)
	{

		return "nodeId:" + nodeId;
	}
	
	
	/**
	 * zset结构的key
	 * @param treeTypeEnum
	 * @return
	 */
	public static String getGlobalNodeCodeTreeCacheZsetKey(TreeTypeEnum treeTypeEnum)
	{


		return "appsvr:tc:zset:gnc:" + treeTypeEnum.getTreeType() ;

	}

	
	/**
	 * 发送刷新时间的flag
	 * @param treeTypeEnum
	 * @return
	 */
	public static String getSendRefreshEventFlagKey(TreeTypeEnum treeTypeEnum) {

		return "appsvr:tc:refreshevent:" + treeTypeEnum.getTreeType() ;
	}
	
	
	/**
	 * 
	 * <一句话功能简述>
	 * <功能详细描述>
	 * @param treeTypeEnum
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static String getRefreshEventLockKey(TreeTypeEnum treeTypeEnum){
	    
        return "lock:tc:refreshevent:" + treeTypeEnum.getTreeType() ;

	    
	}
	
}