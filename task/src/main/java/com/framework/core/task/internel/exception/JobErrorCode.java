package com.framework.core.task.internel.exception;

import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

/**
 * 异常码结构
 * 
 * 举例:102001001
 * 前三位：102 表示 jobcenter
 * 中间三位：001 表示系统参数模块
 * 后三位:自定义
 * 
 * 详细的定义，到 classpath*:META-INF/errorcode/service-error-*.yml 下定义
 * 
 * @author zhangjun
 *
 */
public enum JobErrorCode implements ErrorCode
{

    //登录执行job的企业id失败
    EX_JOB_CENTER_FATECH_EXECUTE_JOBTENANT_IDS_FAIL (103001001),   

    ;


    /**
     * 异常码
     */
    private final int code;
    
    
    private JobErrorCode(int code){
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