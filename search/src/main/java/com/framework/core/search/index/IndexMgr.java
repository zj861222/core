package com.framework.core.search.index;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.framework.core.search.index.factory.Index;
import com.framework.core.search.index.factory.IndexClient;

@Component
public class IndexMgr {
	
	/**
	 * index client管理
	 */
	private Map<String, IndexClient> clientMap = new ConcurrentHashMap<String, IndexClient>(5);
	
	
	/**
	 * index管理
	 */
	private Map<String, Index> indexMap = new ConcurrentHashMap<String, Index>(50);
	
	
    /**
     * 注册 indexclient
     * @param clientName
     * @param client
     */
	public void registerIndexClient(String clientName,IndexClient client) {
		clientMap.put(clientName, client);
	}
	
	
	/**
	 * 注册索引
	 * @param aliasName
	 * @param index
	 */
	public void registerIndex(String aliasName,Index index) {
		indexMap.put(aliasName, index);
	}
	

	/**
	 * 根据别名获取 
	 * @param aliasName
	 * @return
	 */
	public Index  getIndexByAliasName(String aliasName) {
	
		return indexMap.get(aliasName);
	}
	
}