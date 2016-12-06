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
  
  EX_SYS_REDIS_SET_FAIL (100003002),   
  
  EX_SYS_REDIS_GET_FAIL (100003003),  
  
  EX_SYS_REDIS_DELETE_FAIL (100003004),   
  
  EX_SYS_REDIS_LOCK_FAIL (100003005), 	
	


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