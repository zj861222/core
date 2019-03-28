package com.framework.core.message.rabbit.factory.internal.producer;

import java.util.Map;

import com.framework.core.message.rabbit.constants.ExchangeType;

/**
 * 广播消息发送模版
 * 
 * @author zhangjun
 *
 */
public class FanoutProducerTemplate extends BaseProducerTemplate {
	

	/**
	 * 发送广播消息到对应的exchange 
	 * @param exchange  exchange
	 * @param data 消息体
	 */
	public void send(String exchange, Object data) {
		send(exchange, data, null);
	}
	
	/**
	 * 发送广播消息
	 * @param exchange exchange
	 * @param data 消息体
	 * @param attrs 消息的属性
	 */
	public void send(String exchange, Object data,Map<String,Object> attrs) {
		send(exchange, null, exchange, attrs, ExchangeType.EXCHANGE_TYPE_FANOUT);
	}
		

}
