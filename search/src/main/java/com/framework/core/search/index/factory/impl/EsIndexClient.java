package com.framework.core.search.index.factory.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import org.springframework.data.elasticsearch.core.query.AliasQuery;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.search.index.callback.ClientCallback;
import com.framework.core.search.index.callback.ClusterCallback;
import com.framework.core.search.index.callback.NodeCallback;
import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.model.IndexData;
import com.framework.core.search.model.SearchParam;
import com.framework.core.search.model.SearchResult;

import io.jsonwebtoken.lang.Collections;

public class EsIndexClient implements IndexClient
{

	private final Logger logger = LoggerFactory.getLogger(EsIndexClient.class);

	public static final String INDEX_ID_PREFIX = "waiqin365_";

	private static final int BATCH_SIZE = 500;

	private ElasticsearchTemplate elasticsearchTemplate;

	private Client client;

	private String name;

	public EsIndexClient(Client client)
	{

		this.client = client;

		this.elasticsearchTemplate = new ElasticsearchTemplate(client);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean indexExists(String indexAliasName)
	{

		boolean exist = elasticsearchTemplate.indexExists(indexAliasName);

		return exist;
	}

	/**
	 * 获取索引唯一ID标识
	 *
	 * @param indexName
	 * @return
	 */
	public String getIndexNameByAliasName(String indexAlias)
	{
		String indexName = null;

		if (indexAlias != null)
		{
			if (indexAlias.startsWith(INDEX_ID_PREFIX))
			{
				// 兼容输入的索引名称是索引ID的情况
				indexName = indexAlias;
			}
			else
			{
				indexName = this.getIndexAliasNameMap().get(indexAlias);
			}
		}

		return indexName;
	}

	/**
	 * 获取索引别名和索引名称的对应关系，key为索引别名，val为索引名
	 *
	 * @return
	 */
	private Map<String, String> getIndexAliasNameMap()
	{

		// 查询所有索引和别名的map
		Map<String, String> map = getIndexNameMap();

		Map<String, String> indexNameToIdMap = new HashMap<String, String>();
		for (String idx : map.keySet())
		{
			indexNameToIdMap.put(map.get(idx), idx);
		}
		return indexNameToIdMap;
	}

	/**
	 * 获取索引别名和索引名称的对应关系，，key为索引名，val为索引别名
	 *
	 * @return
	 */
	private Map<String, String> getIndexNameMap()
	{
		// 获取系统实际存在的索引名列表
		ClusterState clusterState = client.admin().cluster().prepareState().execute().actionGet().getState();
		String[] indices = clusterState.metaData().indices().keys().toArray(String.class);

		Map<String, String> indexIdToNameMap = new HashMap<String, String>();

		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().routingTable(true).nodes(true)
				.indices(indices);
		MetaData md = client.admin().cluster().state(clusterStateRequest).actionGet(30000).getState().metaData();

		for (IndexMetaData imd : md)
		{
			for (AliasMetaData amd : imd.getAliases().values().toArray(AliasMetaData.class))
			{
				indexIdToNameMap.put(imd.getIndex().getName(), amd.alias());
			}
		}
		return indexIdToNameMap;
	}

	@Override
	public boolean indexHealth(String indexAliasName)
	{

		boolean health = true;

		String indexName = this.getIndexNameByAliasName(indexAliasName);

		if (StringUtils.isNotBlank(indexName))
		{

			// 健康状况
			final ClusterHealthResponse response = executeGet(new ClusterCallback<ClusterHealthResponse>()
			{
				@Override
				public ActionFuture<ClusterHealthResponse> execute(final ClusterAdminClient admin)
				{
					return admin.health(Requests.clusterHealthRequest().timeout("10s"));
				}
			});
			if (response.getIndices().get(indexName).getStatus().equals(ClusterHealthStatus.RED))
			{
				health = false;
			}
		}

		return health;

	}

	// 执行集群操作请求
	@SuppressWarnings("unchecked")
	public <T extends ActionResponse> T executeGet(final ClusterCallback<T> callback)
	{
		final ClusterAdminClient clusterAdmin = client.admin().cluster();
		final ActionFuture<?> action = callback.execute(clusterAdmin);
		final T response = (T) action.actionGet();
		return response;
	}

	// 执行数据操作请求
	public <T extends ActionResponse> T executeGet(final ClientCallback<T> callback)
	{
		final ActionFuture<T> action = callback.execute(client);
		final T response = action.actionGet();
		return response;
	}

	// 执行索引操作请求
	@SuppressWarnings("unchecked")
	public <T extends ActionResponse> T executeGet(final NodeCallback<T> callback)
	{

		final IndicesAdminClient indicesAdmin = client.admin().indices();
		final ActionFuture<?> action = callback.execute(indicesAdmin);
		final T response = (T) action.actionGet();
		return response;
	}

	@Override
	public void createIndex(final String indexAliasName, final Map<String, String> properties,
			final String mappingContent, boolean force)
		throws Exception
	{

		String oldIndexName = this.getIndexNameByAliasName(indexAliasName);

		final String indexName;

		if (oldIndexName == null || force)
		{
			// 旧索引不存在 OR 旧索引已存在但强制重建
			indexName = INDEX_ID_PREFIX + indexAliasName + "_" + DateUtil.date2String(new Date(), "yyyyMMddHHmmssSSS");
		}
		else
		{
			indexName = null;
		}

		// indexName为null，不需要处理
		if (indexName != null)
		{
			// 创建新索引
			executeGet(new NodeCallback<CreateIndexResponse>()
			{
				@Override
				public ActionFuture<CreateIndexResponse> execute(final IndicesAdminClient admin)
				{
					Map<String, String> settings = new HashMap<String, String>();
					settings.put("number_of_shards", properties.get("number_of_shards"));
					settings.put("number_of_replicas", properties.get("number_of_replicas"));
					settings.put("refresh_interval", properties.get("refresh_interval") + "s");

					// settings.put("translog.flush_threshold_size",
					// properties.get("translog.flush_threshold_ops"));
					logger.info(" [indexAliasName={}] [indexId={}] [props={}] [mapping={}]创建索引", new Object[]
					{
						indexAliasName, indexName, properties, mappingContent
					});

					return admin.create(Requests.createIndexRequest(indexName).settings(settings).mapping(
							getIndexDataType(indexAliasName), mappingContent,
							XContentFactory.xContentType(mappingContent)));
				}
			});

			if (oldIndexName != null)
			{
				// 删除旧索引名称
				this.deleteAliasIndexLink(oldIndexName, indexAliasName);
			}

			// 建立别名
			this.createIndexAlias(indexName, indexAliasName);

			if (oldIndexName != null)
			{
				// 删除旧索引
				this.deleteIndexByIndexName(oldIndexName);
			}
		}

	}

	/**
	 * 为索引删除名称
	 *
	 * @param indexId
	 * @param indexName
	 */
	public void deleteAliasIndexLink(String indexName, String indexAliasName) throws Exception
	{

		// 检查别名是否存在
		if (this.getIndexNameByAliasName(indexAliasName) != null)
		{

			AliasQuery aliasQuery = new AliasQuery();

			aliasQuery.setAliasName(indexAliasName);
			aliasQuery.setIndexName(indexName);

			elasticsearchTemplate.removeAlias(aliasQuery);

			logger.info(" 删除索引[indexName=" + indexName + "]的别名[indexAliasName=" + indexAliasName + "]");
		}
	}

	/**
	 * 为索引创建名称
	 *
	 * @param indexId
	 * @param indexName
	 */
	private void createIndexAlias(String indexName, String indexAliasName)
	{
		// 检查别名是否存在
		if (this.getIndexNameByAliasName(indexAliasName) == null)
		{

			// IndicesAliasesRequest req = new IndicesAliasesRequest();
			//
			// req.addAlias(indexAliasName, indexName);
			// client.admin().indices().aliases(req).actionGet(30000L);

			client.admin().indices().prepareAliases().addAlias(indexName, indexAliasName)
					.get(TimeValue.timeValueMillis(30000L));
			logger.info(" 创建索引[indexName=" + indexName + "]的别名[indexAliasName=" + indexAliasName + "]");
		}
	}

	/**
	 * 通过索引名称获取索引数据类型 标准索引名称中不含下划线，临时索引名称含义下划线，以下划线分割，前半部为标准索引名称，后半部为时间戳 参考方法
	 * generateTempIndexName 在本系统中，索引数据类型与标准索引名称保持一致，因此如果为临时索引名称，取前半部
	 *
	 * @param indexName
	 * @return
	 */
	public String getIndexDataType(String indexName)
	{
		return indexName.split("_")[0];
	}

	// @Override
	// public void createIndex(String indexAliasName, String tempIndexName,
	// Map<String, String> properties,
	// String mappingContent, boolean force)
	// throws Exception
	// {
	// String oldIndexName = this.getIndexNameByAliasName(indexAliasName);
	// final String indexName;
	//
	// if (oldIndexName == null || force) {
	// // 旧索引不存在 OR 旧索引已存在但强制重建
	// indexName = INDEX_ID_PREFIX + tempIndexName + "_" +
	// DateUtil.date2String(new Date(), "yyyyMMddHHmmssSSS");
	// } else {
	// indexName = null;
	// }
	//
	// if (indexName != null) {
	// // 创建新索引
	// executeGet(new NodeCallback<CreateIndexResponse>() {
	// @Override
	// public ActionFuture<CreateIndexResponse> execute(final IndicesAdminClient
	// admin) {
	// Map<String, String> settings = new HashMap<String, String>();
	// settings.put("number_of_shards", properties.get("number_of_shards"));
	// settings.put("number_of_replicas", properties.get("number_of_replicas"));
	// settings.put("refresh_interval", properties.get("refresh_interval")+"s");
	//
	//// settings.put("translog.flush_threshold_size",
	// properties.get("translog.flush_threshold_size"));
	// logger.info(" [indexName={}] [indexId={}] [props={}] [mapping={}]创建索引",
	// new Object[] { indexAliasName,
	// indexName, properties, mappingContent });
	// return
	// admin.create(Requests.createIndexRequest(indexName).settings(settings).mapping(getIndexDataType(tempIndexName),
	// mappingContent, XContentFactory.xContentType(mappingContent)));
	// }
	// });
	//
	// //
	// if (oldIndexName != null) {
	// // 删除旧索引名称
	// this.deleteAliasIndexLink(oldIndexName, tempIndexName);
	// }
	//
	// // 建立别名
	// this.createIndexAlias(indexName, indexAliasName);
	//
	// if (oldIndexName != null) {
	// // 删除旧索引
	// this.deleteIndexByIndexName(oldIndexName);
	// }
	// }
	// }

	@Override
	public void deleteIndexByAlias(String indexAliasName)
	{

		final String indexName = this.getIndexNameByAliasName(indexAliasName);

		elasticsearchTemplate.deleteIndex(indexName);

	}

	@Override
	public void deleteIndexByIndexName(String indexName)
	{

		elasticsearchTemplate.deleteIndex(indexName);

	}

	@Override
	public void refreshIndex(String indexAliasName)
	{

		final String indexName = this.getIndexNameByAliasName(indexAliasName);

		elasticsearchTemplate.refresh(indexName);

	}

	@Override
	public void closeIndex(String indexAliasName)
	{
		final String indexName = this.getIndexNameByAliasName(indexAliasName);

		if (indexName != null)
		{
			executeGet(new NodeCallback<CloseIndexResponse>()
			{
				@Override
				public ActionFuture<CloseIndexResponse> execute(final IndicesAdminClient admin)
				{
					return admin.close(Requests.closeIndexRequest(indexName));
				}
			});
		}

	}

	@Override
	public void flushIndex(String indexAliasName)
	{
		final String indexName = this.getIndexNameByAliasName(indexAliasName);

		if (indexName != null)
		{
			executeGet(new NodeCallback<FlushResponse>()
			{
				@Override
				public ActionFuture<FlushResponse> execute(final IndicesAdminClient admin)
				{
					return admin.flush(Requests.flushRequest(indexName));
				}
			});
		}

	}

	@Override
	public void replaceIndex(String oldIndexAliasName, String newIndexAliasName) throws Exception
	{
		String oldIndexName = this.getIndexNameByAliasName(oldIndexAliasName);

		String newIndexName = this.getIndexNameByAliasName(newIndexAliasName);
		if (newIndexName != null)
		{
			if (oldIndexName != null)
			{
				// 1. 把老索引的索引别名先删除
				this.deleteAliasIndexLink(oldIndexName, oldIndexAliasName);
			}
			// 2. 把新索引的索引别名删除
			this.deleteAliasIndexLink(newIndexName, newIndexAliasName);

			// 3. 把老索引的索引别名加到新索引上面
			this.createIndexAliasLink(newIndexName, oldIndexAliasName);

			if (oldIndexName != null)
			{
				// 4. 删除老索引
				this.deleteIndexByIndexName(oldIndexName);
			}
		}

	}

	/**
	 * 为索引创建名称
	 *
	 * @param indexId
	 * @param indexName
	 */
	private boolean createIndexAliasLink(String indexName, String indexAliasName)
	{

		// 检查别名是否存在
		if (this.getIndexNameByAliasName(indexAliasName) == null)
		{

			AliasQuery query = new AliasQuery();

			query.setIndexName(indexName);
			query.setAliasName(indexAliasName);

			boolean success = elasticsearchTemplate.addAlias(query);

			if (success)
			{
				logger.info(" 创建索引[indexName=" + indexName + "]的别名[indexAliasName=" + indexAliasName + "]");
			}

			return success;
		}

		return false;
	}

	@Override
	public void deleteTempIndex(String indexAliasName)
	{
		// 获取系统实际存在的索引名列表
		ClusterState clusterState = client.admin().cluster().prepareState().execute().actionGet().getState();
		String[] indices = clusterState.metaData().indices().keys().toArray(String.class);

		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().routingTable(true).nodes(true)
				.indices(indices);
		MetaData md = client.admin().cluster().state(clusterStateRequest).actionGet(30000).getState().getMetaData();

		String regEx = "^" + INDEX_ID_PREFIX + indexAliasName + "_(\\d){13}_(\\d){17}$";

		Pattern p = Pattern.compile(regEx);
		for (IndexMetaData imd : md)
		{
			if (p.matcher(imd.getIndex().getName()).find())
			{
				boolean validate = false;
				for (AliasMetaData amd : imd.getAliases().values().toArray(AliasMetaData.class))
				{
					if (indexAliasName.equals(amd.alias()))
					{
						validate = true;
						break;
					}
				}
				if (!validate)
				{
					this.deleteIndexByIndexName(imd.getIndex().getName());
					logger.info("删除遗留的临时索引：[id={}]", imd.getIndex().getName());
				}
			}
		}
	}

	@Override
	public void optimize(String indexAliasName)
	{

		// final String indexName =
		// this.getIndexNameByAliasName(indexAliasName);
		//
		// if (indexName != null) {
		// executeGet(new NodeCallback<OptimizeResponse>() {
		// @Override
		// public ActionFuture<OptimizeResponse> execute(final
		// IndicesAdminClient admin) {
		// return admin.optimize(Requests.optimizeRequest(indexName));
		// }
		// });
		// }
	}

	@Override
	public void addIndexData(final String indexAliasName, final IndexData data)
	{

		final String id = data.fetchIndexId();

		Assert.notNull(id);

		//
		// if(StringUtils.isNotBlank(indexName)){
		//
		// IndexQuery indexQuery = new
		// IndexQueryBuilder().withIndexName(indexName).withType(getIndexDataType(indexName)).withId(id).withObject(data).build();
		//
		// elasticsearchTemplate.index(indexQuery);
		// }

		final String indexName = this.getIndexNameByAliasName(indexAliasName);

		if (indexAliasName != null)
		{
			executeGet(new ClientCallback<IndexResponse>()
			{
				@Override
				public ActionFuture<IndexResponse> execute(final Client client)
				{
					String json = JSON.toJSONString(data);
					final IndexRequest request = Requests.indexRequest(indexAliasName).type(getIndexDataType(indexName))
							.id(id).source(json, XContentType.JSON);
					return client.index(request);
				}
			});
		}

	}

	@Override
	public void addIndexDataList(String indexAliasName, List<IndexData> dataList)
	{

		String indexName = this.getIndexNameByAliasName(indexAliasName);

		if (Collections.isEmpty(dataList))
		{
			return;
		}

		if (StringUtils.isBlank(indexName))
		{
			return;
		}

		List<IndexQuery> queries = new ArrayList<>();

		int count = 0;
		for (IndexData data : dataList)
		{

			String id = data.fetchIndexId();
			Assert.notNull(id);

			IndexQuery indexQuery = new IndexQueryBuilder().withIndexName(indexName)
					.withType(getIndexDataType(indexName)).withId(id).withObject(data).build();

			queries.add(indexQuery);

			count++;

			if (count >= BATCH_SIZE)
			{
				elasticsearchTemplate.bulkIndex(queries);
				queries.clear();
				count = 0;
			}
		}

		if (CollectionUtils.isNotEmpty(queries))
		{
			elasticsearchTemplate.bulkIndex(queries);
		}
	}

	@Override
	public void deleteIndexData(String indexAliasName, String id)
	{
		String indexName = this.getIndexNameByAliasName(indexAliasName);

		Assert.notNull(indexName);

		elasticsearchTemplate.delete(indexName, getIndexDataType(indexName), id);

	}

	@Override
	public void deleteIndexDataByQuery(String indexAliasName, QueryBuilder queryBuilder)
	{
		String indexName = this.getIndexNameByAliasName(indexAliasName);
		Assert.notNull(indexName);

		DeleteQuery deleteQuery = new DeleteQuery();

		deleteQuery.setIndex(indexName);
		deleteQuery.setType(getIndexDataType(indexName));

		deleteQuery.setQuery(queryBuilder);

		elasticsearchTemplate.delete(deleteQuery);
	}

	@Override
	public void updateIndexData(String indexAliasName, IndexData data)
	{
		String indexName = this.getIndexNameByAliasName(indexAliasName);
		Assert.notNull(indexName);

		String id = data.fetchIndexId();
		Assert.notNull(id);

		UpdateQuery updateQuery = new UpdateQuery();

		updateQuery.setIndexName(indexName);
		updateQuery.setType(getIndexDataType(indexName));
		updateQuery.setId(id);

		UpdateRequest updateRequest = new UpdateRequest(indexName, getIndexDataType(indexName), data.fetchIndexId());

		// 更新操作，使用doc
		String json = JSON.toJSONString(data);

		updateRequest.doc(json, XContentType.JSON);

		updateQuery.setUpdateRequest(updateRequest);

		elasticsearchTemplate.update(updateQuery);

	}

	@Override
	public void bulkUpdateIndexData(String indexAliasName, List<IndexData> datas)
	{

		if (CollectionUtils.isEmpty(datas))
		{
			return;
		}

		String indexName = this.getIndexNameByAliasName(indexAliasName);
		Assert.notNull(indexName);

		List<UpdateQuery> queries = new ArrayList<>();

		int count = 0;

		for (IndexData data : datas)
		{

			String id = data.fetchIndexId();

			Assert.notNull(id);

			UpdateQuery updateQuery = new UpdateQuery();

			updateQuery.setIndexName(indexName);
			updateQuery.setType(getIndexDataType(indexName));

			UpdateRequest updateRequest = new UpdateRequest(indexName, getIndexDataType(indexName), id);
			updateRequest.upsert(data);

			count++;

			if (count >= BATCH_SIZE)
			{
				elasticsearchTemplate.bulkUpdate(queries);
				queries.clear();
				count = 0;
			}

		}

		if (CollectionUtils.isNotEmpty(queries))
		{
			elasticsearchTemplate.bulkUpdate(queries);
		}

	}

	@Override
	public List<String> getRelatedIndexs(String indexAliaName)
	{

		List<String> result = new ArrayList<String>();
		Map<String, String> idToNameMap = getIndexNameMap();
		Set<String> nameSet = idToNameMap.keySet();
		// String regEx = "^" + INDEX_ID_PREFIX + indexAliaName +
		// "_(\\d){13}_(\\d){17}$";

		String regEx = "^" + INDEX_ID_PREFIX + indexAliaName + "_(\\d){17}$";

		Pattern p = Pattern.compile(regEx);
		for (String indexId : nameSet)
		{
			if (p.matcher(indexId).find())
			{
				result.add(idToNameMap.get(indexId));
			}
		}
		return result;
	}

	@Override
	public void setIndexSettings(String indexAliasName, Map<String, String> settings)
	{

		String indexname = this.getIndexNameByAliasName(indexAliasName);
		
		if (indexname != null && settings != null && settings.size() > 0)
		{

			Settings.Builder updateSettings = Settings.builder();
			// ImmutableSettings.Builder updateSettings =
			// ImmutableSettings.settingsBuilder();
			Set<String> keySet = settings.keySet();
			
			for (String key : keySet)
			{
				updateSettings.put(key, settings.get(key));
			}
			client.admin().indices().prepareUpdateSettings(indexname).setSettings(updateSettings).execute().actionGet();
		}

	}

	/**
	 * 索引优化，大批量插入时，先把 refresh_interval 设置为 -1；插入完成之后再回复原来的设置。
	 * 
	 */
	@Override
	public Map<String, String> optimumSettingsForBulkIndexing(String indexAliasName)
	{
		String indexname = this.getIndexNameByAliasName(indexAliasName);

		IndexMetaData indexMetaData = client.admin().cluster().prepareState().execute().actionGet().getState()
				.metaData().index(indexname);
		Map<String, String> map = new HashMap<String, String>();

		//
		// settings.put("number_of_shards", properties.get("number_of_shards"));
		// settings.put("number_of_replicas",
		// properties.get("number_of_replicas"));
		// settings.put("refresh_interval", properties.get("refresh_interval") +
		// "s");

		map.put("index.refresh_interval", indexMetaData.getSettings().get("index.refresh_interval"));
		map.put("index.number_of_replicas", indexMetaData.getSettings().get("index.number_of_replicas"));

		// map.put("index.refresh_interval",
		// indexMetaData.getSettings().get("index.refresh_interval"));
		// map.put("index.number_of_replicas",
		// indexMetaData.getSettings().get("index.number_of_replicas"));
		// map.put("index.translog.flush_threshold_ops",
		// indexMetaData.getSettings().get("index.translog.flush_threshold_ops"));

		Settings.Builder updateSettings = Settings.builder();
		updateSettings.put("index.refresh_interval", -1);
		updateSettings.put("index.number_of_replicas", 0);

		// updateSettings.put("index.refresh_interval", -1);
		// updateSettings.put("index.number_of_replicas", 0);
		// updateSettings.put("index.translog.flush_threshold_ops", 100000);
		client.admin().indices().prepareUpdateSettings(indexname).setSettings(updateSettings).execute().actionGet();

		return map;
	}

	@Override
	public SearchResult search(final List<String> indexAliasNameList, final SearchParam searchParam)
	{

		long begin = System.currentTimeMillis();
		logger.debug("[model=EsIndexClient][method=search][indexAliasNameList={}][begin={}]", indexAliasNameList,
				begin);

		final SearchResponse response = executeGet(new ClientCallback<SearchResponse>()
		{
			@SuppressWarnings("rawtypes")
			@Override
			public ActionFuture<SearchResponse> execute(final Client client)
			{

				final SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
				// 过滤条件
				if (searchParam.getFiter() != null)
				{
					sourceBuilder.postFilter(searchParam.getFiter());
				}

				// 匹配条件
				if (searchParam.getQuery() != null)
				{
					sourceBuilder.query(searchParam.getQuery());
				}

				// 每页记录数
				sourceBuilder.size(searchParam.getSize());

				// 当前页起始记录

				sourceBuilder.from(searchParam.getSize() * (searchParam.getPage() - 1));

				// 显示字段，默认全部显示
				if (searchParam.getResultFields().size() > 0)
				{
					for (String field : searchParam.getResultFields())
					{
						sourceBuilder.docValueField(field);
					}
				}

				// 高亮设置
				if (searchParam.isHighlight())
				{
					for (String field : searchParam.getHighlightFields())
					{
						sourceBuilder.highlighter().field(field).preTags(searchParam.getHighlightPreTag())
								.postTags(searchParam.getHighlightPostTag());
					}
				}

				// 分组统计
				List<AbstractAggregationBuilder> aggregations = searchParam.getAggregationBuilders();
				if (aggregations != null && aggregations.size() > 0)
				{
					for (AbstractAggregationBuilder aggregation : aggregations)
					{
						sourceBuilder.aggregation(aggregation);
					}
				}

				// 排序字段
				for (String sortField : searchParam.getSortFields().keySet())
				{
					SortOrder sortValue = searchParam.getSortFields().get(sortField);
					sourceBuilder.sort(sortField, sortValue);
				}

				String[] indexNames = new String[indexAliasNameList.size()];

				List<String> indexNameList = getIndexNamesByIndexAliasList(indexAliasNameList);

				Assert.isTrue(CollectionUtils.isNotEmpty(indexNameList));

				SearchRequest request = Requests.searchRequest(indexNameList.toArray(indexNames));

				request.source(sourceBuilder);
				if (searchParam.getSearchType() != null)
				{
					request.searchType(searchParam.getSearchType());
				}
				logger.trace(" [query={}]", sourceBuilder.toString());
				return client.search(request);
			}
		});

		// 构造返回结果
		SearchResult searchResult = new SearchResult();
		SearchHits hits = response.getHits();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		for (SearchHit hit : hits.getHits())
		{
			Map<String, Object> map;
			if (hit.getSource() != null)
			{
				map = hit.getSource();
			}
			else
			{
				map = new HashMap<String, Object>();
			}
			if (hit.getFields() != null && hit.getFields().size() > 0)
			{
				// 返回指定字段
				for (SearchHitField shf : hit.getFields().values())
				{
					map.put(shf.getName(), shf.getValue());
				}
			}

			if (searchParam.isHighlight())
			{
				// 添加高亮字段信息
				Map<String, Object> highlightMap = new HashMap<String, Object>();

				for (HighlightField hf : hit.getHighlightFields().values())
				{
					highlightMap.put(hf.name(), hf.fragments()[0].toString());
				}
				map.put("_highlight", highlightMap);
			}
			resultList.add(map);
		}

		searchResult.setResultList(resultList);
		searchResult.setTotal(hits.getTotalHits());
		searchResult.setPage(searchParam.getPage());
		if (searchResult.getTotal() % searchParam.getSize() == 0)
		{
			searchResult.setTotalPage(searchResult.getTotal() / searchParam.getSize());
		}
		else
		{
			searchResult.setTotalPage(searchResult.getTotal() / searchParam.getSize() + 1);
		}

		if (response.getAggregations() != null)
		{
			Map<String, Aggregation> aggregations = response.getAggregations().asMap();
			searchResult.setAggMaps(aggregations);
		}
		logger.debug("[model=EsIndexClient][method=search][indexAliasNameList={}][cost={}ms]", indexAliasNameList,
				System.currentTimeMillis() - begin);
		return searchResult;
		//

	}

	@Override
	public SearchResult search(String indexName, SearchParam searchParam)
	{
		if (StringUtils.isBlank(indexName))
			return null;
		List<String> indexNameList = new ArrayList<String>();
		indexNameList.add(indexName);
		return search(indexNameList, searchParam);
	}

	@Override
	public List<Map<String, Object>> multiGet(final String indexName, final Set<String> idList,
			final List<String> fields)
	{
		final MultiGetResponse response = executeGet(new ClientCallback<MultiGetResponse>()
		{
			@Override
			public ActionFuture<MultiGetResponse> execute(final Client client)
			{
				MultiGetRequest request = new MultiGetRequest();
				for (String id : idList)
				{
					MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, null, id);
					if (fields != null && fields.size() > 0)
					{
						item.storedFields((String[]) fields.toArray(new String[fields.size()]));
					}
					request.add(item);
				}
				return client.multiGet(request);
			}
		});

		if (response.getResponses() == null || response.getResponses().length == 0)
		{
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<>();

		for (MultiGetItemResponse item : response.getResponses())
		{

			if (item.getResponse().isExists())
			{

				Map<String, Object> obj = item.getResponse().getSource();

				list.add(obj);
			}
		}

		return list;
	}

	@Override
	public List<Map<String, Object>> multiGet(String indexName, String[] idList, List<String> fields)
	{
		if (idList == null || idList.length == 0 || StringUtils.isEmpty(indexName))
		{
			return null;
		}

		Set<String> idLists = new HashSet<>();

		for (String str : idList)
		{
			idLists.add(str);
		}

		return multiGet(indexName, idLists, fields);
	}

	@Override
	public Map<String, Object> get(final String indexName, final String id)
	{

		final GetResponse response = executeGet(new ClientCallback<GetResponse>()
		{
			@Override
			public ActionFuture<GetResponse> execute(final Client client)
			{
				GetRequest request = new GetRequest(indexName, getIndexDataType(indexName), id);
				return client.get(request);
			}
		});

		return response.getSource();
	}

	/**
	 *  根据索引别名 ，获取索引真实名字列表
	 * @param indexAliasList
	 * @return
	 */
	private List<String> getIndexNamesByIndexAliasList(List<String> indexAliasList)
	{

		if (CollectionUtils.isEmpty(indexAliasList))
		{
			return null;
		}

		List<String> all = new ArrayList<>();

		for (String indexAliasName : indexAliasList)
		{

			String realIdxName = this.getIndexNameByAliasName(indexAliasName);

			if (StringUtils.isNotBlank(realIdxName))
			{
				all.add(realIdxName);
			}
		}

		return all;
	}

	// /**
	// * 创建临时索引
	// *
	// *
	// *
	// */
	// @Override
	// public void createIndex(final String indexAliasName, final String
	// tempIndexAliasName, final Map<String, String> properties,
	// final String mappingContent, boolean force)
	// throws Exception
	// {
	//
	// String oldIndexName = this.getIndexNameByAliasName(indexAliasName);
	//
	//
	// final String indexName;
	//
	// if (oldIndexName == null || force)
	// {
	// // 旧索引不存在 OR 旧索引已存在但强制重建
	// indexName = INDEX_ID_PREFIX + tempIndexAliasName + "_" +
	// DateUtil.date2String(new Date(), "yyyyMMddHHmmssSSS");
	// }
	// else
	// {
	// indexName = null;
	// }
	//
	// // indexName为null，不需要处理
	// if (indexName != null)
	// {
	// // 创建新索引
	// executeGet(new NodeCallback<CreateIndexResponse>()
	// {
	// @Override
	// public ActionFuture<CreateIndexResponse> execute(final IndicesAdminClient
	// admin)
	// {
	// Map<String, String> settings = new HashMap<String, String>();
	// settings.put("number_of_shards", properties.get("number_of_shards"));
	// settings.put("number_of_replicas", properties.get("number_of_replicas"));
	// settings.put("refresh_interval", properties.get("refresh_interval") +
	// "s");
	//
	// // settings.put("translog.flush_threshold_size",
	// // properties.get("translog.flush_threshold_ops"));
	// logger.info("[client={}] [indexAliasName={}] [indexName={}] [props={}]
	// [mapping={}]创建索引", new Object[] { name, indexAliasName,
	// indexName, properties, mappingContent });
	//
	// return
	// admin.create(Requests.createIndexRequest(indexName).settings(settings).mapping(
	// getIndexDataType(tempIndexAliasName), mappingContent,
	// XContentFactory.xContentType(mappingContent)));
	// }
	// });
	//
	// if (oldIndexName != null)
	// {
	// // 删除旧索引名称
	// this.deleteAliasIndexLink(oldIndexName, tempIndexAliasName);
	// }
	//
	// // 建立别名
	// this.createIndexAlias(indexName, indexAliasName);
	//
	// if (oldIndexName != null)
	// {
	// // 删除旧索引
	// this.deleteIndexByIndexName(oldIndexName);
	// }
	// }
	//
	//
	// }
}