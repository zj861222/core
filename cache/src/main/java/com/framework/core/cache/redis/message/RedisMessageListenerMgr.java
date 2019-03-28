package com.framework.core.cache.redis.message;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.framework.core.cache.redis.message.subscriber.RedisMessageSubscriber;


/**
 * 管理注册所有的订阅器
 * 
 * @author zhangjun
 *
 */
public class RedisMessageListenerMgr implements ApplicationContextAware
{

	private Vector<RedisMessageSubscriber> vector = new Vector<RedisMessageSubscriber>();
	
	
	private String webContext;
	
	

	public String getWebContext()
	{
		return webContext;
	}

	public void setWebContext(String webContext)
	{
		this.webContext = webContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{

		Map<String, RedisMessageSubscriber> map = applicationContext.getBeansOfType(RedisMessageSubscriber.class);

		if (MapUtils.isEmpty(map))
		{
			return;
		}

		for (Entry<String, RedisMessageSubscriber> entry : map.entrySet())
		{
			vector.add(entry.getValue());
		}

	}

	/**
	 * 获取所有的redis订阅器
	 * @return
	 */
	public Vector<RedisMessageSubscriber> getAllSubscribers()
	{

		return vector;
	}
	


}