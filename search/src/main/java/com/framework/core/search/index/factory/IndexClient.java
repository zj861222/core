package com.framework.core.search.index.factory;



import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;

import com.framework.core.search.model.IndexData;
import com.framework.core.search.model.SearchParam;
import com.framework.core.search.model.SearchResult;


/**
 * 索引器接口，代表一种索引服务的客户端
 */
public interface IndexClient {


    /**
     * 获取索引器名称
     *
     * @return
     */
    public String getName();

    /**
     * 设置索引器名称
     *
     * @param name
     */
    public void setName(String name);

    /**
     * 判断索引是否存在
     *
     * @param indexAliasName 索引别名
     * @return
     */
    public boolean indexExists(final String indexAliasName);

    /**
     * 判断索引是否健康
     *
     * @param indexAliasName 索引别名
     * @return
     */
    public boolean indexHealth(final String indexAliasName);

    /**
     * 创建索引
     *
     * @param indexAliasName  索引别名
     * @param properties 索引配置
     * @param force      是否强制创建索引，不论索引是否已经存在
     */
    public void createIndex(final String indexAliasName, final Map<String, String> properties, final String mappingContent, boolean force) throws Exception;

//    /**
//     * 创建索引
//     *
//     * @param indexAliasName  索引别名
//     * @param tempIndexName 临时索引名称
//     * @param properties 索引配置
//     * @param force      是否强制创建索引，不论索引是否已经存在
//     */
//    public void createIndex(final String indexAliasName, final String tempIndexName, final Map<String, String> properties, final String mappingContent, boolean force)  throws Exception;

    /**
     * 删除索引根据别名
     *
     * @param indexAliasName 索引别名
     */
    public void deleteIndexByAlias(final String indexAliasName);
    
    
    /**
     * 删除索引，根据名字
     *
     * @param indexName 索引名
     */
    public void deleteIndexByIndexName(final String indexName);
    

    /**
     * 更新索引缓存
     *
     * @param indexAliasName 索引别名
     */
    public void refreshIndex(final String indexAliasName);

    /**
     * 关闭索引
     *
     * @param indexAliasName 索引别名
     */
    public void closeIndex(final String indexAliasName);

    /**
     * 刷新索引文件
     *
     * @param indexAliasName 索引别名
     */
    public void flushIndex(final String indexAliasName);

    /**
     * 索引别名替换:将老索引的索引名加到新索引别名上，并删除老索引别名
     *
     * @param oldIndexAliasName 旧索引别名
     * @param newIndexAliasName 新索引别名
     */
    public void replaceIndex(final String oldIndexAliasName, final String newIndexAliasName)  throws Exception;

    /**
     * 删除因系统停机或报错而产生未被删除的临时索引
     * @param indexName
     */
    public void deleteTempIndex(final String indexAliasName);
    
    

    /**
     * 执行索引优化
     */
    public void optimize(final String indexAliasName);


    /**
     * 添加索引数据
     *
     * @param indexAliasName 索引别名
     * @param id        索引ID
     * @param data      索引数据
     */
    public void addIndexData(final String indexAliasName, final IndexData data);

    /**
     * 添加索引数据
     *
     * @param indexAliasName 索引别名
     * 
     * @param datas     索引数据列表
     */
    public void addIndexDataList(final String indexAliasName, final List<IndexData> datas);
    

    /**
     * 删除索引数据
     *
     * @param indexAliasName
     */
    public void deleteIndexData(final String indexAliasName, String id);

    /**
     * 根据query删除索引数据
     * @param indexAliasName
     * @param queryBuilder
     */
    public void deleteIndexDataByQuery(final String indexAliasName, QueryBuilder queryBuilder);

    
    
    
    /**
     * 更新索引数据
     *
     * @param indexAliasName
     */
    public void updateIndexData(final String indexAliasName, final IndexData data);
    
//    public void updateIndexDataStr(final String indexAliasName, final String id, final String jsonData);

    /**
     * 批量更新（删除）数据
     * @param indexName
     * @param datas 待更新的数据列表
     */
    public void bulkUpdateIndexData(final String indexAliasName, final List<IndexData> datas);

    /**
     * 获取所有相关的索引
     * @param name
     * @return
     */
    public List<String> getRelatedIndexs(String name);

    /**
     * 索引配置
     * @param indexAliasName  索引别名
     * @param setting 设置属性
     */
    public void setIndexSettings(String indexAliasName, Map<String, String> setting);

    /**
     * 在批量插入数据之前，进行索引优化配置
     * 返回优化配置之前的配置
     * @param indexName
     * @return
     */
    public Map<String, String> optimumSettingsForBulkIndexing(String indexName);
    
    /**
     * 检索接口
     * @param indexAliasNameList 别名列表
     * @param searchParam
     * @return
     */
    public SearchResult search(final List<String> indexAliasNameList, final SearchParam searchParam);
    
    /**
     * 检索接口
     * @param indexAliasName  别名
     * @param searchParam
     * @return
     */
    public SearchResult search(final String indexAliasName, final SearchParam searchParam);
    
    /**
     * 获取多个
     * @param indexName
     * @param idList
     * @param fields
     * @return
     */
    public List<Map<String, Object>> multiGet(final String indexName, final Set<String> idList, final List<String> fields);
    
    /**
     * 
     * @param indexName
     * @param idList
     * @param fields
     * @return
     */
    public List<Map<String, Object>> multiGet(final String indexName, final String[] idList, final List<String> fields);
    
    /**
     * 
     * @param indexName
     * @param id
     * @return
     */
    public Map<String, Object> get(final String indexName, final String id);
}
