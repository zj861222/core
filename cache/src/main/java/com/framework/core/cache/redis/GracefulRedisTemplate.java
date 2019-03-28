package com.framework.core.cache.redis;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;


/**
 *
 * 重写RedisTemplate,支持读写分离,支持long expire
 *
 */
public class GracefulRedisTemplate<K, V> extends RedisTemplate<K, V> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");

	/**
	 * spring redis 设置超长时间expire，会调用twemproxy不支持的time命令，导致报错。
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	@SuppressWarnings({
		"unchecked", "rawtypes"
	})
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



}
