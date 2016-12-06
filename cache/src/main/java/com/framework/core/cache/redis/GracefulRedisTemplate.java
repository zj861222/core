package com.framework.core.cache.redis;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;

import com.framework.core.cache.redis.serializer.SerializationUtil;

/**
 *
 * 重写RedisTemplate,支持读写分离,支持long expire
 *
 */
public class GracefulRedisTemplate<K, V> extends RedisTemplate<K, V> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");

	/**
	 * spring redis 设置超长时间expire，会调用twemproxy不支持的time命令，导致报错。
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public Boolean longExpire(K key, final long timeout, final TimeUnit unit) {

		try {
			final byte[] rawKey = String.valueOf(key).getBytes("UTF-8");

			return (Boolean) this.execute(new RedisCallback() {
				@Override
				public Boolean doInRedis(RedisConnection connection) {

					return connection.expire(rawKey, TimeoutUtils.toSeconds(timeout, unit));
				}
			}, true);
		} catch (UnsupportedEncodingException e) {
			logger.warn("unsupport this encoding :{}", e);
		}

		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void hSetObject(String key, String hashKey, Object value) {

		try {
			final byte[] rawKey = String.valueOf(key).getBytes("UTF-8");

			final byte[] field = String.valueOf(hashKey).getBytes("UTF-8");

			final byte[] hashvalue = SerializationUtil.serialize(value);

			this.execute(new RedisCallback() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					return connection.hSet(rawKey, field, SerializationUtil.serialize(hashvalue));
				}
			}, true);

		} catch (UnsupportedEncodingException e) {
			logger.warn("unsupport this encoding :{}", e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object hGetObject(String key, String hashKey) {

		try {
			final byte[] rawKey = String.valueOf(key).getBytes("UTF-8");

			final byte[] field = String.valueOf(hashKey).getBytes("UTF-8");

			return this.execute(new RedisCallback() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {

					byte[] reuslt = connection.hGet(rawKey, field);

					return SerializationUtil.deserizlize(reuslt);
				}
			});

		} catch (UnsupportedEncodingException e) {
			logger.warn("unsupport this encoding :{}", e);

			return null;
		}

	}

	@Override
	public void delete(K key) {

		super.delete(key);

	}

	@Override
	public void delete(Collection<K> keys) {

		super.delete(keys);

	}

	/**
	 * 这个方法，不触发同步操作，业务模块不允许调用
	 * 
	 * @param keys
	 * @param isNeedCheckCacheDelete
	 */
	public void deleteWithoutSyncDelete(Collection<K> keys) {

		super.delete(keys);
	}

}
