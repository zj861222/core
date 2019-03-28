package com.framework.core.message.rabbit.factory.internal.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
import com.framework.core.message.rabbit.factory.TopicMessageConsumer;

/**
 * Consumer工厂 读取consumer的class，并且创建queue，绑定到exchange中。
 *
 */
public class TopicMessageConsumerFactory implements ApplicationContextAware {

	private final Logger logger = LoggerFactory.getLogger(TopicMessageConsumerFactory.class);

	private static final String TOPIC_QUEUE_PREFIX = "wq_topic:";

	private final ConnectionFactory connectionFactory;

	private final RabbitAdmin admin;

	private final SimpleMessageConverter simpleMessageConverter;

	private final String context;
	
	private final boolean enableRabbit;

	private Map<String, TopicExchange> topicExchangeMap = new HashMap<String, TopicExchange>();

	public TopicMessageConsumerFactory(ConnectionFactory connectionFactory, RabbitAdmin admin,
			SimpleMessageConverter simpleMessageConverter, String context,boolean enableRabbit) {
		this.connectionFactory = connectionFactory;
		this.admin = admin;
		this.simpleMessageConverter = simpleMessageConverter;
		this.context = context;
		this.enableRabbit = enableRabbit;
		// this.topicExchange = new TopicExchange("amq.topic");
	}

	/**
	 * 获取所有实现了 {@link YhMessageConsumer}接口的bean
	 * 
	 * @param applicationContext
	 *            context
	 * @throws BeansException
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		if(!enableRabbit) {
			return;
		}
		
		registerTopicConsumers(applicationContext);
	}

	/**
	 * 注册topic类型的 consumer
	 */
	private void registerTopicConsumers(ApplicationContext applicationContext) {

		Map<String, TopicMessageConsumer> consumerBeans = applicationContext.getBeansOfType(TopicMessageConsumer.class);
		this.addTopicMessageListener(consumerBeans);

	}

	/**
	 * 添加topic 方法
	 * 
	 * @param consumerBeans
	 */
	private void addTopicMessageListener(Map<String, TopicMessageConsumer> consumerBeans) {
		
		if (MapUtils.isEmpty(consumerBeans)) {
			logger.info("can not load any [topic] rabbit consumer...");
			return;
		}

		try {
			for (TopicMessageConsumer consumer : consumerBeans.values()) {

				// exchange 检验注册
				TopicExchange exchange = topicExchangeMap.get(consumer.getExchangName());

				if (exchange == null) {
					exchange = new TopicExchange(consumer.getExchangName());
					topicExchangeMap.put(consumer.getExchangName(), exchange);
//					
//					admin.declareExchange(exchange);
				}

				SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();

				listenerContainer.setConnectionFactory(connectionFactory);
				
//				listenerContainer.setMessageConverter(this.simpleMessageConverter);

				listenerContainer.setConcurrentConsumers(consumer.getConcurrentConsumers());

				// linstener
				MessageListenerAdapter adapter = new MessageListenerAdapter(consumer);
				adapter.setMessageConverter(this.simpleMessageConverter);
				listenerContainer.setMessageListener(adapter);
				listenerContainer.setQueueNames(this.declareAndGetTopicQueueName(consumer));

				listenerContainer.start();

				logger.info("starting rabbitmq container {} success, concurrentConsumers {}.",
						JSON.toJSONString(consumer), consumer.getConcurrentConsumers());
			}
		} catch (Exception e) {
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
	private String declareAndGetTopicQueueName(TopicMessageConsumer consumer) {
		// new queue
		String queueName = TOPIC_QUEUE_PREFIX + this.context + ":" + consumer.getMessageTopic();
		
		Queue queue = new Queue(queueName);
		admin.declareQueue(queue);

		TopicExchange exchange = topicExchangeMap.get(consumer.getExchangName());

		Assert.notNull(exchange);

		// binding
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(consumer.getMessageTopic());
		admin.declareBinding(binding);

		return queueName;
	}

}
