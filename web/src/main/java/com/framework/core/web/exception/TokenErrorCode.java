package com.framework.core.web.exception;

import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

/**
 * 返回码枚举类
 * 
 * @author zhangjun
 *
 */
public enum TokenErrorCode implements ErrorCode
{


    //登录生成token失败
    EX_LOGIN_GENERATE_TOKEN_FAILED (100011001),   
    //解析token失败
    EX_LOGIN_PARSE_TOKEN_FAILED (100011002),    
    //非法的refresh token参数
    EX_LOGIN_IEAGLLE_REFRESH_TOKEN (100011003),   
    //refresh token过期
    EX_LOGIN_REFRESH_TOKEN_EXPIRE (100011004),   
    //token过期
    EX_LOGIN_TOKEN_EXPIRE (100011005),   
    //刷新token失败
    EX_LOGIN_REFRESH_TOKEN_FAILED (100011006),    
    //无效的token信息
    EX_LOGIN_IEAGLLE_TOKEN (100011007),
    //token超时时间设置不合法
    EX_LOGIN_IEAGLLE_TOKEN_EXPIRE_TIME (100011008),
    //session已失效
    EX_LOGIN_IEAGLLE_TOKEN_4_SESSION_EXPIRE (100011009),


    
    ;


    /**
     * 异常码
     */
    private final int code;
    
    
    private TokenErrorCode(int code){
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