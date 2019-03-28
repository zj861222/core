package com.framework.core.message.rabbit.factory.internal.producer;

import com.framework.core.message.rabbit.constants.ExchangeType;
import com.framework.core.message.rabbit.constants.RabbitConstants;

import java.util.Map;

/**
 * 发送消息到Message broker中 当前是发送到topic的 exchange 中
 * 
 */
public class TopicProducerTemplate extends BaseProducerTemplate {

	/**
	 * 延迟消息的topic前缀
	 */
	private final String TOPIC_DELAY_PREFIX = "waiqin365_delay.";

	/**
	 * 延迟发送消息到默认的exchange--default.amq.delay_topic
	 * 
	 * @param topic
	 *            消息的主题
	 * @param object
	 *            消息对象，框架会自动转化为JSON
	 * @param attributes
	 *            消息属性
	 * @param delayInMinutes
	 *            延迟多少分钟，必须和consumers配置的保持一致
	 */
	public void sendDelay(String topic, Object object, Map<String, Object> attributes, int delayInMinutes) {

		sendDelay(RabbitConstants.DEFAULT_TOPIC_DELAY_EXCHAGE, topic, object, attributes, delayInMinutes);
	}

	/**
	 * 发送延迟消息到exchange
	 * 
	 * @param exchange
	 *            exchange名，必须和consumers配置的保持一致
	 * @param topic
	 *            消息主题
	 * @param object
	 *            消息对象，框架自动转为json
	 * @param attributes
	 *            消息属性
	 * @param delayInMinutes
	 *            延迟多少分钟，必须和consumers配置的保持一致
	 */
	public void sendDelay(String exchange, String topic, Object object, Map<String, Object> attributes,
			int delayInMinutes) {

		String sent_topic = TOPIC_DELAY_PREFIX + delayInMinutes + "m." + topic;

		send(exchange, sent_topic, object, attributes, ExchangeType.EXCHANGE_TYPE_TOPIC);
	}

	/**
	 * 发送消息到默认的topic exchange--default.amq.topic
	 * 
	 * @param topic
	 *            消息主题
	 * @param object
	 *            消息体，java对象
	 */
	public void send(String topic, Object data) {
		send(RabbitConstants.DEFAULT_TOPIC_EXCHAGE, topic, data, null);
	}

	/**
	 * 发送 topic消息到指定的exchange
	 * 
	 * @param exchange
	 *            exchange
	 * @param topic
	 *            topic
	 * @param data
	 *            消息对象
	 * @param attributes
	 *            消息属性
	 */
	public void send(String exchange, String topic, Object data, Map<String, Object> attributes) {

		this.send(exchange, topic, data, attributes, ExchangeType.EXCHANGE_TYPE_TOPIC);
	}

}
