package com.framework.core.test.message.rabbit;


import com.framework.core.message.rabbit.factory.TopicMessageConsumer;

public class TopicConsumerTest2 extends TopicMessageConsumer {

	@Override
	public String getMessageTopic() {
		// TODO Auto-generated method stub
		return "bb.topic";
	}

	@Override
	public void handleMessage(Object message) {

		System.out.println("TopicConsumerTest2-----------------handleMessage:message is ["+message+"]");
	}
	
}