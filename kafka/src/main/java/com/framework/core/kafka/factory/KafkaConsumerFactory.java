package com.framework.core.kafka.factory;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.kafka.AbstractKafkaConsumerExecutor;
import com.framework.core.kafka.factory.internal.KafkaConsumerManager;
import com.framework.core.kafka.factory.internal.KafkaExecutorRunnable;
import com.framework.core.zookeeper.util.ZookeeperClientHelper;

/**
 * 
 * kafka 消费者信息。
 * 
 * @author zhangjun
 *
 */
@Component
public class KafkaConsumerFactory implements ApplicationContextAware
{

	private static Logger logger = LoggerFactory.getLogger(KafkaConsumerFactory.class);

	private int totalConsumerNum;
	
	//注入只是为了控制bean加载顺序，保证 zookeeperClientHelper先加载
	@Resource
	private ZookeeperClientHelper zookeeperClientHelper;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{

		String isEnable = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.enable", "1");
		if (!"1".equals(isEnable))
		{
			logger.warn("KafkaConsumerFactory decide not to start consumer ,[core.kafka.enable]" + isEnable);
			return;
		}

		Map<String, AbstractKafkaConsumerExecutor> map = applicationContext
				.getBeansOfType(AbstractKafkaConsumerExecutor.class);

		if (MapUtils.isEmpty(map))
		{
			return;
		}

		// 初始化kafka消费者
		registerKafkaConsumerExecutorsAndStartConsumer(map);

	}

	/**
	 * 注册 kafka  消费者执行器，启动consumer
	 * 
	 */
	public void registerKafkaConsumerExecutorsAndStartConsumer(Map<String, AbstractKafkaConsumerExecutor> map)
	{

		Set<AbstractKafkaConsumerExecutor> enableCustomers = new HashSet<>();

		for (Entry<String, AbstractKafkaConsumerExecutor> executorEntry : map.entrySet())
		{

			if (executorEntry.getValue().isOpenConsumer())
			{
				enableCustomers.add(executorEntry.getValue());
			}
		}

		totalConsumerNum = enableCustomers.size();

		KafkaConsumerManager.setKafkaStatToStart();

		ExecutorService pool = Executors.newFixedThreadPool(totalConsumerNum);

		for (AbstractKafkaConsumerExecutor executor : enableCustomers)
		{

			// 启动consumer订阅
			KafkaConsumerManager.registerKafkaConsumerExecutorsAndStartConsumer(executor);

			// 启动数据处理线程
			KafkaExecutorRunnable runable = new KafkaExecutorRunnable(executor.getConsumerExecutorBeanName());

			pool.submit(runable);
		}

	}



	/**
	 * bean销毁
	 */
	@PreDestroy
	public void onDestory()
	{

		KafkaConsumerManager.setKafkaStatToStop();

		try
		{

			Thread.sleep(3000L);

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		KafkaConsumerManager.closeAllConsumers();

	}

}