package com.framework.core.cache.redis.queue;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.framework.core.cache.redis.utils.RedisHelper;

/**
 * redis 消息/延迟消息生产者
 * @author zhangjun
 *
 */
@Component
public  class RedisProducerTemplate {
	
	
	@Resource
	private RedisHelper redisHelper;
	
   /**
    *  发送延迟消息
    * @param topic
    * @param data
    * @param delay
    * @param unit
    */
	public void sendDelayMessage(String topic,Object data,long delay,TimeUnit unit) {
		
		
		
	}
	
	
	/**
	 * 发送消息
	 * @param topic
	 * @param data
	 */
	public void sendMessage(String topic,Object data) {
		
	}


	
}