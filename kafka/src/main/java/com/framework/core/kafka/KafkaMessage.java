package com.framework.core.kafka;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 *  message消息体
 *  
 * @author zhangjun
 *
 */
public class KafkaMessage implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1720572025141455078L;

	/**
	 * 默认最大重发的次数3次
	 */
	public static final int DEFAULT_EXTEND_MESSAGE_MAX_RESEND_LIMIT = 3;

	/**
	 * 当前重发的次数
	 */
	private int resendTimes = 0;

	/**
	 * 最大重发次数限制
	 */
	private int maxResendLimit = DEFAULT_EXTEND_MESSAGE_MAX_RESEND_LIMIT;

	/**
	 * 实际的data数据
	 */
	private String data;

	/**
	 * topic
	 */
	private String topic;

	/**
	 * 分片的key
	 */
	private String partitionKey;

	public String getPartitionKey()
	{
		return partitionKey;
	}

	public String getTopic()
	{
		return topic;
	}

	public KafkaMessage()
	{

	}

	public KafkaMessage(String topic, String partitionKey, String data)
	{

		this.data = data;

		this.topic = topic;

		this.partitionKey = partitionKey;

	}

	public KafkaMessage(String topic, String partitionKey, String data, int currentResendTimes)
	{

		this.data = data;

		this.resendTimes = currentResendTimes;

		this.topic = topic;

		this.partitionKey = partitionKey;

	}

	public KafkaMessage(String topic, String partitionKey, String data, int currentResendTimes, int maxResendTimes)
	{

		this.data = data;

		this.resendTimes = currentResendTimes;

		this.maxResendLimit = maxResendTimes;

		this.topic = topic;

		this.partitionKey = partitionKey;

	}

	public void setResendTimes(int resendTimes)
	{
		this.resendTimes = resendTimes;
	}

	public void setMaxResendLimit(int maxResendLimit)
	{
		this.maxResendLimit = maxResendLimit;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public void setPartitionKey(String partitionKey)
	{
		this.partitionKey = partitionKey;
	}

	public int getResendTimes()
	{
		return resendTimes;
	}

	public int getMaxResendLimit()
	{
		return maxResendLimit;
	}

	public String getData()
	{
		return data;
	}

	/**
	 * 增加重发次数
	 */
	public void increaseResendTime()
	{

		resendTimes = resendTimes + 1;
	}

	/**
	 * 判断是否达到最大重发次数
	 * @return
	 */
	public boolean isReachMaxResendLimit()
	{

		return resendTimes >= maxResendLimit;
	}

	/**
	 * 校验参数
	 */
	public boolean isValidateMessage()
	{

		return StringUtils.isNotBlank(topic) && StringUtils.isNotBlank(partitionKey) && StringUtils.isNotBlank(data);

	}

}