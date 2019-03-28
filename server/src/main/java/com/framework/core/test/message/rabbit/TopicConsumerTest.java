package com.framework.core.test.message.rabbit;


import com.framework.core.message.rabbit.factory.TopicMessageConsumer;

public class TopicConsumerTest extends TopicMessageConsumer {

	@Override
	public String getMessageTopic() {
		// TODO Auto-generated method stub
		return "aa.topic";
	}

	@Override
	public void handleMessage(Object message) {

		System.out.println("TopicConsumerTest-----------------handleMessage:message is ["+message+"]");
	}
	
}