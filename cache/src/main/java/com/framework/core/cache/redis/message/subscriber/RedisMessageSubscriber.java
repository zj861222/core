package com.framework.core.cache.redis.message.subscriber;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.util.Assert;

import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.cache.redis.message.RedisMessageListenerMgr;
import com.framework.core.cache.redis.message.RedisTopicCacheKeyBuilder;
import com.framework.core.cache.redis.message.constants.TopicMatchTypeEnum;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.error.exception.BizException;

/**
 * 
 * @author zhangjun
 *
 */
public abstract class RedisMessageSubscriber implements MessageListener
{
	private static Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

	@Resource
	private RedisHelper redisHelper;

	@Resource
	private RedisMessageListenerMgr redisMessageListenerMgr;

	/**
	 * Callback for processing received objects through Redis.
	 * 
	 */
	@Override
	public void onMessage(Message message, byte[] pattern)
	{

		byte[] bodyBtys = message.getBody();

		byte[] channelBtys = message.getChannel();

//		System.out.println("[pattern]:" + new String(pattern) + ",[bodyBtys]:" + new String(bodyBtys)
//				+ ",[channelBtys]:" + new String(channelBtys));

		String topic = new String(channelBtys);

		String data = new String(bodyBtys);

		try
		{
			handleMessage(data);

		}
		catch (Exception e)
		{

			logger.error("RedisMessageSubscriber receive message and process failed,[topic]:" + topic + ",[data]:" + data,e);

			throw new BizException(RedisErrorCode.EX_SYS_REDIS_MSG_RECEIVED_FAILED.getCode(), e);
		}
	}

	/**
	 * 接收message的处理逻辑
	 * @param message
	 */
	public abstract void handleMessage(String message);
	
	

	/**
	 * 获取监听的所有的topic
	 * @return
	 */
	public List<Topic> getAllTopic()
	{
		List<Topic> topicList = new ArrayList<>();

		final String topic = getRedisMessageTopic();

		Assert.isTrue(StringUtils.isNotBlank(topic));

		TopicMatchTypeEnum type = getTopicType();
		Assert.notNull(type);

		// 构建实际的topic
		topicList.add(createRealTopicInstance(type, topic));

		return topicList;
	}

	/**
	 * 创建实际的topic实力
	 * @param type
	 * @param topic
	 * @return
	 */
	private Topic createRealTopicInstance(TopicMatchTypeEnum type, String topic)
	{

		String realTopic = RedisTopicCacheKeyBuilder.buildRealTopic(getTopicPrefix(), topic);

		Topic topicObj = null;

		switch (type)
		{

			case TYPE_CHANNEL:
				topicObj = new ChannelTopic(realTopic);
				break;
			case TYPE_PATTERN_TOPIC:
				topicObj = new PatternTopic(realTopic);
				break;

			default:
				topicObj = new ChannelTopic(realTopic);
				break;
		}

		return topicObj;
	}

	/**
	 * 监听的topic.
	 * 
	 * @return
	 */
	public abstract String getRedisMessageTopic();

	/**
	 * 获取 topic
	 * @return
	 */
	public TopicMatchTypeEnum getTopicType()
	{

		return TopicMatchTypeEnum.TYPE_CHANNEL;
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