package com.framework.core.message.rabbit.factory;

/**
 * 
 * @author zhangjun
 *
 */
public interface MessageConsumer {

	/**
	 * 消费者要处理的消息的Topic。
	 * 
	 * @return 消费者要处理的消息的Topic.
	 */
	String getMessageTopic();

	/**
	 * 处理消息
	 * 
	 * @param message
	 *            消息， 是个字符串。用String.valueOf(message)转化
	 */
	void handleMessage(Object message);
	
	/**
	 * 获取exchange 名字
	 * @return
	 */
	String getExchangName();
	
	
	/**
	 * 获取并发的 consumer 数量
	 * @return
	 */
	int getConcurrentConsumers();
}