package com.framework.core.message.rabbit.factory.internal.producer;

import com.alibaba.fastjson.JSON;
import com.framework.core.alarm.event.RabbitmqSentEvent;
import com.framework.core.message.rabbit.constants.ExchangeType;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.Map;

/**
 * 发送消息到Message broker中 
 * 
 */
public class BaseProducerTemplate implements ApplicationEventPublisherAware {

	private final Logger logger = LoggerFactory.getLogger(BaseProducerTemplate.class);

	private AmqpTemplate amqpTemplate;


	private ApplicationEventPublisher publisher;
//
//	/**
//	 * 延迟消息的topic前缀
//	 */
//	private final String TOPIC_DELAY_PREFIX = "waiqin365_delay.";

//	/**
//	 * 延迟发送消息到默认的exchange--default.amq.delay_topic
//	 * 
//	 * @param topic
//	 *            消息的主题
//	 * @param object
//	 *            消息对象，框架会自动转化为JSON
//	 * @param attributes
//	 *            消息属性
//	 * @param delayInMinutes
//	 *            延迟多少分钟，必须和consumers配置的保持一致
//	 */
//	public void sendDelay(String topic, Object object, Map<String, Object> attributes, int delayInMinutes) {
//
//		sendDelay(RabbitConstants.DEFAULT_TOPIC_DELAY_EXCHAGE, topic, object, attributes, delayInMinutes);
//	}
//
//	/**
//	 * 发送延迟消息到exchange
//	 * 
//	 * @param exchange
//	 *            exchange名，必须和consumers配置的保持一致
//	 * @param topic
//	 *            消息主题
//	 * @param object
//	 *            消息对象，框架自动转为json
//	 * @param attributes
//	 *            消息属性
//	 * @param delayInMinutes
//	 *            延迟多少分钟，必须和consumers配置的保持一致
//	 */
//	public void sendDelay(String exchange, String topic, Object object, Map<String, Object> attributes,
//			int delayInMinutes) {
//
//		String sent_topic = TOPIC_DELAY_PREFIX + delayInMinutes + "m." + topic;
//
//		this.send(exchange, sent_topic, object, attributes);
//	}

//	/**
//	 * 发送消息到默认的topic exchange--default.amq.topic
//	 * 
//	 * @param topic
//	 *            消息主题
//	 * @param object
//	 *            消息体，java对象
//	 */
//	public void send(String topic, Object object) {
//		this.send(RabbitConstants.DEFAULT_TOPIC_EXCHAGE, topic, object, null);
//	}

	/**
	 * 发送消息
	 * 
	 * @param exchange
	 *            exchang名
	 * @param topic
	 *            消息主题
	 * @param object
	 *            消息体 会转化为JSON，然后发送
	 * @param attributes
	 *            消息属性
	 */
	protected void send(String exchange, String topic, Object data, Map<String, Object> attributes,ExchangeType type) {
		// 消息的属性
		MessageProperties properties = new MessageProperties();
		properties.setContentType("text");
		if (attributes != null) {
			for (Map.Entry<String, Object> entry : attributes.entrySet()) {
				properties.setHeader(entry.getKey(), entry.getValue());
			}
		}
		// 消息体
		byte[] body = JSON.toJSONString(data).getBytes(Charsets.toCharset("UTF-8"));
		Message amqpMsg = new Message(body, properties);

		this.amqpTemplate.send(exchange, topic, amqpMsg);

        //上报事件
		publishEvent(exchange, topic, data, attributes, type.getCode());

	}

	// spring setting, do not call
	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}
	
	
	/**
	 * 上报事件
	 * @param exchange
	 * @param topic
	 * @param data
	 * @param attributes
	 * @param type
	 */
	protected void publishEvent(String exchange,String topic,Object data,Map<String, Object> attributes,String type) {
		
		// 发送事件
		this.publisher.publishEvent(new RabbitmqSentEvent(exchange, topic, data, attributes,type));

		logger.debug("send mq message success. exchange:{}, topic:{}, message:{}, type:{}", exchange, topic, data,type);
	}

}
