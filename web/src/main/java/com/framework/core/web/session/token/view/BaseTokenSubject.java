package com.framework.core.web.session.token.view;

import java.io.Serializable;

import com.framework.core.web.session.token.TokenDebugHelper;
import com.framework.core.web.session.token.constants.SourceTypeEnum;

/**
 * 
 * @author zhangjun
 *
 */
public  class BaseTokenSubject implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8645566658131207199L;

	/**
	 * 企业id
	 */
	private long tenantId;
	
	/**
	 * 用户id
	 */
	private long userId;
	
	
	/**
	 * 登录类型
	 */
	private SourceTypeEnum loginType;
	
	/**
	 * token 的超时时间，默认24小时，不能少于6小时；必须小于refresh token的超时时间
	 */
	private long tokenExpireHours = 24;
	
	/**
	 * refresh token的超时时间,默认 7天
	 */
	private long refreshTokenExpireHours = 24*7;



	public long getTenantId() {
		return tenantId;
	}



	public void setTenantId(long tenantId) {
		this.tenantId = tenantId;
	}



	public long getUserId() {
		return userId;
	}



	public void setUserId(long userId) {
		this.userId = userId;
	}



	public SourceTypeEnum getLoginType() {
		return loginType;
	}



	public void setLoginType(SourceTypeEnum loginType) {
		this.loginType = loginType;
	}



	public long getTokenExpireHours() {
		return tokenExpireHours;
	}



	public void setTokenExpireHours(long tokenExpireHours) {
		this.tokenExpireHours = tokenExpireHours;
	}



	public long getRefreshTokenExpireHours() {
		return refreshTokenExpireHours;
	}



	public void setRefreshTokenExpireHours(long refreshTokenExpireHours) {
		this.refreshTokenExpireHours = refreshTokenExpireHours;
	}
	

	/**
	 * 实际的使用的token时间，考虑debug模式
	 * @return
	 */
	public long getRealTokenExpireMinute() {

		return TokenDebugHelper.isTokenDebugEnable()?TokenDebugHelper.getTokenDebugExpireMinute():this.tokenExpireHours*60;
		
	}
	
	/**
	 * 实际的使用的refresh token时间，考虑debug模式
	 * @return
	 */
	public long getRealRefreshTokenExpireMinute() {
		
		return TokenDebugHelper.isTokenDebugEnable()?TokenDebugHelper.getRefreshTokenDebugExpireMinute():this.refreshTokenExpireHours*60;

	}
}



