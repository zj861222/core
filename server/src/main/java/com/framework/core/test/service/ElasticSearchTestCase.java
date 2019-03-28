//package com.waiqin365.crawler.elasticsearch.internal.service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.annotation.Resource;
//
//import org.elasticsearch.search.sort.SortOrder;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.fastjson.JSON;
//import com.waiqin365.crawler.elasticsearch.domain.IndexData;
//import com.waiqin365.crawler.elasticsearch.domain.Order;
//import com.waiqin365.crawler.elasticsearch.internal.builder.BoolQueryBuilderTemplate;
//import com.waiqin365.crawler.elasticsearch.internal.builder.QueryMatchType;
//import com.waiqin365.crawler.elasticsearch.internal.builder.view.CustomizeSearchQuery;
//import com.waiqin365.crawler.elasticsearch.view.ElasticSearchPage;
//
//@Component
//public class ElasticSearchTestCase
//{
//
//	@Resource
//	private CustomizeElasticSearchRepository customizeElasticSearchRepository;
//
//	public void test(int index)
//	{
//		
//		customizeElasticSearchRepository.indexExists(Order.class);
////		testAddData2();
//		testQuery();
//
//	}
//
//	/**
//	 * 看索引是否创建
//	 */
//	public void testIndexCreate()
//	{
//
//		boolean exist = customizeElasticSearchRepository.indexExists(Order.class);
//		boolean exist2 = customizeElasticSearchRepository.indexExists("test_es_order_index");
//		System.out.println("exist:" + exist + ",exist2:" + exist2);
//
//	}
//
//	public void testAddData2()
//	{
//
//		Order orderSingle = new Order();
//		orderSingle.setId(100L);
//		orderSingle.setSkuName("sku_2 1 3" );
//		orderSingle.setUserName("username_" + orderSingle.getId());
//		
//		
//		Order orderSingle2 = new Order();
//
//		orderSingle2.setId(101L);
//		orderSingle2.setSkuName("sku_1 1 3" );
//		orderSingle2.setUserName("username_" + orderSingle2.getId());
//		
//		
//		Order orderSingle3 = new Order();
//
//		orderSingle3.setId(101L);
//		orderSingle3.setSkuName("1 sku 213" );
//		orderSingle3.setUserName("username_" + orderSingle3.getId());
//		
//		try
//		{
//			customizeElasticSearchRepository.addIndexData(orderSingle);
//			
//			customizeElasticSearchRepository.addIndexData(orderSingle2);
//			
//			customizeElasticSearchRepository.addIndexData(orderSingle3);
//
//
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	
//	
//	
//	
//	public void testAddData()
//	{
//
//		List<IndexData> orderList = new ArrayList<>();
//
//		for (long i = 0; i < 10; i++)
//		{
//
//			Order order = new Order();
//
//			order.setId(i);
//
//			order.setSkuName("sku_" + i % 5);
//			order.setUserName("username_" + i);
//
//			orderList.add(order);
//		}
//
//		Order orderSingle = new Order();
//
//		orderSingle.setId(99L);
//		orderSingle.setSkuName("sku_" + orderSingle.getId() % 5);
//		orderSingle.setUserName("username_" + orderSingle.getId());
//
//		try
//		{
//			// 单个插入
//			customizeElasticSearchRepository.addIndexData(orderSingle);
//
//			// 批量插入
//			customizeElasticSearchRepository.addIndexDataList(orderList);
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void testDeleteData() throws Exception
//	{
//
//		// 删除根据id
//		customizeElasticSearchRepository.deleteIndexDataById(Order.class, "99");
//
//		List<String> ids = new ArrayList<>();
//		ids.add("1");
//		ids.add("2");
//		// 批量删除id
//		customizeElasticSearchRepository.deleteIndexDataByIds(Order.class, ids);
//
//		// 根据class定义删除索引
//		customizeElasticSearchRepository.deleteIndexByClass(Order.class);
//		// 根据名字删除索引
//		customizeElasticSearchRepository.deleteIndex("test_es_order_index");
//
//	}
//
//	
//	private CustomizeSearchQuery buildCustomizeSearchQuery(){
//		
//		
//		CustomizeSearchQuery query = new CustomizeSearchQuery();
//	
//
//
////		Map<String, Object> filedContentMap = new HashMap<>();
////		filedContentMap.put("skuName", "sku_1");
////		BoolQueryBuilderTemplate template = new BoolQueryBuilderTemplate().withMatchQueryForAttrVal(QueryMatchType.TYPE_MUST, filedContentMap);
////		query.withBoolQueryBuilder(template);
//
//		
//
////		Map<String, Object> filedContentMap = new HashMap<>();
////		filedContentMap.put("skuName", "sku_2 3");
////		BoolQueryBuilderTemplate template = new BoolQueryBuilderTemplate().withMatchPhraseQueryForAttrVal(QueryMatchType.TYPE_MUST, filedContentMap);
////		query.withBoolQueryBuilder(template);
//		
//		
//		
//		Map<String, Object> filedContentMap = new HashMap<>();
//		filedContentMap.put("skuName", "sku_2");
//		BoolQueryBuilderTemplate template = new BoolQueryBuilderTemplate().withTermQueryForAttrVal(QueryMatchType.TYPE_MUST, filedContentMap);
//		query.withBoolQueryBuilder(template);	
//		
//		
//		
//
//		List<String> highlightFields = new ArrayList<>();
//		highlightFields.add("skuName");
//		query.withHighlightFields(highlightFields);
//		
//		
//		Map<String, SortOrder> sortMap = new HashMap<>();
//		sortMap.put("id", SortOrder.DESC);
//		query.withSortMap(sortMap);
//
//		return query;
//		
//	}
//	
//	
//	
//	public void testQuery()
//	{
//
//		
//		
//		List<String> idList = new ArrayList<>();
//		idList.add("1");
//		idList.add("2");
//		idList.add("621");
//		
//		List<Order> orderList = customizeElasticSearchRepository.queryByIds(Order.class, idList);
//
////		System.out.println(JSON.toJSONString(orderList));
//
//		
//		CustomizeSearchQuery searchQuery = buildCustomizeSearchQuery();
//		
//		ElasticSearchPage basePage = new ElasticSearchPage();
//		basePage.setPageNo(0);
//		basePage.setPageSize(5);
//		
//		ElasticSearchPage page = customizeElasticSearchRepository.queryByPage(Order.class, searchQuery, basePage);
//		
//		System.out.println(JSON.toJSONString(page));
//
//	}
//
//}