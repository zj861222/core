package com.framework.core.message.rabbit.factory.internal.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.framework.core.message.rabbit.factory.FanoutMessageConsumer;

/**
 * fanout 模式
 * 
 * @author zhangjun
 *
 */
public class FanoutMessageConsumerFactory implements ApplicationContextAware {

	private final Logger logger = LoggerFactory.getLogger(FanoutMessageConsumerFactory.class);

	private static final String FANOUT_QUEUE_PREFIX = "wq_fanout:";

	private final ConnectionFactory connectionFactory;

	private final RabbitAdmin admin;

	private final SimpleMessageConverter simpleMessageConverter;

	private final String context;
	
	private final boolean enableRabbit ;

	private Map<String, FanoutExchange> fanoutExchangeMap = new HashMap<String, FanoutExchange>();

	public FanoutMessageConsumerFactory(ConnectionFactory connectionFactory, RabbitAdmin admin,
			SimpleMessageConverter simpleMessageConverter, String context,boolean enableRabbit) {
		this.connectionFactory = connectionFactory;
		this.admin = admin;
		this.simpleMessageConverter = simpleMessageConverter;
		this.context = context;
		this.enableRabbit = enableRabbit;
		// this.topicExchange = new TopicExchange("amq.topic");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		
		if(!enableRabbit) {
			return;
		}

		Map<String, FanoutMessageConsumer> consumerBeans = applicationContext
				.getBeansOfType(FanoutMessageConsumer.class);

		if (MapUtils.isEmpty(consumerBeans)) {
			return;
		}

		registerFanoutConsumerListener(consumerBeans);

	}

	/**
	 * 
	 * @param applicationContext
	 */
	private void registerFanoutConsumerListener(Map<String, FanoutMessageConsumer> map) {

		try {
			for (FanoutMessageConsumer consumer : map.values()) {

				// exchange 检验注册
				FanoutExchange exchange = fanoutExchangeMap.get(consumer.getExchangName());

				if (exchange == null) {
					exchange = new FanoutExchange(consumer.getExchangName());
					fanoutExchangeMap.put(consumer.getExchangName(), exchange);
					//
					// admin.declareExchange(exchange);
				}

				SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();

				listenerContainer.setConnectionFactory(connectionFactory);

				// listenerContainer.setMessageConverter(this.simpleMessageConverter);

				listenerContainer.setConcurrentConsumers(consumer.getConcurrentConsumers());

				// linstener
				MessageListenerAdapter adapter = new MessageListenerAdapter(consumer);
				adapter.setMessageConverter(this.simpleMessageConverter);
				listenerContainer.setMessageListener(adapter);
				listenerContainer.setQueueNames(this.declareAndGetFanoutQueueName(consumer));

				listenerContainer.start();

				logger.info("starting rabbitmq container {} success, concurrentConsumers {}.",
						JSON.toJSONString(consumer), consumer.getConcurrentConsumers());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("starting rabbitmq container failed.", e);
			throw e;
		}

	}

	/**
	 * 定义queue，绑定到exchange上
	 * 
	 * @param consumer
	 * @return
	 */
	String declareAndGetFanoutQueueName(FanoutMessageConsumer consumer) {
		// new queue
		String queueName = FANOUT_QUEUE_PREFIX + this.context + ":" + consumer.getMessageTopic();

		Queue queue = new Queue(queueName);
		admin.declareQueue(queue);

		FanoutExchange exchange = fanoutExchangeMap.get(consumer.getExchangName());

		Assert.notNull(exchange);
		
		admin.declareExchange(exchange);

		// binding
		Binding binding = BindingBuilder.bind(queue).to(exchange);
		admin.declareBinding(binding);

		return queueName;
	}

}