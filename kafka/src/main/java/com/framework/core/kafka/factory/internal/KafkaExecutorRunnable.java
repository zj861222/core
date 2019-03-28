package com.framework.core.kafka.factory.internal;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.core.kafka.AbstractKafkaConsumerExecutor;

/**
 * kafka 执行器线程
 * 
 * @author zhangjun
 *
 */
public class KafkaExecutorRunnable implements Runnable
{

	private static Logger logger = LoggerFactory.getLogger(KafkaExecutorRunnable.class);

	private String executorbeanName;

	public KafkaExecutorRunnable(String beanName)
	{

		this.executorbeanName = beanName;
	}

	@Override
	public void run()
	{

		while (KafkaConsumerManager.isKafkaStart())
		{

			// 轮训的获取kafka消息，只要不发生zk异常，或者 kafka poll异常
			// 当发生这2种异常的时候，先关闭consumer，然后重启consumer。
			// 然后继续下一轮无限循环
			executeDataProcess();

			try
			{
				Thread.sleep(2000L);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * 执行kafka数据处理，当发生异常的时候，才会退出，这个时候，先关闭，然后重启consumer。
	 * 
	 * 继续下一次循环
	 */
	private void executeDataProcess()
	{

		try
		{

			// 这里不会抛出业务模块处理message的异常，抛出的都是比如 customer poll不到信息，zk更新失败之类的异常。
			// 如果是这类异常，那么直接关闭consumer，重新启动订阅consumer。
			execute();
		}
		catch (Exception e)
		{

			logger.error("KafkaExecutorRunnable run with unexpect exception,[bean]:" + executorbeanName, e);

			KafkaConsumerManager.closeAndRestartConsumer(executorbeanName);

		}

	}

	/**
	 * 注意 这里不会抛出业务模块处理message的异常，抛出的都是比如 customer poll不到信息，zk更新失败之类的异常。
	 * 
	 * 如果是这类异常，那么直接关闭consumer，重新启动订阅consumer。
	 * @throws Exception
	 */
	private void execute() throws Exception
	{

		AbstractKafkaConsumerExecutor executor = KafkaConsumerManager.getKafkaConsumerExecutorByName(executorbeanName);

		if (executor == null)
		{
			return;
		}

		while (KafkaConsumerManager.isKafkaStart())
		{

			KafkaConsumer<String, String> consumer = KafkaConsumerManager.getKafkaConsumerByName(executorbeanName);
			if (consumer == null)
			{
				return;
			}

			ConsumerRecords<String, String> customerRecords = consumer.poll(executor.getPoolTimeOut());
			//
			if (customerRecords == null || customerRecords.isEmpty())
			{
				// 获取不到休息0.5s再获取
				Thread.sleep(500L);

				continue;
			}

			// 遍历records
			for (ConsumerRecord<String, String> record : customerRecords)
			{

				CustomizeKafkaOffset customizeKafkaOffset = new CustomizeKafkaOffset(CustomizeKafkaOffset.PREFIX,
						record.topic(), record.partition());

				long offsetSaved = customizeKafkaOffset.readCurrentOffsetData();

				if (record.offset() < offsetSaved)
				{

					logger.info("KafkaExecutorRunnable ingore has processed record, [beanName]:" + executorbeanName
							+ ",[topic]:" + record.topic() + ",[partition]:" + record.partition() + ",[offset]:"
							+ record.offset() + ",[offsetSaved]:" + offsetSaved);
					continue;
				}

				logger.debug("KafkaExecutorRunnable begin processed record, [beanName]:" + executorbeanName
						+ ",[topic]:" + record.topic() + ",[partition]:" + record.partition() + ",[offset]:"
						+ record.offset() + ",[offsetSaved]:" + offsetSaved);

				executor.processData(record);

			}

		}

	}

}