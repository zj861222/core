package com.framework.core.web.tree.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;

import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.tree.constants.RedisTreeCacheKeyBuilder;
import com.framework.core.web.tree.constants.TreeTypeEnum;
import com.framework.core.web.tree.exception.TreeCacheChangeException;
import com.framework.core.web.tree.model.CodedTreeData;
import com.framework.core.web.tree.utils.TreeNodeUtils;

@Component
public class RedisTreeCacheDataFetcher
{

	private static final Logger logger = LoggerFactory.getLogger(RedisTreeCacheDataFetcher.class);

	private static final long ONE_DAY = 24 * 60 * 60;

	@Resource
	private RedisHelper redisHelper;

	public Map<String, Long> getNodeCodeFromCacheByNodeIds(TreeTypeEnum treeTypeEnum, List<Long> nodeIds)
	{
		String key = RedisTreeCacheKeyBuilder.getTreeCacheMapKey(treeTypeEnum);

		List<String> hashKeyList = new ArrayList<>();

		for (Long id : nodeIds)
		{
			hashKeyList.add(RedisTreeCacheKeyBuilder.buildIdHashKey(id));
		}

		Map<String, Long> mapResult = new HashMap<>();

		List<String> valList = redisHelper.hashMultiGet(key, hashKeyList);

		if (CollectionUtils.isNotEmpty(valList))
		{

			for (int idx = 0; idx < valList.size(); idx++)
			{

				String val = valList.get(idx);

				if (StringUtils.isNotBlank(val))
				{

					long nodeCode = Long.parseLong(val);

					mapResult.put(String.valueOf(nodeIds.get(idx)), nodeCode);
				}

			}

			// 有val不为空的，就返回;否则尝试从db库里取
			if (MapUtils.isNotEmpty(mapResult))
			{
				return mapResult;
			}

		}

		return null;
	}

	/**
	 * 从缓存获取父节点ids
	 * @param treeTypeEnum
	 * @param nodeId
	 * @param nodeCode
	 * @return
	 * @throws TreeCacheChangeException
	 */
	public List<Long> getParentNodeIds(TreeTypeEnum treeTypeEnum, long nodeId, long nodeCode)
		throws TreeCacheChangeException
	{

		// 根据nodecode反推出父节点的nodecode
		List<Long> parentNodeCodes = TreeNodeUtils.getParentNodeCodes(nodeCode);

		// 没有父节点，直接返回
		if (CollectionUtils.isEmpty(parentNodeCodes))
		{
			return null;
		}

		// 自己也进去查询,用做校验缓存是否变更了
		parentNodeCodes.add(nodeCode);

		// 从redis获取
		List<Long> parentIds = getNodeIdsFromCacheByNodeCode(treeTypeEnum, nodeId, nodeCode, parentNodeCodes);

		if (CollectionUtils.isNotEmpty(parentIds))
		{
			// 排除掉原节点
			parentIds.remove(nodeId);

			return parentIds;
		}

		return null;
	}

	/**
	 * 查询父节点的ids。
	 * @param treeTypeEnum 
	 * @param checkNodeId 用来验证缓存是否一致的节点,可传null 不校验
	 * @param checkNodeCode 用来验证缓存是否一致的节点code，可传null 不校验
	 * @param nodeCodes 父节点列表
	 * @return
	 * @throws TreeCacheChangeException 
	 */
	private List<Long> getNodeIdsFromCacheByNodeCode(TreeTypeEnum treeTypeEnum, long checkNodeId, long checkNodeCode,
			List<Long> nodeCodes)
		throws TreeCacheChangeException
	{

		String key = RedisTreeCacheKeyBuilder.getTreeCacheMapKey(treeTypeEnum);

		List<String> hashKeyList = new ArrayList<>();

		for (Long code : nodeCodes)
		{
			String hashKey = RedisTreeCacheKeyBuilder.buildNodeCodeHashKey(code);
			hashKeyList.add(hashKey);

		}

		List<String> valList = redisHelper.hashMultiGet(key, hashKeyList);

		Map<String, Long> codeMap = new HashMap<>();

		List<Long> parentIds = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(valList))
		{

			for (int idx = 0; idx < valList.size(); idx++)
			{

				String val = valList.get(idx);

				if (StringUtils.isNotBlank(val))
				{

					Long nowNodeId = Long.parseLong(val);
					Assert.notNull(nowNodeId);

					long nowNodeCode = nodeCodes.get(idx);

					// 当checkNodeId和 checkNodeCode 不为null，执行校验逻辑

					// 缓存不一致
					if (nowNodeCode == checkNodeId && nowNodeId != checkNodeId)
					{
						throw new TreeCacheChangeException(treeTypeEnum);
					}
					codeMap.put(String.valueOf(nowNodeCode), nowNodeId);

				}

			}

			// 有val不为空的，就返回;否则尝试从db库里取
			if (MapUtils.isNotEmpty(codeMap))
			{

				for (Entry<String, Long> entry : codeMap.entrySet())
				{

					Long id = entry.getValue();

					// id不为null，
					if (id != null)
					{
						parentIds.add(id);
					}

				}

				// 删掉默认增加的root节点 -1
				Long defaultRoot = -1L;
				parentIds.remove(defaultRoot);

				return parentIds;
			}

		}

		return null;
	}

	/**
	 * pipelined模式放置TreeCache缓存
	 * @param dataList
	 */
	@SuppressWarnings("unchecked")
	public void pipelinedSetDataToRedisCache(TreeTypeEnum treeTypeEnum, final List<CodedTreeData> dataList)
	{

		if (CollectionUtils.isEmpty(dataList))
		{
			return;
		}

		logger.info("begin to pipelinedToRedisCache, [treeTypeEnum]:" + treeTypeEnum.getTreeType() + ",[list size]="
				+ dataList.size());

		try
		{

			final String hashMapKey = RedisTreeCacheKeyBuilder.getTreeCacheMapKey(treeTypeEnum);


			
			final String globalNodeCodeZsetKey = RedisTreeCacheKeyBuilder
					.getGlobalNodeCodeTreeCacheZsetKey(treeTypeEnum);

			// final String nodeCodeZsetKey =
			// getNodeCodeTreeCacheZsetKey(treeTypeEnum);

			redisHelper.getGracefulRedisTemplate().executePipelined(new RedisCallback<Object>()
			{

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException
				{

					final RedisSerializer<String> keySerializer = redisHelper.getGracefulRedisTemplate()
							.getKeySerializer();

					final RedisSerializer<String> hashKeySerializer = redisHelper.getGracefulRedisTemplate()
							.getHashKeySerializer();

					final RedisSerializer<String> hashValSerializer = redisHelper.getGracefulRedisTemplate()
							.getHashValueSerializer();

					final RedisSerializer<String> valSerializer = redisHelper.getGracefulRedisTemplate()
							.getValueSerializer();

					Map<byte[], byte[]> hashes = new HashMap<>();

					byte[] globalNodeCodeZsetKeyBytes = keySerializer.serialize(globalNodeCodeZsetKey);

					// final byte[] nodeCodeZsetKeyBytes =
					// keySerializer.serialize(nodeCodeZsetKey);

					//hashmap结构的key
					final byte[] keyBytes = keySerializer.serialize(hashMapKey);

					// 先删除2个key的值
					connection.del(keyBytes, globalNodeCodeZsetKeyBytes);

					try
					{

						for (CodedTreeData data : dataList)
						{

							if (data == null)
							{
								continue;
							}

							// 插入id作为hashkey的
							String idHashKey = RedisTreeCacheKeyBuilder.buildIdHashKey(data.getId());

							byte[] nodecodeValByte = hashValSerializer.serialize(String.valueOf(data.getNodeCode()));

							// id作为hashkey，val为nodecode
							hashes.put(hashKeySerializer.serialize(idHashKey), nodecodeValByte);

							// 插入nodecode作为hashkey的
							String nodeCodeHashKey = RedisTreeCacheKeyBuilder.buildNodeCodeHashKey(data.getNodeCode());
							// nodecode作为hashkey，val为 nodeid

							byte[] idHashValByte = hashValSerializer.serialize(String.valueOf(data.getId()));
							hashes.put(hashKeySerializer.serialize(nodeCodeHashKey), idHashValByte);

							byte[] idValByte = valSerializer.serialize(String.valueOf(data.getId()));

							connection.zAdd(globalNodeCodeZsetKeyBytes,
									Double.parseDouble(data.getGlobalNodeCode() + ""), idValByte);

							// // 以nodecode为score的zset结构
							// nodeCodeRawTuples
							// .add(new DefaultTuple(idValByte,
							// Double.longBitsToDouble(data.getNodeCode())));

							// connection.zAdd(nodeCodeZsetKeyBytes,
							// Double.parseDouble(data.getNodeCode()+""),
							// idValByte);

						}

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					//
					if (MapUtils.isNotEmpty(hashes))
					{

						connection.hMSet(keyBytes, hashes);
						connection.expire(keyBytes, ONE_DAY);
						connection.expire(globalNodeCodeZsetKeyBytes, ONE_DAY);

					}

					return null;

				}
			});

		}
		catch (Exception e)
		{
			logger.error("pipelinedToRedisCache exception :[list]:" + JSON.toJSONString(dataList), e);

			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}

	}
	
	
	/**
	 * 获取节点的子节点。
	 * 
	 * @param treeTypeEnum
	 * @param nodeId
	 * @param nodeCode
	 * @return
	 */
	public Pair<Boolean,List<Long>> getAllSubNodeIdsFromCache(TreeTypeEnum treeTypeEnum, long nodeId, long nodeCode) throws TreeCacheChangeException {
		

		String key = RedisTreeCacheKeyBuilder.getGlobalNodeCodeTreeCacheZsetKey(treeTypeEnum);

		double fromScore = TreeNodeUtils.getGlobalNodeCodeFromScoreForSubNode(treeTypeEnum, nodeCode);

		double endScore = TreeNodeUtils.getGlobalNodeCodeEndScoreForSubNode(treeTypeEnum, nodeCode);

		Set<String> records = redisHelper.zsetRangeByScore(key, fromScore, endScore);

		//如果 records 为空，直接返回空
		if (CollectionUtils.isEmpty(records))
		{
			return null;
		}

		List<Long> idsList = new ArrayList<>();
		
		boolean hasFindSelf = false;

		for (String idStr : records)
		{

			if (StringUtils.isNotBlank(idStr))
			{
				long id = Long.parseLong(idStr);

				// 自身不加入
				if (id != nodeId)
				{
					idsList.add(id);
				}
				else
				{
					hasFindSelf = true;
				}

			}
		}
		
		// 如果在同层节点没找到自己，那么认为这个是异常的，可能是2次redis查询的数据不一致，中间可能有更新了缓存，抛出异常
		if (!hasFindSelf)
		{
			throw new TreeCacheChangeException(treeTypeEnum);
		}
		
		
		//检验nodeid对应的globalnodecode 还是不是原来的那个，不是的话，抛出异常。
		Set<String> checkRecords = redisHelper.zsetRangeByScore(key, fromScore, fromScore);
		
		if(CollectionUtils.isEmpty(checkRecords) || !checkRecords.contains(nodeId+"")) {
			throw new TreeCacheChangeException(treeTypeEnum);
		}

		return Pair.of(hasFindSelf, idsList);
	}
	
	
	
	
	/**
	 * 查询下面所有的父节点的子节点
	 * @param key
	 * @param fatherNodes  key为nodeid，value为 nodecode
	 * @return
	 * @throws TreeCacheChangeException 
	 */
	@SuppressWarnings("unchecked")
	public Map<String,List<Long>> pipelinedZrangeByScores(TreeTypeEnum treeTypeEnum, final Map<String,Long> fatherNodesMap) throws TreeCacheChangeException
	{
		
		if(MapUtils.isEmpty(fatherNodesMap)) {
			return null;
		}
		

		String key = RedisTreeCacheKeyBuilder.getGlobalNodeCodeTreeCacheZsetKey(treeTypeEnum);
		

        List<String> nodeIdStrs = new ArrayList<>();
        
        final List<Pair<Double,Double>> queryParmList = new ArrayList<>();
        
		for(Entry<String,Long> entry:fatherNodesMap.entrySet()) {
			
			nodeIdStrs.add(entry.getKey());
			
			Long nodeCode = entry.getValue();
			
			if(nodeCode == null) {
				continue;
			}
			
			double fromScore = TreeNodeUtils.getGlobalNodeCodeFromScoreForSubNode(treeTypeEnum, nodeCode);
			double endScore = TreeNodeUtils.getGlobalNodeCodeEndScoreForSubNode(treeTypeEnum, nodeCode);
			
			queryParmList.add(Pair.of(fromScore, fromScore)); //用做校验，验证返回的nodeid是不是传入的nodeid
			queryParmList.add(Pair.of(fromScore, endScore));  //用做查询子节点
		}
		
		
		final RedisSerializer<String> keySerializer = redisHelper.getGracefulRedisTemplate().getKeySerializer();
		final RedisSerializer<String> valSerializer = redisHelper.getGracefulRedisTemplate().getValueSerializer();

		final byte[] rawKey = keySerializer.serialize(key);

		List<Set<String>> pipelineResults = redisHelper.getGracefulRedisTemplate().executePipelined(new RedisCallback<Object>()
		{

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException
			{

				for (Pair<Double, Double> pair : queryParmList)
				{
					connection.zRangeByScore(rawKey, pair.getLeft(), pair.getRight());
				}

				return null;
			}
		});

		
		if (CollectionUtils.isEmpty(pipelineResults))
		{
			return null;
		}
		
		int i = 0;
		
		Map<String,List<Long>> resultMap = new HashMap<>();
		
		for(String nodeIdStr: nodeIdStrs) {
			
			//STEP1    **********************开始校验新缓存的nodecode对应的nodeid是不是原来的nodeid  **********************
			
			//根据(fromScore, fromScore)查询的节点id，用来校验nodecode对应的nodeid是否还是原来的
			Set<String> parentNode = pipelineResults.get(i*2);
			
			//查询不到有2种可能:1.缓存不存在，那么直接返回null 2. 原来的globalnodecode不存在了，也就是说 树形缓存变化了，也直接返回null，通过本地db查询
			if(parentNode == null || parentNode.size() == 0) {
				return null;
			}
			
			Assert.isTrue(parentNode.size()==1);
			
			for(String realNodeId:parentNode) {
				//找到的nodeid和原来nodeid不一致，抛出异常
				if(!nodeIdStr.equals(realNodeId)) {
					throw new TreeCacheChangeException(treeTypeEnum);
				}	
			}
			
					
				
			//STEP2    **********************获取实际的节点的子节点  **********************
			
			//获取子节点
			Set<String> subNodes = pipelineResults.get(i*2+1);
			
			List<Long> subNodeIds = new ArrayList<>();

			
			//理论不存在subNodes为空这种场景,因为如果前面父节点能查到，那么父节点+子节点不可能为null
            Assert.isTrue(CollectionUtils.isNotEmpty(subNodes));
			

            for(String realNodeId:subNodes) {
            	
            	//子节点
            	if(StringUtils.isNotBlank(realNodeId)) {
            		subNodeIds.add(Long.parseLong(realNodeId));
            	}
  
            }
            
            
            resultMap.put(nodeIdStr, subNodeIds);
 
			i++;
		}
	

		return resultMap;
	}
	

}