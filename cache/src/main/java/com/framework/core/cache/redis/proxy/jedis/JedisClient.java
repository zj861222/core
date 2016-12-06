package com.framework.core.cache.redis.proxy.jedis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author zhangjun
 *
 */
public interface JedisClient {
	
	/**
	 *  hash set
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	public void hashSet(String key, String hashKey, Object value);
	
    /**
     * hash get
     * @param key
     * @param hashKey
     * @return
     */
	public Object hashGet(String key, String hashKey);
	
	/**
	 * hash删除 
	 * @param key
	 * @param hashKey
	 */
	public void hDelete(String key, String hashKey);
	
	/**
	 * 删除
	 * @param key
	 */
	public void delete(String key);
	
	/**
	 * 超时
	 * @param key
	 * @param timeout
	 * @param unit
	 */
	public void expire(String key,long timeout,TimeUnit unit);
	
	
	/**
	 * 获取key下所有的hashkey的值
	 * @param key
	 * @return
	 */
	public Map<String,Object> entries(String key);
	
	
	/**
	 * 批量删除
	 * @param keys
	 */
	public void delete(final Collection<String> keys);


}
