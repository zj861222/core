package com.framework.core.cache.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;

import com.framework.core.common.strategy.ChoiceStrategy;
import com.framework.core.common.strategy.RoundRobinStrategy;

/**
 * 
 * @author zhangjun
 *
 * @param <H>
 * @param <HK>
 * @param <HV>
 */
public class RedisHashOperations<H, HK, HV>
{

	/**
	 * 主库
	 */
	private HashOperations<H, HK, HV> hashOperations;

	/**
	 * 从库
	 */
	private HashOperations<H, HK, HV> hashOperationsReadOnly;

	private List<HashOperations<H, HK, HV>> hashOperationsList = new ArrayList<>();

	//选择从库的策略
	private ChoiceStrategy strategy = new RoundRobinStrategy();

	public HashOperations<H, HK, HV> getHashOperations()
	{
		return hashOperations;
	}

	public void setHashOperations(HashOperations<H, HK, HV> hashOperations)
	{
		this.hashOperations = hashOperations;
	}

	public HashOperations<H, HK, HV> getHashOperationsReadOnly()
	{
		return hashOperationsReadOnly;
	}

	public void setHashOperationsReadOnly(HashOperations<H, HK, HV> hashOperationsReadOnly)
	{
		this.hashOperationsReadOnly = hashOperationsReadOnly;
	}

	public void delete(H key, Object... hashKeys)
	{

		hashOperations.delete(key, hashKeys);

	}

	public Boolean hasKey(H key, Object hashKey)
	{
		return getRandomReadHashOperations().hasKey(key, hashKey);
	}

	public HV get(H key, Object hashKey)
	{
		return getRandomReadHashOperations().get(key, hashKey);
	}

	public List<HV> multiGet(H key, Collection<HK> hashKeys)
	{
		return getRandomReadHashOperations().multiGet(key, hashKeys);
	}

	public Long increment(H key, HK hashKey, long delta)
	{
		return hashOperations.increment(key, hashKey, delta);
	}

	public Double increment(H key, HK hashKey, double delta)
	{
		return hashOperations.increment(key, hashKey, delta);
	}

	public Set<HK> keys(H key)
	{
		return getRandomReadHashOperations().keys(key);
	}

	public Long size(H key)
	{
		return getRandomReadHashOperations().size(key);
	}

	public void putAll(H key, Map<? extends HK, ? extends HV> m)
	{

		hashOperations.putAll(key, m);
	}

	public void put(H key, HK hashKey, HV value)
	{

		hashOperations.put(key, hashKey, value);
	}

	public Boolean putIfAbsent(H key, HK hashKey, HV value)
	{
		return hashOperations.putIfAbsent(key, hashKey, value);
	}

	public List<HV> values(H key)
	{
		return getRandomReadHashOperations().values(key);
	}

	public Map<HK, HV> entries(H key)
	{
		return getRandomReadHashOperations().entries(key);
	}

	public RedisOperations<H, ?> getOperations()
	{
		return null;
	}

	/**
	 * @param key
	 * @param options
	 * @return
	 * @since 1.4
	 */
	public Cursor<Map.Entry<HK, HV>> scan(H key, ScanOptions options)
	{
		return null;
	}

	/**
	 * 获取随机的读库，可能是只读库，也可能是主库。
	 * 
	 * @return
	 */
	private HashOperations<H, HK, HV> getRandomReadHashOperations()
	{

		if (hashOperationsList.isEmpty())
		{
			initList();
		}

		HashOperations<H, HK, HV> result = null;
		if (CollectionUtils.isNotEmpty(hashOperationsList))
		{
			result =  strategy.getInstance(hashOperationsList);
		}
		
		return result!=null?result:hashOperationsReadOnly;
	}

	/**
	 * 初始化list
	 */
	private synchronized void initList()
	{

		if (CollectionUtils.isNotEmpty(hashOperationsList))
		{
			return;
		}

		hashOperationsList.add(hashOperations);
		hashOperationsList.add(hashOperationsReadOnly);

	}

}
