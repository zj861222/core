package com.framework.core.kafka.constants;



/**
 * kafka处理失败消息的策略
 * 
 * @author zhangjun
 *
 */
public enum KafkaFailedMessagePolicy {
	
	//重新发送
	POLICY_RESEND_FAILED_MSG,

	//忽略失败消息
	POLICY_IGNORE_FAILED_MSG,
	
	//自定义失败消息处理策略
	POLICY_CUSTOM_FAILED_MSG,

	;
	
	
}