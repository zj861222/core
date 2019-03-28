package com.framework.core.cache.redis.exception;

import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

/**
 * 
 * @author zhangjun
 *
 */
public enum RedisErrorCode implements ErrorCode {
	
	
  //redis 
  EX_SYS_REDIS_SET_EXPIRE_FAIL (100003001), 
  //Redis set 操作失败
  EX_SYS_REDIS_SET_FAIL (100003002),   
  //Redis get 操作失败
  EX_SYS_REDIS_GET_FAIL (100003003),  
  //Redis delete 操作失败
  EX_SYS_REDIS_DELETE_FAIL (100003004),   
  //Redis 加锁失败
  EX_SYS_REDIS_LOCK_FAIL (100003005), 	
  // redis value序列化失败	
  EX_SYS_REDIS_SERIAL_FAIL (100003006), 

  // redis value反序列化失败	
  EX_SYS_REDIS_DESERIAL_FAIL (100003007),   
  //获取链接失败
  EX_SYS_REDIS_GET_CONNECTION_FAIL (100003008),   
  
  //redis cmd 执行失败
  EX_SYS_REDIS_CMD_EXE_FAIL (100003009),   
  
  //redis message发送失败
  EX_SYS_REDIS_MSG_SEND_FAILED (100003010),   
  
  //redis message接收处理失败
  EX_SYS_REDIS_MSG_RECEIVED_FAILED (100003011),   

  //生成id失败
  EX_SYS_REDIS_GENERATE_ID_FAILED (100003012),   

	;

	

    /**
     * 异常码
     */
    private final int code;
    
    
    private RedisErrorCode(int code){
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