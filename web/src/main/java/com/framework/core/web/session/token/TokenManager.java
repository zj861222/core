package com.framework.core.web.session.token;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.framework.core.cache.redis.utils.RedisHelper;

import com.framework.core.common.utils.DateUtil;
import com.framework.core.common.utils.JwtBuilderUtils;
import com.framework.core.common.view.JwtInfo;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.exception.TokenErrorCode;
import com.framework.core.web.session.SessionHolder;
import com.framework.core.web.session.filter.SessionFilter;
import com.framework.core.web.session.token.constants.TokenConstants;
import com.framework.core.web.session.token.constants.TokenTypeEnum;
import com.framework.core.web.session.token.view.BaseTokenSubject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

/**
 * 
 * @author zhangjun
 *
 */
public class TokenManager {

	private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

	private static final String ROOT_DOMAIN = ".waiqin365.com";
	
	private static final String TOKEN_HEADER_STRING = "Authorization";
//
//	private static final String REFRESH_TOKEN_HEADER_STRING = "x_refresh_token";

	/**
	 * 关联的token
	 */
	private static final String SESSION_REFER_TOKEN = "refer_token";

	private static TokenManager instance;

	private JwtBuilderUtils jwtBuilderUtils;

	private RedisHelper redisHelper;

	private boolean enableToken;

	private String enableStr;

	public String getEnableStr() {
		return enableStr;
	}

	public void setEnableStr(String enableStr) {
		this.enableStr = enableStr;
	}

	public boolean isEnableToken() {
		return enableToken;
	}

	public void setEnableToken(boolean enableToken) {
		this.enableToken = enableToken;
	}

	public void init() {

		instance = this;

		instance.jwtBuilderUtils = this.jwtBuilderUtils;
		instance.redisHelper = this.redisHelper;

		instance.enableToken = "true".equals(this.enableStr) ? true : false;

	}

	public JwtBuilderUtils getJwtBuilderUtils() {
		return jwtBuilderUtils;
	}

	public void setJwtBuilderUtils(JwtBuilderUtils jwtBuilderUtils) {
		this.jwtBuilderUtils = jwtBuilderUtils;
	}

	public RedisHelper getRedisHelper() {
		return redisHelper;
	}

	public void setRedisHelper(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

	/**
	 * 生成token，并且设置token到 cookie和header
	 * 
	 * @param iSubject
	 * @throws BizException
	 */
	public static Pair<JwtInfo, JwtInfo> generateAndSetToken(BaseTokenSubject iSubject) throws BizException {

		Pair<JwtInfo, JwtInfo> pair = generateToken(iSubject);

		setToken(Pair.of(pair.getLeft().getJwtStr(), pair.getRight().getJwtStr()), iSubject);

		return pair;

	}

	/**
	 * 根据登录之后生成的信息，生成token
	 * 
	 * @param iSubject
	 *            主题信息
	 * @return
	 * @throws BizException
	 */
	public static Pair<JwtInfo, JwtInfo> generateToken(BaseTokenSubject iSubject) throws BizException {

		checkExpireTimeIsIllegal(iSubject.getTokenExpireHours(), iSubject.getRefreshTokenExpireHours());
		try {

			String subject = JSON.toJSONString(iSubject);

			StringBuilder sb = new StringBuilder().append(iSubject.getUserId()).append("_")
					.append(iSubject.getTenantId()).append("_").append(iSubject.getLoginType().getCode());

			JwtInfo token = instance.jwtBuilderUtils.createJWT("jwt", sb.toString(), subject,
					DateUtil.getMillisecondByHours(iSubject.getTokenExpireHours()));

			JwtInfo refreshToken = instance.jwtBuilderUtils.createJWT("jwt", sb.toString(), subject,
					DateUtil.getMillisecondByHours(iSubject.getRefreshTokenExpireHours()));

			return Pair.of(token, refreshToken);

		} catch (Exception e) {
			logger.error("generate token failed,message is :" + e.getMessage(), e);
			throw new BizException(TokenErrorCode.EX_LOGIN_GENERATE_TOKEN_FAILED.getCode());
		}

	}

	/**
	 * 检查超时参数设置是否合法
	 * 
	 * @param tokenTimeout
	 * @param refreshTokenTimeout
	 */
	private static void checkExpireTimeIsIllegal(long tokenTimeout, long refreshTokenTimeout) throws BizException {

		// token超时小于6小时，或者 token超时大于等于refreshtoken，认为不合法
		if (tokenTimeout < 6 || tokenTimeout >= refreshTokenTimeout) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN_EXPIRE_TIME.getCode());
		}

	}

	// /**
	// * 判断token是否过期
	// *
	// * @param token
	// * @return
	// * @throws BizException
	// */
	// private static boolean isTokenExpire(String token) throws BizException {
	//
	// try {
	// return getjwtBuilderUtils().checkIsExpire(token);
	// } catch (Exception e) {
	//
	// throw new BizException(BizCodeEnum.EX_LOGIN_PARSE_TOKEN_FAILED);
	// }
	//
	// }

	/**
	 * 解析token，获取claims对象
	 * 
	 * @param token
	 * @param type
	 *            0-token， 1-refreshtoken
	 * @return
	 * @throws BizException
	 */
	private static Claims parseToken(String token, TokenTypeEnum type) throws BizException {

		try {
			return instance.jwtBuilderUtils.parseJWT(token);

		} catch (ExpiredJwtException e) {

			if (type.getCode() == 0) {
				throw new BizException(TokenErrorCode.EX_LOGIN_TOKEN_EXPIRE.getCode());
			} else {
				throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_FAILED.getCode());
			}

		} catch (Exception e) {

			logger.error("parseToken failed,message is :" + e.getMessage(), e);

			throw new BizException(TokenErrorCode.EX_LOGIN_PARSE_TOKEN_FAILED.getCode());
		}

	}

	/**
	 * 获取token中的subject对象信息
	 * 
	 * @param token
	 * @param targetCalss
	 * @return
	 * @throws BizException
	 */
	public static BaseTokenSubject getTokenSubject(String token) throws BizException {

		try {

			String subject = parseToken(token, TokenTypeEnum.TYPE_TOKEN).getSubject();

			return JSON.parseObject(subject, BaseTokenSubject.class);

		} catch (Exception e) {
			logger.error("getTokenSubject failed,message is :" + e.getMessage(), e);

			throw new BizException(TokenErrorCode.EX_LOGIN_PARSE_TOKEN_FAILED.getCode());
		}

	}

	/**
	 * 设置token到header和cookie中，并且，在session中写入一个参数
	 * 
	 * @param pair
	 * @param uid
	 * @param tenantId
	 * @param type
	 */
	public static void setToken(Pair<String, String> pair, BaseTokenSubject subject) {

		if (pair == null) {
			return;
		}

//		setToken2Header(pair, SessionFilter.getResponse());

		setToken2Cookie(pair, SessionFilter.getResponse(), subject.getRefreshTokenExpireHours());

		// token设置到cookie或者header之后，在当前threadlocal里确认当前登录用户的session
		// id，fetchSessionCacheKey
		SessionHolder.determinCurrentSessionId(subject.getUserId(), subject.getTenantId(), subject.getLoginType(),
				subject.getRefreshTokenExpireHours());

		// 设置一个登录标记状态，如果session被清掉了，那么查不到这个状态
		SessionFilter.getRequest().getSession().setAttribute(SESSION_REFER_TOKEN, pair.getLeft());
	}

	/**
	 * 设置 token和refresh token到cookie
	 * 
	 * @param pair
	 */
	private static void setToken2Cookie(Pair<String, String> pair, HttpServletResponse response,
			long refreshTokenExpireHours) {

		if (response == null || pair == null) {
			return;
		}

		int expire = Integer.parseInt(String.valueOf(DateUtil.getSecondByHours(refreshTokenExpireHours)));
		// 设置token
		Cookie tokenCookie = new Cookie(TokenConstants.COOKIE_AUTH_TOKENE, pair.getLeft());
		tokenCookie.setMaxAge(expire);
		tokenCookie.setPath("/"); // 设置根目录
		tokenCookie.setDomain(ROOT_DOMAIN);
		// tokenCookie.setHttpOnly(false); // http only提升安全性
		// tokenCookie.setDomain(domain); //使用顶级域名，前面加点，比如 .waiqin365.com

//		// 设置 refresh_token
//		Cookie refreshTokenCookie = new Cookie(TokenConstants.AUTH_REFRESH_TOKEN, pair.getRight());
//		refreshTokenCookie.setMaxAge(expire);
//		refreshTokenCookie.setPath("/");
		// refreshTokenCookie.setSecure(true);
		// refreshTokenCookie.setHttpOnly(true);

		response.addCookie(tokenCookie);
//		response.addCookie(refreshTokenCookie);

	}

//	/**
//	 * token和refresh token设置到 header
//	 * 
//	 * @param pair
//	 */
//	private static void setToken2Header(Pair<String, String> pair, HttpServletResponse response) {
//
//		if (response == null || pair == null) {
//			return;
//		}
//
//		response.addHeader(TOKEN_HEADER_STRING, pair.getLeft());
//		response.addHeader(REFRESH_TOKEN_HEADER_STRING, pair.getRight());
//	}

	/**
	 * 刷新token接口.
	 * 
	 * 会做几步校验
	 * 
	 * 1. token和refresh token subject是否相同 ，uid是否相同 2. refresh token 是否过期 3.
	 * refresh token是否在黑名单中。
	 * 
	 * 
	 * 生成新的token之后
	 * 
	 * 1. 将旧的token放到缓存 2. 将旧的refresh放到1级和2级缓存
	 * 
	 * 
	 * @param oldToken
	 * @param oldRefreshToken
	 * @return
	 * @throws BizException
	 */
	public static Pair<JwtInfo, JwtInfo> refreshTokenPair(String oldToken, String oldRefreshToken) throws BizException {

		if (StringUtils.isEmpty(oldToken) || StringUtils.isEmpty(oldRefreshToken)) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_REFRESH_TOKEN.getCode());
		}

		Claims oldRefreshTokenClaims = parseToken(oldRefreshToken, TokenTypeEnum.TYPE_REFRESH_TOKEN);

		// 判断是否为空
		if (oldRefreshTokenClaims == null || StringUtils.isEmpty(oldRefreshTokenClaims.getSubject())) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_REFRESH_TOKEN.getCode());
		}

		BaseTokenSubject subject = JSON.parseObject(oldRefreshTokenClaims.getSubject(), BaseTokenSubject.class);

		long oldRefreshTokenUid = subject.getUserId();

		// 检查是否在黑名单列表
		isRefreshTokenInBlackCache(subject, oldRefreshToken);

		try {

			JwtInfo tokenInfo = instance.jwtBuilderUtils.createJWT("jwt", String.valueOf(oldRefreshTokenUid),
					oldRefreshTokenClaims.getSubject(), DateUtil.getMillisecondByHours(subject.getTokenExpireHours()));

			JwtInfo refreshTokenInfo = instance.jwtBuilderUtils.createJWT("jwt", String.valueOf(oldRefreshTokenUid),
					oldRefreshTokenClaims.getSubject(),
					DateUtil.getMillisecondByHours(subject.getRefreshTokenExpireHours()));

			// 把旧的token和refresh token加到黑名单
			setRefreshTokenToBlackList(subject, oldRefreshToken);

			setTokenToBlackCache(subject, oldToken);

			// 将缓存中的session信息 时间刷新,和refresh token一样，缓存超时为 7天
			SessionHolder.refreshSessionExpireTime(subject.getUserId(), subject.getTenantId(), subject.getLoginType(),
					subject.getRefreshTokenExpireHours(), TimeUnit.HOURS);

			// 设置到token
			setToken(Pair.of(tokenInfo.getJwtStr(), refreshTokenInfo.getJwtStr()), subject);

			return Pair.of(tokenInfo, refreshTokenInfo);

		} catch (BizException e) {
			throw e;
		} catch (Exception e) {

			logger.error("refresh token failed,message is :" + e.getMessage(), e);

			throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_FAILED.getCode());

		}

	}

	/**
	 * refresh token 放到黑名单
	 * 
	 * @param subject
	 * @param refreshToken
	 * @throws BizException
	 */
	private static void setRefreshTokenToBlackList(BaseTokenSubject subject, String refreshToken) throws BizException {
		// 计算离 1970.1.1多少天
		long days = DateUtil.getDaysRange(new Date());

		String lv1Key = getRefreshTokenCacheKey(subject, 1);

		String lv2Key = getRefreshTokenCacheKey(subject, 2);

		// 删除过期的hashkey,比如 24*7/24+1= 8
		lazyDeleteExpireCache(lv2Key, days, subject.getRefreshTokenExpireHours() / 24 + 1);

		// 设置1级缓存，5分钟
		instance.redisHelper.hashSet(lv1Key, refreshToken, String.valueOf(days)); // hash结构，因为refreshToken太长了，不能用他作为key
		instance.redisHelper.expire(lv1Key, 5, TimeUnit.MINUTES); // 缓存5分钟

		// 设置2级缓存，7天. 因为周期长，会不断的刷新超时时间为7天，导致永远不过期，需要lazy delete操作， 这就是为什么前面获取离
		// 1970年1.1多少天

		instance.redisHelper.hashSet(lv2Key, refreshToken, String.valueOf(days)); // hash结构，因为refreshToken太长了，不能用他作为key
		instance.redisHelper.expire(lv2Key, subject.getRefreshTokenExpireHours(), TimeUnit.HOURS); // 缓存7天

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
	 * @throws BizException
	 */
	private static void lazyDeleteExpireCache(String key, long compareDay, long expireDays) throws BizException {

		Map<String, String> map = instance.redisHelper.hashGetByKey(key);

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
			instance.redisHelper.hashDelete(key, deleteHashKeyList.toArray(array));
		}

	}

	/**
	 * 判断refresh token是否在黑名单中，因为调用频率不高，可以从缓存中查询是否在黑名单中.
	 * 
	 * @param subject
	 * @param refreshToken
	 * @throws BizException
	 */
	private static void isRefreshTokenInBlackCache(BaseTokenSubject subject, String refreshToken) throws BizException {

		// 判断是否在1级缓存,如果在1级缓存中，表示不在黑名单里
		String lv1Value = instance.redisHelper.hashGet(getRefreshTokenCacheKey(subject, 1), refreshToken);

		if (lv1Value != null) {
			return;
		}

		// 如果在lv2 缓存中，并且不在lv1 缓存中，那么认为是在黑名单中
		String lv2Value = instance.redisHelper.hashGet(getRefreshTokenCacheKey(subject, 2), refreshToken);

		if (lv2Value != null) {
			throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_EXPIRE.getCode());
		}

	}

	/**
	 * 生成黑名单缓存的key
	 * 
	 * @param subject
	 * @param lv
	 *            缓存级别，目前分为 1级和2级， 默认1级缓存 5分钟，2级缓存7天
	 * @return
	 */
	private static String getRefreshTokenCacheKey(BaseTokenSubject subject, int lv) {

		return subject.getUserId() + "_" + subject.getTenantId() + "_" + subject.getLoginType().getCode() + "_lv" + lv
				+ "_RTB";
	}

	/**
	 * 获取token的缓存key
	 * 
	 * @param subject
	 * @return
	 */
	private static String getTokenCacheKey(BaseTokenSubject subject) {

		return subject.getUserId() + "_" + subject.getTenantId() + "_" + subject.getLoginType().getCode() + "_TB";
	}

	/**
	 * 把token加到黑名单cache中
	 * 
	 * @param subject
	 *            主题
	 * @param token
	 *            token
	 * @throws BizException
	 */
	public static void setTokenToBlackCache(BaseTokenSubject subject, String token) throws BizException {

		// 计算离 1970.1.1多少天
		long days = DateUtil.getDaysRange(new Date());

		String cacheKey = getTokenCacheKey(subject);

		// 删除过期的hashkey,比如 24/24+1 = 2
		lazyDeleteExpireCache(cacheKey, days, subject.getTokenExpireHours() / 24 + 1);

		// 设置缓存失效，默认1天
		instance.redisHelper.hashSet(cacheKey, token, String.valueOf(days)); // hash结构，因为refreshToken太长了，不能用他作为key
		instance.redisHelper.expire(cacheKey, subject.getTokenExpireHours(), TimeUnit.HOURS); // 默认缓存1天

	}

	/**
	 * 判断token是否在黑名单缓存
	 * 
	 * @param subject
	 * @param token
	 * @return
	 * @throws BizException
	 */
	private static boolean isTokenInBlackCache(BaseTokenSubject subject, String token) throws BizException {
		String cacheKey = getTokenCacheKey(subject);

		String obj = instance.redisHelper.hashGet(cacheKey, token);

		return obj == null ? false : true;
	}

	/**
	 * 判断token是否有效
	 * 
	 * 1. 判断token是否失效 2. 判断token是否在黑名单中，这个考虑是否可以不校验，每次请求都调用，缓存的调用频率会比较高
	 * 
	 * 
	 * @param token
	 * @return
	 * @throws BizException
	 */
	public static void isValidToken(String token) throws BizException {

		Claims claim = parseToken(token, TokenTypeEnum.TYPE_TOKEN);

		if (claim == null || StringUtils.isEmpty(claim.getSubject())) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN.getCode());
		}

		BaseTokenSubject subject = JSON.parseObject(claim.getSubject(), BaseTokenSubject.class);

		long uid = subject.getUserId();

		// // 判断refresh token是否过期
		// Date now = new Date();
		// if (claim.getExpiration().before(now)) {
		// throw new BizException(BizCodeEnum.EX_LOGIN_IEAGLLE_TOKEN);
		// }

		// step2， 判断是否在黑名单中
		if (isTokenInBlackCache(subject, token)) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN.getCode());
		}

		// 前面2步校验都通过，将当前登录用户的token session id设置到TokenSessionHolder
		SessionHolder.determinCurrentSessionId(uid, subject.getTenantId(), subject.getLoginType(),
				subject.getRefreshTokenExpireHours());

		// 判断session是否存在，如果session不在，需要重新登录;这个主要用在
		// 1. 修改密码，清空session就可以让用户强制重新登录
		// 2. 修改了企业的token超时时间，也可以通过这个方法让用户重新登录，更新超时配置
		// 3. 在web 2处登录，第二个登录的会把第一个t下线
		checkSessionExist(token);

	}

	/**
	 * 判断session是否存在，如果session不在，需要重新登录;这个主要用在 1. 修改密码，清空session就可以让用户强制重新登录 2.
	 * 修改了企业的token超时时间，也可以通过这个方法让用户重新登录，更新超时配置 3. 在web 2处登录，第二个登录的会把第一个t下线
	 * 
	 * @param subject
	 * @throws BizException
	 */
	private static void checkSessionExist(String token) throws BizException {

		Object obj = SessionFilter.getRequest().getSession().getAttribute(SESSION_REFER_TOKEN);

		// 判断当前session对应的token 是不是这个token
		if (obj == null || !String.valueOf(obj).equals(token)) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN_4_SESSION_EXPIRE.getCode());
		}

	}

	/**
	 * 从request 获取token
	 * 
	 * @return
	 */
	public static String fetchTokenFromRequest(HttpServletRequest httpRequest) {

		// 先从header离找，header找不到到cookie中找，主要是为了兼容老版本的未设置token的问题
		String token = fetchTokenFromRequestHeader(httpRequest);

		if (StringUtils.isEmpty(token)) {
			token = fecthTokenFromRequestCookie(httpRequest);
		}

		return token;
	}

	/**
	 * 从header中获取token, Authorization: Bearer xxxxxxx 这种格式，xxxx为token字符
	 *  
	 * @return
	 */
	private static String fetchTokenFromRequestHeader(HttpServletRequest httpRequest) {

		String bearer = httpRequest.getHeader(TOKEN_HEADER_STRING);
		
		//Bearer+一个空格，刚好7位，去除前面7位后面就是token
		if (StringUtils.isEmpty(bearer) || bearer.length() <= 7) {
			return null;
		}
		
        //截取 Bearer+空格后面的token字符
		bearer = bearer.substring(7);


		return bearer;
	}

	/**
	 * 从request的cookie中获取token
	 * 
	 * @return
	 */
	private static String fecthTokenFromRequestCookie(HttpServletRequest httpRequest) {

		Cookie[] cookies = httpRequest.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (TokenConstants.COOKIE_AUTH_TOKENE.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * 是否开启token验证
	 * 
	 * @return
	 */
	public static boolean isTokenEnable() {

		return instance.enableToken;

	}

}