package com.framework.core.dal.exception;

import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

public enum DalErrorCode implements ErrorCode {
	//数据库异常
  EX_SYSTEM_DB_ERROR (100001001),    
   //influx db error
  EX_SYS_INFLUX_MAPPING_FAILED (100001002),    


	;

	

    /**
     * 异常码
     */
    private final int code;
    
    
    private DalErrorCode(int code){
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