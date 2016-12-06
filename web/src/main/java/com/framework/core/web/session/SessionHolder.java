package com.framework.core.web.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.framework.core.cache.redis.proxy.jedis.JedisClient;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.session.token.TokenEhcacheMgr;
import com.framework.core.web.session.token.TokenManager;
import com.framework.core.web.session.token.constants.SourceTypeEnum;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * token对应的session 信息
 * 
 * @author zhangjun
 *
 */
public class SessionHolder {

	
	private static final ThreadLocal<String> SESSION_LOCAL = new ThreadLocal<String>();
	
	
	private static final ThreadLocal<Long> REFRESH_TOKEN_EXPIRE_LOCAL = new ThreadLocal<Long>();

	
	private static SessionHolder instance;

	private JedisClient jedisClient;

	private Cache sessionEhCache;

	private TokenEhcacheMgr tokenEhcacheMgr;
	
	

	public TokenEhcacheMgr getTokenEhcacheMgr() {
		return tokenEhcacheMgr;
	}

	public void setTokenEhcacheMgr(TokenEhcacheMgr tokenEhcacheMgr) {
		this.tokenEhcacheMgr = tokenEhcacheMgr;
	}

	public Cache getSessionEhCache() {
		return sessionEhCache;
	}

	public void setSessionEhCache(Cache sessionEhCache) {
		this.sessionEhCache = sessionEhCache;
	}

	public JedisClient getJedisClient() {
		return jedisClient;
	}

	public void setJedisClient(JedisClient jedisClient) {
		this.jedisClient = jedisClient;
	}



	private static String fetchSessionCacheKey(long uid,long tenantId,SourceTypeEnum type) {

		return new StringBuilder().append(uid).append("_").append(tenantId).append("_").append(type.getCode()).append("_session").toString();
	}

	public void init() {

		instance = this;

		instance.jedisClient = this.jedisClient;

		instance.sessionEhCache = this.tokenEhcacheMgr.getCache();

	}


	
	/**
	 * 刷新session超时时间
	 * @param uid 用户id
	 * @param tenantId 企业id
	 * @param type 登录类型
	 * @param timeout 超时时间
	 * @param unit 超时单位
	 * @throws BizException
	 */
	public static void refreshSessionExpireTime(long uid, long tenantId,SourceTypeEnum type,long timeout, TimeUnit unit) throws BizException {

		instance.jedisClient.expire(fetchSessionCacheKey(uid,tenantId,type), timeout, unit);

	}


	
	/**
	 * 删除用户session信息，退出，修改密码等场景时，可以调用
	 * @param uid 用户id
	 * @param tenantId 企业id
	 * @throws BizException
	 */
	public static void clearUserSession(long uid,long tenantId) throws BizException {

		SourceTypeEnum[] list = SourceTypeEnum.values();
		
		
		List<String> keys=  new ArrayList<String>();
		
		for(SourceTypeEnum record:list) {
			keys.add(fetchSessionCacheKey(uid,tenantId,record));
		}
		
		//asdasd
		instance.sessionEhCache.removeAll(keys);

		instance.jedisClient.delete(keys);

	}

	/**
	 * 设置session的属性
	 * 
	 * @param key
	 * @param hashKey
	 * @param t
	 * @throws BizException
	 */
	public static <T, V> void setAttrbute(String key, T hashKey, V value) throws BizException {

		// 清理ehcache
		instance.sessionEhCache.remove(key);

		instance.jedisClient.hashSet(key, String.valueOf(hashKey), value);

		instance.jedisClient.expire(key, getCurrentSessionExpireTime(), TimeUnit.HOURS);

	}

	/**
	 * 获取session属性
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 * @throws BizException
	 */
	public static <T> Object getAttrbute(String key, T hashKey) throws BizException {

		Element element = instance.sessionEhCache.get(key);

		if (element != null) {

			Map<T, Object> sessionMap = (Map<T, Object>) element.getObjectValue();

			if (sessionMap == null | sessionMap.isEmpty()) {
				return null;
			}

			return sessionMap.get(String.valueOf(hashKey));
		}

		Map<String, Object> entrys = instance.jedisClient.entries(key);

		if (entrys == null || entrys.isEmpty()) {
			return null;
		}

		// session放到ehcache
		element = new Element(key, entrys);
		instance.sessionEhCache.put(element);
		
		//更新session里值的超时时间，因为前面有一层ehcache挡着，所以这个调用频率并不算太高。否则的话，会导致redis的expire方法调用频率很高
		instance.jedisClient.expire(key, getCurrentSessionExpireTime(), TimeUnit.HOURS);

		return entrys.get(String.valueOf(hashKey));

	}

	/**
	 * 删除
	 * 
	 * @param key
	 * @param hashKey
	 * @throws BizException
	 */
	public static <T> void removeAttrbute(String key, T hashKey) throws BizException {

		instance.sessionEhCache.remove(key);

		instance.jedisClient.hDelete(key, JSON.toJSONString(hashKey));
	}

	/**
	 * 失效
	 * 
	 * @param key
	 * @throws BizException
	 */
	public static void invalidate(String key) throws BizException {

		instance.sessionEhCache.remove(key);

		instance.jedisClient.delete(key);
	}

	/**
	 * 获取token对应的session id
	 * 
	 * @return
	 */
	public static String fetchTokenSessionId() {

		return TokenManager.isTokenEnable() ? SESSION_LOCAL.get() : null;

	}
	
	
	/**
	 * 获取本次请求对应的的session超时时间
	 * @return
	 */
	public static long getCurrentSessionExpireTime() {
		return REFRESH_TOKEN_EXPIRE_LOCAL.get() == null?24*7:REFRESH_TOKEN_EXPIRE_LOCAL.get();
	}


	
	/**
	 *  决定当前的session id
	 * @param uid 用户id
	 * @param tenantId 企业id
	 * @param type 登录类型
	 */
	public static void determinCurrentSessionId(long uid,long tenantId,SourceTypeEnum type, long refreshTokenExpireHours) {

		SESSION_LOCAL.set(fetchSessionCacheKey(uid,tenantId,type));
		REFRESH_TOKEN_EXPIRE_LOCAL.set(refreshTokenExpireHours);

	}

}