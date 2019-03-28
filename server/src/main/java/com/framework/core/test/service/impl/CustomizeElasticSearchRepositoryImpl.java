package com.framework.core.test.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;

import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.AliasQuery;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.framework.core.test.service.CustomizeElasticSearchRepository;

import io.jsonwebtoken.lang.Collections;

/**
 * 自定义Repository实现类
 *
 * 接口的实现类名字后缀必须为impl才能在扫描包时被找到（可参考spring data elasticsearch自定义repository章节）
 *
 */
@Repository
public class CustomizeElasticSearchRepositoryImpl implements CustomizeElasticSearchRepository
{

	private static final int BATCH_SIZE = 500;

	@Resource
	private ElasticsearchTemplate elasticsearchTemplate;
//
//	private <T> boolean checkIsEsClass(Class<T> classT)
//	{
//
//		return IndexData.class.isAssignableFrom(classT);
//
//	}

//	private <T> String getIndexName(Class<T> classT)
//	{
//
//		checkIsEsClass(classT);
//
//		Document document = classT.getAnnotation(Document.class);
//
//		Assert.notNull(document, "You must set the @document tag for the elasticsearch domain!");
//
//		String indexName = document.indexName();
//
//		Assert.notNull(indexName, "You must set the indexName value in @document tag!");
//
//		return indexName;
//	}

//	/**
//	 * 
//	 * @param classT
//	 * @return
//	 */
//	private <T> String getIndexType(Class<T> classT)
//	{
//
//		checkIsEsClass(classT);
//
//		Document document = classT.getAnnotation(Document.class);
//
//		Assert.notNull(document, "You must set the @document tag for the elasticsearch domain!");
//
//		String indexType = document.indexStoreType();
//
//		if (StringUtils.isEmpty(indexType))
//		{
//
//			indexType = getIndexName(classT) + "_def_type";
//		}
//
//		return indexType;
//	}

	@Override
	public <T> boolean createIndex(Class<T> classT)
	{
		
		return elasticsearchTemplate.createIndex(classT);

	}
//
//	@Override
//	public boolean indexExists(String indexName)
//	{
//		
//		
//		AliasQuery query = new AliasQuery();
//		
//		query.setIndexName("test_es_order_index");
//		query.setAliasName("test_es_order_index_alias");
//			
//		elasticsearchTemplate.addAlias(query);
//		
//		
////		elasticsearchTemplate.getClient().admin().indices().
//		
//		
//		// 获取系统实际存在的索引名列表
//		ClusterState clusterState = elasticsearchTemplate.getClient().admin().cluster().prepareState().execute().actionGet().getState();
//		String[] indices = clusterState.metaData().indices().keys().toArray(String.class);
//
//		Map<String, String> indexIdToNameMap = new HashMap<String, String>();
//
//		ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().routingTable(true).nodes(true).indices(indices);
//		MetaData md = elasticsearchTemplate.getClient().admin().cluster().state(clusterStateRequest).actionGet(30000).getState().metaData();
//
//		for (IndexMetaData imd : md) {
//			
//			for (AliasMetaData amd : imd.getAliases().values().toArray(AliasMetaData.class)) {
//				indexIdToNameMap.put(imd.getIndex().getName(), amd.alias());
//			}
//		}
//		
//		System.out.println("indexIdToNameMap:"+JSON.toJSONString(indexIdToNameMap));
//		
//				
//		return elasticsearchTemplate.indexExists(indexName);
//	}
//
//	@Override
//	public <T> boolean indexExists(Class<T> classT)
//	{
//
//		String indexName = getIndexName(classT);
//
//		return indexExists(indexName);
//	}
//
//	@Override
//	public void deleteIndex(String indexName) throws Exception
//	{
//		elasticsearchTemplate.deleteIndex(indexName);
//	}
//
//	@Override
//	public <T> void deleteIndexByClass(Class<T> classT) throws Exception
//	{
//		String indexName = getIndexName(classT);
//		deleteIndex(indexName);
//	}
//
//	@Override
//	public void refreshIndex(String indexName) throws Exception
//	{
//		elasticsearchTemplate.refresh(indexName);
//
//	}
//
//	@Override
//	public <T> void refreshIndex(Class<T> classT) throws Exception
//	{
//		String indexName = getIndexName(classT);
//
//		refreshIndex(indexName);
//
//	}
//
//	@Override
//	public void addIndexData(IndexData data) throws Exception
//	{
//
//		IndexQuery indexQuery = new IndexQueryBuilder().withId(data.fetchIndexId()).withObject(data).build();
//
//		elasticsearchTemplate.index(indexQuery);
//
//	}
//
//	@Override
//	public void addIndexDataList(List<IndexData> dataList) throws Exception
//	{
//
//		if (Collections.isEmpty(dataList))
//		{
//			return;
//		}
//
//		List<IndexQuery> queries = new ArrayList<>();
//
//		int count = 0;
//		for (IndexData data : dataList)
//		{
//
//			IndexQuery indexQuery = new IndexQueryBuilder().withId(data.fetchIndexId()).withObject(data).build();
//			queries.add(indexQuery);
//			count++;
//
//			if (count >= BATCH_SIZE)
//			{
//				elasticsearchTemplate.bulkIndex(queries);
//				queries.clear();
//				count = 0;
//			}
//		}
//
//		if (CollectionUtils.isNotEmpty(queries))
//		{
//			elasticsearchTemplate.bulkIndex(queries);
//		}
//	}
//
//	@Override
//	public <T> void deleteIndexDataById(Class<T> classT, String id) throws Exception
//	{
//
//		String indexName = getIndexName(classT);
//
//		String type = getIndexType(classT);
//
//		elasticsearchTemplate.delete(indexName, type, id);
//
//	}
//
//	@Override
//	public <T> void deleteIndexDataByIds(Class<T> classT, List<String> ids) throws Exception
//	{
//		if (CollectionUtils.isEmpty(ids))
//		{
//			return;
//		}
//
//		String[] idsStr = new String[ids.size()];
//
//		for (int i = 0; i < ids.size(); i++)
//		{
//			idsStr[i] = ids.get(i);
//		}
//
//		String indexName = getIndexName(classT);
//
//		String type = getIndexType(classT);
//
//		DeleteQuery deleteQuery = new DeleteQuery();
//
//		BoolQueryBuilder qb = QueryBuilders.boolQuery();
//		qb.must(QueryBuilders.idsQuery().addIds(idsStr));
//		deleteQuery.setQuery(qb);
//		deleteQuery.setIndex(indexName);
//		deleteQuery.setType(type);
//
//		elasticsearchTemplate.delete(deleteQuery);
//
//	}
//
//	@Override
//	public <T> void deleteByQuery(Class<T> classT, Map<String, Object> filedContentMap)
//	{
//
//		String indexName = getIndexName(classT);
//
//		String type = getIndexType(classT);
//		DeleteQuery dq = new DeleteQuery();
//
//		BoolQueryBuilder qb = QueryBuilders.boolQuery();
//		if (filedContentMap != null)
//			for (String key : filedContentMap.keySet())
//			{// 字段查询
//				qb.must(QueryBuilders.matchQuery(key, filedContentMap.get(key)));
//			}
//		dq.setQuery(qb);
//		dq.setIndex(indexName);
//
//		dq.setType(type);
//
//		elasticsearchTemplate.delete(dq);
//	}
//
//	/**
//	 * 构建search query对象
//	 * @param pageable
//	 * @param highlightFields
//	 * @param sortBuilders
//	 * @param queryBuilder
//	 * @param filterBuilder
//	 * @param aggregationBuilders
//	 * @param indices
//	 * @param minScore
//	 * @return
//	 */
//	@SuppressWarnings("rawtypes")
//	public SearchQuery buildSearchQuery(Pageable pageable, Field[] highlightFields, List<SortBuilder> sortBuilders,
//			QueryBuilder queryBuilder, QueryBuilder filterBuilder, List<AbstractAggregationBuilder> aggregationBuilders,
//			String[] indices, float minScore)
//	{
//
//		NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//
//		if (pageable != null)
//		{
//			builder.withPageable(pageable);
//		}
//
//		if (highlightFields != null)
//		{
//			builder.withHighlightFields(highlightFields);
//		}
//
//		if (CollectionUtils.isNotEmpty(sortBuilders))
//		{
//			for (SortBuilder sb : sortBuilders)
//			{
//				builder.withSort(sb);
//			}
//		}
//
//		if (queryBuilder != null)
//		{
//			builder.withQuery(queryBuilder);
//		}
//
//		if (filterBuilder != null)
//		{
//			builder.withFilter(filterBuilder);
//		}
//
//		if (CollectionUtils.isNotEmpty(aggregationBuilders))
//		{
//			for (AbstractAggregationBuilder aggrebuilder : aggregationBuilders)
//			{
//				builder.addAggregation(aggrebuilder);
//			}
//		}
//
//		if (indices != null)
//		{
//			builder.withIndices(indices);
//		}
//
//		if (minScore > 0)
//		{
//			builder.withMinScore(minScore);
//		}
//
//		return builder.build();
//	}
//
//	/**
//	 * 构建高亮builder
//	 * @param highlightFields
//	 * @return
//	 */
//	private Field[] buildHighlightFiled(final List<String> highlightFields)
//	{
//
//		Field[] hfields = new Field[0];
//		if (highlightFields != null)
//		{
//			hfields = new Field[highlightFields.size()];
//			for (int i = 0; i < highlightFields.size(); i++)
//			{
//				hfields[i] = new HighlightBuilder.Field(highlightFields.get(i)).preTags("<em style='color:red'>")
//						.postTags("</em>").fragmentSize(250);
//			}
//		}
//
//		return hfields;
//	}
//
//	/**
//	 * 构建分页builder
//	 * @param page
//	 * @return
//	 */
//	private Pageable buildPageable(ElasticSearchPage page)
//	{
//
//		return page == null ? null : PageRequest.of(page.getPageNo(), page.getPageSize());
//	}
//
//	/**
//	 * 构建排序builder
//	 * @param sortMap
//	 * @return
//	 */
//	@SuppressWarnings("rawtypes")
//	private List<SortBuilder> buildFieldSortBuilders(Map<String, SortOrder> sortMap)
//	{
//
//		if (MapUtils.isEmpty(sortMap))
//		{
//			return null;
//		}
//
//		List<SortBuilder> list = new ArrayList<>();
//
//		for (Entry<String, SortOrder> entry : sortMap.entrySet())
//		{
//
//			list.add(new FieldSortBuilder(entry.getKey()).order(entry.getValue()));
//		}
//
//		return list;
//	}
//
//	/**
//	 * 构建查询builder
//	 * @param filedContentMap
//	 * @return
//	 */
//	private QueryBuilder buildQueryBuilder(Map<String, Object> filedContentMap)
//	{
//
//		if (MapUtils.isEmpty(filedContentMap))
//		{
//			return null;
//		}
//		BoolQueryBuilder qb = QueryBuilders.boolQuery();
//		for (String key : filedContentMap.keySet())
//		{// 字段查询
//			qb.must(QueryBuilders.matchQuery(key, filedContentMap.get(key)));
//
//		}
//
//		return qb;
//	}
//
//	@Override
//	public <T> List<T> queryByIds(Class<T> classT, List<String> ids)
//	{
//		if (CollectionUtils.isEmpty(ids))
//		{
//			return null;
//		}
//
//		String indexName = getIndexName(classT);
//
//		String[] idsSet = new String[ids.size()];
//
//		for (int i = 0; i < ids.size(); i++)
//		{
//
//			idsSet[i] = ids.get(i);
//
//		}
//
//		QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds(idsSet);
//
//		NativeSearchQueryBuilder nsb = new NativeSearchQueryBuilder().withQuery(queryBuilder).withIndices(indexName);
//		SearchQuery searchQuery = nsb.build();// 查询建立
//
//		return elasticsearchTemplate.queryForList(searchQuery, classT);
//	}
//
//	@Override
//	public <T> ElasticSearchPage queryByPage(final Class<T> classT, ElasticSearchPage basePage,
//			final Map<String, Object> filedContentMap, final List<String> highlightFields, final Map<String, SortOrder> sortMap)
//	{
//
//		// 高亮
//		Field[] hfields = buildHighlightFiled(highlightFields);
//
//		// 排序
//		@SuppressWarnings("rawtypes")
//		List<SortBuilder> sortBuilders = buildFieldSortBuilders(sortMap);
//
//		// 分页
//		Pageable pageable = buildPageable(basePage);
//
//		// 查询条件
//		QueryBuilder queryBuilder = buildQueryBuilder(filedContentMap);
//
//		//
//		SearchQuery searchQuery = buildSearchQuery(pageable, hfields, sortBuilders, queryBuilder, null, null, null, -1);
//
//		if (CollectionUtils.isEmpty(highlightFields))
//		{
//
//			AggregatedPage<T> page = elasticsearchTemplate.queryForPage(searchQuery, classT);
//
//			Assert.notNull(page);
//
//			basePage.setTotalRecord(page.getTotalElements());
//
//			basePage.setData(page.getContent());
//
//			return basePage;
//		}
//
//		AggregatedPage<T> page = elasticsearchTemplate.queryForPage(searchQuery, classT, new SearchResultMapper()
//		{
//
//			@SuppressWarnings("hiding")
//			@Override
//			public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable)
//			{
//
//				List<T> chunk = new ArrayList<T>();
//
//				for (SearchHit searchHit : response.getHits())
//				{
//
//					if (response.getHits().getHits().length <= 0)
//					{
//						return null;
//					}
//
//					Assert.notNull(searchHit.getSource());
//
//					// 将文档中的每一个对象转换json串值
//					String json = searchHit.getSourceAsString();
//					// 将json串值转换成对应的实体对象
//					T data = (T) JSON.parseObject(json, classT);
//
//					// // 返回数据是否为空
//					// if (MapUtils.isEmpty())
//					// {
//					// chunk.add(data);
//					//
//					// continue;
//					// }
//
//					Map<String, Object> entityMap = new HashMap<>();
//
//					for (String highName : highlightFields)
//					{
//						Text text[] = searchHit.getHighlightFields().get(highName).fragments();
//						if (text.length > 0)
//						{
//							String highValue = searchHit.getHighlightFields().get(highName).fragments()[0].toString();
//							entityMap.put(highName, highValue);
//						}
//					}
//
//					//
//					for (Entry<String, Object> entry : entityMap.entrySet())
//					{
//						try
//						{
//							java.lang.reflect.Field field = classT.getDeclaredField(entry.getKey());
//							field.setAccessible(true);
//							field.set(data, entry.getValue());
//
//						}
//						catch (NoSuchFieldException e)
//						{
//							e.printStackTrace();
//						}
//						catch (SecurityException e)
//						{
//							e.printStackTrace();
//						}
//						catch (IllegalArgumentException e)
//						{
//							e.printStackTrace();
//						}
//						catch (IllegalAccessException e)
//						{
//							e.printStackTrace();
//						}
//
//					}
//
//					chunk.add(data);
//				}
//
//				if (chunk.size() > 0)
//				{
//					return new AggregatedPageImpl<T>((List<T>) chunk);
//				}
//
//				return new AggregatedPageImpl<T>(new ArrayList<T>());
//			}
//		});
//
//		Assert.notNull(page);
//
//		basePage.setTotalRecord(page.getTotalElements());
//
//		basePage.setData(page.getContent());
//
//		return basePage;
//	}
//
//	/**
//	 * 
//	 * @param customSearchQuery
//	 * @return
//	 */
//	private SearchQuery buildSearchQueryRequest(CustomizeSearchQuery customSearchQuery, ElasticSearchPage Page)
//	{
//
//		Field[] hfields = buildHighlightFiled(customSearchQuery.getHighlightFields());
//
//		@SuppressWarnings("rawtypes")
//		List<SortBuilder> sortBuilders = buildFieldSortBuilders(customSearchQuery.getSortMap());
//
//		Pageable pageable = buildPageable(Page);
//
//		BoolQueryBuilderTemplate boolBuilderTemplate = customSearchQuery.getBoolQueryBuilderTemplate();
//
//		QueryBuilder builder = (boolBuilderTemplate == null ? null : boolBuilderTemplate.getBoolQueryBuilder());
//
//		SearchQuery searchQuery = buildSearchQuery(pageable, hfields, sortBuilders, builder, null, null, null, -1);
//
//		return searchQuery;
//	}
//
//	@Override
//	public <T> ElasticSearchPage queryByPage(final Class<T> classT, final CustomizeSearchQuery customSearchQuery,
//			ElasticSearchPage basePage)
//	{
//
//		SearchQuery searchQuery = buildSearchQueryRequest(customSearchQuery, basePage);
//
//		if (CollectionUtils.isEmpty(customSearchQuery.getHighlightFields()))
//		{
//
//			AggregatedPage<T> page = elasticsearchTemplate.queryForPage(searchQuery, classT);
//
//			Assert.notNull(page);
//
//			basePage.setTotalRecord(page.getTotalElements());
//
//			basePage.setData(page.getContent());
//
//			return basePage;
//		}
//
//		AggregatedPage<T> page = elasticsearchTemplate.queryForPage(searchQuery, classT, new SearchResultMapper()
//		{
//
//			@SuppressWarnings("hiding")
//			@Override
//			public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable)
//			{
//
//				List<T> chunk = new ArrayList<T>();
//
//				for (SearchHit searchHit : response.getHits())
//				{
//
//					if (response.getHits().getHits().length <= 0)
//					{
//						return null;
//					}
//
//					// 将文档中的每一个对象转换json串值
//					String json = searchHit.getSourceAsString();
//					// 将json串值转换成对应的实体对象
//					T data = (T) JSON.parseObject(json, classT);
//
//
//					if (MapUtils.isEmpty(searchHit.getSource()))
//					{
//						chunk.add(data);
//
//						continue;
//					}
//					
//
//					Map<String, Object> entityMap = new HashMap<>();
//
//					
//					for (String highName : customSearchQuery.getHighlightFields())
//					{
//						Text text[] = searchHit.getHighlightFields().get(highName).fragments();
//						if (text.length > 0)
//						{
//							String highValue = searchHit.getHighlightFields().get(highName).fragments()[0].toString();
//							entityMap.put(highName, highValue);
//						}
//					}
//
//					//
//					for (Entry<String, Object> entry : entityMap.entrySet())
//					{
//						try
//						{
//							java.lang.reflect.Field field = classT.getDeclaredField(entry.getKey());
//
//							field.setAccessible(true);
//
//							field.set(data, entry.getValue());
//
//						}
//						catch (NoSuchFieldException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						catch (SecurityException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						catch (IllegalArgumentException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						catch (IllegalAccessException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//					}
//
//					chunk.add(data);
//				}
//				if (chunk.size() > 0)
//				{
//					return new AggregatedPageImpl<T>((List<T>) chunk);
//				}
//				return new AggregatedPageImpl<T>(new ArrayList<T>());
//			}
//		});
//
//		Assert.notNull(page);
//
//		basePage.setTotalRecord(page.getTotalElements());
//
//		basePage.setData(page.getContent());
//
//		return basePage;
//
//	}
//
//	@Override
//	public <T> List<T> queryByList(Class<T> classT, CustomizeSearchQuery searchQuery)
//	{
//		SearchQuery query = buildSearchQueryRequest(searchQuery, null);
//
//		return elasticsearchTemplate.queryForList(query, classT);
//
//	}
//
//	@Override
//	public <T> T queryOne(Class<T> classT, CustomizeSearchQuery searchQuery)
//	{
//
//		List<T> list = queryByList(classT, searchQuery);
//
//		Assert.isTrue(list == null || list.size() == 1);
//
//		return list.get(0);
//
//	}

}