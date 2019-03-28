package com.framework.core.cache.redis.message;


/**
 * 
 * @author zhangjun
 *
 */
public class RedisTopicCacheKeyBuilder {
	
	
	
	/**
	 * 构建实际传入redis的topic,比如业务传的是 11，context是appsvr  那么实际的topic是 rdtopic:appsvr:11
	 * 
	 * @param prefix 一般是webcontext，比如appsvr
	 * @param bizTopic 业务的topic
	 * @return
	 */
	public static String buildRealTopic(String prefix,String bizTopic) {
		
		return "rdtopic:"+prefix+":"+bizTopic;
	}
	
}