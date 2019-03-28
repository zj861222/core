package com.framework.core.web.session.token;

import org.springframework.beans.factory.InitializingBean;

/**
 * 
 *  token debug 帮助类
 *  
 * @author zhangjun
 *
 */
public class TokenDebugHelper implements InitializingBean {
	
	
	private static TokenDebugHelper instance;
	
	/**
	 * 是否开启debug模式
	 */
	private String tokenDebugEnable;
	
	/**
	 * 
	 */
	private boolean isEnable;
	
	/**
	 * debug模式下强制指定的token超时时间，分钟
	 */
	private long debugTokenExpireMinute;
	
	/**
	 * debug模式下强制指定的refresh token超时时间，分钟
	 */
	private long debugRefreshTokenExpireMinute;

	
	public String getTokenDebugEnable() {
		return tokenDebugEnable;
	}


	public void setTokenDebugEnable(String tokenDebugEnable) {
		this.tokenDebugEnable = tokenDebugEnable;
	}

	public long getDebugTokenExpireMinute() {
		return debugTokenExpireMinute;
	}

	public void setDebugTokenExpireMinute(long debugTokenExpireMinute) {
		this.debugTokenExpireMinute = debugTokenExpireMinute;
	}


	public long getDebugRefreshTokenExpireMinute() {
		return debugRefreshTokenExpireMinute;
	}


	public void setDebugRefreshTokenExpireMinute(long debugRefreshTokenExpireMinute) {
		this.debugRefreshTokenExpireMinute = debugRefreshTokenExpireMinute;
	}


	public boolean isEnable() {
		return isEnable;
	}


	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		
		instance = new TokenDebugHelper();
		instance.tokenDebugEnable = this.tokenDebugEnable;
		instance.debugTokenExpireMinute = this.debugTokenExpireMinute;
		instance.debugRefreshTokenExpireMinute = this.debugRefreshTokenExpireMinute;
		
		instance.isEnable = "true".equals(this.tokenDebugEnable);
		
	}
	
	
	public static boolean isTokenDebugEnable() {
		
		return instance.isEnable;
	}

	/**
	 * token debug 模式下的超时时间
	 * @return
	 */
	public static long getTokenDebugExpireMinute() {
		
		return instance.debugTokenExpireMinute;
		
	}
	
	/**
	 * refresh token debug模式下的超时时间
	 * @return
	 */
	public static long getRefreshTokenDebugExpireMinute() {
		
		return instance.debugRefreshTokenExpireMinute;

	}
	
	
}