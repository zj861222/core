package com.framework.core.cache.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;

/**
 * 
 * @author zhangjun
 *
 * @param <K>
 * @param <V>
 */
public class RedisValueOperations<K, V> {

	private ValueOperations<K, V> valueOperations;

	private ValueOperations<K, V> valueOperationsReadOnly;

	public ValueOperations<K, V> getValueOperations() {
		return valueOperations;
	}

	public void setValueOperations(ValueOperations<K, V> valueOperations) {
		this.valueOperations = valueOperations;
	}

	public ValueOperations<K, V> getValueOperationsReadOnly() {
		return valueOperationsReadOnly;
	}

	public void setValueOperationsReadOnly(ValueOperations<K, V> valueOperationsReadOnly) {
		this.valueOperationsReadOnly = valueOperationsReadOnly;
	}

	public void set(K key, V value) {
		valueOperations.set(key, value);
	}

	/**
	 * 设置到本中心,并mq发送消息清除另外一个云的数据
	 * 
	 * @param key
	 * @param value
	 */
	public void setAndSync(K key, V value) {

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

	public void set(K key, V value, long timeout, TimeUnit unit) {

		valueOperations.set(key, value, timeout, unit);
	}

	/**
	 * 设置到本中心,并mq发送消息清除另外一个云的数据
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 * @param unit
	 */
	public void setAndSync(K key, V value, long timeout, TimeUnit unit) {

		valueOperations.set(key, value, timeout, unit);
	}

	public Boolean setIfAbsent(K key, V value) {
		return valueOperations.setIfAbsent(key, value);
	}

	public void multiSet(Map<? extends K, ? extends V> m) {
		valueOperations.multiSet(m);
	}

	/**
	 * 设置到本中心,并mq发送消息清除另外一个云的数据
	 * 
	 * @param m
	 */
	public void multiSetAndSync(Map<? extends K, ? extends V> m) {
		valueOperations.multiSet(m);
		Set<String> keys = (Set<String>) m.keySet();
	}

	public Boolean multiSetIfAbsent(Map<? extends K, ? extends V> m) {
		return valueOperations.multiSetIfAbsent(m);
	}

	public V get(Object key) {
		return valueOperationsReadOnly.get(key);
	}

	public V getAndSet(K key, V value) {
		return valueOperations.getAndSet(key, value);
	}

	public List<V> multiGet(Collection<K> keys) {
		return valueOperationsReadOnly.multiGet(keys);
	}

	public Long increment(K key, long delta) {
		return valueOperations.increment(key, delta);
	}

	public Double increment(K key, double delta) {
		return valueOperations.increment(key, delta);
	}

	public Integer append(K key, String value) {
		return null;
	}

	public String get(K key, long start, long end) {
		return null;
	}

	public void set(K key, V value, long offset) {
		valueOperations.set(key, value, offset);
	}

	public Long size(K key) {
		return valueOperationsReadOnly.size(key);
	}

	/**
	 *
	 * @return
	 */

	public RedisOperations<K, V> getOperations() {
		return null;
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @since 1.5
	 */

	public Boolean setBit(K key, long offset, boolean value) {
		return null;
	}

	/**
	 * @param key
	 * @param offset
	 * @return
	 * @since 1.5
	 */

	public Boolean getBit(K key, long offset) {
		return null;
	}

}
