package com.framework.core.cache.redis;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;



/**
 * 
 * @author zhangjun
 *
 * @param <K>
 * @param <V>
 */
public class RedisListOperations<K, V> {

	private ListOperations<K, V> listOperations;

	private ListOperations<K, V> listOperationsReadOnly;


	public ListOperations<K, V> getListOperations() {
		return listOperations;
	}

	public void setListOperations(ListOperations<K, V> listOperations) {
		this.listOperations = listOperations;
	}

	public ListOperations<K, V> getListOperationsReadOnly() {
		return listOperationsReadOnly;
	}

	public void setListOperationsReadOnly(ListOperations<K, V> listOperationsReadOnly) {
		this.listOperationsReadOnly = listOperationsReadOnly;
	}

	public List<V> range(K key, long start, long end) {
		return listOperationsReadOnly.range(key, start, end);
	}

	public void trim(K key, long start, long end) {
		listOperations.trim(key, start, end);
	}

	public Long size(K key) {
		return listOperationsReadOnly.size(key);
	}

	public Long leftPush(K key, V value) {
		return listOperations.leftPush(key, value);
	}

	public Long leftPushAll(K key, V... values) {
		return listOperations.leftPushAll(key, values);
	}

	/**
	 * Insert all {@literal values} at the head of the list stored at
	 * {@literal key}.
	 *
	 * @param key
	 *            must not be {@literal null}.
	 * @param values
	 *            must not be {@literal empty} nor contain {@literal null}
	 *            values.
	 * @return
	 * @since 1.5
	 */

	public Long leftPushAll(K key, Collection<V> values) {
		return listOperations.leftPushAll(key, values);
	}

	public Long leftPushIfPresent(K key, V value) {
		return listOperations.leftPushIfPresent(key, value);
	}

	public Long leftPush(K key, V pivot, V value) {
		return listOperations.leftPush(key, pivot, value);
	}

	public Long rightPush(K key, V value) {
		return listOperations.rightPush(key, value);
	}

	public Long rightPushAll(K key, V... values) {
		return listOperations.rightPushAll(key, values);
	}

	/**
	 * Insert all {@literal values} at the tail of the list stored at
	 * {@literal key}.
	 *
	 * @param key
	 *            must not be {@literal null}.
	 * @param values
	 *            must not be {@literal empty} nor contain {@literal null}
	 *            values.
	 * @return
	 * @since 1.5
	 */

	public Long rightPushAll(K key, Collection<V> values) {
		return listOperations.rightPushAll(key, values);
	}

	public Long rightPushIfPresent(K key, V value) {
		return listOperations.rightPushIfPresent(key, value);
	}

	public Long rightPush(K key, V pivot, V value) {
		return listOperations.rightPush(key, pivot, value);
	}

	public void set(K key, long index, V value) {
		listOperations.set(key, index, value);
	}

	public Long remove(K key, long i, Object value) {
		Long result = listOperations.remove(key, i, value);

		return result;
	}
	
	/**
	 * 会同步删除到另外一个中心
	 * @param key
	 * @param i
	 * @param value
	 * @return
	 */
	public Long removeAndSync(K key, long i, Object value) {
		
		Long result = this.remove(key, i, value);
		
		return result;
	}

	public V index(K key, long index) {
		return listOperationsReadOnly.index(key, index);
	}

	public V leftPop(K key) {
		return listOperations.leftPop(key);
	}

	public V leftPop(K key, long timeout, TimeUnit unit) {
		return listOperations.leftPop(key, timeout, unit);
	}

	public V rightPop(K key) {
		return listOperations.rightPop(key);
	}

	public V rightPop(K key, long timeout, TimeUnit unit) {
		return listOperations.rightPop(key, timeout, unit);
	}

	public V rightPopAndLeftPush(K sourceKey, K destinationKey) {
		return listOperations.rightPopAndLeftPush(sourceKey, destinationKey);
	}

	public V rightPopAndLeftPush(K sourceKey, K destinationKey, long timeout, TimeUnit unit) {
		return listOperations.rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
	}

	public RedisOperations<K, V> getOperations() {
		return null;
	}

}
