package com.framework.core.web.session.token;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.event.SessionExceptionEvent;

import com.framework.core.common.utils.DateUtil;
import com.framework.core.common.utils.JwtBuilderUtils;
import com.framework.core.common.view.JwtInfo;
import com.framework.core.error.exception.BizException;
import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.web.exception.TokenErrorCode;
import com.framework.core.web.session.SessionCacheHelper;
import com.framework.core.web.session.SessionHolder;
import com.framework.core.web.session.filter.SessionFilter;
import com.framework.core.web.session.token.constants.SessionAttrbutes;
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

	private static TokenManager instance;

	private JwtBuilderUtils jwtBuilderUtils;

	private boolean enableToken;

	private boolean cookieDomainEnable;

	private String enableStr;

	private String cookieDomainEnableStr;

	// 超时token的保护时间
	private int expireTokenProtectTime;

	// token刷新时的保护时间
	private int tokenRefreshProtectTime;

	// refresh token的保护时间
	private int refreshTokenProtectTime;

	public int getRefreshTokenProtectTime() {
		return refreshTokenProtectTime;
	}

	public void setRefreshTokenProtectTime(int refreshTokenProtectTime) {
		this.refreshTokenProtectTime = refreshTokenProtectTime;
	}

	public int getTokenRefreshProtectTime() {
		return tokenRefreshProtectTime;
	}

	public void setTokenRefreshProtectTime(int tokenRefreshProtectTime) {
		this.tokenRefreshProtectTime = tokenRefreshProtectTime;
	}

	public int getExpireTokenProtectTime() {
		return expireTokenProtectTime;
	}

	public void setExpireTokenProtectTime(int expireTokenProtectTime) {
		this.expireTokenProtectTime = expireTokenProtectTime;
	}

	public String getCookieDomainEnableStr() {
		return cookieDomainEnableStr;
	}

	public void setCookieDomainEnableStr(String cookieDomainEnableStr) {
		this.cookieDomainEnableStr = cookieDomainEnableStr;
	}

	public boolean isCookieDomainEnable() {
		return cookieDomainEnable;
	}

	public void setCookieDomainEnable(boolean cookieDomainEnable) {
		this.cookieDomainEnable = cookieDomainEnable;
	}

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

		instance.tokenRefreshProtectTime = this.tokenRefreshProtectTime;

		instance.expireTokenProtectTime = this.expireTokenProtectTime;

		instance.refreshTokenProtectTime = this.refreshTokenProtectTime;

		instance.jwtBuilderUtils = this.jwtBuilderUtils;

		instance.enableToken = "true".equals(this.enableStr) ? true : false;

		instance.cookieDomainEnable = "true".equals(this.cookieDomainEnableStr) ? true : false;

	}

	public JwtBuilderUtils getJwtBuilderUtils() {
		return jwtBuilderUtils;
	}

	public void setJwtBuilderUtils(JwtBuilderUtils jwtBuilderUtils) {
		this.jwtBuilderUtils = jwtBuilderUtils;
	}

	/**
	 * 生成token，并且设置token到 cookie和header
	 * 
	 * @param iSubject
	 */
	public static Pair<JwtInfo, JwtInfo> generateAndSetToken(BaseTokenSubject iSubject) {

		// 登录清空redis信息
		SessionCacheHelper.invalidateRedisSessionBeforeLogin(iSubject);

		Pair<JwtInfo, JwtInfo> pair = generateToken(iSubject);

		setToken(Pair.of(pair.getLeft().getJwtStr(), pair.getRight().getJwtStr()), iSubject);

		TokenLogger.printDebugInfo("Token Manager:generateAndSetToken: [uid]:" + iSubject.getUserId() + ",[tenantId]:"
				+ iSubject.getTenantId() + ",[token]:" + pair.getLeft() + ",[refresh token]:" + pair.getRight());

		return pair;

	}

	/**
	 * 根据登录之后生成的信息，生成token
	 * 
	 * @param iSubject
	 *            主题信息
	 * @return
	 */
	public static Pair<JwtInfo, JwtInfo> generateToken(BaseTokenSubject iSubject) {

		checkExpireTimeIsIllegal(iSubject.getTokenExpireHours(), iSubject.getRefreshTokenExpireHours());

		try {

			String subject = JSON.toJSONString(iSubject);

			StringBuilder sb = new StringBuilder().append(iSubject.getUserId()).append("_")
					.append(iSubject.getTenantId()).append("_").append(iSubject.getLoginType().getCode());

			JwtInfo token = instance.jwtBuilderUtils.createJWT("jwt", sb.toString(), subject,
					DateUtil.getMillisecondByMinutes(iSubject.getRealTokenExpireMinute()));

			JwtInfo refreshToken = instance.jwtBuilderUtils.createJWT("jwt", sb.toString(), subject,
					DateUtil.getMillisecondByMinutes(iSubject.getRealRefreshTokenExpireMinute()));

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
	private static void checkExpireTimeIsIllegal(long tokenTimeout, long refreshTokenTimeout) {

		// token超时大于等于refreshtoken，认为不合法，
		// tokenTimeout不能设置太短，因为web端没有刷新机制防止用的途中，token突然到期需要重新登录的情况

		if (tokenTimeout >= refreshTokenTimeout) {

			logger.error("Token Manager->checkExpireTimeIsIllegal failed,tokenTimeout is :" + tokenTimeout
					+ ",refreshTokenTimeout=" + refreshTokenTimeout);

			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN_EXPIRE_TIME.getCode());
		}

	}

	/**
	 * 解析token，获取claims对象
	 * 
	 * @param token
	 * @param type
	 *            0-token， 1-refreshtoken
	 * @return
	 */
	private static Claims parseToken(String token, TokenTypeEnum type) {

		try {
			return instance.jwtBuilderUtils.parseJWT(token);

		} catch (ExpiredJwtException e) {

			logger.error("parseToken failed for expire:[token]:" + token + ",[type]:" + type.getCode(), e);

			if (type.getCode() == 0) {
				throw new BizException(TokenErrorCode.EX_LOGIN_TOKEN_EXPIRE.getCode());
			} else {
				throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_FAILED.getCode());
			}

		} catch (Exception e) {

			logger.error("parseToken failed unexpected,[token]:" + token + "[message]:" + e.getMessage(), e);

			throw new BizException(TokenErrorCode.EX_LOGIN_PARSE_TOKEN_FAILED.getCode());
		}

	}

	/**
	 * 解析token，获取claims对象
	 * 
	 * @param token
	 * @param type
	 *            0-token， 1-refreshtoken
	 * @return
	 */
	public static Pair<Boolean, Claims> parseTokenEx(String token, TokenTypeEnum type) {

		try {
			return instance.jwtBuilderUtils.parseJWTEx(token);

		} catch (ExpiredJwtException e) {

			logger.error("parseTokenEx failed for expire:[token]:" + token + ",[type]:" + type.getCode(), e);

			if (type.getCode() == 0) {
				throw new BizException(TokenErrorCode.EX_LOGIN_TOKEN_EXPIRE.getCode());
			} else {
				throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_FAILED.getCode());
			}

		} catch (Exception e) {

			logger.error("parseTokenEx failed unexpected,[token]:" + token + "[message]:" + e.getMessage(), e);

			throw new BizException(TokenErrorCode.EX_LOGIN_PARSE_TOKEN_FAILED.getCode());
		}

	}

	/**
	 * 获取token中的subject对象信息
	 * 
	 * @param token
	 * @param targetCalss
	 * @return
	 */
	public static BaseTokenSubject getTokenSubject(String token) {

		try {

			String subject = parseToken(token, TokenTypeEnum.TYPE_TOKEN).getSubject();

			return JSON.parseObject(subject, BaseTokenSubject.class);

		} catch (Exception e) {

			logger.error("Token Manager getTokenSubject failed,[token]:" + token + "[message] :" + e.getMessage(), e);

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

		// setToken2Header(pair, SessionFilter.getResponse());

		setToken2Cookie(pair, SessionFilter.getResponse(), subject.getRealRefreshTokenExpireMinute(), subject);

		//设置当前登录用户的tenant id
		setTenantId2Cookie(subject.getTenantId());
		
		
		HttpSession session = SessionFilter.getRequest().getSession();

		Map<String, Object> objMaps = new HashMap<String, Object>();
		//
		Enumeration<String> se = session.getAttributeNames();
		while (se.hasMoreElements()) {
			String key = se.nextElement();
			objMaps.put(key, session.getAttribute(key));
		}

		// token设置到cookie或者header之后，在当前threadlocal里确认当前登录用户的session
		// id，fetchSessionCacheKey
		SessionHolder.determinCurrentSessionId(subject.getUserId(), subject.getTenantId(), subject.getLoginType(),
				subject.getRealRefreshTokenExpireMinute());

		// 把登录前session的数据同步过来
		cloneSession(objMaps);

		// 设置一个登录标记状态，如果session被清掉了，那么查不到这个状态
		SessionFilter.getRequest().getSession().setAttribute(SessionAttrbutes.SESSION_REFER_TOKEN, pair.getLeft());

		// 设置一个登录标记状态，设置对应的refresh token
		SessionFilter.getRequest().getSession().setAttribute(SessionAttrbutes.SESSION_REFER_REFRESH_TOKEN,
				pair.getRight());

		// 因为登录之前清理掉了重登的标志，SessionAttrbutes.SESSION_FORCE_RELOGIN
		// // 清理用户强制重新登录的flag
		// SessionHolder.clearForceReloginFlagIgnoreException(subject.getUserId(),
		// subject.getTenantId(),
		// subject.getLoginType());

	}

	/**
	 * 拷贝转换到redis session前的一些配置
	 * 
	 * @param objMaps
	 */
	private static void cloneSession(Map<String, Object> objMaps) {

		if (MapUtils.isNotEmpty(objMaps)) {

			for (Entry<String, Object> entry : objMaps.entrySet()) {
				SessionFilter.getRequest().getSession().setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * 设置 token和refresh token到cookie
	 * 
	 * @param pair
	 */
	private static void setToken2Cookie(Pair<String, String> pair, HttpServletResponse response, long minuts,
			BaseTokenSubject subject) {

		if (response == null || pair == null) {
			return;
		}

		int expire = Integer.parseInt(String.valueOf(DateUtil.getSecondByMinutes(minuts)));
		// 设置token
		Cookie tokenCookie = new Cookie(TokenConstants.COOKIE_AUTH_TOKENE, pair.getLeft());
		tokenCookie.setMaxAge(expire);
		tokenCookie.setPath("/"); // 设置根目录

		if (instance.cookieDomainEnable) {
			tokenCookie.setDomain(ROOT_DOMAIN);
		}
		// tokenCookie.setHttpOnly(false); // http only提升安全性
		// tokenCookie.setDomain(domain); //使用顶级域名，前面加点，比如 .waiqin365.com

		// // 设置 refresh_token
		// Cookie refreshTokenCookie = new
		// Cookie(TokenConstants.AUTH_REFRESH_TOKEN, pair.getRight());
		// refreshTokenCookie.setMaxAge(expire);
		// refreshTokenCookie.setPath("/");
		// refreshTokenCookie.setSecure(true);
		// refreshTokenCookie.setHttpOnly(true);

		response.addCookie(tokenCookie);

		// 设置登录类型
		Cookie loginTypeCookie = new Cookie(TokenConstants.COOKIE_SOURCE_TYPE, subject.getLoginType().getCode());
		loginTypeCookie.setMaxAge(expire);
		loginTypeCookie.setPath("/"); // 设置根目录

		if (instance.cookieDomainEnable) {
			loginTypeCookie.setDomain(ROOT_DOMAIN);
		}
		response.addCookie(loginTypeCookie);
		
	}
	
	
	/**
	 * 设置tenantid到cookie
	 * @param tenantId
	 * @param response
	 */
	private static void setTenantId2Cookie(long tenantId) {

		// 设置登录类型
		Cookie loginTypeCookie = new Cookie(TokenConstants.COOKIE_SOURCE_TALENT_4_GRAY, String.valueOf(tenantId));
		loginTypeCookie.setPath("/"); // 设置根目录

		if (instance.cookieDomainEnable) {
			loginTypeCookie.setDomain(ROOT_DOMAIN);
		}
		
		if(SessionFilter.getResponse()!=null) {
		
		     SessionFilter.getResponse().addCookie(loginTypeCookie);
		}
		
	}
	

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
	 */
	public static Pair<JwtInfo, JwtInfo> refreshTokenPair(String oldToken, String oldRefreshToken) {

		TokenLogger.printDebugInfo("Token Manager refreshTokenPair start: [oldToken]:" + oldToken
				+ ",[oldRefreshToken]:" + oldRefreshToken);

		if (StringUtils.isEmpty(oldToken) || StringUtils.isEmpty(oldRefreshToken)) {

			logger.error("Token Manager refreshTokenPair failed for inaglle parm: [oldToken]:" + oldToken
					+ ",[oldRefreshToken]:" + oldRefreshToken);
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_REFRESH_TOKEN.getCode());
		}

		try {

			Claims oldRefreshTokenClaims = parseToken(oldRefreshToken, TokenTypeEnum.TYPE_REFRESH_TOKEN);

			// 判断是否为空
			if (oldRefreshTokenClaims == null || StringUtils.isEmpty(oldRefreshTokenClaims.getSubject())) {

				logger.error("Token Manager refreshTokenPair failed. token is invalied: [oldToken]:" + oldToken
						+ ",[oldRefreshToken]:" + oldRefreshToken);

				throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_REFRESH_TOKEN.getCode());
			}

			BaseTokenSubject subject = JSON.parseObject(oldRefreshTokenClaims.getSubject(), BaseTokenSubject.class);

			long oldRefreshTokenUid = subject.getUserId();

			// 检查是否在黑名单列表
			SessionCacheHelper.isRefreshTokenInBlackCache(subject, oldRefreshToken);

			JwtInfo tokenInfo = instance.jwtBuilderUtils.createJWT("jwt", String.valueOf(oldRefreshTokenUid),
					oldRefreshTokenClaims.getSubject(),
					DateUtil.getMillisecondByMinutes(subject.getRealTokenExpireMinute()));

			JwtInfo refreshTokenInfo = instance.jwtBuilderUtils.createJWT("jwt", String.valueOf(oldRefreshTokenUid),
					oldRefreshTokenClaims.getSubject(),
					DateUtil.getMillisecondByMinutes(subject.getRealRefreshTokenExpireMinute()));

			// 把旧的token和refresh token加到黑名单
			SessionCacheHelper.setRefreshTokenToBlackList(subject, oldRefreshToken, instance.refreshTokenProtectTime);

			// 把token加到黑名单和刷新保护缓存中
			SessionCacheHelper.setTokenToBlackCache(subject, oldToken, instance.tokenRefreshProtectTime);

			// 将缓存中的session信息 时间刷新,和refresh token一样，缓存超时默认为 7天
			SessionCacheHelper.refreshSessionExpireTime(subject.getUserId(), subject.getTenantId(),
					subject.getLoginType(), subject.getRealRefreshTokenExpireMinute(), TimeUnit.MINUTES);

			// 设置到token
			setToken(Pair.of(tokenInfo.getJwtStr(), refreshTokenInfo.getJwtStr()), subject);

			TokenLogger.printDebugInfo("Token Manager refreshTokenPair end: [oldToken]:" + oldToken
					+ ",[oldRefreshToken]:" + oldRefreshToken + ",[newToken]:" + tokenInfo.getJwtStr()
					+ ",[newRefreshToken]:" + refreshTokenInfo.getJwtStr());

			return Pair.of(tokenInfo, refreshTokenInfo);

		} catch (BizException e) {

			logger.error("Token Manager->refreshTokenPair biz failed,[oldToken]:" + oldToken + ",[oldRefreshToken]:"
					+ oldRefreshToken + "[message]:" + e.getMessage(), e);

			reportEvent(SessionExceptionEvent.TYPE_REFRESH_TOKEN_AUTH_FAILED,
					"TokenManager=>[refreshTokenPair]:bizException",
					"[oldToken]=" + oldToken + ",[oldRefreshToken]=" + oldRefreshToken, e);

			throw e;
		} catch (Exception e) {

			logger.error("Token Manager->refreshTokenPair unexpercted failed,[oldToken]:" + oldToken
					+ ",[oldRefreshToken]:" + oldRefreshToken + "[message]:" + e.getMessage(), e);

			reportEvent(SessionExceptionEvent.TYPE_REFRESH_TOKEN_AUTH_FAILED,
					"TokenManager=>[refreshTokenPair]:exception",
					"[oldToken]=" + oldToken + ",[oldRefreshToken]=" + oldRefreshToken, e);

			throw new BizException(TokenErrorCode.EX_LOGIN_REFRESH_TOKEN_FAILED.getCode());

		}

	}

	/**
	 * 判断token是否有效
	 * 
	 * 1. 判断token是否失效 2. 判断token是否在黑名单中，这个考虑是否可以不校验，每次请求都调用，缓存的调用频率会比较高
	 * 
	 * 
	 * @param token
	 * @return
	 */
	public static void isValidToken(String token) {

		TokenLogger.printDebugInfo("TokenManager->isValidToken start :[token]=" + token);

		Pair<Boolean, Claims> pair = parseTokenEx(token, TokenTypeEnum.TYPE_TOKEN);

		Claims claim = pair.getRight();

		if (claim == null || StringUtils.isEmpty(claim.getSubject())) {
			throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN.getCode());
		}

		BaseTokenSubject subject = JSON.parseObject(claim.getSubject(), BaseTokenSubject.class);

		TokenLogger.printDebugInfo("TokenManager->isValidToken parseTokenEx:[isExpire]" + pair.getLeft() + ",[uid]="
				+ subject.getUserId() + ",[tenantId]=" + subject.getTenantId() + ",[loginType]="
				+ subject.getLoginType().getCode() + ",[token]=" + token);

		boolean isExpire = pair.getLeft();

		if (isExpire) {
			// 校验过期的token
			validateExpireToken(subject, token, claim);
		} else {
			// 正常的校验
			validateNotExpireToken(subject, token);
		}
		
		//当前用户加载到cookie
		setTenantId2Cookie(subject.getTenantId());

		// long uid = subject.getUserId();
		//
		// try {
		//
		// // 是否在刷新保护期
		// boolean isInRefreshProtectTime =
		// SessionCacheHelper.isTokenInRefershProtectTime(subject, token);
		//
		// // 如果不在刷新保护期，那么检查下是否在刷新黑名单中
		// if (!isInRefreshProtectTime &&
		// SessionCacheHelper.isTokenInBlackCache(subject, token)) {
		// throw new
		// BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN.getCode());
		// }
		//
		// // 前面2步校验都通过，将当前登录用户的token session id设置到TokenSessionHolder
		// SessionHolder.determinCurrentSessionId(uid, subject.getTenantId(),
		// subject.getLoginType(),
		// subject.getRealRefreshTokenExpireMinute());
		//
		// // 校验用户是不是强制登录
		// SessionCacheHelper.isForceReloginIgnoreException(uid,
		// subject.getTenantId(), subject.getLoginType());
		//
		// // 如果在刷新保护期，不需要再检查session了，因为这个时候refer_token必然不一致
		// if (!isInRefreshProtectTime) {
		// // 判断session是否存在，如果session不在，需要重新登录;这个主要用在
		// // 1. 修改密码，清空session就可以让用户强制重新登录，
		// // 2. 修改了企业的token超时时间，也可以通过这个方法让用户重新登录，更新超时配置
		// // 3. 在web 2处登录，第二个登录的会把第一个t下线
		// SessionCacheHelper.checkSessionExist(token);
		// }
		//
		// } catch (BizException e) {
		//
		// logger.error("token鉴权失败!errorcode=" + e.getErrorCode() + ",userId=" +
		// uid + ",tenantId="
		// + subject.getTenantId() + ",token :" + token, e);
		//
		// throw e;
		//
		// } catch (Exception e) {
		// logger.error("token鉴权失败!非预期异常!userId=" + uid + ",tenantId=" +
		// subject.getTenantId() + ",token :" + token,
		// e);
		// throw new BizException(BaseCode.EX_SYSTEM_UNKNOW.getCode());
		// }

	}

	/**
	 * token已经校验过期了，但是也不能粗暴的认为不能登录，要保证兼容性，可能刷新的请求没发出去，正常的业务请求已经发出来了，要让用户能继续用一会，
	 * 
	 * 给刷新token留下点时间
	 * 
	 * @param subject
	 * @param token
	 */
	private static void validateExpireToken(BaseTokenSubject subject, String token, Claims claim) {

		long uid = subject.getUserId();

		try {

			// 将当前登录用户的token session id设置到TokenSessionHolder
			SessionHolder.determinCurrentSessionId(uid, subject.getTenantId(), subject.getLoginType(),
					subject.getRealRefreshTokenExpireMinute());

			// 校验用户是不是强制重新登录
			SessionCacheHelper.isForceReloginIgnoreException(uid, subject.getTenantId(), subject.getLoginType());

			// token是否在refresh token刷新之后的token保护期
			boolean isInRefreshProtectTime = SessionCacheHelper.isTokenInRefershProtectTime(subject, token);

			// 如果在刷新保护期，那么下面2步校验步需要做了:1.黑名单校验 2.session校验
			if (isInRefreshProtectTime) {
				return;
			}

			// 如果不在刷新保护期，那么检查下是否在token黑名单中
			boolean isInTokenBlack = SessionCacheHelper.isTokenInBlackCache(subject, token);
			if (isInTokenBlack) {
				throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN.getCode());
			}

			// 如果在刷新保护期，不需要再检查session了，因为这个时候refer_token必然不一致
			// 判断session是否存在，如果session不在，需要重新登录;这个主要用在
			// 1. 修改密码，清空session就可以让用户强制重新登录，
			// 2. 修改了企业的token超时时间，也可以通过这个方法让用户重新登录，更新超时配置
			// 3. 在web 2处登录，第二个登录的会把第一个t下线
			SessionCacheHelper.checkSessionExist(token);

			// 1. 如果是过期的token.校验现在有没有超过refresh token的超时时间。
			// 2. 因为过期token使用是有时间控制的，允许过期使用一小段时间，超过了不能再用。

			// 是否达到refresh token的超时时间
			hasReachFreshTokenExpireTime(claim, subject);

			// 把这个过期的token加到黑名单保护中，类似刷新refresh token中的操作
			SessionCacheHelper.setTokenToBlackCache(subject, token, instance.expireTokenProtectTime);

		} catch (BizException e) {

			logger.info("token鉴权失败!errorcode=" + e.getErrorCode() + ",userId=" + uid + ",tenantId="
					+ subject.getTenantId() + ",token :" + token, e);

			throw e;

		} catch (Exception e) {
			logger.error("token鉴权失败!非预期异常!userId=" + uid + ",tenantId=" + subject.getTenantId() + ",token :" + token,
					e);
			throw new BizException(BaseCode.EX_SYSTEM_UNKNOW.getCode());
		}

	}

	/**
	 * 检测是否达到refresh token的超时时间
	 * 
	 * @param claim
	 * @param subject
	 */
	private static void hasReachFreshTokenExpireTime(Claims claim, BaseTokenSubject subject) {

		Date createDate = claim.getIssuedAt();

		Date now = new Date();

		Date refreTokenExpire = DateUtil.getDateAfterMinutes(createDate, subject.getRealRefreshTokenExpireMinute());

		// 如果已经达到了refresh token的超时时间，那么认为是超时的
		if (now.after(refreTokenExpire)) {

			throw new BizException(TokenErrorCode.EX_LOGIN_TOKEN_EXPIRE.getCode());
		}

	}

	/**
	 * 校验token时，token没过期，按照正常的逻辑走：
	 * 
	 * 这个流程会请求2次redis:
	 * 
	 * 1. 取所有的session信息 2. 查询token是否在黑名单里，这个不能放在session中
	 * 
	 * @param subject
	 * @param token
	 */
	private static void validateNotExpireToken(BaseTokenSubject subject, String token) {

		long uid = subject.getUserId();

		try {

			// 将当前登录用户的token session id设置到TokenSessionHolder
			SessionHolder.determinCurrentSessionId(uid, subject.getTenantId(), subject.getLoginType(),
					subject.getRealRefreshTokenExpireMinute());

			// 校验用户是不是强制重新登录
			SessionCacheHelper.isForceReloginIgnoreException(uid, subject.getTenantId(), subject.getLoginType());

			// token是否在refresh token刷新之后的token保护期
			boolean isInRefreshProtectTime = SessionCacheHelper.isTokenInRefershProtectTime(subject, token);

			// 如果在刷新保护期，那么下面2步校验步需要做了:1.黑名单校验 2.session校验
			if (isInRefreshProtectTime) {
				return;
			}

			// 如果不在刷新保护期，那么检查下是否在刷新黑名单中
			if (SessionCacheHelper.isTokenInBlackCache(subject, token)) {
				throw new BizException(TokenErrorCode.EX_LOGIN_IEAGLLE_TOKEN.getCode());
			}

			// 如果在刷新保护期，不需要再检查session了，因为这个时候refer_token必然不一致
			// 判断session是否存在，如果session不在，需要重新登录;这个主要用在
			// 1. 修改密码，清空session就可以让用户强制重新登录，
			// 2. 修改了企业的token超时时间，也可以通过这个方法让用户重新登录，更新超时配置
			// 3. 在web 2处登录，第二个登录的会把第一个t下线
			SessionCacheHelper.checkSessionExist(token);

		} catch (BizException e) {

			logger.info("token鉴权失败!errorcode=" + e.getErrorCode() + ",userId=" + uid + ",tenantId="
					+ subject.getTenantId() + ",token :" + token, e);

			throw e;

		} catch (Exception e) {
			logger.error("token鉴权失败!非预期异常!userId=" + uid + ",tenantId=" + subject.getTenantId() + ",token :" + token,
					e);
			throw new BizException(BaseCode.EX_SYSTEM_UNKNOW.getCode());
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

		// Bearer+一个空格，刚好7位，去除前面7位后面就是token
		if (StringUtils.isEmpty(bearer) || bearer.length() <= 7) {
			return null;
		}

		// 截取 Bearer+空格后面的token字符
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
	 * 从request的cookie中获取登录类型
	 * 
	 * @return
	 */
	public static String fecthSourceTypeFromRequestCookie(HttpServletRequest httpRequest) {

		Cookie[] cookies = httpRequest.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (TokenConstants.COOKIE_SOURCE_TYPE.equals(cookie.getName())) {
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

}