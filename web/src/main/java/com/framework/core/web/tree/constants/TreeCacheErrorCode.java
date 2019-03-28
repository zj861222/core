package com.framework.core.web.tree.constants;


import com.framework.core.error.exception.code.ErrorCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

/**
 * 异常码结构
 * 
 * 举例:101001001
 * 前三位：101 表示 平台的
 * 中间三位：001 表示平台的模块
 * 后三位:自定义
 * 
 * 详细的定义，到 classpath*:META-INF/errorcode/service-error-*.yml 下定义
 * 
 * @author zhangjun
 *
 */
public enum TreeCacheErrorCode implements ErrorCode
{
	


	  //树形节点模版未注册
	  EX_PLAT_TREE_CACHE_TEMPLATE_NOT_EXIST (101001110),  	
	  
	  //缓存中未找到节点
	  EX_PLAT_TREE_CACHE_NOT_FIND_CODE_IN_CACHE (101001111),  
	  
	  //节点不存在
	  EX_PLAT_TREE_CACHE_CODE_NOT_EXIST (101001112),  	
	  
	  //等待锁超时
	  EX_PLAT_TREE_CACHE_WAIT_LOCK_TIMEOUT (101001113),  	



	  
	;

    /**
     * 异常码
     */
    private final int code;
    
    
    
    
    private TreeCacheErrorCode(int code){
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