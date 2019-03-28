package com.framework.core.web.tree.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;

import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.tree.TreeCacheService;
import com.framework.core.web.tree.TreeCacheTemplateManager;
import com.framework.core.web.tree.constants.RedisTreeCacheKeyBuilder;
import com.framework.core.web.tree.constants.RefreshDbEventTypeEnum;
import com.framework.core.web.tree.constants.TreeCacheErrorCode;
import com.framework.core.web.tree.constants.TreeTypeEnum;
import com.framework.core.web.tree.exception.TreeCacheChangeException;
import com.framework.core.web.tree.utils.TreeNodeUtils;
import com.google.common.collect.Lists;

/**
 * 
 * @author zhangjun
 *
 */
@Service
public class TreeCacheServiceImpl implements TreeCacheService
{

	private static final Logger logger = LoggerFactory.getLogger(TreeCacheServiceImpl.class);

	@Resource
	private TreeCacheTemplateManager treeCacheTemplateManager;

	@Resource
	private RedisHelper redisHelper;

//	@Resource
//	private RefreshDbEventSender refreshDbEventSender;

	@Resource
	private RedisTreeCacheDataFetcher redisTreeCacheDataFetcher;

	@Resource
	private DatabaseTreeCacheDataFetcher databaseTreeCacheDataFetcher;

//	@Override
//	public TreeCacheRelation getTreeNodeRelation(TreeTypeEnum treeTypeEnum, long sourceNodeId, long compareNodeId)
//	{
//
//		Assert.notNull(treeTypeEnum);
//
//		List<Long> nodeIds = new ArrayList<>();
//		nodeIds.add(sourceNodeId);
//		nodeIds.add(compareNodeId);
//
//		Map<String, Long> map = getNodeCodeByNodeIds(treeTypeEnum, nodeIds);
//
//		if (MapUtils.isEmpty(map))
//		{
//			throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE.getCode());
//		}
//
//		Long sourceNodeCode = map.get(String.valueOf(sourceNodeId));
//
//		Long targetNodeCode = map.get(String.valueOf(compareNodeId));
//
//		if (sourceNodeCode == null || targetNodeCode == null)
//		{
//			throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE.getCode());
//		}
//
//		return new TreeCacheRelation(sourceNodeId, sourceNodeCode, compareNodeId, targetNodeCode);
//
//	}

	/**
	 * 根据nodeids 获取nodecode ids
	 * @param treeTypeEnum
	 * @param nodeIds
	 * @return
	 */
	private Map<String, Long> getNodeCodeByNodeIds(TreeTypeEnum treeTypeEnum, List<Long> nodeIds)
	{

		if (CollectionUtils.isEmpty(nodeIds))
		{
			return null;
		}

		Map<String, Long> map = redisTreeCacheDataFetcher.getNodeCodeFromCacheByNodeIds(treeTypeEnum, nodeIds);

		if (MapUtils.isNotEmpty(map))
		{
			return map;
		}

		// 发送缓存刷新事件
		sendBuildCacheEvent(treeTypeEnum);

		map = databaseTreeCacheDataFetcher.getNodeCodeFromDbDirectly(treeTypeEnum, nodeIds);

		return map;

	}

	@Override
	public List<Long> getParentNodeIds(TreeTypeEnum treeTypeEnum, long nodeId)
	{

		long nodeCode = getNodeCodeById(treeTypeEnum, nodeId);

		int lv = TreeNodeUtils.getNodeCodeLv(nodeCode);

		if (lv == 0 || lv == 1)
		{
			return null;
		}

		List<Long> parentIds = null;

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

			logger.warn("Tree Cache getParentNodeIds TreeCacheChangeException,[type]:" + treeTypeEnum.getTreeType()
					+ ",[nodeId]" + nodeId, e);
		}

		// 发送缓存刷新事件
		sendBuildCacheEvent(treeTypeEnum);

		// 直接从数据库读取
		parentIds = databaseTreeCacheDataFetcher.getParentNodeIds(treeTypeEnum, nodeId, nodeCode);

		return parentIds;
	}

	// 查找子节点
	@Override
	public List<Long> getAllSubNodeIds(TreeTypeEnum treeTypeEnum, long nodeId)
	{

		// 找到节点code
		long nodeCode = getNodeCodeById(treeTypeEnum, nodeId);

		// 从缓存中获取
		List<Long> idsList;

		try
		{
			Pair<Boolean, List<Long>> pair = redisTreeCacheDataFetcher.getAllSubNodeIdsFromCache(treeTypeEnum, nodeId,
					nodeCode);

			// 要能找到自身节点，无论有没有子节点，都返回
			//
			if (pair != null && pair.getLeft())
			{
				return pair.getRight();
			}

		}
		catch (TreeCacheChangeException e)
		{

			logger.warn("Tree Cache getAllSubNodeIds TreeCacheChangeException,[type]:" + treeTypeEnum.getTreeType()
					+ ",[nodeId]" + nodeId, e);
		}

		// 发送缓存刷新事件
		sendBuildCacheEvent(treeTypeEnum);

		// 从数据库直接获取
		idsList = databaseTreeCacheDataFetcher.searchSubNodeIdsDirectlyFromDbByNodeCode(treeTypeEnum, nodeCode, nodeId);

		return idsList;
	}

	/**
	 *  根据nodeid查找nodecode
	 * @param treeTypeEnum
	 * @param nodeId
	 * @return
	 */
	private long getNodeCodeById(TreeTypeEnum treeTypeEnum, long nodeId)
	{

		Assert.notNull(treeTypeEnum);

		List<Long> nodeIds = Lists.newArrayList(nodeId);

		Map<String, Long> map = getNodeCodeByNodeIds(treeTypeEnum, nodeIds);

		// 如果返回的map为空，找不到节点
		if (MapUtils.isEmpty(map))
		{
			throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE.getCode());
		}

		Long nodeCode = map.get(String.valueOf(nodeId));

		Assert.notNull(nodeCode);

		return nodeCode;
	}

	/**
	 * 构建树形cache。
	 * 
	 * @param treeTypeEnum
	 */
	@Override
	public void buildTreeCache(TreeTypeEnum treeTypeEnum)
	{

		databaseTreeCacheDataFetcher.buildTreeCache(treeTypeEnum);

	}

	/**
	 * 发送构建缓存的事件
	 * @param treeTypeEnum
	 */
	private void sendBuildCacheEvent(TreeTypeEnum treeTypeEnum)
	{


		String flagKey = RedisTreeCacheKeyBuilder.getSendRefreshEventFlagKey(treeTypeEnum);

		// 如果val不为空，说明前不久才刷新，那么无需重复刷新
		String val = redisHelper.valueGet(flagKey);

		// val不存在，才发出刷新事件
		if (StringUtils.isBlank(val))
		{

			//
			RefreshDbEventTypeEnum refreshDbType = treeTypeEnum.getRefreshDbType();

			Assert.notNull(refreshDbType);

			// 发送刷新tree 缓存的事件
			refreshDbEventSender.sendEvent(refreshDbType, treeTypeEnum.getTreeType());

			redisHelper.valueSet(flagKey, "1", 10, TimeUnit.SECONDS);
		}

	}

	
	
	
	
	
	
	@Override
	public void clearTenantTreeCache( TreeTypeEnum treeTypeEnum)
	{

		String hashTableKey = RedisTreeCacheKeyBuilder.getTreeCacheMapKey(treeTypeEnum);

		String globalNodeCodeZsetKey = RedisTreeCacheKeyBuilder.getGlobalNodeCodeTreeCacheZsetKey(treeTypeEnum
				);

		// 刷新事件的flag，清理缓存时也必须清掉这个
		String flagKey = RedisTreeCacheKeyBuilder.getSendRefreshEventFlagKey(treeTypeEnum);

		List<String> keys = new ArrayList<>();

		keys.add(flagKey);
		keys.add(hashTableKey);

		keys.add(globalNodeCodeZsetKey);

		redisHelper.batchDelete(keys);
	}

	@Override
	public int queryNodeLv(TreeTypeEnum treeTypeEnum, long nodeId)
	{

		long nodeCode = getNodeCodeById(treeTypeEnum, nodeId);

		int lv = TreeNodeUtils.getNodeCodeLv(nodeCode);

		return lv;
	}

	@Override
	public List<Long> querySelfAndUnionAllSubNodeIds(TreeTypeEnum treeTypeEnum, List<Long> nodeIds)
	{

		if (CollectionUtils.isEmpty(nodeIds))
		{
			return null;
		}

		// key为nodeid，value为nodecode
		Map<String, Long> map = getNodeCodeByNodeIds(treeTypeEnum, nodeIds);

		if (MapUtils.isEmpty(map))
		{
			throw new BizException(TreeCacheErrorCode.EX_PLAT_TREE_CACHE_CODE_NOT_EXIST.getCode());
		}

		// key为nodecode，value为nodeid
		Map<String, String> nodeCodeKeyMap = new HashMap<>();

		List<String> nodeIdsTmpList = new ArrayList<>();

		for (Entry<String, Long> entry : map.entrySet())
		{
			Long nodeCode = entry.getValue();
			Assert.notNull(nodeCode);
			nodeCodeKeyMap.put(nodeCode.toString(), entry.getKey());
			nodeIdsTmpList.add(entry.getKey());
		}

		// 对map进行精简，只留下所有的顶层节点，比如 11,1111,12,1212,只留下 11和12就行了
		for (String nodeIdTmp : nodeIdsTmpList)
		{

			Long nodeCode = map.get(nodeIdTmp);

			List<Long> parentNodeCodes = TreeNodeUtils.getParentNodeCodes(nodeCode);

			if (CollectionUtils.isEmpty(parentNodeCodes))
			{
				continue;
			}

			for (Long parentNodeCode : parentNodeCodes)
			{

				String val = nodeCodeKeyMap.get(parentNodeCode.toString());

				if (StringUtils.isNotBlank(val))
				{
					// map里删掉 这个nodeid对应的，有父节点在，就不需要这个节点了
					map.remove(nodeIdTmp);
				}

			}
		}

		// 查询所有的子节点和自身节点
		Map<String, List<Long>> resultMap = getSelfAndSubNodes(treeTypeEnum, map);
		if (MapUtils.isEmpty(resultMap))
		{
			return null;
		}

		// 执行子节点去重合并操作
		Set<Long> resultSet = new HashSet<Long>();
		for (Entry<String, List<Long>> entry : resultMap.entrySet())
		{
			List<Long> subNodeIds = entry.getValue();
			if (CollectionUtils.isNotEmpty(subNodeIds))
			{
				for (Long id : subNodeIds)
				{
					resultSet.add(id);
				}
			}
		}

		return new ArrayList<>(resultSet);
	}

	/**
	 * 获取所有的子节点和自身节点
	 * @param treeTypeEnum
	 * @param map
	 * @return
	 */
	private Map<String, List<Long>> getSelfAndSubNodes(TreeTypeEnum treeTypeEnum, Map<String, Long> map)
	{

		Map<String, List<Long>> resultMap = null;
		try
		{
			resultMap = redisTreeCacheDataFetcher.pipelinedZrangeByScores(treeTypeEnum, map);

			if (MapUtils.isNotEmpty(resultMap))
			{
				return resultMap;
			}

		}
		catch (TreeCacheChangeException e)
		{
			logger.warn("Tree Cache getAllSubNodes TreeCacheChangeException,[type]:" + treeTypeEnum.getTreeType()
					+ ",[map]" + JSON.toJSONString(map), e);
		}

		// 发送刷新事件
		sendBuildCacheEvent(treeTypeEnum);

		return databaseTreeCacheDataFetcher.searchSlefAndSubNodesDirectlyFromDb(treeTypeEnum, map);

	}

}
