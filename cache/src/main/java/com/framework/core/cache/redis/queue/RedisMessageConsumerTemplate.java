package com.framework.core.cache.redis.queue;


/**
 * redis 延迟队列消费者
 * @author zhangjun
 *
 */
public abstract class  RedisMessageConsumerTemplate {
	
	/**
	 * 监听的主题
	 * @return
	 */
	abstract String getDelayTopic();
	
	/**
	 * 处理消息
	 * @param object
	 */
	abstract void handleMessage(Object data);
	
}