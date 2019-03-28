package com.framework.core.test.message.redis;


import org.springframework.stereotype.Component;

import com.framework.core.cache.redis.message.subscriber.RedisMessageSubscriber;


@Component
public class MyRedisMessageSubscriber extends RedisMessageSubscriber {

	@Override
	public void handleMessage(String message)
	{
		
		System.out.println(",[message]:"+message);
		
	}

	@Override
	public String getRedisMessageTopic()
	{
		return "tp:test:1";
	}
	
	
}