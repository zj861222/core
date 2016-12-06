package com.framework.core.cache.redis.proxy.jedis.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.framework.core.cache.redis.proxy.jedis.JedisClient;

import com.framework.core.cache.redis.proxy.jedis.JedisExcuteTemplate;
import com.framework.core.cache.redis.proxy.jedis.JedisExcuteTemplate.Callback;
import com.framework.core.cache.redis.serializer.SerializationUtil;

/**
 * 
 * @author zhangjun
 *
 */
public class JedisClientImpl implements JedisClient {

	private JedisExcuteTemplate jedisExcuteTemplate;

	public JedisExcuteTemplate getJedisExcuteTemplate() {
		return jedisExcuteTemplate;
	}

	public void setJedisExcuteTemplate(JedisExcuteTemplate jedisExcuteTemplate) {
		this.jedisExcuteTemplate = jedisExcuteTemplate;
	}

	@Override
	public void hashSet(final String key, final String hashKey, final Object value) {

		jedisExcuteTemplate.excute(new Callback() {
			byte[] objSeria = SerializationUtil.serialize(value);

			public Object command(RedisConnection redisConnection) {

				return redisConnection.hSet(key.getBytes(), hashKey.getBytes(), objSeria);
			}
		});

	}

	@Override
	public Object hashGet(final String key, final String hashKey) {

		return jedisExcuteTemplate.excute(new Callback() {

			public Object command(RedisConnection redisConnection) {
				byte[] str = redisConnection.hGet(key.getBytes(), hashKey.getBytes());
				return SerializationUtil.deserizlize(str);
			}
		});
	}

	@Override
	public void hDelete(final String key, final String hashKey) {

		jedisExcuteTemplate.excute(new Callback() {

			public Object command(RedisConnection redisConnection) {

				return redisConnection.hDel(key.getBytes(), hashKey.getBytes());

			}
		});

	}

	@Override
	public void delete(final String key) {

		jedisExcuteTemplate.excute(new Callback() {

			public Object command(RedisConnection redisConnection) {

				return redisConnection.del(key.getBytes());

			}
		});

	}
	
	
	
	@Override
	public void delete(final Collection<String> keys) {

		if(CollectionUtils.isEmpty(keys)) {
			return;
		}
		
		jedisExcuteTemplate.excute(new Callback() {

			public Object command(RedisConnection redisConnection) {

				return redisConnection.del(rawKeys(keys));

			}
		});

	}
	
	
	
	private byte[][] rawKeys(Collection<String> keys) {
		final byte[][] rawKeys = new byte[keys.size()][];

		int i = 0;
		for (String key : keys) {
			rawKeys[i++] = rawKey(key);
		}

		return rawKeys;
	}
	
	
	private byte[] rawKey(String key) {

		return key.getBytes();
	}

	

	@Override
	public void expire(final String key, final long timeout, final TimeUnit unit) {
		
		
		jedisExcuteTemplate.excute(new Callback() {

			public Object command(RedisConnection redisConnection) {

				return redisConnection.expire(key.getBytes(), TimeoutUtils.toSeconds(timeout, unit));

			}
		});
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> entries(String key) {

		
		if(StringUtils.isEmpty(key)) {
			return null;
		}

		
		final byte[] rawKey = key.getBytes();
		
		return (Map<String, Object>) jedisExcuteTemplate.excute(new Callback() {

			public Object command(RedisConnection redisConnection) {

				Map<byte[], byte[]> entries =  redisConnection.hGetAll(rawKey);
				
				return deserializeHashMap(entries);

			}
		});
	}
	
	
	
	@SuppressWarnings("unchecked")
	Map<String, Object> deserializeHashMap(Map<byte[], byte[]> entries) {
		// connection in pipeline/multi mode
		if (entries == null) {
			return null;
		}

		Map<String, Object> map = new LinkedHashMap<String, Object>(entries.size());

		for (Map.Entry<byte[], byte[]> entry : entries.entrySet()) {

			map.put(new String(entry.getKey()), SerializationUtil.deserizlize(entry.getValue()));
		}

		return map;
	}

}
