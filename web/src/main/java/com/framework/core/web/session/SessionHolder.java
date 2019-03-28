package com.framework.core.web.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.framework.core.web.session.token.TokenManager;
import com.framework.core.web.session.token.TokenRedisKeyBuilder;
import com.framework.core.web.session.token.constants.SessionAttrbutes;
import com.framework.core.web.session.token.constants.SourceTypeEnum;

import net.sf.ehcache.Element;

/**
 * token对应的session 信息
 * 
 * @author zhangjun
 *
 */
public class SessionHolder {

	private static final Logger logger = LoggerFactory.getLogger(SessionHolder.class);

	private static final ThreadLocal<String> SESSION_LOCAL = new ThreadLocal<String>();

	private static final ThreadLocal<Long> REFRESH_TOKEN_EXPIRE_LOCAL = new ThreadLocal<Long>();

	private static final ThreadLocal<Boolean> forceRefreshEhcache = new ThreadLocal<Boolean>();

	/**
	 * 删除用户session信息，退出，修改密码等场景时，可以调用。 sourceType 为null，那么当前session也清理掉
	 * 
	 * @param uid
	 *            用户id
	 * @param tenantId
	 *            企业id
	 */
	public static void clearUserSession(long uid, long tenantId, SourceTypeEnum sourceType) {

		try {
			SourceTypeEnum[] list = SourceTypeEnum.values();

			List<Pair<String, String>> blackTokens = new ArrayList<Pair<String, String>>();
			List<String> keys = new ArrayList<String>();

			for (SourceTypeEnum record : list) {
				// 自身的session不清楚
				if (sourceType != null && record.getCode().equals(sourceType.getCode())) {
					continue;
				}
				String key = TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, record);
				keys.add(key);

				// 找出对应登录方式的refresh token
				String referRefreshToken = (String) getAttrbute(key, SessionAttrbutes.SESSION_REFER_REFRESH_TOKEN);
				if (!StringUtils.isEmpty(referRefreshToken)) {
					// 登录方式和refresh token组成pair
					blackTokens.add(Pair.of(record.getCode(), referRefreshToken));
				}
			}

			// STEP1 清空ehcache和redis里的session 信息
			SessionCacheHelper.batchRemoveEhcacheSessions(keys);
			SessionCacheHelper.batchRemoveRedisSessions(keys);

			// STEP2:
			// 设置重新登录的flag。因为强制重新登录的提示消息和正常的sesion超时的消息不一样，同样时session都被清理掉了也需要区分出来
			// 因为获取session时优先从本地session获取，所以只删掉redis里的session还可能有问题，需要设置flag禁止登录，用户重新登录再清除flag
			// 因为要设置超时时间，所以不批量设置了
			for (String sessionId : keys) {
				SessionCacheHelper.setSessionForceReloginFlag(sessionId, getCurrentSessionExpireTime());
			}

			// STEP3 refresh token放到黑名单
			// refresh token放到黑名单，防止调用刷新接口
			SessionCacheHelper.setRefreshTokensToBlackList(uid, tenantId, blackTokens);

		} catch (Exception e) {
			logger.error("SessionHolder clearUserSession failed,uid is {},tenantid is{},sourceType is{}", uid, tenantId,
					sourceType);

		}

	}

	/**
	 * 设置session的属性
	 * 
	 * @param key
	 * @param hashKey
	 * @param t
	 */
	public static <T, V> void setAttrbute(String key, T hashKey, V value) {

		// 清理ehcache
		SessionCacheHelper.removeEhcacheSession(key);

		// 设置属性，并设置超时
		SessionCacheHelper.setRedisSessionAttrbute(key, hashKey, value, getCurrentSessionExpireTime());

	}

	/**
	 * 获取session属性
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public static <T> Object getAttrbute(String key, T hashKey) {

		Element element = SessionCacheHelper.getEhcacheSession(key);

		// 保证每个线程请求过来，先强制刷新本地的ehcache，
		// 1.保证本地的ehcache是最新的 2.保证后续的session操作不会频繁的操作redis
		// 为什么保证本地ehcache是最新的 ？因为在客户端登录时，登录时在a机器，登录成功之后到操作到了b机器，cookie粘滞没有作用
		// 因为客户端开始登录页面时没有session参数的。这种情况下，会导致登录时删除的是a机器的refer_token的ehcache，
		// 而登录成功之后到了b机器，会从b机器的ehcache获取refer_token的值，导致这里取到的refer_token是一个旧的值
		// 导致登录校验失败。所以我们在每个线程来的时候强制刷新本地的ehcache
		if (element != null && hasForceRefreshLocalEhcache()) {

			Map<T, Object> sessionMap = (Map<T, Object>) element.getObjectValue();

			if (sessionMap == null | sessionMap.isEmpty()) {
				return null;
			}

			return sessionMap.get(String.valueOf(hashKey));
		}

		return forceGetAndRefreshAttrbuteEhCache(key, hashKey);

	}

	/**
	 * 强制从redis重新取一份刷新本地ehcache
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 */
	private static Object forceGetAndRefreshAttrbuteEhCache(String key, Object hashKey) {

		Object obj = SessionCacheHelper.getAndRefreshAttrbuteEhCache(key, hashKey, getCurrentSessionExpireTime());

		forceRefreshEhcache.set(true);

		return obj;
	}

	/**
	 * 删除
	 * 
	 * @param key
	 * @param hashKey
	 */
	public static <T> void removeAttrbute(String key, T hashKey) {

		SessionCacheHelper.removeEhcacheSession(key);

		SessionCacheHelper.removeRedisSessionAttrbute(key, hashKey);
	}

	/**
	 * session 失效
	 * 
	 * @param key
	 */
	public static void invalidate(String key) {

		SessionCacheHelper.removeEhcacheSession(key);

		SessionCacheHelper.removeRedisSession(key);
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
	 * 
	 * @return
	 */
	public static long getCurrentSessionExpireTime() {
		return REFRESH_TOKEN_EXPIRE_LOCAL.get() == null ? 24 * 7 * 60 : REFRESH_TOKEN_EXPIRE_LOCAL.get();
	}

	/**
	 * 决定当前的session id
	 * 
	 * @param uid
	 *            用户id
	 * @param tenantId
	 *            企业id
	 * @param type
	 *            登录类型
	 */
	public static void determinCurrentSessionId(long uid, long tenantId, SourceTypeEnum type,
			long refreshTokenExpireMinute) {

		SESSION_LOCAL.set(TokenRedisKeyBuilder.fetchSessionIdCacheKey(uid, tenantId, type));
		REFRESH_TOKEN_EXPIRE_LOCAL.set(refreshTokenExpireMinute);

	}

	/**
	 * 
	 */
	public static void clearThreadLocal() {
		SESSION_LOCAL.remove();
		REFRESH_TOKEN_EXPIRE_LOCAL.remove();
		forceRefreshEhcache.remove();
	}

	/**
	 * 本线程是否强制刷新过ehcache，保证每次请求来的时候本地的ehcache是最新的
	 * 
	 * @return
	 */
	private static boolean hasForceRefreshLocalEhcache() {

		Boolean hasRefresh = forceRefreshEhcache.get();

		return hasRefresh == null ? false : hasRefresh;
	}

}