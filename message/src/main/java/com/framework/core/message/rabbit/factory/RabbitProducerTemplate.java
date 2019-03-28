package com.framework.core.message.rabbit.factory;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationEventPublisher;



/**
 *  rabbit 生产者 
 * @author zhangjun
 *
 */
public class RabbitProducerTemplate {
	
	
    private AmqpTemplate amqpTemplate;
	
    final String TOPIC_EXCHAGE = "amq.topic";
    
    private ApplicationEventPublisher publisher;	

	
}