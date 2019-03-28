package com.framework.core.web.session.token.constants;


/**
 * session里的属性
 * @author zhangjun
 *
 */
public class SessionAttrbutes {
	
	

	/**
	 * 关联的token
	 */
	public static final String SESSION_REFER_TOKEN = "refer_token";
	


	/**
	 * 关联的refresh token
	 */
	public static final String SESSION_REFER_REFRESH_TOKEN = "refer_refresh_token";
	
	
	/**
	 * 强制重新登录
	 */
	public static final String SESSION_FORCE_RELOGIN = "fore_relogin";
	
	
	
	/**
	 * 保护期的token: 意思就是 refresh token时，旧的token失效了，换成了新的token，但是旧的token还要让他继续能用一会，因为有可能出现
	 * 
	 * 并发的情况，正好在刷新的时候，客户端传旧的token还要让它继续能用一会，这个就是保护期的意思
	 */
	public static final String SESSION_PROTECTED_OLD_TOKEN = "protected_old_token";
	
	
	
}