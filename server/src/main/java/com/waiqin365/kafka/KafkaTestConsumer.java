package com.waiqin365.kafka;


import org.springframework.stereotype.Component;

import com.framework.core.kafka.AbstractKafkaConsumerExecutor;

@Component
public class KafkaTestConsumer extends AbstractKafkaConsumerExecutor {

	
	private String TOPIC = "tp_test_01";

	
	@Override
	public String getTopic()
	{
		return TOPIC;
	}

	@Override
	public boolean isOpenConsumer()
	{
		return true;
	}

	@Override
	public boolean handleMessage(String topic, int partition, String data)
	{
		
		
		System.out.println("KafkaTestConsumer handleMessage,[topic]:"+topic+",[partition]:"+partition+",[data]:"+data);
		
		return true;
	}

	@Override
	public String getGroupId()
	{
		return "waiqin123";
	}
	
}