package com.framework.core.kafka.exception;

import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

/**
 * 
 * @author zhangjun
 *
 */
public enum KafkaErrorCode implements ErrorCode {
	
	
  //kafka 消费者启动失败
  EX_KAFKA_CONSUMER_START_FAILED (100014001), 
  
  //kafka send message fail
  EX_KAFKA_SEND_MSG_FAIL (100014002), 


	;

	

    /**
     * 异常码
     */
    private final int code;
    
    
    private KafkaErrorCode(int code){
        this.code = code;
    }
	

	@Override
	public String getMessage() {
		
		return ErrorCodeLoader.getErrorMessageByCode(this.code);
	}

	@Override
	public int getCode() {
		return code;
	}
	
	
	
}	