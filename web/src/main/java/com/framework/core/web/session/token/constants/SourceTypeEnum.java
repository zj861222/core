package com.framework.core.web.session.token.constants;

import java.util.List;

public enum SourceTypeEnum
{

	//web请求
	SOURCE_TYPE_WEB ("WEB"),
	
	//H5类型
	SOURCE_TYPE_H5 ("H5"),
	
	//客户端，包括android和ios
	SOURCE_TYPE_CLIENT ("CLIENT"),


	;
    /**
     * code
     */
    private String code;
	
	
    private SourceTypeEnum(String code){
        this.code = code;
    }
    
    
    public String getCode() {
        
        return code;
    }

    
    public void setCode(String code) {
    
        this.code = code;
    }
    
    
    

    
}	