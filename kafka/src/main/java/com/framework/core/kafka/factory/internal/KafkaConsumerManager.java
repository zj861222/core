package com.framework.core.kafka.factory.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.framework.core.common.lock.SegmentLock;
import com.framework.core.error.exception.BizException;
import com.framework.core.kafka.AbstractKafkaConsumerExecutor;
import com.framework.core.kafka.exception.KafkaErrorCode;

/**
 * 
 * kafka 消费者管理。
 * 
 * 
 * @author zhangjun
 *
 */
public class KafkaConsumerManager
{

	private static Logger logger = LoggerFactory.getLogger(KafkaConsumerManager.class);

	private static SegmentLock<String> segmentLock = new SegmentLock<>(5, false);

	// executor执行状态
	private static Map<String, AtomicBoolean> executorStat = new HashMap<>();

	private static AtomicBoolean isKafkaStart = new AtomicBoolean(false);

	/**
	 * kafka消费者map
	 */
	private static Map<String, KafkaConsumer<String, String>> kafkaConsumerMap = new HashMap<>();

	/**
	 * 注册执行器的map
	 */
	private static Map<String, AbstractKafkaConsumerExecutor> regisExecutorMap = new HashMap<>();

	/**
	 * 注册自定义的消费者executor,启动consumer
	 * @param consumerExeutor
	 */
	public static void registerKafkaConsumerExecutorsAndStartConsumer(AbstractKafkaConsumerExecutor consumerExeutor)
	{
		regisExecutorMap.put(consumerExeutor.getConsumerExecutorBeanName(), consumerExeutor);

		executorStat.put(consumerExeutor.getConsumerExecutorBeanName(), new AtomicBoolean(false));
		// 启动consumer，订阅topic
		startAndRegisterConsumer(consumerExeutor);

	}

	
	/**
	 * kafka状态设置为start
	 * @return
	 */
	public static boolean setKafkaStatToStart()
	{

		boolean isSuccess = isKafkaStart.compareAndSet(false, true);

		return isSuccess;
	}

	/**
	 * kafka状态设置为stop
	 * @return
	 */
	public static boolean setKafkaStatToStop()
	{

		boolean isSuccess = isKafkaStart.compareAndSet(true, false);

		return isSuccess;
	}

	/**
	 * kafka是否启动
	 * @return
	 */
	public static boolean isKafkaStart()
	{
		return isKafkaStart.get();
	}

	/**
	 * 根据bean明获取对应的consumer实例
	 * @param beanName
	 * @return
	 */
	public static KafkaConsumer<String, String> getKafkaConsumerByName(String beanName)
	{

		return kafkaConsumerMap.get(beanName);
	}

	/**
	 * 根据bean明获取对应的consumer executor实例
	 * @param beanName
	 * @return
	 */
	public static AbstractKafkaConsumerExecutor getKafkaConsumerExecutorByName(String beanName)
	{

		return regisExecutorMap.get(beanName);
	}

	public static void closeAllConsumers()
	{

		for (KafkaConsumer<String, String> consumer : kafkaConsumerMap.values())
		{

			if (consumer == null)
			{
				continue;
			}

			try
			{
				consumer.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		kafkaConsumerMap.clear();

	}

	/**
	 * 关闭并且重启consumer，重新订阅topic
	 * @param consumerBeanName
	 */
	public static void closeAndRestartConsumer(String consumerBeanName)
	{

		KafkaConsumer<String, String> consumer = kafkaConsumerMap.get(consumerBeanName);

		if (consumer == null)
		{
			return;
		}

		kafkaConsumerMap.remove(consumerBeanName);

		try
		{
			consumer.close();
		}
		catch (Exception e)
		{
			logger.error("KafkaConsumerManager closeAndRestartConsumer exception", e);
		}

		consumer = null;

		AbstractKafkaConsumerExecutor executor = regisExecutorMap.get(consumerBeanName);
		Assert.notNull(executor);

		int times = 0;

		do
		{
			try
			{

				startAndRegisterConsumer(executor);
				break;

			}
			catch (Exception e)
			{
				times++;
				logger.error("closeAndRestartConsumer restart consumer failed,[consumerBeanName]:" + consumerBeanName
						+ ",[times]:" + times, e);
			}

		}
		while (times <= 3);

	}

	/**
	 * 启动并注册消费者
	 * 
	 * @param consumerExeutor
	 */
	private static void startAndRegisterConsumer(AbstractKafkaConsumerExecutor consumerExeutor)
	{

		if (consumerExeutor == null || !consumerExeutor.isOpenConsumer())
		{

			return;
		}

		boolean isLocked = false;

		try
		{
			// 加锁
			isLocked = segmentLock.lock(consumerExeutor.getConsumerExecutorBeanName(), 1, TimeUnit.MINUTES);

			if (isLocked)
			{

				try
				{
					// 启动consumer 订阅
					realStartConsumer(consumerExeutor);

				}
				catch (Exception e)
				{
					logger.error("启动kafka consumer订阅失败,[bean]:" + consumerExeutor.getConsumerExecutorBeanName(), e);
					throw e;
				}

			}
			else
			{
				logger.error("启动kafka consumer订阅失败,未获取到锁,[bean]:" + consumerExeutor.getConsumerExecutorBeanName());
				throw new BizException(KafkaErrorCode.EX_KAFKA_CONSUMER_START_FAILED.getCode(), "启动kafka消费者异常");
			}

		}
		finally
		{

			// 释放锁
			if (isLocked)
			{
				segmentLock.unlock(consumerExeutor.getConsumerExecutorBeanName());
			}

		}

	}

	/**
	 * 重启
	 * @param consumerExeutor
	 */
	private static void realStartConsumer(AbstractKafkaConsumerExecutor consumerExeutor)
	{

		if (kafkaConsumerMap.containsKey(consumerExeutor.getConsumerExecutorBeanName()))
		{
			return;
		}

		List<String> topics = consumerExeutor.getTopics();

		if (topics.isEmpty())
		{

			logger.warn("KafkaConsumerExecutor未配置监听主题![bean]:" + consumerExeutor.getConsumerExecutorBeanName());
			return;
		}

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(
				consumerExeutor.getKafkaConsumerProperties());

		// 消费者订阅消息
		consumer.subscribe(topics, new KafkaConsumerRebalanceListener(consumerExeutor.getConsumerExecutorBeanName()));

		kafkaConsumerMap.put(consumerExeutor.getConsumerExecutorBeanName(), consumer);
	}

}