package com.framework.core.web.tree;

import java.util.List;

//import javax.annotation.Resource;

import com.framework.core.web.tree.constants.TreeTypeEnum;
import com.framework.core.web.tree.model.TreeData;


/**
 * 
 * 
 *  树形chache 模版
 * @author zhangjun
 *
 */
public abstract class TreeCacheTemplate  {
	
//	@Resource
//	private TreeCacheService treeCacheService;
	
	/**
	 * 获取所有的树形节点的数据
	 * @return
	 */
	public abstract List<TreeData> getAllTreeData();
	
	
	/**
	 * 模版对应的树形节点类型
	 * @return
	 */
	public abstract TreeTypeEnum getTreeType();
	
//	/**
//	 * 最多多少层
//	 * 
//	 * @return
//	 */
//	public int getMaxLv() {
//		
//		TreeTypeEnum type  =  getTreeType();
//		
//		Assert.notNull(type);
//		
//		return type.getMaxLv();
//	}
	
	

//	/**
//	 * 当前handler处理的刷新事件类型
//	 * @return
//	 */
//	@Override
//	public RefreshDbEventTypeEnum determinRefreshEventType() {
//		
//		TreeTypeEnum type  =  getTreeType();
//		
//		Assert.notNull(type);
//
//		return type.getRefreshDbType();
//
//	}
//
//	
//	
//	
//	/**
//	 * 处理db刷新事件的逻辑
//	 * 
//	 * @param event
//	 * @return
//	 */
//	@Override
//	public void processEvent(RefreshDbEvent event) {
//		
//		String treeType = event.getData();
//		
//		Assert.notNull(treeType);
//		
//		TreeTypeEnum typeEnum = TreeTypeEnum.getTreeTypeEnum(treeType);
//		
//		Assert.notNull(typeEnum);
//		
//		treeCacheService.buildTreeCache(typeEnum);
//	}
//	
}