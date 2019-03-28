package com.framework.core.cache.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.ZSetOperations;

import com.framework.core.common.strategy.ChoiceStrategy;
import com.framework.core.common.strategy.RoundRobinStrategy;


/**
 * 
 * @author zhangjun
 *
 * @param <K>
 * @param <V>
 */
public class RedisZSetOperations<K, V> {

	private ZSetOperations<K, V> zsetOperations;

	private ZSetOperations<K, V> zsetOperationsReadOnly;
	
	private List<ZSetOperations<K, V>> operationsList = new ArrayList<>();

	// 选择从库的策略
	private ChoiceStrategy strategy = new RoundRobinStrategy();

	public ZSetOperations<K, V> getZsetOperations() {
		return zsetOperations;
	}

	public void setZsetOperations(ZSetOperations<K, V> zsetOperations) {
		this.zsetOperations = zsetOperations;
	}

	public ZSetOperations<K, V> getZsetOperationsReadOnly() {
		return zsetOperationsReadOnly;
	}

	public void setZsetOperationsReadOnly(ZSetOperations<K, V> zsetOperationsReadOnly) {
		this.zsetOperationsReadOnly = zsetOperationsReadOnly;
	}

	public Long intersectAndStore(K key, K otherKey, K destKey) {
		return zsetOperations.intersectAndStore(key, otherKey, destKey);
	}

	public Long intersectAndStore(K key, Collection<K> otherKeys, K destKey) {
		return zsetOperations.intersectAndStore(key, otherKeys, destKey);
	}

	public Long unionAndStore(K key, K otherKey, K destKey) {
		return zsetOperations.unionAndStore(key, otherKey, destKey);
	}

	public Long unionAndStore(K key, Collection<K> otherKeys, K destKey) {
		return zsetOperations.unionAndStore(key, otherKeys, destKey);
	}

	public Set<V> range(K key, long start, long end) {
		return getRandomReadHashOperations().range(key, start, end);
	}

	public Set<V> reverseRange(K key, long start, long end) {
		return getRandomReadHashOperations().reverseRange(key, start, end);
	}

	public Set<ZSetOperations.TypedTuple<V>> rangeWithScores(K key, long start, long end) {
		return getRandomReadHashOperations().rangeWithScores(key, start, end);
	}

	public Set<ZSetOperations.TypedTuple<V>> reverseRangeWithScores(K key, long start, long end) {
		return getRandomReadHashOperations().reverseRangeWithScores(key, start, end);
	}

	public Set<V> rangeByScore(K key, double min, double max) {
		return getRandomReadHashOperations().rangeByScore(key, min, max);
	}

	public Set<V> rangeByScore(K key, double min, double max, long offset, long count) {
		return getRandomReadHashOperations().rangeByScore(key, min, max, offset, count);
	}

	public Set<V> reverseRangeByScore(K key, double min, double max) {
		return getRandomReadHashOperations().reverseRangeByScore(key, min, max);
	}

	public Set<V> reverseRangeByScore(K key, double min, double max, long offset, long count) {
		return getRandomReadHashOperations().reverseRangeByScore(key, min, max, offset, count);
	}

	public Set<ZSetOperations.TypedTuple<V>> rangeByScoreWithScores(K key, double min, double max) {
		return getRandomReadHashOperations().rangeByScoreWithScores(key, min, max);
	}

	public Set<ZSetOperations.TypedTuple<V>> rangeByScoreWithScores(K key, double min, double max, long offset,
			long count) {
		return getRandomReadHashOperations().rangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<ZSetOperations.TypedTuple<V>> reverseRangeByScoreWithScores(K key, double min, double max) {
		return getRandomReadHashOperations().reverseRangeByScoreWithScores(key, min, max);
	}

	public Set<ZSetOperations.TypedTuple<V>> reverseRangeByScoreWithScores(K key, double min, double max, long offset,
			long count) {
		return getRandomReadHashOperations().reverseRangeByScoreWithScores(key, min, max, offset, count);
	}

	public Boolean add(K key, V value, double score) {
		return zsetOperations.add(key, value, score);
	}

	public Long add(K key, Set<ZSetOperations.TypedTuple<V>> typedTuples) {
		return zsetOperations.add(key, typedTuples);
	}

	public Double incrementScore(K key, V value, double delta) {
		return zsetOperations.incrementScore(key, value, delta);
	}

	public Long rank(K key, Object o) {
		return getRandomReadHashOperations().rank(key, o);
	}

	public Long reverseRank(K key, Object o) {
		return getRandomReadHashOperations().reverseRank(key, o);
	}

	public Double score(K key, Object o) {
		return getRandomReadHashOperations().score(key, o);
	}

	public Long remove(K key, Object... values) {
		Long result = zsetOperations.remove(key, values);

		return result;
	}

	public Long removeRange(K key, long start, long end) {
		Long result = zsetOperations.removeRange(key, start, end);

		return result;
	}

	public Long removeRangeByScore(K key, double min, double max) {
		Long result = zsetOperations.removeRangeByScore(key, min, max);

		return result;
	}

	public Long count(K key, double min, double max) {
		return zsetOperations.count(key, min, max);
	}

	/**
	 * Returns the number of elements of the sorted set stored with given
	 * {@code key}.
	 *
	 * @param key
	 * @return
	 * @see #zCard(Object)
	 */

	public Long size(K key) {
		return getRandomReadHashOperations().size(key);
	}

	/**
	 * Returns the number of elements of the sorted set stored with given
	 * {@code key}.
	 *
	 * @param key
	 * @return
	 * @since 1.3
	 */

	public Long zCard(K key) {
		return getRandomReadHashOperations().zCard(key);
	}


	/**
	 * 获取随机的读库，可能是只读库，也可能是主库。
	 * 
	 * @return
	 */
	private ZSetOperations<K, V> getRandomReadHashOperations()
	{

		if (operationsList.isEmpty())
		{
			initList();
		}

		ZSetOperations<K, V> result = null;

		if (CollectionUtils.isNotEmpty(operationsList))
		{
			result = strategy.getInstance(operationsList);
		}

		return result == null ? zsetOperationsReadOnly : result;
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

		operationsList.add(zsetOperations);
		operationsList.add(zsetOperationsReadOnly);

	}
	
	
}
