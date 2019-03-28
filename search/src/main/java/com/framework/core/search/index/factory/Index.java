package com.framework.core.search.index.factory;

import java.util.List;
import java.util.Map;

import com.framework.core.search.index.builder.IndexBuilder;

/**
 * 
 */
public interface Index
{
	// 索引状态
	public static final int STATUS_INIT = 0;// 初始化状态
	
	public static final int STATUS_ACTIVE = 1;// 激活状态

	/**
	 * 获取索引别名
	 * @return
	 */
	String getAliasName();

	/**
	 * 设置索引名
	 * @param name
	 */
	void setAliasName(String aliasName);

	/**
	 * 获取索引状态
	 * @return
	 */
	int getStatus();

	/**
	 * 设置索引状态
	 * @param status
	 */
	void setStatus(int status);

	/**
	 * 获取索引配置属性
	 * @return
	 */
	Map<String, String> getProperties();

	/**
	 * 设置索引配置属性
	 * @param properties
	 */
	void setProperties(Map<String, String> properties);

	/**
	 * 设置索引客户端
	 * @param client
	 */
	void addClient(IndexClient client);

	/**
	 * 获取索引建造器
	 * @return
	 */
	public IndexBuilder getIndexBuilder();

	/**
	 * 设置索引建造器的类名
	 * @return
	 */
	public void setIndexBuilderClassName(String indexBuilderClassName);

	/**
	 * 设置索引mapping文件
	 * @return
	 */
	public void setMappingContent(String mappingContent);

	public String getMappingContent();

	/**
	 * 返回indexClient列表，按优先级排列
	 * @return
	 */
	public List<IndexClient> getIndexClients();

	/**
	 *
	 * @return
	 */
	public void setClientStatus(IndexClient indexClient, boolean status);

	public boolean isClientValid(IndexClient indexClient);

	public boolean isClientDown(IndexClient indexClient, String indexName);

}
