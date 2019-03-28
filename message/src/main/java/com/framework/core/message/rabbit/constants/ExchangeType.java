package com.framework.core.message.rabbit.constants;

/**
 * 
 * @author zhangjun
 *
 */
public enum ExchangeType {

	/**
	 * 直接发送
	 */
	EXCHANGE_TYPE_DIRECT("direct"),

	/**
	 * 根据主题路由
	 */
	EXCHANGE_TYPE_TOPIC("topic"),

	/**
	 * 广播
	 */
	EXCHANGE_TYPE_FANOUT("fanout"),
	
	/**
	 * 延迟队列
	 */
	EXCHANGE_TYPE_TOPIC_DELAY("topic_delay"),	


	;

	
	private final String code;
	
	
	public String getCode() {
		return code;
	}


	private ExchangeType(String code) {
		this.code = code;
	}
	
	
}