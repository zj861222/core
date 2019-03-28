package com.framework.core.cache.redis.queue;


public class RedisMessage {
	
	/**
	 * 
	 */
	private String exchange;
	
	
	/**
	 * 主题
	 */
	private String topic;
	
	
	/**
	 * 数据
	 */
	private String data;
	
	
	/**
	 * 延迟时间
	 */
	private int delayInMinutes; 
	
}