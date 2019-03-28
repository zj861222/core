package com.framework.core.test.message.rabbit;


import com.framework.core.message.rabbit.factory.TopicMessageConsumer;

public class TopicConsumerTest4 extends TopicMessageConsumer {

	@Override
	public String getMessageTopic() {
		// TODO Auto-generated method stub
		return "111_topic";
	}

	@Override
	public void handleMessage(Object message) {

		System.out.println("TopicConsumerTest3-----------------handleMessage:message is ["+message+"]");
	}
	
	 @Override
     public String getExchangName(){
    	 
    	 return "default2.test.amq.topic";
     }
	
	
}