package com.framework.core.cache.redis;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;



/**
 * 
 * @author zhangjun
 *
 * @param <K>
 * @param <V>
 */
public class RedisSetOperations<K, V> {

	private SetOperations<K, V> setOperations;

	private SetOperations<K, V> setOperationsReadOnly;



	public SetOperations<K, V> getSetOperations() {
		return setOperations;
	}

	public void setSetOperations(SetOperations<K, V> setOperations) {
		this.setOperations = setOperations;
	}

	public SetOperations<K, V> getSetOperationsReadOnly() {
		return setOperationsReadOnly;
	}

	public void setSetOperationsReadOnly(SetOperations<K, V> setOperationsReadOnly) {
		this.setOperationsReadOnly = setOperationsReadOnly;
	}

	public Set<V> difference(K key, K otherKey) {
		return setOperationsReadOnly.difference(key, otherKey);
	}

	public Set<V> difference(K key, Collection<K> otherKeys) {
		return setOperationsReadOnly.difference(key, otherKeys);
	}

	public Long differenceAndStore(K key, K otherKey, K destKey) {
		return setOperations.differenceAndStore(key, otherKey, destKey);
	}

	public Long differenceAndStore(K key, Collection<K> otherKeys, K destKey) {
		return setOperations.differenceAndStore(key, otherKeys, destKey);
	}

	public Set<V> intersect(K key, K otherKey) {
		return setOperationsReadOnly.intersect(key, otherKey);
	}

	public Set<V> intersect(K key, Collection<K> otherKeys) {
		return setOperationsReadOnly.intersect(key, otherKeys);
	}

	public Long intersectAndStore(K key, K otherKey, K destKey) {
		return setOperations.intersectAndStore(key, otherKey, destKey);
	}

	public Long intersectAndStore(K key, Collection<K> otherKeys, K destKey) {
		return setOperations.intersectAndStore(key, otherKeys, destKey);
	}

	public Set<V> union(K key, K otherKey) {
		return setOperationsReadOnly.union(key, otherKey);
	}

	public Set<V> union(K key, Collection<K> otherKeys) {
		return setOperationsReadOnly.union(key, otherKeys);
	}

	public Long unionAndStore(K key, K otherKey, K destKey) {
		return setOperations.unionAndStore(key, otherKey, destKey);
	}

	public Long unionAndStore(K key, Collection<K> otherKeys, K destKey) {
		return setOperations.unionAndStore(key, otherKeys, destKey);
	}

	public Long add(K key, V... values) {
		return setOperations.add(key, values);
	}

	public Boolean isMember(K key, Object o) {
		return setOperationsReadOnly.isMember(key, o);
	}

	public Set<V> members(K key) {
		return setOperationsReadOnly.members(key);
	}

	public Boolean move(K key, V value, K destKey) {
		return setOperations.move(key, value, destKey);
	}

	public V randomMember(K key) {
		return setOperationsReadOnly.randomMember(key);
	}

	public Set<V> distinctRandomMembers(K key, long count) {
		return null;
	}

	public List<V> randomMembers(K key, long count) {
		return null;
	}

	public Long remove(K key, Object... values) {
		Long result = setOperations.remove(key, values);
		
		return result;
	}

	public V pop(K key) {
		return setOperations.pop(key);
	}

	public Long size(K key) {
		return setOperationsReadOnly.size(key);
	}

	public RedisOperations<K, V> getOperations() {
		return null;
	}

	/**
	 * Iterate over elements in set at {@code key}.
	 *
	 * @param key
	 * @param options
	 * @return
	 * @since 1.4
	 */

	public Cursor<V> scan(K key, ScanOptions options) {
		return null;
	}


}
