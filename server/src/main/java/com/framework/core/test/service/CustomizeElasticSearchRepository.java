package com.framework.core.test.service;

import java.util.List;
import java.util.Map;

import org.elasticsearch.search.sort.SortOrder;

//import com.waiqin365.crawler.elasticsearch.domain.IndexData;
//import com.waiqin365.crawler.elasticsearch.internal.builder.view.CustomizeSearchQuery;
//import com.waiqin365.crawler.elasticsearch.view.ElasticSearchPage;


public interface CustomizeElasticSearchRepository {
    /**
     * 创建索引,
     * 
     * 
     * @return
     */
    public <T> boolean createIndex(Class<T> classT);
    
//    
//    /**
//     * 判断索引是否存在
//     * @param indexName 索引名称
//     * @return
//     */
//    public boolean indexExists(String indexName);
//    
//    
//    
//    /**
//     * 判断索引是否存在
//     * @param indexName 索引名称
//     * @return
//     */
//    public <T> boolean indexExists(Class<T> classT);
//    
//    
//    
//    /**
//     * 删除索引
//     * @param indexName    索引名称
//     */
//    public void deleteIndex(String indexName) throws Exception;
//    
//    
//    /**
//     * 删除索引，根据搜索引擎类 class
//     * @param indexName    索引名称
//     */
//    public <T> void deleteIndexByClass(Class<T> classT) throws Exception;
//
//    /**
//     * 更新索引缓存
//     * @param indexName    索引名称
//     */
//    public void refreshIndex(String indexName) throws Exception;
//    
//    
//    /**
//     * 更新索引缓存，，根据搜索引擎类 class
//     * @param indexName    索引名称
//     */
//    public <T> void refreshIndex(Class<T> classT) throws Exception;
//    
//    
//    /**
//     * 添加索引数据
//     * @param indexName 索引名称
//     * @param data      索引数据
//     */
//    public void addIndexData(IndexData data) throws Exception;
//
//    /**
//     * 批量添加索引数据
//     * @param indexName 索引名称
//     * @param dataList     索引数据列表
//     */
//    public void addIndexDataList(List<IndexData> dataList) throws Exception;
//    
//    
//    /**
//     * 删除索引数据
//     * @param indexName
//     */
//    public <T> void deleteIndexDataById(Class<T> classT, String id) throws Exception;
//    
//    
//    /**
//     * 删除索引数据
//     * @param indexName
//     */
//    public  <T> void deleteIndexDataByIds(Class<T> classT, List<String> ids) throws Exception;
//
//
//    /**
//     * 删除
//     * @param filedContentMap
//     * @return
//     */
//    public <T> void deleteByQuery(Class<T> classT,Map<String,Object> filedContentMap) ;
//    
//    
//
//    /**
//     * 根据ids查询
//     * @param classT
//     * @param ids
//     * @return
//     */
//    public <T> List<T> queryByIds(Class<T> classT,List<String> ids);
//    
//    
//    /**
//     * 分页查询
//     * @param classT
//     * @param searchQuery
//     * @param page
//     * @return
//     */
//    public <T> ElasticSearchPage queryByPage(Class<T> classT, CustomizeSearchQuery  searchQuery,ElasticSearchPage basePage);
//    
//    
//    
//    /**
//     * 查询list
//     * @param classT
//     * @param searchQuery
//     * @return
//     */
//    public <T> List<T> queryByList(Class<T> classT, CustomizeSearchQuery  searchQuery);
//    
//    
//    /**
//     * 查询单个
//     * @param classT
//     * @param searchQuery
//     * @return
//     */
//    public <T> T queryOne(Class<T> classT, CustomizeSearchQuery  searchQuery);
//    
//    
//    
//    /**
//     * 分页查询
//     * @param classT
//     * @param basePage
//     * @param filedContentMap
//     * @param highlightFields
//     * @param sortMap
//     * @return
//     */
//    public <T> ElasticSearchPage queryByPage(Class<T> classT, ElasticSearchPage basePage,Map<String, Object> filedContentMap, final List<String> highlightFields, Map<String,SortOrder> sortMap);
//    
//    
    
}