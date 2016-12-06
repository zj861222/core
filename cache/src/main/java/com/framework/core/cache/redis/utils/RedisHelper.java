package com.framework.core.cache.redis.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.framework.core.cache.redis.GracefulRedisTemplate;
import com.framework.core.cache.redis.RedisHashOperations;
import com.framework.core.cache.redis.RedisValueOperations;
import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.error.exception.BizException;

/**
 * 
 * @author zhangjun
 */
public class RedisHelper {

	private GracefulRedisTemplate gracefulRedisTemplate;

	private RedisValueOperations<String, String> redisValueOperations;


	private RedisHashOperations<String, String, String> redisHashOperations;
	

	public RedisHashOperations<String, String, String> getRedisHashOperations() {
		return redisHashOperations;
	}



	public void setRedisHashOperations(RedisHashOperations<String, String, String> redisHashOperations) {
		this.redisHashOperations = redisHashOperations;
	}

	public GracefulRedisTemplate getGracefulRedisTemplate() {
		return gracefulRedisTemplate;
	}

	public void setGracefulRedisTemplate(GracefulRedisTemplate gracefulRedisTemplate) {
		this.gracefulRedisTemplate = gracefulRedisTemplate;
	}

	public RedisValueOperations<String, String> getRedisValueOperations() {
		return redisValueOperations;
	}

	public void setRedisValueOperations(RedisValueOperations<String, String> redisValueOperations) {
		this.redisValueOperations = redisValueOperations;
	}

	/**
	 * value结构，set操作，
	 * 
	 * 注意: 超时时间请不要设置太长！
	 * 
	 * @param key
	 * @param t
	 * @param timeout
	 * @param unit
	 * @throws BizException
	 */
	public <T> void valueSet(String key, String value, long timeout, TimeUnit unit) throws BizException {

		try {

			redisValueOperations.set(key, value, timeout, unit);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * value结构，，删除session中的key信息
	 * 
	 * @param key
	 * @throws BizException
	 */
	public void delete(String key) throws BizException {

		try {
			gracefulRedisTemplate.delete(key);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_DELETE_FAIL.getCode(), e);

		}
	}

	/**
	 * value结构，get操作
	 * 
	 * @param key
	 * @param targetClass
	 * @return
	 * @throws BizException
	 */
	public  String valueGet(String key) throws BizException {

		try {
			String jsonObj = redisValueOperations.get(key);

	
			return jsonObj;

		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * value结构，可用作锁的场景。
	 * 
	 * 记住，这里没有设置超时时间，请慎重使用此方法，后面请设置超时时间
	 * 
	 * @param key
	 * @param t
	 * @return
	 * @throws BizException
	 */
	public  Boolean valueSetIfAbsent(String key, String value) throws BizException {

		try {
	
			return redisValueOperations.setIfAbsent(key, value);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}

	}

	/**
	 * value型数据结构 : 批量set 请慎重使用，这里没有设置超时时间，请慎重使用此方法，后面请设置超时时间
	 * 
	 * @param m
	 * @throws BizException
	 */
	public void valueMultiSet(Map<String, String> m) throws BizException {
		try {
			redisValueOperations.multiSet(m);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * value型数据结构 : 批量获取
	 * 
	 * @param keys
	 * @return
	 * @throws BizException
	 */
	public List<String> valueMultiGet(Collection<String> keys) throws BizException {
		try {
			return redisValueOperations.multiGet(keys);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * hash类型: 数据结果，批量删除
	 * 
	 * @param key
	 * @param hashKeys
	 *            string类型的
	 * @throws BizException
	 */
	public void hashDelete(String key, String... hashKeys) throws BizException {
		try {
			redisHashOperations.delete(key, hashKeys);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_DELETE_FAIL.getCode(), e);

		}
	}



	/**
	 * hash类型，set方法 请慎重使用，这里没有设置超时时间，会导致key永久存在。 请慎重使用此方法， 1.
	 * 后面设置超时时间，超时时间只能针对key设置。
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 * @throws BizException
	 */
	public  void hashSet(String key, String hashKey, String value) throws BizException {

		try {
			redisHashOperations.put(key, hashKey, value);

		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * hash结构的 get
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 * @throws BizException
	 */
	public String hashGet(String key, String hashKey) throws BizException {

		try {
			return redisHashOperations.get(key, hashKey);

		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);
		}
	}

	/**
	 * hash类型，set方法 请慎重使用， 1. 这里虽然设置了超时时间，但是不能保证操作的原子性，有可能setexpire失败，导致key永远存在
	 * 2. 设置超时是针对key操作的。 hash结构是多个hashkey公用一个key，这个操作会不断刷新整个key下面所有的数据的超时时间
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 * @throws BizException
	 */
	public  void hashSetEx(String key, String hashKey, String value, long timeout, TimeUnit unit) throws BizException {

		try {
			redisHashOperations.put(key, hashKey, value);

			expire(key, timeout, unit);

		} catch (BizException e) {
			throw e;
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * hash结构: 批量get
	 * 
	 * @param key
	 * @param hashKeys
	 * @return
	 * @throws BizException
	 */
	public List<String> hashMultiGet(String key, Collection<String> hashKeys) throws BizException {
		try {
			return redisHashOperations.multiGet(key, hashKeys);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * hash结构: 批量set 请慎重使用，这里没有设置超时时间，会导致key永久存在。
	 * 请慎重使用此方法，后面设置超时时间，超时时间只能针对key设置。
	 * 
	 * @param key
	 * @param m
	 * @throws BizException
	 */
	public void putAll(String key, Map<String, String> m) throws BizException {

		try {
			redisHashOperations.putAll(key, m);

		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * hash结构: 获取key下的所有的hashkey，和value
	 * 
	 * @param key
	 * @return
	 * @throws BizException
	 */
	public Map<String, String> hashGetByKey(String key) throws BizException {

		try {
			return redisHashOperations.entries(key);
			
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 设置key的超时时间。 注意: 不要设置过长的超时时间。 针对单个key设置超时时间 spring redis
	 * 设置超长时间expire，会调用twemproxy不支持的time命令，导致报错。
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws BizException
	 */
	public Boolean expire(String key, long timeout, TimeUnit unit) throws BizException {
		try {
			return gracefulRedisTemplate.longExpire(key, timeout, unit);
		} catch (Exception e) {
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}
	
	
}