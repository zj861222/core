package com.framework.core.test.message.rabbit;


import com.framework.core.message.rabbit.factory.FanoutMessageConsumer;

/**
 * 
 * @author zhangjun
 *
 */
public class FanoutConsumerTest3 extends FanoutMessageConsumer {

	@Override
	public String getMessageTopic() {
		return "fanout_3";
	}

	@Override
	public void handleMessage(Object message) {

		System.out.println("FanoutConsumerTest3 handle message:"+message);
	}

	@Override
	public String getExchangName() {
		return "default_fanout_2";
	}
	
}