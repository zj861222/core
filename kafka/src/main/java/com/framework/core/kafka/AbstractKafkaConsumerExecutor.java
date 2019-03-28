package com.framework.core.kafka;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.framework.core.kafka.constants.KafkaFailedMessagePolicy;
import com.framework.core.kafka.factory.internal.AbstractConsumerPropertiesBuilder;
import com.framework.core.kafka.factory.internal.CustomizeKafkaOffset;

/**
 * kafka消费者。
 * 
 * @author zhangjun
 *
 */
public abstract class AbstractKafkaConsumerExecutor extends AbstractConsumerPropertiesBuilder implements BeanNameAware
{

	private static Logger logger = LoggerFactory.getLogger(AbstractKafkaConsumerExecutor.class);

	/**
	 * 消费者bean的名字
	 */
	private String consumerBeanName;

	/**
	 * clientid
	 */
	private String clientId;

	public static final long DEFAULT_POLL_TIME_OUT = 500;

	/**
	 * 获取consumer对应的topic主题
	 * 
	 * @return
	 */
	public abstract String getTopic();

	/**
	 * 获取所有的topics
	 * 
	 * @return
	 */
	public List<String> getTopics()
	{

		List<String> list = new ArrayList<>();

		String topic = getTopic();

		Assert.isTrue(StringUtils.isNotBlank(topic));

		list.add(getTopic());

		return list;
	}

	/**
	 * 是否开启consumer消费。
	 * 
	 * @return
	 */
	public abstract boolean isOpenConsumer();

	/**
	 * 处理 message 。
	 * 
	 * @param topic topic 
	 * @param partition 分片id 
	 * @param data 数据
	 * @return
	 */
	public abstract boolean handleMessage(String topic, int partition, String data);

	@Override
	public String getClientId()
	{

		return clientId;
	}

	@Override
	public void setBeanName(String name)
	{
		consumerBeanName = name;

		clientId = name;
	}

	/**
	 * 获取消费者bean的名字
	 * @return
	 */
	public String getConsumerExecutorBeanName()
	{

		return consumerBeanName;

	}

	/**
	 * pool操作的超时时间，防止pool过慢,默认为500毫秒
	 * 
	 * @return
	 */
	public long getPoolTimeOut()
	{

		return DEFAULT_POLL_TIME_OUT;
	}

	/**
	 * 处理数据,注意!!!
	 * 这里业务异常是不会抛出的，抛出的都是非业务异常，比如zk连接问题之类
	 * 
	 * @param key
	 * @param value
	 * @param offsetMgr
	 * @throws Exception
	 */
	public void processData(ConsumerRecord<String, String> record)
		throws Exception
	{
		
//		System.out.println("processData,topic="+record.topic()+",partition="+record.partition()+",val="+record.value()+",offset="+record.offset());
		
		KafkaMessage messageObj = buildDataObject(record.value());

		boolean isSuccess = false;

		if (messageObj == null)
		{
			logger.error(
					"AbstractKafkaConsumerExecutor parse ExtendMessage failed,it is not a extendmessage object,ignore it,topic="+record.topic()+",partition="+record.partition()+",val="+record.value()+",offset="+record.offset());

			isSuccess = true;
		}
		else
		{

			try
			{
				isSuccess = handleMessage(record.topic(), record.partition(), messageObj.getData());

			}
			catch (Exception e)
			{

				logger.error("AbstractKafkaConsumerExecutor handleMessage failed,[topic]:" + record.topic() + ",[partition]:"
						+ record.partition() + ",[data]:" + messageObj.getData(), e);

				isSuccess = false;
			}
		}

		// 处理成功了
		if (isSuccess)
		{
			onHandleSuccess(messageObj, record);
		}
		else
		{
			try
			{
				onHandleFailed(messageObj, record);
			}
			catch (Exception e)
			{
				logger.error("AbstractKafkaConsumerExecutor onHandleFailed failed,[topic]:" + record.topic() + ",[partition]:"
						+ record.partition() + ",[value]:" + record.value(), e);
				throw e;
			}
		}

	}

	/**
	 * 解析数据到KafkaMessage对象
	 * @param data
	 * @return
	 */
	public KafkaMessage buildDataObject(String data)
	{

		try
		{
			KafkaMessage messageObj = JSON.parseObject(data, KafkaMessage.class);

			return messageObj;
		}
		catch (Exception e)
		{
			logger.warn("AbstractKafkaConsumerExecutor parse message data failed,it is not KafkaMessage object![data]:"
					+ data, e);
			return null;
		}

	}


	
	/**
	 * 处理消息成功
	 * @param messageObj
	 * @param record
	 * @throws Exception
	 */
	public void onHandleSuccess(KafkaMessage messageObj, ConsumerRecord<String, String> record) throws Exception
	{

		// 推进offset
		saveOffset(record.topic(), record.partition(), record.offset());
	}

	/**
	 *  处理message失败。
	 *  
	 * @param key
	 * @param value
	 * @throws Exception 
	 */
	public void onHandleFailed(KafkaMessage messageObj, ConsumerRecord<String, String> record) throws Exception
	{

		KafkaFailedMessagePolicy policy = getProcessFailedMessagePolicy();

		Assert.notNull(policy);

		switch (policy)
		{

			case POLICY_RESEND_FAILED_MSG:
				// 重发失败消息
				resendFailedMessage(messageObj, record);
				return;

			case POLICY_IGNORE_FAILED_MSG:
				// 忽略失败消息，推进offset
				ignoreFailedMessage(messageObj, record);
				return;

			case POLICY_CUSTOM_FAILED_MSG:
				// 自定义策略
				customPolicy4FailedMessage(messageObj, record);
				return;

			default:
				resendFailedMessage(messageObj, record);
				return;

		}

	}

	/**
	 * 消息处理失败的时候的策略
	 * 
	 * 
	 * @return
	 */
	public KafkaFailedMessagePolicy getProcessFailedMessagePolicy()
	{

		return KafkaFailedMessagePolicy.POLICY_RESEND_FAILED_MSG;
	}


	
	/**
	 *  推进offset
	 * @param topic
	 * @param partition
	 * @param targetOffset
	 * @throws Exception
	 */
	public void saveOffset(String topic,int partition,long targetOffset) throws Exception
	{
		
		CustomizeKafkaOffset offsetObject = new CustomizeKafkaOffset(CustomizeKafkaOffset.PREFIX, topic, partition);
		
		// 保存当前的offset
		offsetObject.saveOffsetInExternalStore(targetOffset);

	}

	/**
	 *  处理message失败。重新发送
	 *  
	 * @param key
	 * @param value
	 * @param offsetObject
	 * @throws Exception 
	 */
	private void resendFailedMessage(KafkaMessage messageObj, ConsumerRecord<String, String> record) throws Exception
	{

		// 判断是否达到最大重发次数
		boolean isReachMaxResendLimit = messageObj.isReachMaxResendLimit();

		if (isReachMaxResendLimit)
		{
			logger.error(
					"AbstractKafkaConsumerExecutor process message failed, resend and find  it has reach the max send limit,[key]:"
							+ messageObj.getPartitionKey() + ",[value]:" + messageObj.getData() + ",[topic]:"
							+ messageObj.getTopic()+",[Partition]:"+record.partition()+",[offset]:"+record.offset());
			return;
		}

		// 重发次数加1
		messageObj.increaseResendTime();

		// 1. 重发消息到通道
		KafkaMessageSender.sendMessage(messageObj);

		// 2. 保存推进offset
		saveOffset(record.topic(), record.partition(), record.offset());

		logger.warn("AbstractKafkaConsumerExecutor process message failed, and resend Failed Message,[key]:"
				+ messageObj.getPartitionKey() + ",[value]:" + messageObj.getData() + ",[topic]:"
				+ messageObj.getTopic()+",[Partition]:"+record.partition()+",[offset]:"+record.offset());

	}

	/**
	 *  处理message失败。重新发送
	 *  
	 * @param key
	 * @param value
	 *  @param offsetObject
	 * @throws Exception 
	 */
	private void ignoreFailedMessage(KafkaMessage messageObj, ConsumerRecord<String, String> record) throws Exception
	{
		// 1. 推进offset,忽略失败消息
		saveOffset(record.topic(), record.partition(), record.offset());

		logger.warn("AbstractKafkaConsumerExecutor process message failed, and ignore Failed Message,[key]:"
				+ messageObj.getPartitionKey() + ",[value]:" + messageObj.getData() + ",[topic]:"
				+ messageObj.getTopic()+",[Partition]:"+record.partition()+",[offset]:"+record.offset());
	}

	/**
	 * 自定义策略处理失败消息。
	 *  
	 * @param key
	 * @param value
	 * @throws Exception 
	 */
	public void customPolicy4FailedMessage(KafkaMessage messageObj, ConsumerRecord<String, String> record) throws Exception
	{

	}

}