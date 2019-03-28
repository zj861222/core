
package com.framework.core.search.index.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.core.search.index.builder.IndexBuilder;
import com.framework.core.search.index.factory.Index;
import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.utils.ApplicationContextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class EsIndex implements Index
{
	/**
	 * 索引别名
	 */
	private String aliasName;
	
	/**
	 * 配置属性
	 */
	private Map<String, String> properties;
	
	private int status = STATUS_INIT;
	
	private List<IndexClient> clientList;
	
	private Map<String, Boolean> clientMap;
	
	/**
	 * 数据同步builder实现类
	 */
	private String indexBuilderClassName;
	
	/**
	 * 数据同步builder
	 */
	private IndexBuilder indexBuilder;
	
	private String mappingContent;
	
	private final Logger logger = LoggerFactory.getLogger(EsIndex.class);

	public EsIndex(String aliasName, IndexClient client, Map<String, String> properties)
	{
		this.aliasName = aliasName;
		clientList = new ArrayList<IndexClient>();
		clientList.add(client);
		clientMap = new ConcurrentHashMap<String, Boolean>();
		clientMap.put(client.getName(), true);
		this.properties = properties;
	}

	/**
	 * 获取索引名
	 *
	 * @return
	 */
	@Override
	public String getAliasName()
	{
		return this.aliasName;
	}

	/**
	 * 设置索引名
	 *
	 * @param name
	 */
	@Override
	public void setAliasName(String aliasName)
	{
		this.aliasName = aliasName;
	}

	/**
	 * 获取索引状态
	 *
	 * @return
	 */
	@Override
	public int getStatus()
	{
		return this.status;
	}

	/**
	 * 设置索引状态
	 *
	 * @param status
	 */
	@Override
	public void setStatus(int status)
	{
		this.status = status;
	}

	/**
	 * 获取索引配置属性
	 *
	 * @return
	 */
	@Override
	public Map<String, String> getProperties()
	{
		return this.properties;
	}

	/**
	 * 设置索引配置属性
	 *
	 * @param properties
	 */
	@Override
	public void setProperties(Map<String, String> properties)
	{
		this.properties = properties;
	}

	/**
	 * 设置索引客户端
	 *
	 * @param client
	 */
	@Override
	public void addClient(IndexClient client)
	{
		clientList.add(client);
		clientMap.put(client.getName(), true);
	}

	/**
	 * 获取索引建造器
	 * @return
	 */
	public IndexBuilder getIndexBuilder()
	{
		if (indexBuilder == null)
		{
			try
			{
				indexBuilder = (IndexBuilder) ApplicationContextUtil.getBean(Class.forName(indexBuilderClassName));
			}
			catch (ClassNotFoundException e)
			{
				throw new Error("找不到" + indexBuilderClassName + "类的定义", e);
			}
		}
		return indexBuilder;
	}

	/**
	 * 设置索引建造器
	 * @return
	 */
	public void setIndexBuilderClassName(String indexBuilderClassName)
	{
		this.indexBuilderClassName = indexBuilderClassName;
	}

	@Override
	public List<IndexClient> getIndexClients()
	{
		List<IndexClient> resultList = new ArrayList<IndexClient>(clientList.size());
		
		for (int i = clientList.size() - 1; i >= 0; i--)
		{
			IndexClient client = clientList.get(i);
			if (clientMap.get(client.getName()) == Boolean.TRUE)
			{
				resultList.add(0, client);
			}
			else
			{
				resultList.add(client);
			}
		}
		return resultList;
	}

	@Override
	public void setClientStatus(IndexClient indexClient, boolean status)
	{
		for (IndexClient client : clientList)
		{
			if (client == indexClient)
			{
				clientMap.put(client.getName(), status);
				if (status)
				{
					logger.info("[client={}] [aliasName={}]现在是可用状态", indexClient.getName(), aliasName);
				}
				else
				{
					logger.info("[client={}] [aliasName={}]出错了", indexClient.getName(), aliasName);
				}
			}
		}
	}

	@Override
	public boolean isClientValid(IndexClient indexClient)
	{
		Boolean status = null;
		for (IndexClient client : clientList)
		{
			if (client == indexClient)
			{
				status = clientMap.get(client.getName());
			}
		}
		return status != null ? status : false;
	}

	@Override
	public boolean isClientDown(IndexClient indexClient, String indexName)
	{
		return indexClient.indexHealth(indexName);
	}

	@Override
	public void setMappingContent(String mappingContent)
	{
		this.mappingContent = mappingContent;
	}

	@Override
	public String getMappingContent()
	{
		return this.mappingContent;
	}

}
