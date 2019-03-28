package com.framework.core.cache.redis.constants;


/**
 * 构建redis key
 * @author zhangjun
 *
 */
public class RedisKeyBuilder {
	
	
	
	/**
	 * 申请注册server 的 index
	 * @param context
	 * @param index
	 * @return
	 */
	public static String generateServerRegisterIndexKey(String context,String index){
		
		return "reg:server:"+context+":index:"+index;
	}
	
	
}