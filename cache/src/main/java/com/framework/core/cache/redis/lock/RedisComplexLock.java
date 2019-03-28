package com.framework.core.cache.redis.lock;

import com.framework.core.common.utils.MD5;

import redis.clients.jedis.Jedis;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * redis 重度分布式锁，更加安全可靠
 * 
 * @author zhangjun
 *
 */
public class RedisComplexLock
{

	private static final String LOCK_SUCCESS = "OK";
	
	private static final String SET_IF_NOT_EXIST = "NX";
	
	//PX 表示毫秒，命令执行有问题，所以这里用EX。 表示单位为 秒
	private static final String SET_WITH_EXPIRE_TIME = "EX";
	
	// 要确保上述操作是原子性的,要使用Lua语言来实现.
	// 首先获取锁对应的value值，检查是否与token相等，如果相等则删除锁（解锁）
	private static final String LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

	private static final Long RELEASE_SUCCESS = 1L;

	private RedisConnectionFactory jedisConnectionFactory;


	public RedisConnectionFactory getJedisConnectionFactory()
	{
		return jedisConnectionFactory;
	}

	public void setJedisConnectionFactory(RedisConnectionFactory jedisConnectionFactory)
	{
		this.jedisConnectionFactory = jedisConnectionFactory;
	}

	/**
	 * 加锁逻辑: 
	 * 锁超时失效:如果不显示释放锁,默认 60s 失效
	 * @param key 锁的key
	 * @return token 为null表示没锁住
	 */
	public String tryLock(String key)
	{

		return tryLock(key, 60, TimeUnit.SECONDS);

	}

	/**
	 * 加锁，指定锁超时时间.
	 * 
	 * 加锁逻辑: 
	 * 
	 * @param key 锁的key
	 * @param timeout 锁最长的生存时间
	 * @param unit 锁的超时时间单位
	 * @return token 释放锁的令牌，为null表示没锁住
	 */
	public String tryLock(String key, long timeout, TimeUnit unit)
	{

		Jedis jedis = null;
		try
		{

			TimeUnit timeUnit = TimeUnit.SECONDS;

			long seconds = timeUnit.convert(timeout, unit);

			// 加锁失败, 抛出异常
			long nowTime = System.currentTimeMillis();

			jedis = getJedis();

			String token = MD5.md5(key + "_" + nowTime);

			Assert.notNull(jedis);

			String result = jedis.set(key, token, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, seconds);

			if (LOCK_SUCCESS.equals(result))
			{
				return token;
			}
			
			return null;

		}
		finally
		{

			releaseConnection(jedis);
		}

	}


	/**
	 * 释放锁
	 * @param lockKey lockkey
	 * @param token 加锁返回的token
	 * @return
	 */
	public boolean releaseLock(String lockKey, String token)
	{

		Jedis jedis = null;

		try
		{
			jedis = getJedis();

			Object result = jedis.eval(LUA_SCRIPT, Collections.singletonList(lockKey), Collections.singletonList(token));

			return RELEASE_SUCCESS.equals(result);
		}
		finally
		{
			releaseConnection(jedis);
		}

	}

	/**
	 * 
	 * @return
	 */
	private Jedis getJedis()
	{

		RedisConnection jedisConnection = jedisConnectionFactory.getConnection();
		Jedis jedis = (Jedis) jedisConnection.getNativeConnection();
		return jedis;
	}

	/**
	 * release jedis
	 * 
	 * @param jedis
	 */
	private void releaseConnection(Jedis jedis)
	{
		if (jedis != null)
		{
			jedis.close();
		}
	}

}
