package com.framework.core.web.session.token;

import com.framework.core.web.session.token.constants.SourceTypeEnum;
import com.framework.core.web.session.token.view.BaseTokenSubject;

/**
 * 
 * @author zhangjun
 *
 */
public class TokenRedisKeyBuilder {
	
	
	
	/**
	 * 生成session id的redis cache key
	 * @param uid
	 * @param tenantId
	 * @param type
	 * @return
	 */
	public static String fetchSessionIdCacheKey(long uid, long tenantId, SourceTypeEnum type) {

		return new StringBuilder().append(uid).append("_").append(tenantId).append("_").append(type.getCode())
				.append("_session").toString();
	}
	
	
	
	/**
	 * 生成refresh token的黑名单列表的key
	 * @param uid
	 * @param tenantId
	 * @param loginType
	 * @return
	 */
	public static String fetchRefreshBlackKey(long uid,long tenantId,String loginType) {

		return uid + "_" + tenantId + "_" + loginType + "_RTB";
	}
	
	
	/**
	 * 获取refresh token保护期的key
	 * @return
	 */
	public static String fetchRefreshTokenProtectKey(long uid, long tenantId, SourceTypeEnum type){
		
		return uid + "_" + tenantId+ "_" + type.getCode() + "_RTP" ;
		
	}

	
	/**
	 * 刷新token时调用，用来存放旧 的token，只存放5分钟。
	 * 
	 * 当用户请求和刷新token请求并发时，可能导致用户正常操作token校验失败；
	 * 
	 * 通过这个key，校验token时，如果在这个缓存里面，那么暂时让用户还能用，不做refer_token的校验
	 * 
	 * @param subject
	 * @return
	 */
	public static String getTokenRefreshProtectCacheKey(BaseTokenSubject subject) {

		return subject.getUserId() + "_" + subject.getTenantId() + "_" + subject.getLoginType().getCode() + "_TP";
	}
	
	
	/**
	 * 获取token的缓存key
	 * 
	 * @param subject
	 * @return
	 */
	public static String getTokenBlackCacheKey(BaseTokenSubject subject) {

		return subject.getUserId() + "_" + subject.getTenantId() + "_" + subject.getLoginType().getCode() + "_TB";
	}
	
	
	/**
	 * 过期token 的保护期缓存，保护几分钟:
	 * 
	 * @param subject
	 * @return
	 */
	public static String getUserExpireTokenProtectedKey(BaseTokenSubject subject) {
		
		return subject.getUserId() + "_" + subject.getTenantId() + "_" + subject.getLoginType().getCode() + "_EXPIRE_TB";

	}
	
	
	


}