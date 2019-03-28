package com.framework.core.kafka;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.framework.core.kafka.factory.KafkaProducerFactory;

/**
 * 
 * kafka消息发送
 * 
 * @author zhangjun
 *
 */
@Component
public class KafkaMessageSender implements InitializingBean
{

	private static Logger logger = LoggerFactory.getLogger(KafkaMessageSender.class);

	private static KafkaMessageSender instance;

	@Resource
	private KafkaProducerFactory kafkaProducerFactory;

	@Override
	public void afterPropertiesSet() throws Exception
	{
		instance = new KafkaMessageSender();

		instance.kafkaProducerFactory = this.kafkaProducerFactory;
	}

	/**
	 * 发送kafka消息，可能抛出bizexception，为runtimeException.
	 * 
	 * @param mesage
	 * 
	 * 
	 */
	public static void sendMessage(KafkaMessage message)
	{

		if (message == null || message.isReachMaxResendLimit())
		{
			logger.warn("KafkaMessageSender ignore message which is null or reach the resend limit! [message]:"
					+ (message == null ? "null" : JSON.toJSONString(message)));
			return;
		}

		// 校验message参数
		if (!message.isValidateMessage())
		{
			logger.warn("KafkaMessageSender ignore message which is not vaild,[message]:" + JSON.toJSONString(message));
			return;
		}

		instance.kafkaProducerFactory.send(message.getTopic(), message.getPartitionKey(), JSON.toJSONString(message));
	}

	/**
	 * 发送消息,可能抛出bizexception，为runtimeException.
	 * @param topic 主题
	 * @param partitionKey 分片的key
	 * @param data 消息数据
	 */
	public static void sendMessage(String topic, String partitionKey, String data)
	{

		KafkaMessage message = new KafkaMessage(topic, partitionKey, data);

		sendMessage(message);
	}

}