package com.framework.core.cache.redis.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.Assert;
import com.framework.core.cache.redis.GracefulRedisTemplate;
import com.framework.core.cache.redis.RedisHashOperations;
import com.framework.core.cache.redis.RedisListOperations;
import com.framework.core.cache.redis.RedisValueOperations;
import com.framework.core.cache.redis.RedisZSetOperations;
import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.error.exception.BizException;
import redis.clients.jedis.Jedis;

/**
 * 
 * @author zhangjun
 */
public class RedisHelper
{

	private static final String SET_IF_NOT_EXIST = "NX";

	// PX 表示毫秒，命令执行有问题，所以这里用EX。 表示单位为 秒
	private static final String SET_WITH_EXPIRE_TIME = "EX";

	private static final String SUCCESS = "OK";

	// 限定为主库
	@SuppressWarnings("rawtypes")
	private GracefulRedisTemplate gracefulRedisTemplate;

	private RedisValueOperations<String, String> redisValueOperations;

	private RedisHashOperations<String, String, String> redisHashOperations;

	private RedisListOperations<String, String> redisListOperations;

	private RedisZSetOperations<String, String> redisZSetOperations;

	public RedisZSetOperations<String, String> getRedisZSetOperations()
	{
		return redisZSetOperations;
	}

	public void setRedisZSetOperations(RedisZSetOperations<String, String> redisZSetOperations)
	{
		this.redisZSetOperations = redisZSetOperations;
	}

	public RedisListOperations<String, String> getRedisListOperations()
	{
		return redisListOperations;
	}

	public void setRedisListOperations(RedisListOperations<String, String> redisListOperations)
	{
		this.redisListOperations = redisListOperations;
	}

	public RedisHashOperations<String, String, String> getRedisHashOperations()
	{
		return redisHashOperations;
	}

	public void setRedisHashOperations(RedisHashOperations<String, String, String> redisHashOperations)
	{
		this.redisHashOperations = redisHashOperations;
	}

	@SuppressWarnings("rawtypes")
	public GracefulRedisTemplate getGracefulRedisTemplate()
	{
		return gracefulRedisTemplate;
	}

	@SuppressWarnings("rawtypes")
	public void setGracefulRedisTemplate(GracefulRedisTemplate gracefulRedisTemplate)
	{
		this.gracefulRedisTemplate = gracefulRedisTemplate;
	}

	public RedisValueOperations<String, String> getRedisValueOperations()
	{
		return redisValueOperations;
	}

	public void setRedisValueOperations(RedisValueOperations<String, String> redisValueOperations)
	{
		this.redisValueOperations = redisValueOperations;
	}

	/**
	 * 主库value结构，set操作，
	 * 
	 * 注意: 超时时间请不要设置太长！
	 * 
	 * @param key
	 * @param t
	 * @param timeout
	 * @param unit
	 */
	public <T> void valueSet(String key, String value, long timeout, TimeUnit unit)
	{

		try
		{

			redisValueOperations.set(key, value, timeout, unit);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * 主库value结构，，删除session中的key信息
	 * 
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public void delete(String key)
	{

		try
		{
			gracefulRedisTemplate.delete(key);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_DELETE_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库value结构，get操作
	 * 
	 * @param key
	 * @param targetClass
	 * @return
	 */
	public long valueGetLong(String key)
	{
		try
		{

			String obj = (String) gracefulRedisTemplate.opsForValue().get(key);

			return StringUtils.isEmpty(obj) ? 0L : Long.parseLong(obj);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库数值增加
	 * @param key
	 * @param delta
	 */
	@SuppressWarnings("unchecked")
	public void increase(String key, long delta)
	{
		try
		{
			gracefulRedisTemplate.opsForValue().increment(key, delta);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}

	}

	/**
	 * 主库数值减少
	 * @param key
	 * @param delta
	 */
	@SuppressWarnings("unchecked")
	public void decrease(String key, long delta)
	{
		try
		{
			gracefulRedisTemplate.opsForValue().increment(key, -delta);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}

	}

	/**
	 * 读库，value结构，get操作
	 * 
	 * @param key
	 * @param targetClass
	 * @return
	 */
	public String valueGet(String key)
	{

		try
		{
			String jsonObj = redisValueOperations.get(key);

			return jsonObj;

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库value结构，可用作锁的场景。
	 * 
	 * 记住，这里没有设置超时时间，请慎重使用此方法，后面请设置超时时间
	 * 
	 * @param key
	 * @param t
	 * @return
	 */
	public Boolean valueSetIfAbsent(String key, String value)
	{

		try
		{

			return redisValueOperations.setIfAbsent(key, value);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}

	}

	/**
	 * 主库value型数据结构 : 批量set 请慎重使用，这里没有设置超时时间，请慎重使用此方法，后面请设置超时时间
	 * 
	 * @param m
	 */
	public void valueMultiSet(Map<String, String> m)
	{
		try
		{
			redisValueOperations.multiSet(m);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 读库value型数据结构 : 批量获取
	 * 
	 * @param keys
	 * @return
	 */
	public List<String> valueMultiGet(Collection<String> keys)
	{
		try
		{
			return redisValueOperations.multiGet(keys);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 *主库 hash类型: 数据结果，批量删除
	 * 
	 * @param key
	 * @param hashKeys
	 *            string类型的
	 */
	public void hashDelete(String key, String... hashKeys)
	{
		try
		{
			redisHashOperations.delete(key, hashKeys);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_DELETE_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 hash类型，set方法 请慎重使用，这里没有设置超时时间，会导致key永久存在。 请慎重使用此方法， 1.
	 * 后面设置超时时间，超时时间只能针对key设置。
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	public void hashSet(String key, String hashKey, String value)
	{

		try
		{
			redisHashOperations.put(key, hashKey, value);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * 读库hash结构的 get
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public String hashGet(String key, String hashKey)
	{

		try
		{
			return redisHashOperations.get(key, hashKey);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);
		}
	}

	/**
	 * 主库  hash类型，set方法 请慎重使用， 1. 这里虽然设置了超时时间，但是不能保证操作的原子性，有可能setexpire失败，导致key永远存在
	 * 2. 设置超时是针对key操作的。 hash结构是多个hashkey公用一个key，这个操作会不断刷新整个key下面所有的数据的超时时间
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	public void hashSetEx(String key, String hashKey, String value, long timeout, TimeUnit unit)
	{

		try
		{
			redisHashOperations.put(key, hashKey, value);

			expire(key, timeout, unit);

		}
		catch (BizException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * 读库 hash结构: 批量get
	 * 
	 * @param key
	 * @param hashKeys
	 * @return
	 */
	public List<String> hashMultiGet(String key, Collection<String> hashKeys)
	{
		try
		{
			return redisHashOperations.multiGet(key, hashKeys);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 hash结构: 批量set 请慎重使用，这里没有设置超时时间，会导致key永久存在。
	 * 请慎重使用此方法，后面设置超时时间，超时时间只能针对key设置。
	 * 
	 * @param key
	 * @param m
	 */
	public void putAll(String key, Map<String, String> m)
	{

		try
		{
			redisHashOperations.putAll(key, m);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * 从库 hash结构: 获取key下的所有的hashkey，和value
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, String> hashGetByKey(String key)
	{

		try
		{
			return redisHashOperations.entries(key);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库 设置key的超时时间。 注意: 不要设置过长的超时时间。 针对单个key设置超时时间 spring redis
	 * 设置超长时间expire，会调用twemproxy不支持的time命令，导致报错。
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean expire(String key, long timeout, TimeUnit unit)
	{
		try
		{
			return gracefulRedisTemplate.longExpire(key, timeout, unit);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 读库 lrange
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> lrange(String key, long start, long end)
	{

		try
		{
			return redisListOperations.range(key, start, end);

		}
		catch (Exception e)
		{

			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库截取list结构中的一段
	 * 
	 * @param key
	 * @param start
	 * @param end
	 */
	public void ltrim(String key, long start, long end)
	{

		try
		{
			redisListOperations.trim(key, start, end);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * 主库  left push
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long leftPush(String key, String value)
	{
		try
		{
			return redisListOperations.leftPush(key, value);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);
		}
	}

	/**
	 * 主库 left push all
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public Long leftPushAll(String key, String... values)
	{
		try
		{
			return redisListOperations.leftPushAll(key, values);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库leftPushAll
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public Long leftPushAll(String key, Collection<String> values)
	{

		try
		{
			return redisListOperations.leftPushAll(key, values);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库rightPush
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long rightPush(String key, String value)
	{

		try
		{
			return redisListOperations.rightPush(key, value);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库rightPushAll
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public Long rightPushAll(String key, String... values)
	{

		try
		{
			return redisListOperations.rightPushAll(key, values);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 rightPushAll
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public Long rightPushAll(String key, Collection<String> values)
	{

		try
		{
			return redisListOperations.rightPushAll(key, values);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 leftPop
	 * 
	 * @param key
	 * @return
	 */
	public String leftPop(String key)
	{

		try
		{
			return redisListOperations.leftPop(key);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 leftPop
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public String leftPop(String key, long timeout, TimeUnit unit)
	{

		try
		{
			return redisListOperations.leftPop(key, timeout, unit);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 rightPop
	 * 
	 * @param key
	 * @return
	 */
	public String rightPop(String key)
	{

		try
		{
			return redisListOperations.rightPop(key);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 rightPop
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public String rightPop(String key, long timeout, TimeUnit unit)
	{

		try
		{
			return redisListOperations.rightPop(key, timeout, unit);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 rightPopAndLeftPush
	 * 
	 * @param sourceKey
	 * @param destinationKey
	 * @return
	 */
	public String rightPopAndLeftPush(String sourceKey, String destinationKey)
	{

		try
		{
			return redisListOperations.rightPopAndLeftPush(sourceKey, destinationKey);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库 rightPopAndLeftPush
	 * 
	 * @param sourceKey
	 * @param destinationKey
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public String rightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit)
	{

		try
		{
			return redisListOperations.rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 读库 计算 listSize
	 * 
	 * @param key
	 * @return
	 */
	public Long listSize(String key)
	{

		try
		{
			return redisListOperations.size(key);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 读库 返回指定score 区段之间元素
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<String> zsetRangeByScore(String key, double min, double max)
	{

		try
		{
			return redisZSetOperations.rangeByScore(key, min, max);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 *  读库 返回区间指定元素）,集合中元素是按score从小到大排序的
	 * @param key
	 * @param start 最小下标
	 * @param end 最大下标
	 * @return
	 */
	public Set<String> zsetRange(String key, long start, long end)
	{
		try
		{
			return redisZSetOperations.range(key, start, end);
		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * 读库 返回区间指定元素,集合中元素是按score从大到小排序的，
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<String> zsetRevrange(String key, long start, long end)
	{
		try
		{
			return redisZSetOperations.reverseRange(key, start, end);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 返回区间指定元素的数量
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public long zsetCountByScore(String key, double start, double end)
	{

		try
		{
			return redisZSetOperations.count(key, start, end);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 读库 获取member对应的score
	 * @param key
	 * @param member
	 * @return
	 */
	public double zsetScore(String key, String member)
	{

		try
		{
			return redisZSetOperations.score(key, member);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 删除指定index区间的元素
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public long zsetRemrangebyrank(String key, int start, int end)
	{

		try
		{
			return redisZSetOperations.removeRange(key, start, end);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库 删除指定score区间的元素
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public long zsetRemrangebyscore(String key, double min, double max)
	{

		try
		{
			return redisZSetOperations.removeRangeByScore(key, min, max);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库 添加元素
	 * @param key
	 * @param score
	 * @param member
	 */
	public void zsetAddMember(String key, double score, String member)
	{

		try
		{
			redisZSetOperations.add(key, member, score);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 删除
	 * @param key
	 * @param member
	 */
	public void zsetRemoveMember(String key, String member)
	{

		try
		{
			redisZSetOperations.remove(key, member);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 给指定score增加值incr
	 * @param key
	 * @param incr
	 * @param member
	 */
	public void zsetIncrby(String key, double incr, String member)
	{

		try
		{
			redisZSetOperations.incrementScore(key, member, incr);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 zset add
	 * @param key
	 * @param typedTuples
	 * @return
	 */
	public Long zsetAdd(String key, Set<ZSetOperations.TypedTuple<String>> typedTuples)
	{

		try
		{
			return redisZSetOperations.add(key, typedTuples);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 是否含有这个key
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean hasKey(String key)
	{

		try
		{
			return gracefulRedisTemplate.hasKey(key);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GET_FAIL.getCode(), e);

		}
	}

	/**
	 * 主库 批量删除key
	 * @param keys
	 */
	@SuppressWarnings("unchecked")
	public void batchDelete(List<String> keys)
	{

		try
		{
			gracefulRedisTemplate.delete(keys);

		}
		catch (Exception e)
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SET_FAIL.getCode(), e);

		}

	}

	/**
	 * 主库 使用 setnx 
	 * @param key
	 * @param value
	 * @param timeout
	 * @param unit
	 * 
	 */
	public boolean setNxWithTimeOut(String key, String value, long timeout, TimeUnit unit)
	{

		TimeUnit timeUnit = TimeUnit.SECONDS;
		long seconds = timeUnit.convert(timeout, unit);

		Jedis jedis = getWriteJedis();

		Assert.notNull(jedis);

		String result = jedis.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, seconds);

		return SUCCESS.equals(result);
	}

	/**
	 * 获取读库的jedis实例
	 * @return
	 */
	public Jedis getWriteJedis()
	{

		RedisConnection jedisConnection = gracefulRedisTemplate.getConnectionFactory().getConnection();
		Jedis jedis = (Jedis) jedisConnection.getNativeConnection();
		return jedis;
	}

}