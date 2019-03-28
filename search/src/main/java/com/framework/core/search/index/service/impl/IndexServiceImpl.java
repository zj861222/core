package com.framework.core.search.index.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.framework.core.search.index.IndexMgr;
import com.framework.core.search.index.builder.IndexBuilder;
import com.framework.core.search.index.callback.IndexCallback;
import com.framework.core.search.index.callback.SearchCallback;
import com.framework.core.search.index.factory.Index;
import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.index.service.IndexService;
import com.framework.core.search.model.IndexData;
import com.framework.core.search.model.SearchParam;
import com.framework.core.search.model.SearchResult;


@Service
public class IndexServiceImpl implements IndexService {

	private final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

	
	@Resource
	private IndexMgr indexMgr;
	
	/**
	 * 通过索引名获取索引信息
	 *
	 * @param indexName
	 *            索引名称
	 * @return
	 */
	public Index getIndex(String indexAliasName)
	{
		return indexMgr.getIndexByAliasName(indexAliasName);
	}

	

	/**
	 * 判断索引是否存在
	 *
	 * @param indexAliasName  索引别名
	 *            
	 * @return
	 */
	@Override
	public boolean indexExists(final String indexAliasName)
	{
		Index index = getIndex(indexAliasName);
		boolean result = false;
		if (index != null)
		{
			result = execute(new SearchCallback<Boolean>()
			{
				@Override
				public Boolean execute(IndexClient indexClient)
				{
					return indexClient.indexExists(indexAliasName);
				}
			}, index);
		}
		return result;
	}

	/**
	 * 创建索引
	 *
	 * @param indexName
	 *            索引名称
	 * @param force
	 *            是否强制创建索引，不论索引是否已经存在
	 * @throws Exception 
	 */
	@Override
	public void createIndex(final String indexAliasName, final boolean force) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			final Map<String, String> properties = index.getProperties();
			final String mappingContent = index.getMappingContent();
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient) throws Exception
				{
					// String tempIndexName =
					// generateTempIndexName(indexAliasName);
					// indexClient.createIndex(indexAliasName, tempIndexName,
					// properties, mappingContent, force);
					indexClient.createIndex(indexAliasName, properties, mappingContent, force);

				}
			}, index);
		}
	}

	/**
	 * 删除索引
	 *
	 * @param indexName
	 *            索引名称
	 * @throws Exception 
	 */
	@Override
	public void deleteIndex(final String indexAliasName) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.deleteIndexByAlias(indexAliasName);
				}
			}, index);
		}
	}

	/**
	 * 更新索引缓存
	 *
	 * @param indexName
	 *            索引名称
	 * @throws Exception 
	 */
	@Override
	public void refreshIndex(final String indexAliasName) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.refreshIndex(indexAliasName);
				}
			}, index);
		}
	}

	/**
	 * 关闭索引
	 *
	 * @param indexName
	 *            索引名称
	 * @throws Exception 
	 */
	@Override
	public void closeIndex(final String indexAliasName) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.closeIndex(indexAliasName);
				}
			}, index);
		}
	}

	/**
	 * 刷新索引文件
	 *
	 * @param indexName
	 *            索引名称
	 * @throws Exception 
	 */
	@Override
	public void flushIndex(final String indexAliasName) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.flushIndex(indexAliasName);
				}
			}, index);
		}
	}

	/**
	 * 执行索引优化
	 * @throws Exception 
	 */
	@Override
	public void optimize(final String indexAliasName) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.optimize(indexAliasName);
				}
			}, index);
		}
	}

	/**
	 * 执行索引重建 重建策略： 1. 建一个新索引 2. 向新导入数据 3. 删除原索引的别名 4. 为新索引增加别名
	 * 目前重建可以先停掉canal，暂停同步更新，但不会影响线上搜索服务。
	 * 
	 * @param indexName
	 *            索引名称
	 */
	@Override
	public void rebuild(final String indexAliasName)
	{
		try {
			final Index index = getIndex(indexAliasName);
			
			if (index != null) {
//                Jedis jedis = redisUtil.getConnection();
//                jedis.set(ISearchConstans.REDIS_INDEX_REBUILDING_STATUS, ISearchConstans.REDIS_INDEX_REBUILDING_STATUS_IS_REBUILDING);
//                
				final Map<String, String> properties = index.getProperties();
				
				final String mappingContent = index.getMappingContent();
				final IndexBuilder indexBuilder = index.getIndexBuilder();
				
				execute(new IndexCallback() {
					@Override
					public void execute(IndexClient indexClient) throws Exception {
						logger.info("[module=IndexMgrService] [method=rebuild] [client={}] [indexAliasName={}] 重建索引",
								indexClient.getName(), indexAliasName);
						long begin = System.currentTimeMillis();
						
						indexClient.deleteTempIndex(indexAliasName);
						// 创建临时索引
						String tempIndexAliasName = generateTempIndexName(indexAliasName);
						indexClient.createIndex(tempIndexAliasName, properties, mappingContent, false);
						
						// 针对批量插入数据，进行索引的优化设置
						Map<String, String> originalSettings = indexClient.optimumSettingsForBulkIndexing(tempIndexAliasName);
						logger.info("[module=IndexMgrService] [method=rebuild] [client=" + indexClient.getName()
								+ "] [indexAliasName=" + indexAliasName + "]原来的设置：" + originalSettings.toString());
						// 重建数据
						
						if (!indexBuilder.buildAll(tempIndexAliasName, indexAliasName, indexClient)) {
							throw new Exception("重建失败");
						}
						
						// 恢复原来的索引设置
						indexClient.setIndexSettings(tempIndexAliasName, originalSettings);
						
						//刷新新创建的索引缓存
						refreshIndex(tempIndexAliasName);

						// 索引替换：将老索引的索引名加到新索引上，并删除老索引
						indexClient.replaceIndex(indexAliasName, tempIndexAliasName);
						logger.info("[module=IndexMgrService] [method=rebuild] [client=" + indexClient.getName()
								+ "] [indexAliasName=" + indexAliasName + "] 重建索引完毕，耗时：" + (System.currentTimeMillis() - begin));
						index.setClientStatus(indexClient, true);
					}
				}, index);
				
//                jedis.set(ISearchConstans.REDIS_INDEX_REBUILDING_STATUS, ISearchConstans.REDIS_INDEX_REBUILDING_STATUS_NOT_REBUILDING);
//                redisUtil.closeConnection(jedis);
			}
		} catch (Exception e) {
			logger.info("Exception:", e);
		} finally {
//            Jedis jedis = redisUtil.getConnection();
//            jedis.set(ISearchConstans.REDIS_INDEX_REBUILDING_STATUS, ISearchConstans.REDIS_INDEX_REBUILDING_STATUS_NOT_REBUILDING);
//            redisUtil.closeConnection(jedis);
        }
	}

	/**
	 * 添加索引数据
	 *
	 * @param indexName
	 *            索引名称
	 * @param data
	 *            索引数据
	 * @throws Exception 
	 */
	@Override
	public void addIndexData(final String indexAliasName, final IndexData data) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.addIndexData(indexAliasName, data);
				}
			}, index);
		}
	}

	@Override
	public void addIndexData(final String indexAliasName, final List<IndexData> dataList) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.addIndexDataList(indexAliasName, dataList);
				}
			}, index);
		}
	}

	/**
	 * 删除索引数据
	 *
	 * @param indexName
	 * @throws Exception 
	 */
	@Override
	public void deleteIndexData(final String indexAliasName, final String id) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					List<String> list = indexClient.getRelatedIndexs(indexAliasName);
					for (String thisName : list)
					{
						indexClient.deleteIndexData(thisName, id);
					}
				}
			}, index);
		}
	}

	@Override
	public void deleteIndexDataByQuery(final String indexAliasName, final QueryBuilder query) throws Exception
	{
		Index index = getIndex(indexAliasName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					indexClient.deleteIndexDataByQuery(indexAliasName, query);

					// List<String> list =
					// indexClient.getRelatedIndexs(indexAliasName);
					//
					// for (String thisName : list)
					// {
					// indexClient.deleteIndexDataByQuery(indexAliasName,
					// query);
					//
					// }
				}
			}, index);
		}
	}

	/**
	 * 更新索引数据
	 *
	 * @param indexName
	 * @throws Exception 
	 */
	@Override
	public void updateIndexData(final String indexName, final IndexData data) throws Exception
	{
		Index index = getIndex(indexName);
		if (index != null)
		{
			execute(new IndexCallback()
			{
				@Override
				public void execute(IndexClient indexClient)
				{
					List<String> list = indexClient.getRelatedIndexs(indexName);
					for (String thisName : list)
					{
						indexClient.updateIndexData(thisName, data);
					}
				}
			}, index);
		}
	}

	// @Override
	// public void updateIndexDataStr(final String indexName, final String id,
	// final String jsonData) throws Exception {
	// Index index = this.indexMap.get(indexName);
	// boolean result = false;
	// if (index != null) {
	// execute(new IndexCallback() {
	// @Override
	// public void execute(IndexClient indexClient) {
	// List<String> list = indexClient.getRelatedIndexs(indexName);
	// for (String thisName : list) {
	//
	// indexClient.updateIndexDataStr(thisName, id, jsonData);
	// }
	// }
	// }, index);
	// }
	// }

	@Override
	public void execute(IndexCallback callback, Index index) throws Exception
	{
		List<IndexClient> indexClientList = index.getIndexClients();
		for (IndexClient indexClient : indexClientList)
		{
			try
			{
				callback.execute(indexClient);

			}
			catch (Exception e)
			{
				logger.error("[client=" + indexClient.getName() + "] [indexAliasName=" + index.getAliasName()
						+ "] Exception:" + e.getMessage(), e);
				throw e;
			}
		}
	}

	@Override
	public <T> T execute(SearchCallback<T> callback, Index index)
	{
		List<IndexClient> indexClientList = index.getIndexClients();
		for (IndexClient indexClient : indexClientList)
		{
			try
			{
				logger.debug("[client=" + indexClient.getName() + "] [indexAliasName=" + index.getAliasName() + "]");
				return callback.execute(indexClient);
			}
			catch (Exception e)
			{
				logger.error("[client=" + indexClient.getName() + "] [indexAliasName=" + index.getAliasName()
						+ "] Exception:" + e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * 通过标准索引名称创建临时索引名称 临时索引名称以下划线分割，前半部为标准索引名称，后半部为时间戳
	 * 
	 * @param indexName
	 * @return
	 */
	public String generateTempIndexName(String indexName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(indexName);
		sb.append("_");
		sb.append(System.currentTimeMillis());
		return sb.toString();
	}

	// 读文件，返回字符串
	private String readFile(String path)
	{

		BufferedReader reader = null;
		String laststr = "";
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
			reader = new BufferedReader(new InputStreamReader(is));
			;
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null)
			{
				// 显示行号
				// logger.infoln("line " + line + ": " + tempString);
				laststr = laststr + tempString;
				line++;
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e1)
				{
				}
			}
		}
		return laststr;
	}

	@Override
	public SearchResult search(final String indexAliasName, final SearchParam searchParam)
	{

		Index index = getIndex(indexAliasName);

		Assert.notNull(index);

		SearchResult result = execute(new SearchCallback<SearchResult>()
		{

			@Override
			public SearchResult execute(IndexClient indexClient)
			{
				return indexClient.search(indexAliasName, searchParam);
			}
		}, index);

		return result;
	}
}
	

