package com.framework.core.cache.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.ValueOperations;

import com.framework.core.common.strategy.ChoiceStrategy;
import com.framework.core.common.strategy.RoundRobinStrategy;

/**
 * 
 * @author zhangjun
 *
 * @param <K>
 * @param <V>
 */
public class RedisValueOperations<K, V>
{

	private ValueOperations<K, V> valueOperations;

	private ValueOperations<K, V> valueOperationsReadOnly;

	private List<ValueOperations<K, V>> operationsList = new ArrayList<>();

	// 选择从库的策略
	private ChoiceStrategy strategy = new RoundRobinStrategy();

	public ValueOperations<K, V> getValueOperations()
	{
		return valueOperations;
	}

	public void setValueOperations(ValueOperations<K, V> valueOperations)
	{
		this.valueOperations = valueOperations;
	}

	public ValueOperations<K, V> getValueOperationsReadOnly()
	{
		return valueOperationsReadOnly;
	}

	public void setValueOperationsReadOnly(ValueOperations<K, V> valueOperationsReadOnly)
	{
		this.valueOperationsReadOnly = valueOperationsReadOnly;
	}

	public void set(K key, V value)
	{
		valueOperations.set(key, value);
	}

	/**
	 * 设置到本中心,并mq发送消息清除另外一个云的数据
	 * 
	 * @param key
	 * @param value
	 */
	public void setAndSync(K key, V value)
	{

		valueOperations.set(key, value);
	}

	/**
	 * Set {@code key} to hold the string {@code value} until {@code timeout}.
	 *
	 * @param key
	 * @param value
	 * @param timeout
	 * @param unit
	 */

	public void set(K key, V value, long timeout, TimeUnit unit)
	{

		valueOperations.set(key, value, timeout, unit);
	}

	public Boolean setIfAbsent(K key, V value)
	{
		return valueOperations.setIfAbsent(key, value);
	}

	public void multiSet(Map<? extends K, ? extends V> m)
	{
		valueOperations.multiSet(m);
	}

	public Boolean multiSetIfAbsent(Map<? extends K, ? extends V> m)
	{
		return valueOperations.multiSetIfAbsent(m);
	}

	public V get(Object key)
	{
		return getRandomReadHashOperations().get(key);
	}

	public V getAndSet(K key, V value)
	{
		return valueOperations.getAndSet(key, value);
	}

	public List<V> multiGet(Collection<K> keys)
	{
		return getRandomReadHashOperations().multiGet(keys);
	}

	public Long increment(K key, long delta)
	{
		return valueOperations.increment(key, delta);
	}

	public Double increment(K key, double delta)
	{
		return valueOperations.increment(key, delta);
	}

	public void set(K key, V value, long offset)
	{
		valueOperations.set(key, value, offset);
	}

	public Long size(K key)
	{
		return getRandomReadHashOperations().size(key);
	}

	/**
	 * 获取随机的读库，可能是只读库，也可能是主库。
	 * 
	 * @return
	 */
	private ValueOperations<K, V> getRandomReadHashOperations()
	{

		if (operationsList.isEmpty())
		{
			initList();
		}

		ValueOperations<K, V> result = null;

		if (CollectionUtils.isNotEmpty(operationsList))
		{
			result = strategy.getInstance(operationsList);
		}

		return result == null ? valueOperationsReadOnly : result;
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

		operationsList.add(valueOperations);
		operationsList.add(valueOperationsReadOnly);

	}

}
