package com.framework.core.web.tree;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.framework.core.web.tree.constants.TreeTypeEnum;



@Component
public class TreeCacheTemplateManager implements ApplicationContextAware {

	
	private Map<String, TreeCacheTemplate> treeCacheTemplateMap = new HashMap<>();
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		
		Map<String, TreeCacheTemplate> map = applicationContext.getBeansOfType(TreeCacheTemplate.class);
		
		if(MapUtils.isEmpty(map)) {
			return;
		}
		
		
		for(Entry<String, TreeCacheTemplate> entry:map.entrySet()) {
			
			TreeCacheTemplate instance = entry.getValue();
			
			if(instance.getTreeType() == null) {
				throw new RuntimeException("TreeCacheTemplate tree type can not be null!!!class:"+instance.getClass().getName());
			}
			
			treeCacheTemplateMap.put(instance.getTreeType().getTreeType(), instance);
		}
		
	}
	
	
	/**
	 * 获取模版实例
	 * @param treeTypeEnum
	 * @return
	 */
	public TreeCacheTemplate getTreeCacheTemplateInstance(TreeTypeEnum treeTypeEnum) {
		
		
		return treeCacheTemplateMap.get(treeTypeEnum.getTreeType());
	}
	
}