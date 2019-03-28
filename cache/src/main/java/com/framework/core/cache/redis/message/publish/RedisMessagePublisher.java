package com.framework.core.cache.redis.message.publish;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.framework.core.cache.redis.GracefulRedisTemplate;
import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.cache.redis.message.RedisMessageListenerMgr;
import com.framework.core.cache.redis.message.RedisTopicCacheKeyBuilder;
import com.framework.core.error.exception.BizException;

@Component
public class RedisMessagePublisher
{

	private static Logger logger = LoggerFactory.getLogger(RedisMessagePublisher.class);

	@Resource
	private GracefulRedisTemplate<String, String> gracefulRedisTemplate;

	@Resource
	private RedisMessageListenerMgr redisMessageListenerMgr;

	/**
	 * 发送redis消息
	 * @param topic
	 * @param message
	 */
	public void sendMessage(String topic, String message)
	{

		String realTopic = RedisTopicCacheKeyBuilder.buildRealTopic(getTopicPrefix(), topic);

		try
		{

			gracefulRedisTemplate.convertAndSend(realTopic, message);

		}
		catch (Exception e)
		{

			logger.error("redis message发送失败,[topic]:" + realTopic + ",[message]:" + message, e);

			throw new BizException(RedisErrorCode.EX_SYS_REDIS_MSG_SEND_FAILED.getCode(), e);
		}
	}

	/**
	 * 获取webcontext
	 * @return
	 */
	public String getTopicPrefix()
	{

		String webContext = redisMessageListenerMgr.getWebContext();

		Assert.isTrue(StringUtils.isNotBlank(webContext));

		return webContext;
	}

}