package com.framework.core.dal.exception;

import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

public enum DalErrorCode implements ErrorCode {
	//数据库异常
  EX_SYSTEM_DB_ERROR (100001001),    
   //influx db error
  EX_SYS_INFLUX_MAPPING_FAILED (100001002),    

  //非法主库信息
  EX_SYSTEM_DB_FALTE_EX_MASTER_DB_INFO_IDEGGLE (100001003),   
  
  //没有实现dataSourceInfoFetcher
  EX_SYSTEM_DB_FALTE_EX_DB_INFO_FATCHER_NOT_IMPLEMENT (100001004),    
  //摧毁数据源失败
  EX_SYSTEM_DB_FALTE_EX_DESTORY_DATASOURCE_FAILED (100001005),    
  //摧毁连接
  EX_SYSTEM_DB_FALTE_EX_DESTORY_CONN_FAILED (100001006),    




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