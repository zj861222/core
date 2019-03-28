package com.framework.core.cache.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.SetOperations;

import com.framework.core.common.strategy.ChoiceStrategy;
import com.framework.core.common.strategy.RoundRobinStrategy;

/**
 * 
 * @author zhangjun
 *
 * @param <K>
 * @param <V>
 */
public class RedisSetOperations<K, V>
{

	private SetOperations<K, V> setOperations;

	private SetOperations<K, V> setOperationsReadOnly;

	private List<SetOperations<K, V>> operationsList = new ArrayList<>();

	// 选择从库的策略
	private ChoiceStrategy strategy = new RoundRobinStrategy();

	public SetOperations<K, V> getSetOperations()
	{
		return setOperations;
	}

	public void setSetOperations(SetOperations<K, V> setOperations)
	{
		this.setOperations = setOperations;
	}

	public SetOperations<K, V> getSetOperationsReadOnly()
	{
		return setOperationsReadOnly;
	}

	public void setSetOperationsReadOnly(SetOperations<K, V> setOperationsReadOnly)
	{
		this.setOperationsReadOnly = setOperationsReadOnly;
	}

	public Set<V> difference(K key, K otherKey)
	{
		return getRandomReadHashOperations().difference(key, otherKey);
	}

	public Set<V> difference(K key, Collection<K> otherKeys)
	{
		return getRandomReadHashOperations().difference(key, otherKeys);
	}

	public Long differenceAndStore(K key, K otherKey, K destKey)
	{
		return setOperations.differenceAndStore(key, otherKey, destKey);
	}

	public Long differenceAndStore(K key, Collection<K> otherKeys, K destKey)
	{
		return setOperations.differenceAndStore(key, otherKeys, destKey);
	}

	public Set<V> intersect(K key, K otherKey)
	{
		return getRandomReadHashOperations().intersect(key, otherKey);
	}

	public Set<V> intersect(K key, Collection<K> otherKeys)
	{
		return getRandomReadHashOperations().intersect(key, otherKeys);
	}

	public Long intersectAndStore(K key, K otherKey, K destKey)
	{
		return setOperations.intersectAndStore(key, otherKey, destKey);
	}

	public Long intersectAndStore(K key, Collection<K> otherKeys, K destKey)
	{
		return setOperations.intersectAndStore(key, otherKeys, destKey);
	}

	public Set<V> union(K key, K otherKey)
	{
		return getRandomReadHashOperations().union(key, otherKey);
	}

	public Set<V> union(K key, Collection<K> otherKeys)
	{
		return getRandomReadHashOperations().union(key, otherKeys);
	}

	public Long unionAndStore(K key, K otherKey, K destKey)
	{
		return setOperations.unionAndStore(key, otherKey, destKey);
	}

	public Long unionAndStore(K key, Collection<K> otherKeys, K destKey)
	{
		return setOperations.unionAndStore(key, otherKeys, destKey);
	}

	public Long add(K key, @SuppressWarnings("unchecked") V... values)
	{
		return setOperations.add(key, values);
	}

	public Boolean isMember(K key, Object o)
	{
		return getRandomReadHashOperations().isMember(key, o);
	}

	public Set<V> members(K key)
	{
		return getRandomReadHashOperations().members(key);
	}

	public Boolean move(K key, V value, K destKey)
	{
		return setOperations.move(key, value, destKey);
	}

	public V randomMember(K key)
	{
		return getRandomReadHashOperations().randomMember(key);
	}

	// public Set<V> distinctRandomMembers(K key, long count) {
	// return null;
	// }
	//
	// public List<V> randomMembers(K key, long count) {
	// return null;
	// }

	public Long remove(K key, Object... values)
	{
		Long result = setOperations.remove(key, values);

		return result;
	}

	public V pop(K key)
	{
		return setOperations.pop(key);
	}

	public Long size(K key)
	{
		return getRandomReadHashOperations().size(key);
	}

	// public RedisOperations<K, V> getOperations() {
	// return null;
	// }

	// /**
	// * Iterate over elements in set at {@code key}.
	// *
	// * @param key
	// * @param options
	// * @return
	// * @since 1.4
	// */
	//
	// public Cursor<V> scan(K key, ScanOptions options) {
	// return null;
	// }

	/**
	 * 获取随机的读库，可能是只读库，也可能是主库。
	 * 
	 * @return
	 */
	private SetOperations<K, V> getRandomReadHashOperations()
	{

		if (operationsList.isEmpty())
		{
			initList();
		}

		SetOperations<K, V> result = null;

		if (CollectionUtils.isNotEmpty(operationsList))
		{
			result = strategy.getInstance(operationsList);
		}

		return result == null ? setOperationsReadOnly : result;
	}

	/**
	 * 初始化list
	 */
	private synchronized void initList()
	{

		if (CollectionUtils.isNotEmpty(operationsList))
		{
			return;
		}

		operationsList.add(setOperations);
		operationsList.add(setOperationsReadOnly);

	}

}
