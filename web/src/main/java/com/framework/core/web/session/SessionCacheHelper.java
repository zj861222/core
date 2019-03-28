package com.framework.core.web.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.event.SessionExceptionEvent;
import com.framework.core.cache.redis.proxy.jedis.JedisClient;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.error.exception.BizException;
import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.web.exception.TokenErrorCode;
import com.framework.core.web.session.filter.SessionFilter;
import com.framework.core.web.session.token.TokenEhcacheMgr;
import com.framework.core.web.session.token.TokenLogger;
import com.framework.core.web.session.token.TokenRedisKeyBuilder;
import com.framework.core.web.session.token.constants.SessionAttrbutes;
import com.framework.core.web.session.token.constants.SourceTypeEnum;
import com.framework.core.web.session.token.view.BaseTokenSubject;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * 
 * @author zhangjun
 *
 */
public class SessionCacheHelper {

	private static final Logger logger = LoggerFactory.getLogger(SessionCacheHelper.class);

	private static SessionCacheHelper instance;

	/**
	 * redis helper
	 */
	private RedisHelper redisHelper;

	private JedisClient jedisClient;

	private Cache sessionEhCache;

	private TokenEhcacheMgr tokenEhcacheMgr;

	public RedisHelper getRedisHelper() {
		return redisHelper;
	}

	public void setRedisHelper(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

	public JedisClient getJedisClient() {
		return jedisClient;
	}

	public void setJedisClient(JedisClient jedisClient) {
		this.jedisClient = jedisClient;
	}

	public Cache getSessionEhCache() {
		return sessionEhCache;
	}

	public void setSessionEhCache(Cache sessionEhCache) {
		this.sessionEhCache = sessionEhCache;
	}

	public TokenEhcacheMgr getTokenEhcacheMgr() {
		return tokenEhcacheMgr;
	}

	public void setTokenEhcacheMgr(TokenEhcacheMgr tokenEhcacheMgr) {
		this.tokenEhcacheMgr = tokenEhcacheMgr;
	}

	public void init() {

		instance = this;

		instance.jedisClient = this.jedisClient;

		instance.sessionEhCache = this.tokenEhcacheMgr.getCache();

		instance.redisHelper = this.redisHelper;
	}

	/**
	 * ehcache 删除session信息
	 * 
	 * @param key
	 */
	public static void removeEhcacheSession(String sessionId) {

		instance.sessionEhCache.remove(sessionId);

	}

	/**
	 * ehcache 删除session信息
	 * 
	 * @param key
	 */
	public static Element getEhcacheSession(String sessionId) {
		return instance.sessionEhCache.get(sessionId);
	}

	/**
	 * ehcache 批量删除session信息
	 * 
	 * @param key
	 */
	public static void batchRemoveEhcacheSessions(Collection<String> sessionIds) {

		instance.sessionEhCache.removeAll(sessionIds);

	}

	/**
	 * 批量删除redis session信息
	 * 
	 * @param keys
	 */
	public static void batchRemoveRedisSessions(Collection<String> sessionIds) {
		instance.jedisClient.delete(sessionIds);
	}

	/**
	 * redis删除session
	 * 
	 * @param key
	 */
	public static void removeRedisSession(String sessionId) {

		instance.jedisClient.delete(sessionId);
//		instance.sessionEhCache.remove(sessionId);

	}

	/**
	 * 删除redis 中session属性
	 * 
	 * @param sessionId
	 * @param attrbute
	 */
	public static void removeRedisSessionAttrbute(String sessionId, Object attrbute) {

		instance.jedisClient.hDelete(sessionId, JSON.toJSONString(attrbute));

	}

	/**
	 * refresh token 列表 加到黑名单，只允许 SessionHolder调用用来清除session，使refresh toekn失效
	 * 
	 * @param tokens
	 */
	public static void setRefreshTokensToBlackList(long uid, long tenantId,
			List<Pair<String, String>> refrehTokenPairs) {

		if (CollectionUtils.isEmpty(refrehTokenPairs)) {
			return;
		}

		for (Pair<String, String> pair : refrehTokenPairs) {

			// refresh token 黑名单列表的key
			String refreshBlackKey = TokenRedisKeyBuilder.fetchRefreshBlackKey(uid, tenantId, pair.getLeft());

			// 计算离 1970.1.1多少天
			long days = DateUtil.getDaysRange(new Date());

			// hash结构，因为refreshToken太长了，不能用他作为key
			hashSetIngoreException(refreshBlackKey, pair.getRight(), String.valueOf(days),
					SessionHolder.getCurrentSessionExpireTime(), TimeUnit.MINUTES);

		}

	}

	/**
	 * 忽略异常,为了让redis挂了的情况下，还能使用session登录
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 * @param timeout
	 * @param unit
	 */
	private static void hashSetIngoreException(String key, String hashKey, String value, long timeout, TimeUnit unit) {
		try {
			instance.redisHelper.hashSet(key, hashKey, value);
			instance.redisHelper.expire(key, timeout, unit);

		} catch (Exception e) {

			StringBuilder sb = new StringBuilder().append("[key]=").append(key).append(",[hashKey]=").append(hashKey)
					.append(",[value]=").append(value);
			logger.error("SessionCacheHelper hashSetIngoreException operation failed [hashSet],param:", sb.toString());
			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[hashSetIngoreException]",
					sb.toString(), e);

		}

	}

	/**
	 * 忽略异常,为了让redis挂了的情况下，还能使用session登录
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 * @param timeout
	 * @param unit
	 */
	private static void valueSetIngoreException(String key, String value, long timeout, TimeUnit unit) {
		try {
			instance.redisHelper.valueSet(key, value, timeout, unit);

		} catch (Exception e) {

			StringBuilder sb = new StringBuilder().append("[key]=").append(key).append(",[value]=").append(value);
			logger.error("SessionCacheHelper valueSetIngoreException operation failed [valueSet],param:",
					sb.toString());
			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[valueSetIngoreException]",
					sb.toString(), e);

		}

	}

	/**
	 * 忽略异常,为了让redis挂了的情况下，还能使用session登录
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 * @param timeout
	 * @param unit
	 */
	private static String valueGetIngoreException(String key) {
		try {
			return instance.redisHelper.valueGet(key);

		} catch (Exception e) {

			logger.error("SessionCacheHelper valueGetIngoreException operation failed [valueGet],key:" + key, e);
			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[valueSetIngoreException]", key, e);

			return null;

		}

	}

	/**
	 * 设置强制重新登录的flag，修改密码时调用，强制把用户其他来源的登录请求踢下线，让其重新登录
	 * 
	 * @param sessionId
	 *            redis里的session id
	 * @param timeoutMinuts
	 *            超时分钟数
	 */
	public static void setSessionForceReloginFlag(String sessionId, long timeoutMinuts) {

		instance.jedisClient.hashSet(sessionId, SessionAttrbutes.SESSION_FORCE_RELOGIN, "1");

		instance.jedisClient.expire(sessionId, timeoutMinuts, TimeUnit.MINUTES);
	}

	/**
	 * 上报事件
	 * 
	 * @param methodInfo
	 * @param args
	 * @param e
	 */
	private static void reportEvent(int type, String methodInfo, String param, Exception e) {

		int errorcode = BaseCode.EX_SYSTEM_UNKNOW.getCode();

		if (e instanceof BizException) {
			errorcode = ((BizException) e).getErrorCode();
		}

		SessionExceptionEvent event = new SessionExceptionEvent(type, SessionFilter.getRequest().getRequestURI(),
				methodInfo, param, errorcode, e);
		EventPublisherUtils.reportEvent(event);
	}

	/**
	 * 刷新redis session超时时间
	 * 
	 * @param uid
	 *            用户id
	 * @param tenantId
	 *            企业id
	 * @param type
	 *            登录类型
	 * @param timeout
	 *            超时时间
	 * @param unit
	 *            超时单位
	 */
	public static void refreshSessionExpireTime(long uid, long tenantId, SourceTypeEnum type, long timeout,
			TimeUnit unit) {

		try {
			instance.jedisClient.expire(TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type), timeout,
					unit);
		} catch (Exception e) {
			logger.error("SessionHolder refreshSessionExpireTime failed,key is {}",
					TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type));
		}

	}

	/**
	 * 强制重新登录的flag，一般在登录之后调用。
	 * 
	 * @param key
	 */
	public static void clearForceReloginFlagIgnoreException(long uid, long tenantId, SourceTypeEnum type) {

		try {
			instance.redisHelper.hashDelete(TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type),
					SessionAttrbutes.SESSION_FORCE_RELOGIN);

		} catch (Exception e) {
			logger.error(
					"SessionCacheHelper clearForceReloginFlagIgnoreException get value from redis failed,key is {}",
					TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type));
		}
	}

	/**
	 * 校验是否强制重新登录
	 * 
	 * @param uid
	 * @param tenantId
	 * @param type
	 */
	public static void isForceReloginIgnoreException(long uid, long tenantId, SourceTypeEnum type) {

		TokenLogger.printDebugInfo("SessionCacheHelper->isForceReloginIgnoreException:[uid]=" + uid + ",[tenantId]="
				+ tenantId + ",[type]=" + type.getCode());
		Object obj = null;

		try {
			String sessionId = TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type);

			// 这样能用到缓存
			obj = SessionHolder.getAttrbute(sessionId, SessionAttrbutes.SESSION_FORCE_RELOGIN);

			TokenLogger.printDebugInfo("SessionCacheHelper->isForceReloginIgnoreException:[uid]=" + uid + ",[tenantId]="
					+ tenantId + ",[type]=" + type.getCode() + ",[result]=" + obj);

		} catch (Exception e) {

			logger.error("SessionCacheHelper->isForceReloginIgnoreException get value from redis failed,key is {}",
					TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type));
		}

		if (obj != null) {
			throw new BizException(TokenErrorCode.EX_LOGIN_RELOGIN_BY_PWD_MODIFIED.getCode());
		}

		// Object obj = null;
		//
		// try {
		// obj =
		// instance.redisHelper.hashGet(TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid,
		// tenantId, type),
		// SessionAttrbutes.SESSION_FORCE_RELOGIN);
		// } catch (Exception e) {
		// logger.error("SessionHolder isForceRelogin get value from redis
		// failed,key is {}",
		// TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type));
		// }
		//
		// if (obj != null) {
		// throw new
		// BizException(TokenErrorCode.EX_LOGIN_RELOGIN_BY_PWD_MODIFIED.getCode());
		// }

	}

	/**
	 * 
	 * @param sessionId
	 * @param hashKey
	 * @param value
	 * @param timeOutMins
	 */
	public static void setRedisSessionAttrbute(String sessionId, Object hashKey, Object value, long timeOutMins) {

		instance.jedisClient.hashSet(sessionId, String.valueOf(hashKey), value);

		instance.jedisClient.expire(sessionId, timeOutMins, TimeUnit.MINUTES);

	}

	/**
	 * 从redis获取值，并且刷新到ehcache。
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public static <T> Object getAndRefreshAttrbuteEhCache(String key, T hashKey, long timeOutMins) {

		Map<String, Object> entrys = instance.jedisClient.entries(key);

		if (entrys == null || entrys.isEmpty()) {
			return null;
		}

		// session放到ehcache
		Element element = new Element(key, entrys);

		instance.sessionEhCache.put(element);

		// 更新session里值的超时时间，因为前面有一层ehcache挡着，所以这个调用频率并不算太高。否则的话，会导致redis的expire方法调用频率很高
		instance.jedisClient.expire(key, timeOutMins, TimeUnit.MINUTES);

		return entrys.get(String.valueOf(hashKey));

	}

	/**
	 * 登录生成token之前，先删掉redis里之前的session残留数据，因为有可能redis存的数据是脏数据，
	 * 导致每次获取session都反序列化失败(因为获取attrbute是一次把session数据都取出来缓存在ehcache)。
	 * 
	 * 但是这里也不能调用session的invaidate方法，因为调用这个方法之前，会在session里存放验证码等信息，
	 * invaidate会把本地存的数据给删掉
	 * 
	 * @param iSubject
	 */
	public static void invalidateRedisSessionBeforeLogin(BaseTokenSubject iSubject) {

		try {
			String sessionId = TokenRedisKeyBuilder.fetchSessionIdCacheKey(iSubject.getUserId(), iSubject.getTenantId(),
					iSubject.getLoginType());

			// remove本地的ehcache 缓存
			removeEhcacheSession(sessionId);

			// remove redis session缓存
			removeRedisSession(sessionId);

		} catch (Exception e) {

			StringBuilder sb = new StringBuilder().append("[uid]=").append(iSubject.getUserId()).append(",[tenantId]=")
					.append(iSubject.getTenantId()).append(",[loginType]=").append(iSubject.getLoginType().getCode());
			logger.error("SessionCacheHelper invalidateRedisSessionBeforeLogin operation failed [hashSet],param:",
					sb.toString());
			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[invalidateRedisSessionBeforeLogin]",
					sb.toString(), e);
		}

	}

	/**
	 * refresh token 放到黑名单和保护期
	 * 
	 * @param subject
	 * @param refreshToken
	 * @param refreshTokenProtectMins
	 */
	public static void setRefreshTokenToBlackList(BaseTokenSubject subject, String refreshToken,
			int refreshTokenProtectMins) {

		// 计算离 1970.1.1多少天
		long days = DateUtil.getDaysRange(new Date());

		String protectKey = TokenRedisKeyBuilder.fetchRefreshTokenProtectKey(subject.getUserId(), subject.getTenantId(),
				subject.getLoginType());

		String blackKey = TokenRedisKeyBuilder.fetchRefreshBlackKey(subject.getUserId(), subject.getTenantId(),
				subject.getLoginType().getCode());

		// 删除过期的hashkey,比如 24*7/24+1= 8
		lazyDeleteExpireCache(blackKey, days, subject.getRefreshTokenExpireHours() / 24 + 1);

		// 设置refresh 保护期的缓存
		// hash结构，因为refreshToken太长了，不能用他作为key
		hashSetIngoreException(protectKey, refreshToken, String.valueOf(days), refreshTokenProtectMins,
				TimeUnit.MINUTES);

		// 设置黑名单缓存，默认7天. 因为周期长，会不断的刷新超时时间为7天，导致永远不过期，需要lazy delete操作，
		// 这就是为什么前面获取离
		// 1970年1.1多少天
		// hash结构，因为refreshToken太长了，不能用他作为key
		hashSetIngoreException(blackKey, refreshToken, String.valueOf(days), subject.getRealRefreshTokenExpireMinute(),
				TimeUnit.MINUTES);

		TokenLogger.printDebugInfo("SessionCacheHelper->setRefreshTokenToBlackList:[uid]=" + subject.getUserId()
				+ ",[tenantId]=" + subject.getTenantId() + ",[logintype]=" + subject.getLoginType().getCode()
				+ ",[refreshToken]=" + refreshToken + ",[protecttime]=" + refreshTokenProtectMins);

	}

	/**
	 * 手动补单，删除过期的key。
	 * 
	 * 设置2级缓存，7天. 因为周期长，会不断的刷新超时时间为7天，导致永远不过期，需要lazy delete操作， 这就是为什么前面获取离
	 * 1970年1.1多少天
	 * 
	 * @param key
	 * @param compareDay
	 * @param expireDays
	 *            超时天数
	 */
	private static void lazyDeleteExpireCache(String key, long compareDay, long expireDays) {

		Map<String, String> map = hashGetByKeyIngoreException(key);

		if (map == null || map.isEmpty()) {
			return;
		}

		Set<Entry<String, String>> sets = map.entrySet();

		List<String> deleteHashKeyList = new ArrayList<String>();

		for (Entry<String, String> entry : sets) {

			long day = Long.parseLong(entry.getValue());

			// 超过7天的删除
			if (compareDay - day > expireDays) {
				deleteHashKeyList.add(entry.getKey());
			}
		}

		// 批量删除过期的 hash key
		if (!CollectionUtils.isEmpty(deleteHashKeyList)) {
			String[] array = new String[deleteHashKeyList.size()];
			hashDeleteIngoreException(key, deleteHashKeyList.toArray(array));
		}

	}

	/**
	 * 
	 * @param key
	 * @param hashKeys
	 */
	private static void hashDeleteIngoreException(String key, String[] hashKeys) {

		try {
			instance.redisHelper.hashDelete(key, hashKeys);

		} catch (Exception e) {

			logger.error("SessionHolder hashDeleteIngoreException get value from redis failed,key :" + key
					+ ",hashkeys :" + hashKeys);

		}

	}

	/**
	 * 忽略异常,为了让redis挂了的情况下，还能使用session登录
	 * 
	 * @param key
	 * @return
	 */
	private static Map<String, String> hashGetByKeyIngoreException(String key) {

		try {
			return instance.redisHelper.hashGetByKey(key);
		} catch (Exception e) {

			logger.error("SessionCacheHelper hashGetByKeyIngoreException operation failed [hashGetByKey],key is {}",
					key);

			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[hashGetByKeyIngoreException]",
					"[key]=" + key, e);

			return null;
		}

	}

	/**
	 * 忽略异常,为了让redis挂了的情况下，还能使用session登录
	 * 
	 * @param key
	 * @param hashKey
	 */
	private static String hashGetIngoreException(String key, String hashKey) {
		try {
			return instance.redisHelper.hashGet(key, hashKey);
		} catch (Exception e) {

			StringBuilder sb = new StringBuilder().append("[key]=").append(key).append(",[hashKey]=").append(hashKey);

			logger.error("SessionCacheHelper hashGetIngoreException operation failed [hashGet],param:", sb.toString());

			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[hashGetIngoreException]",
					sb.toString(), e);

			return null;
		}
	}
	//
	// /**
	// * 忽略异常,为了让redis挂了的情况下，还能使用session登录
	// *
	// * @param key
	// * @param hashKey
	// * @param value
	// * @param timeout
	// * @param unit
	// */
	// private static void valueSetIngoreException(String key, String value,
	// long timeout, TimeUnit unit) {
	// try {
	// instance.redisHelper.valueSet(key, value, timeout, unit);
	//
	// } catch (Exception e) {
	//
	// StringBuilder sb = new
	// StringBuilder().append("[key]=").append(key).append(",[value]=").append(value);
	// logger.error("SessionCacheHelper valueSetIngoreException operation failed
	// [set],param:", sb.toString());
	// reportEvent(SessionExceptionEvent.TYPE_REDIS_EX,
	// "SessionCacheHelper=>[valueSetIngoreException]",
	// sb.toString(), e);
	//
	// }
	//
	// }

	/**
	 * 看token是否在刷新保护期
	 * 
	 * @param subject
	 * @param token
	 * @return
	 */
	public static boolean isTokenInRefershProtectTime(BaseTokenSubject subject, String token) {

		// String sessionId =
		// TokenRedisKeyBuilder.fetchSessionIdCacheKey(subject.getUserId(),
		// subject.getTenantId(),
		// subject.getLoginType());

		String protectKey = TokenRedisKeyBuilder.getTokenRefreshProtectCacheKey(subject);

		try {

			String protectedToken = valueGetIngoreException(protectKey);

			TokenLogger.printDebugInfo("SessionCacheHelper->isTokenInRefershProtectTime [protectKey]=" + protectKey
					+ ",[protectedToken]=" + protectedToken + ",[token]=" + token);

			// 如果和redis中的值一样，说明这个token在刷新保护期
			if (token.equals(protectedToken)) {
				return true;
			}

		} catch (Exception e) {

			StringBuilder sb = new StringBuilder().append("[protectKey]=").append(protectKey);
			logger.error("SessionCacheHelper->isTokenInRefershProtectTime operation failed [set],param:",
					sb.toString());
			reportEvent(SessionExceptionEvent.TYPE_REDIS_EX, "SessionCacheHelper=>[isTokenInRefershProtectTime]",
					sb.toString(), e);
		}

		return false;

	}

	/**
	 * 判断token是否在黑名单缓存
	 * 
	 * @param subject
	 * @param token
	 * @return
	 */
	public static boolean isTokenInBlackCache(BaseTokenSubject subject, String token) {

		String cacheKey = TokenRedisKeyBuilder.getTokenBlackCacheKey(subject);

		String obj = hashGetIngoreException(cacheKey, token);

		TokenLogger.printDebugInfo("SessionCacheHelper->isTokenInBlackCache:[sessionId]=" + cacheKey + ",[obj]=" + obj);

		return obj == null ? false : true;
	}

	/**
	 * 判断refresh token是否在黑名单中，因为调用频率不高，可以从缓存中查询是否在黑名单中.
	 * 
	 * @param subject
	 * @param refreshToken
	 */
	public static void isRefreshTokenInBlackCache(BaseTokenSubject subject, String refreshToken) {

		// 判断是否在1级缓存,如果在1级缓存中，表示不在黑名单里
		String protectedValue = hashGetIngoreException(TokenRedisKeyBuilder.fetchRefreshTokenProtectKey(
				subject.getUserId(), subject.getTenantId(), subject.getLoginType()), refreshToken);

		TokenLogger.printDebugInfo("SessionCacheHelper->isRefreshTokenInBlackCache:[protectedValue]=" + protectedValue
				+ ",[uid]=" + subject.getUserId() + ",[tenantId]=" + subject.getTenantId() + ",[logintype]="
				+ subject.getLoginType().getCode() + ",[refreshToken]=" + refreshToken);
		if (protectedValue != null) {
			return;
		}

		// 如果在刷新黑名单缓存中，并且不在refresh token刷新保护期缓存中，那么认为是在黑名单中
		String blackCache = hashGetIngoreException(TokenRedisKeyBuilder.fetchRefreshBlackKey(subject.getUserId(),
				subject.getTenantId(), subject.getLoginType().getCode()), refreshToken);

		TokenLogger.printDebugInfo("SessionCacheHelper->isRefreshTokenInBlackCache:[blackCache]=" + blackCache
				+ ",[uid]=" + subject.getUserId() + ",[tenantId]=" + subject.getTenantId() + ",[logintype]="
				+ subject.getLoginType().getCode() + ",[refreshToken]=" + refreshToken);

		if (blackCache != null) {
			throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_EXPIRE.getCode());
		}

	}

	/**
	 * 把token加到黑名单cache中,同时设置token的保护期
	 * 
	 * @param subject
	 *            主题
	 * @param token
	 *            token
	 */
	public static void setTokenToBlackCache(BaseTokenSubject subject, String token, int tokenProtectMinutes) {

		// 计算离 1970.1.1多少天
		long days = DateUtil.getDaysRange(new Date());

		String blackCacheKey = TokenRedisKeyBuilder.getTokenBlackCacheKey(subject);

		// 删除过期的hashkey,比如 24/24+1 = 2
		lazyDeleteExpireCache(blackCacheKey, days, subject.getTokenExpireHours() / 24 + 1);

		// 设置缓存失效，默认1天
		// hash结构，因为refreshToken太长了，不能用他作为key.
		// 把token加到黑名单中
		hashSetIngoreException(blackCacheKey, token, String.valueOf(days), subject.getRealRefreshTokenExpireMinute(),
				TimeUnit.MINUTES);

		String protectKey = TokenRedisKeyBuilder.getTokenRefreshProtectCacheKey(subject);

		// 设置token保护期的值
		valueSetIngoreException(protectKey, token, tokenProtectMinutes, TimeUnit.MINUTES);

		TokenLogger.printDebugInfo("SessionCacheHelper->setTokenToBlackCache:[uid]=" + subject.getUserId()
				+ ",[tenantId]=" + subject.getTenantId() + ",[logintype]=" + subject.getLoginType().getCode()
				+ ",[token]=" + token + ",[tokenProtectMinutes]=" + tokenProtectMinutes);

	}

	/**
	 * 判断session是否存在，如果session不在，需要重新登录;这个主要用在 1. 修改密码，清空session就可以让用户强制重新登录 2.
	 * 修改了企业的token超时时间，也可以通过这个方法让用户重新登录，更新超时配置 3. 在web 2处登录，第二个登录的会把第一个t下线
	 * 
	 * @param subject
	 */
	public static void checkSessionExist(String token) {

		Object obj = SessionFilter.getRequest().getSession().getAttribute(SessionAttrbutes.SESSION_REFER_TOKEN);

		TokenLogger.printDebugInfo("SessionCacheHelper->checkSessionExist,[redisToken] = " + obj + "[token]=:" + token);

		if (obj == null) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN_4_SESSION_EXPIRE.getCode());
		}

		// 账号在其他地方登录，导致redis中查到的当前状态的token和传入的token不一致
		if (!String.valueOf(obj).equals(token)) {
			logger.info("TokenManager checkSessionExist failed,[redisToken] = {}, [token] is:{}", obj, token);
			throw new BizException(TokenErrorCode.EX_LOGIN_RELOGIN_BY_LOGIN_IN_OTHER_PLACE.getCode());

		}

	}

}