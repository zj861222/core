package com.framework.core.cache.redis.message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.util.ErrorHandler;

import com.framework.core.cache.redis.message.subscriber.RedisMessageSubscriber;

/**
 * 
 * @author zhangjun
 *
 */
public class RedisMessageListenerContainerFactory
		implements FactoryBean<RedisMessageListenerContainer>, InitializingBean, DisposableBean, SmartLifecycle
{

	private RedisMessageListenerContainer redisMessageListenerContainer;

	/**
	 * Specify the max time to wait for subscription registrations, in milliseconds. The default is 2000ms, that is, 2 second.
	 */
	private long maxSubscriptionRegistrationWaitingMillisSeconds;

	/**
	 * Specify the interval between recovery attempts, in milliseconds. The default is 5000 ms, that is, 5 seconds
	 */
	private long recoveryIntervalMillisSeconds;

	private RedisConnectionFactory redisConnectionFactory;

	private ErrorHandler errorHandler;

	private RedisMessageListenerMgr redisMessageListenerMgr;

	public RedisMessageListenerContainer getRedisMessageListenerContainer()
	{
		return redisMessageListenerContainer;
	}

	public void setRedisMessageListenerContainer(RedisMessageListenerContainer redisMessageListenerContainer)
	{
		this.redisMessageListenerContainer = redisMessageListenerContainer;
	}

	public long getMaxSubscriptionRegistrationWaitingMillisSeconds()
	{
		return maxSubscriptionRegistrationWaitingMillisSeconds;
	}

	public void setMaxSubscriptionRegistrationWaitingMillisSeconds(long maxSubscriptionRegistrationWaitingMillisSeconds)
	{
		this.maxSubscriptionRegistrationWaitingMillisSeconds = maxSubscriptionRegistrationWaitingMillisSeconds;
	}

	public long getRecoveryIntervalMillisSeconds()
	{
		return recoveryIntervalMillisSeconds;
	}

	public void setRecoveryIntervalMillisSeconds(long recoveryIntervalMillisSeconds)
	{
		this.recoveryIntervalMillisSeconds = recoveryIntervalMillisSeconds;
	}

	public RedisConnectionFactory getRedisConnectionFactory()
	{
		return redisConnectionFactory;
	}

	public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory)
	{
		this.redisConnectionFactory = redisConnectionFactory;
	}

	public ErrorHandler getErrorHandler()
	{
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler)
	{
		this.errorHandler = errorHandler;
	}

	public RedisMessageListenerMgr getRedisMessageListenerMgr()
	{
		return redisMessageListenerMgr;
	}

	public void setRedisMessageListenerMgr(RedisMessageListenerMgr redisMessageListenerMgr)
	{
		this.redisMessageListenerMgr = redisMessageListenerMgr;
	}

	@Override
	public void destroy() throws Exception
	{
		redisMessageListenerContainer.destroy();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		redisMessageListenerContainer = new RedisMessageListenerContainer();

		redisMessageListenerContainer.afterPropertiesSet();
		
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);

		redisMessageListenerContainer.setErrorHandler(errorHandler);

		redisMessageListenerContainer
				.setMaxSubscriptionRegistrationWaitingTime(maxSubscriptionRegistrationWaitingMillisSeconds);

		redisMessageListenerContainer.setRecoveryInterval(recoveryIntervalMillisSeconds);
		
		// redisMessageListenerContainer.setSubscriptionExecutor(subscriptionExecutor);
		// redisMessageListenerContainer.setTaskExecutor(taskExecutor);
		
//		redisMessageListenerContainer.start();

		//注册订阅的监听器
		registerListener();
	}
	
	
	
	
	private void registerListener() {
		

		Vector<RedisMessageSubscriber> allSubscriber = redisMessageListenerMgr.getAllSubscribers();

		// 注册订阅的listener
		if (allSubscriber != null && allSubscriber.size() > 0)
		{
			Map<MessageListener, Collection<? extends Topic>> listenersMap = new HashMap<>();
			for (RedisMessageSubscriber subscriber : allSubscriber)
			{
				// 添加listener
			
				Collection<? extends Topic> topics = subscriber.getAllTopic();
				
				MessageListener messageListener = (MessageListener)subscriber;
				
				listenersMap.put(messageListener, topics);
			}
			
			redisMessageListenerContainer.setMessageListeners(listenersMap);

		}
		
	}

	@Override
	public RedisMessageListenerContainer getObject() throws Exception
	{
		return redisMessageListenerContainer;
	}

	@Override
	public Class<?> getObjectType()
	{
		return RedisMessageListenerContainer.class;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}

	@Override
	public void start()
	{
		redisMessageListenerContainer.start();
	}

	@Override
	public void stop()
	{
		redisMessageListenerContainer.stop();
	}

	@Override
	public boolean isRunning()
	{
		// TODO Auto-generated method stub
		return redisMessageListenerContainer.isRunning();
	}

	@Override
	public int getPhase()
	{
		return redisMessageListenerContainer.getPhase();
	}

	@Override
	public boolean isAutoStartup()
	{
		return redisMessageListenerContainer.isAutoStartup();
	}

	@Override
	public void stop(Runnable callback)
	{
		redisMessageListenerContainer.stop(callback);
	}

//	@Override
//	public void setBeanName(String name)
//	{
//		redisMessageListenerContainer.setBeanName(name);
//	}

}
