package com.framework.core.web.tree.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.framework.core.common.lock.SegmentLock;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.tree.TreeCacheBuilder;
import com.framework.core.web.tree.TreeCacheTemplate;
import com.framework.core.web.tree.TreeCacheTemplateManager;
import com.framework.core.web.tree.constants.TreeCacheErrorCode;
import com.framework.core.web.tree.constants.RedisTreeCacheKeyBuilder;
import com.framework.core.web.tree.constants.TreeTypeEnum;
import com.framework.core.web.tree.exception.TreeCacheChangeException;
import com.framework.core.web.tree.model.CodedTreeData;
import com.framework.core.web.tree.model.TreeData;
import com.framework.core.web.tree.utils.TreeNodeUtils;

@Component
public class DatabaseTreeCacheDataFetcher
{
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTreeCacheDataFetcher.class);
    
    @Resource
    private TreeCacheTemplateManager treeCacheTemplateManager;
    
    @Resource
    private RedisTreeCacheDataFetcher redisTreeCacheDataFetcher;
    
    // 分段锁
    private SegmentLock<String> segmentLock = new SegmentLock<>();
    
    public Map<String, Long> getNodeCodeFromDbDirectly(TreeTypeEnum treeTypeEnum, List<Long> nodeIds)
    {
        
        String lockKey = "treecache_" + treeTypeEnum.getTreeType();
        
        boolean isLocked = false;
        
        try
        {
            isLocked = segmentLock(lockKey);
            
            // 等待1分钟没获取到锁，抛出异常
            if (!isLocked)
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_WAIT_LOCK_TIMEOUT.getCode());
            }
            
            // 再尝试从缓存获取一下
            Map<String, Long> mapResult =
                redisTreeCacheDataFetcher.getNodeCodeFromCacheByNodeIds(treeTypeEnum, nodeIds);
            
            // 有val不为空的，就返回;否则尝试从db库里取
            if (MapUtils.isNotEmpty(mapResult))
            {
                return mapResult;
            }
            
            List<CodedTreeData> all = fetchAndCodedTreeData(treeTypeEnum);
            
            if (CollectionUtils.isEmpty(all))
            {
                return null;
            }
            
            Map<String, CodedTreeData> map = getMapStructure(all);
            
            if (MapUtils.isEmpty(map))
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE.getCode());
            }
            
            mapResult = new HashMap<>();
            
            for (Long id : nodeIds)
            {
                
                String hashKey = RedisTreeCacheKeyBuilder.buildIdHashKey(id);
                
                CodedTreeData source = map.get(hashKey);
                
                if (source == null)
                {
                    throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE.getCode());
                }
                
                mapResult.put(source.getId() + "", source.getNodeCode());
                
            }
            
            return mapResult;
            
        }
        finally
        {
            
            if (isLocked)
            {
                segmentLock.unlock(lockKey);
            }
            
        }
        
    }
    
    /**
     * 获取树形数据，并进行编码。每次执行这个，说明没走缓存，需要刷新生成缓存了
     * @param treeTypeEnum
     * @return
     */
    private List<CodedTreeData> fetchAndCodedTreeData(TreeTypeEnum treeTypeEnum)
    {
        
        TreeCacheTemplate template = getTreeCacheTemplateInstance(treeTypeEnum);
        
        List<TreeData> dataList = template.getAllTreeData();
        
        if (CollectionUtils.isEmpty(dataList))
        {
            logger.info("buildTreeCache while data is null, exist!![treeTypeEnum]:" + treeTypeEnum.getTreeType());
            return null;
        }
        
        TreeCacheBuilder builder = new TreeCacheBuilder();
        // 编码
        List<CodedTreeData> result = builder.codedSourceTreeData(dataList, treeTypeEnum.getMaxLv());
        
        return result;
        
    }
    
    /**
     * 获取实例
     * @param treeTypeEnum
     * @return
     */
    private TreeCacheTemplate getTreeCacheTemplateInstance(TreeTypeEnum treeTypeEnum)
    {
        
        TreeCacheTemplate template = treeCacheTemplateManager.getTreeCacheTemplateInstance(treeTypeEnum);
        
        if (template == null)
        {
            throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_TEMPLATE_NOT_EXIST.getCode());
        }
        
        return template;
    }
    
    private Map<String, CodedTreeData> getMapStructure(List<CodedTreeData> result)
    {
        
        if (CollectionUtils.isEmpty(result))
        {
            return null;
        }
        
        Map<String, CodedTreeData> map = new HashMap<>();
        
        for (CodedTreeData data : result)
        {
            String idkey = RedisTreeCacheKeyBuilder.buildIdHashKey(data.getId());
            map.put(idkey, data);
            
            String nodeCodeKey = RedisTreeCacheKeyBuilder.buildNodeCodeHashKey(data.getNodeCode());
            map.put(nodeCodeKey, data);
            
        }
        
        return map;
    }
    
    /**
     * 查询父节点id
     * @param treeTypeEnum
     * @param nodeId
     * @param nodeCode
     * @return
     */
    public List<Long> getParentNodeIds(TreeTypeEnum treeTypeEnum, long nodeId, long nodeCode)
    {
        
        String lockKey = "treecache_" + treeTypeEnum.getTreeType();
        
        boolean isLocked = false;
        
        try
        {
            isLocked = segmentLock(lockKey);
            
            // 等待1分钟没获取到锁，抛出异常
            if (!isLocked)
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_WAIT_LOCK_TIMEOUT.getCode());
            }
            
            List<Long> parentIds = new ArrayList<>();
            
            try
            {
                parentIds = redisTreeCacheDataFetcher.getParentNodeIds(treeTypeEnum, nodeId, nodeCode);
                
                if (CollectionUtils.isNotEmpty(parentIds))
                {
                    return parentIds;
                }
                
            }
            catch (TreeCacheChangeException e)
            {
                logger.warn(" DatabaseTreeCacheDataFetcher Tree Cache getParentNodeIds TreeCacheChangeException,[type]:"
                    + treeTypeEnum.getTreeType() + ",[nodeId]" + nodeId, e);
                
            }
            
            // 树形节点编码
            List<CodedTreeData> all = fetchAndCodedTreeData(treeTypeEnum);
            
            if (CollectionUtils.isEmpty(all))
            {
                return null;
            }
            
            Map<String, CodedTreeData> map = getMapStructure(all);
            
            if (MapUtils.isEmpty(map))
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE.getCode());
            }
            
            String oriNodeHashKey = RedisTreeCacheKeyBuilder.buildNodeCodeHashKey(nodeCode);
            
            CodedTreeData orisource = map.get(oriNodeHashKey);
            
            // 根据nodecode查找不到，或者查到的nodeid不是 nodeid,重新遍历查找nodeid对应的nodecode
            if (orisource == null || orisource.getId() != nodeId)
            {
                
                boolean hasFind = false;
                for (CodedTreeData data : all)
                {
                    
                    if (data.getId() == nodeId)
                    {
                        hasFind = true;
                        // 修改nodecode值
                        nodeCode = data.getNodeCode();
                        break;
                    }
                }
                
                // 没找到，节点不存在
                if (!hasFind)
                {
                    throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_CODE_NOT_EXIST.getCode());
                }
                
            }
            
            // 根据nodecode反推出父节点的nodecode
            List<Long> parentNodeCodes = TreeNodeUtils.getParentNodeCodes(nodeCode);
            
            // 没有父节点，直接返回
            if (CollectionUtils.isEmpty(parentNodeCodes))
            {
                return null;
            }
            
            parentIds = new ArrayList<>();
            
            for (Long code : parentNodeCodes)
            {
                String hashKey = RedisTreeCacheKeyBuilder.buildNodeCodeHashKey(code);
                CodedTreeData source = map.get(hashKey);
                Assert.notNull(source);
                parentIds.add(source.getId());
            }
            
            // 排除默认添加的 root节点
            if (CollectionUtils.isNotEmpty(parentIds))
            {
                // 删掉默认增加的root节点 -1
                Long defaultRoot = -1L;
                parentIds.remove(defaultRoot);
            }
            
            return parentIds;
            
        }
        finally
        {
            
            if (isLocked)
            {
                segmentLock.unlock(lockKey);
            }
        }
        
    }
    
    /**
     * 构建树形缓存
     * @param treeTypeEnum
     */
    public void buildTreeCache(TreeTypeEnum treeTypeEnum)
    {
        
        // 编码
        List<CodedTreeData> result = fetchAndCodedTreeData(treeTypeEnum);
        
        if (CollectionUtils.isEmpty(result))
        {
            return;
        }
        // 放到redis
        redisTreeCacheDataFetcher.pipelinedSetDataToRedisCache(treeTypeEnum, result);
    }
    
    /**
     * 从数据库直接查询子节点
     * @param treeTypeEnum
     * @param nodeCode
     * @param nodeId
     * @return
     */
    public List<Long> searchSubNodeIdsDirectlyFromDbByNodeCode(TreeTypeEnum treeTypeEnum, long nodeCode, long nodeId)
    {
        
        String lockKey = "treecache_" + treeTypeEnum.getTreeType();
        
        boolean isLocked = false;
        
        try
        {
            isLocked = segmentLock(lockKey);
            
            // 等待1分钟没获取到锁，抛出异常
            if (!isLocked)
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_WAIT_LOCK_TIMEOUT.getCode());
            }
            
            try
            {
                Pair<Boolean, List<Long>> pair =
                    redisTreeCacheDataFetcher.getAllSubNodeIdsFromCache(treeTypeEnum, nodeId, nodeCode);
                
                // 要能找到自身节点，无论有没有子节点，都返回
                //
                if (pair != null && pair.getLeft())
                {
                    return pair.getRight();
                }
                
            }
            catch (Exception e)
            {
                
                logger.warn(
                    " DatabaseTreeCacheDataFetcher Tree Cache searchSubNodeIdsDirectlyFromDbByNodeCode TreeCacheChangeException,[type]:"
                        + treeTypeEnum.getTreeType() + ",[nodeId]" + nodeId,
                    e);
            }
            
            List<CodedTreeData> all = fetchAndCodedTreeData(treeTypeEnum);
            
            if (CollectionUtils.isEmpty(all))
            {
                return null;
            }
            
            CodedTreeData source = null;
            
            for (CodedTreeData data : all)
            {
                if (data.getId() == nodeId)
                {
                    source = data;
                    break;
                }
            }
            
            // 没找到nodeid对应的信息，抛出异常
            if (source == null)
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_CODE_NOT_EXIST.getCode());
            }
            
            List<Long> ids = new ArrayList<>();
            
            // 注意。这里的 source.getNodeCode() 可能和
            // nodecode不等，因为是第一次查询时的缓存未必还在，所以可能需要重新计算
            double fromScoreNow =
                TreeNodeUtils.getGlobalNodeCodeFromScoreForSubNode(treeTypeEnum, source.getNodeCode());
            double endScoreNow = TreeNodeUtils.getGlobalNodeCodeEndScoreForSubNode(treeTypeEnum, source.getNodeCode());
            
            for (CodedTreeData data : all)
            {
                // globalnodecode 范围查询
                if (data.getGlobalNodeCode() >= fromScoreNow && data.getGlobalNodeCode() <= endScoreNow)
                {
                    if (data.getId() != nodeId)
                    {
                        ids.add(data.getId());
                    }
                    
                }
                
            }
            
            return ids;
            
        }
        finally
        {
            
            if (isLocked)
            {
                segmentLock.unlock(lockKey);
            }
            
        }
    }
    
    /**
     * 直接从数据库查询子节点和自身节点
     * @param treeTypeEnum
     * @param map
     * @return
     */
    public Map<String, List<Long>> searchSlefAndSubNodesDirectlyFromDb(TreeTypeEnum treeTypeEnum, Map<String, Long> map)
    {
        
        String lockKey = "treecache_" + treeTypeEnum.getTreeType();
        
        boolean isLocked = false;
        
        try
        {
            isLocked = segmentLock(lockKey);
            
            // 等待1分钟没获取到锁，抛出异常
            if (!isLocked)
            {
                throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_WAIT_LOCK_TIMEOUT.getCode());
            }
            
            //  ********************查询所有的节点***************************
            List<CodedTreeData> all = fetchAndCodedTreeData(treeTypeEnum);
            if (CollectionUtils.isEmpty(all))
            {
                return null;
            }
            
            //  ********************根据需要查询的节点，构造查询条件fromScore， endScore***************************
            Map<String, Pair<Double, Double>> queryRangeMap = new HashMap<>();
            for (CodedTreeData data : all)
            {
                long nodeid = data.getId();
                
                Long code = map.get(String.valueOf(nodeid));
                //
                if (code != null)
                {
                    
                    //注意 这里用 CodedTreeData的nodecode新数据，而不是原来的老数据code
                    
                    double fromScore =
                        TreeNodeUtils.getGlobalNodeCodeFromScoreForSubNode(treeTypeEnum, data.getNodeCode());
                    double endScore =
                        TreeNodeUtils.getGlobalNodeCodeEndScoreForSubNode(treeTypeEnum, data.getNodeCode());
                    
                    queryRangeMap.put(String.valueOf(data.getId()), Pair.of(fromScore, endScore));
                }
            }
            
            //  ********************遍历所有的节点，判断globalCode是否在各个节点的fromScore 和endScore范围内 ***************************
            
            Map<String, List<Long>> resultMap = new HashMap<>();
            for (CodedTreeData data : all)
            {
                
                long globalCode = data.getGlobalNodeCode();
                
                for (Entry<String, Pair<Double, Double>> entry : queryRangeMap.entrySet())
                {
                    
                    Pair<Double, Double> pair = entry.getValue();
                    
                    //在fromScore 和 endScore 之间
                    if (pair.getLeft() <= globalCode && globalCode <= pair.getRight())
                    {
                        
                        List<Long> list = resultMap.get(entry.getKey());
                        if (list == null)
                        {
                            list = new ArrayList<>();
                            resultMap.put(entry.getKey(), list);
                        }
                        
                        list.add(data.getId());
                    }
                    
                }
            }
            
            return resultMap;
        }
        finally
        {
            
            if (isLocked)
            {
                segmentLock.unlock(lockKey);
            }
            
        }
        
    }
    
    /**
     * 分布式锁
     * @param lockKey
     * @return
     */
    private boolean segmentLock(String lockKey)
    {
        
        return segmentLock.lock(lockKey, 30, TimeUnit.SECONDS);
    }
    
}