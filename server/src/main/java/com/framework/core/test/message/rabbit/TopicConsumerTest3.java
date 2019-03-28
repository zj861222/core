package com.framework.core.test.message.rabbit;


import com.framework.core.message.rabbit.factory.TopicMessageConsumer;

public class TopicConsumerTest3 extends TopicMessageConsumer {

	@Override
	public String getMessageTopic() {
		return "abcded";
	}

	@Override
	public void handleMessage(Object message) {

		System.out.println("TopicConsumerTest3-----------------handleMessage:message is ["+message+"]");
	}
	
}