
package com.framework.core.search.index.service;
import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import com.framework.core.search.index.callback.IndexCallback;
import com.framework.core.search.index.callback.SearchCallback;
import com.framework.core.search.index.factory.Index;
import com.framework.core.search.model.IndexData;
import com.framework.core.search.model.SearchParam;
import com.framework.core.search.model.SearchResult;




/**
 * 索引管理接口:  管理所有的索引和索引客户端，以及对外提供索引操作接口
 */
public interface IndexService {
	
    /**
     * 通过索引别名获取索引信息
     * 
     * @param indexAliasName 索引别名
     * @return
     */
    public Index getIndex(String indexAliasName);

    /**
     * 判断索引别名是否存在
     * @param indexAliasName 索引别名
     * @return
     */
    public boolean indexExists(String indexAliasName);

    /**
     * 创建索引
     * @param indexAliasName    索引别名
     * @param force        是否强制创建索引，不论索引是否已经存在
     */
    public void createIndex(String indexAliasName, boolean force) throws Exception;

    /**
     * 根据索引别名删除索引
     * @param indexAliasName    索引别名
     */
    public void deleteIndex(String indexAliasName) throws Exception;

    /**
     * 根据索引别名更新索引缓存
     * 
     * @param indexAliasName    索引别名
     */
    public void refreshIndex(String indexAliasName) throws Exception;

    /**
     * 根据索引别名关闭索引
     * @param indexAliasName    索引别名
     */
    public void closeIndex(String indexAliasName) throws Exception;

    /**
     * 根据索引别名刷新索引文件
     * @param indexAliasName    索引别名
     */
    public void flushIndex(String indexAliasName) throws Exception;

    /**
     * 根据索引别名执行索引优化
     * @param indexAliasName
     * @throws Exception
     */
    public void optimize(String indexAliasName) throws Exception;

    /**
     *根据索引别名, 执行索引重建
     * @param indexAliasName 索引别名
     */
    public void rebuild(final String indexAliasName);

    /**
     * 根据索引别名,添加索引数据
     * @param indexAliasName 索引别名
     * @param data      索引数据
     */
    public void addIndexData(String indexAliasName, IndexData data) throws Exception;

    /**
     * 根据索引别名,批量添加索引数据
     * @param indexName 索引名称
     * @param dataList     索引数据列表
     */
    public void addIndexData(String indexAliasName, List<IndexData> dataList) throws Exception;

    /**
     * 根据索引别名,删除索引数据
     * @param indexAliasName
     */
    public void deleteIndexData(String indexAliasName, String id) throws Exception;

    /**
     * 根据索引别名,query删除索引数据
     * @param indexAliasName
     * @param query
     */
    public void deleteIndexDataByQuery(String indexAliasName, QueryBuilder query) throws Exception;

    /**
     * 更新索引数据
     * @param indexAliasName
     */
    public void updateIndexData(final String indexAliasName,  final IndexData data) throws Exception;
    
//    public void updateIndexDataStr(final String indexName, final String id, final String jsonData) throws Exception;

    public void execute(IndexCallback callback, Index index) throws Exception;

    
    
    public String generateTempIndexName(String indexName);

    
    public <T> T execute(SearchCallback<T> callback, Index index);
    
    
    /**
     * 执行搜索。
     * 
     * @param indexName 索引别名
     * @param searchParam 搜索参数
     * @return
     */
    SearchResult search(final String indexAliasName, final SearchParam searchParam);
    
}
