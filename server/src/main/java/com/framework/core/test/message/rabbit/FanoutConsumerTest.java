package com.framework.core.test.message.rabbit;


import com.framework.core.message.rabbit.factory.FanoutMessageConsumer;

/**
 * 
 * @author zhangjun
 *
 */
public class FanoutConsumerTest extends FanoutMessageConsumer {

	@Override
	public String getMessageTopic() {
		return "fanout_1";
	}

	@Override
	public void handleMessage(Object message) {

		System.out.println("FanoutConsumerTest handle message:"+message);
	}

	@Override
	public String getExchangName() {
		return "default_fanout";
	}
	
}