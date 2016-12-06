package com.framework.core.web.session.token.constants;



public enum TokenTypeEnum
{

	//token
	TYPE_TOKEN (0),
	
	//refresh token
	TYPE_REFRESH_TOKEN (1),

	;
    /**
     * 校验结果
     */
    private int code;
	
	
    private TokenTypeEnum(int code){
        this.code = code;
    }
    
    
    public int getCode() {
        
        return code;
    }

    
    public void setCode(int code) {
    
        this.code = code;
    }
    
}	