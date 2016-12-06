package com.framework.core.cache.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;



/**
 * 
 * @author zhangjun
 *
 * @param <H>
 * @param <HK>
 * @param <HV>
 */
public class RedisHashOperations<H, HK, HV>  {

	private HashOperations<H, HK, HV> hashOperations;

	private HashOperations<H, HK, HV> hashOperationsReadOnly;


	public HashOperations<H, HK, HV> getHashOperations() {
		return hashOperations;
	}

	public void setHashOperations(HashOperations<H, HK, HV> hashOperations) {
		this.hashOperations = hashOperations;
	}

	public HashOperations<H, HK, HV> getHashOperationsReadOnly() {
		return hashOperationsReadOnly;
	}

	public void setHashOperationsReadOnly(HashOperations<H, HK, HV> hashOperationsReadOnly) {
		this.hashOperationsReadOnly = hashOperationsReadOnly;
	}

	public void delete(H key, Object... hashKeys) {

		hashOperations.delete(key, hashKeys);

	}

	public Boolean hasKey(H key, Object hashKey) {
		return hashOperationsReadOnly.hasKey(key, hashKey);
	}

	public HV get(H key, Object hashKey) {
		return hashOperationsReadOnly.get(key, hashKey);
	}

	public List<HV> multiGet(H key, Collection<HK> hashKeys) {
		return hashOperationsReadOnly.multiGet(key, hashKeys);
	}

	public Long increment(H key, HK hashKey, long delta) {
		return hashOperations.increment(key, hashKey, delta);
	}

	public Double increment(H key, HK hashKey, double delta) {
		return hashOperations.increment(key, hashKey, delta);
	}

	public Set<HK> keys(H key) {
		return hashOperationsReadOnly.keys(key);
	}

	public Long size(H key) {
		return hashOperationsReadOnly.size(key);
	}

	public void putAll(H key, Map<? extends HK, ? extends HV> m) {

		hashOperations.putAll(key, m);
	}
	
	/**
	 * 会同步删除到另外一个中心
	 * @param key
	 * @param m
	 */
	public void putAllAndSync(H key, Map<? extends HK, ? extends HV> m) {

		this.putAll(key, m);
	}

	public void put(H key, HK hashKey, HV value) {
		
		hashOperations.put(key, hashKey, value);
	}
	
	/**
	 * 会同步删除到另外一个中心
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	public void putAndSync(H key, HK hashKey, HV value){
		
		this.put(key, hashKey, value);
	}

	public Boolean putIfAbsent(H key, HK hashKey, HV value) {
		return hashOperations.putIfAbsent(key, hashKey, value);
	}

	public List<HV> values(H key) {
		return hashOperationsReadOnly.values(key);
	}

	public Map<HK, HV> entries(H key) {
		return hashOperationsReadOnly.entries(key);
	}

	public RedisOperations<H, ?> getOperations() {
		return null;
	}

	/**
	 * @param key
	 * @param options
	 * @return
	 * @since 1.4
	 */

	public Cursor<Map.Entry<HK, HV>> scan(H key, ScanOptions options) {
		return null;
	}



}
