package com.framework.core.kafka.factory.internal;

import java.util.Collection;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerRebalanceListener implements ConsumerRebalanceListener
{

	private static Logger logger = LoggerFactory.getLogger(KafkaConsumerRebalanceListener.class);

	private String consumerBeanName;

	public KafkaConsumerRebalanceListener(String consumerBeanName)
	{
		this.consumerBeanName = consumerBeanName;

	}

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions)
	{

		logger.warn("Kafka:ReBalance...............................Before,[consumerBeanName]:" + consumerBeanName);
		for (TopicPartition partition : partitions)
		{
			logger.warn("Kafka:ReBalanceBefore topic=" + partition.topic() + " , partition = " + partition.partition()
					+ ", consumerBeanName=" + consumerBeanName);
		}

	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions)
	{

		logger.warn("Kafka:ReBalanceAfter...............................After,[consumerBeanName]:" + consumerBeanName);

		Consumer<String, String> consumer = KafkaConsumerManager.getKafkaConsumerByName(consumerBeanName);

		if (consumer == null)
		{
			return;
		}

		for (TopicPartition partition : partitions)
		{
			try
			{

				// if(true){
				// throw new BizException(1,"test ex");
				// }

				CustomizeKafkaOffset customizeKafkaOffset = new CustomizeKafkaOffset(CustomizeKafkaOffset.PREFIX,
						partition.topic(), partition.partition());

				long offset = customizeKafkaOffset.readNextOffsetFromExternalStore();

				logger.warn("Kafka:ReBalanceAfter topic=" + partition.topic() + ", partition =" + partition.partition()
						+ ", offset = " + offset + ",consumerBeanName=" + consumerBeanName);

				// 每次rebalance，根据zk里的自己管理的offset，重新设置offset的值，
				consumer.seek(partition, offset);

			}
			catch (Exception e)
			{
				logger.warn("节点Rebalance出现异常，即将关闭消费者,并且重启consumer订阅topic ", e);

				KafkaConsumerManager.closeAndRestartConsumer(consumerBeanName);

				logger.warn("节点Rebalance出现异常，关闭消费者,并且重启consumer订阅topic成功!", e);

			}

		}

	}

}