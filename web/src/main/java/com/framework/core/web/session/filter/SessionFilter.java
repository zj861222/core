
package com.framework.core.web.session.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSON;
import com.framework.core.common.view.BizResult;
import com.framework.core.dal.datasource.DataSourceManager;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.exception.TokenErrorCode;
import com.framework.core.web.session.SessionHolder;
import com.framework.core.web.session.strategy.TokenStrategyExecutor;
import com.framework.core.web.session.token.TokenManager;
import com.framework.core.web.session.token.constants.SourceTypeEnum;
import com.framework.core.web.session.token.constants.TokenTypeEnum;
import com.framework.core.web.session.token.view.BaseTokenSubject;
import com.framework.core.web.session.wrapper.SessionRequestWrapper;
import com.framework.core.web.session.wrapper.XSSRequestWrapper;

import io.jsonwebtoken.Claims;

/**
 * session filter，重写getsession方法，使用缓存存储session信息
 * 
 * @author zhangjun
 *
 */
public abstract class SessionFilter implements Filter {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** The Constant requestLocal. */
	private static final ThreadLocal REQUEST_LOCAL = new ThreadLocal();
	/** The Constant responseLocal. */
	private static final ThreadLocal RESPONSE_LOCAL = new ThreadLocal();

	private static final ThreadLocal<String> CURRENT_TOKEN_LOCAL = new ThreadLocal<String>();

	private TokenStrategyExecutor tokenStrategyExecutor;

	public TokenStrategyExecutor getTokenStrategyExecutor() {
		return tokenStrategyExecutor;
	}

	public void setTokenStrategyExecutor(TokenStrategyExecutor tokenStrategyExecutor) {
		this.tokenStrategyExecutor = tokenStrategyExecutor;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {
			// 看是否开启了token验证
			if (TokenManager.isTokenEnable()) {
				enableTokenValidateAndRedisSession(request, response, chain);
			} else {
				chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request), response);

			}
		} finally {
			// 清理threadlocal
			clearThreadLocal();
		}

	}

	/**
	 * 决定是否开启token校验，redis存储session的功能
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @throws ServletException
	 * @throws IOException
	 */
	private void enableTokenValidateAndRedisSession(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		ServletRequest sessionRequest = new SessionRequestWrapper((HttpServletRequest) request);
		REQUEST_LOCAL.set(sessionRequest);
		RESPONSE_LOCAL.set(response);

		try {
			// 校验token
			tokenStrategyExecutor.tokenValidate((HttpServletRequest) request, (HttpServletResponse) response);

		} catch (BizException e) {

			logger.info("SessionFilter token 校验失败,biz code :" + e.getErrorCode() + ",message :" + e.getMessage()+",url:"+(getRequest()!=null?getRequest().getRequestURI():"null"));
			// 对异常信息做处理
			redirctToLoginPage((HttpServletRequest) request, (HttpServletResponse) response, e);

			return;

		} catch (Exception e) {
			logger.error("SessionFilter token 校验失败，非预期异常,message : " + e.getMessage()+",url:"+(getRequest()!=null?getRequest().getRequestURI():"null"), e);
			redirctToLoginPage((HttpServletRequest) request, (HttpServletResponse) response,
					new BizException(e, "token校验非预期异常"));

			return;

		}

		chain.doFilter(sessionRequest, response);

	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param e
	 */
	protected void redirctToLoginPage(HttpServletRequest request, HttpServletResponse response, BizException e)
			throws IOException {

		String token = getCurrentToken();

		if (StringUtils.isNotEmpty(token)) {

			try {
				Pair<Boolean, Claims> pair = TokenManager.parseTokenEx(token, TokenTypeEnum.TYPE_TOKEN);

				if (pair != null) {
					BaseTokenSubject subject = JSON.parseObject(pair.getRight().getSubject(), BaseTokenSubject.class);

					redirctToLoginPage(request, response, e, subject.getLoginType());

					return;
				}

			} catch (Exception e1) {
				logger.error("redirctToLoginPage token解析失败，非预期异常, message : " + e1.getMessage() + ",[token]=" + token,
						e1);
			}

		}

		// 如果前面没能根据token 解析到 登录方式，下面尝试根据cookie中的值取下试试
		SourceTypeEnum type = tryGetLoginTypeFromCookie(request);

		redirctToLoginPage(request, response, e, type);

	}

	/**
	 * 尝试从cookie中获取
	 * 
	 * @param request
	 * @return
	 */
	private SourceTypeEnum tryGetLoginTypeFromCookie(HttpServletRequest request) {

		String sourceType = TokenManager.fecthSourceTypeFromRequestCookie(request);

		if (SourceTypeEnum.SOURCE_TYPE_CLIENT.getCode().equals(sourceType)) {
			return SourceTypeEnum.SOURCE_TYPE_CLIENT;
		} else if (SourceTypeEnum.SOURCE_TYPE_WEB.getCode().equals(sourceType)) {
			return SourceTypeEnum.SOURCE_TYPE_WEB;
		} else if (SourceTypeEnum.SOURCE_TYPE_H5.getCode().equals(sourceType)) {
			return SourceTypeEnum.SOURCE_TYPE_H5;
		} else {
			return null;
		}
	}

	protected abstract void redirctToLoginPage(HttpServletRequest request, HttpServletResponse response, BizException e,
			SourceTypeEnum sourceType) throws IOException;

	// /**
	// *
	// * @return
	// * @throws IOException
	// * @throws BizException
	// */
	// protected void redirctToLoginPage(HttpServletRequest
	// request,HttpServletResponse response, BizException e) throws IOException
	// {
	//
	// String url =
	// response.encodeRedirectURL(request.getRequestURL().toString()) ;
	//
	// response.sendRedirect("https://cloud.waiqin365.com?returnUrl="+url);
	//
	// }

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig config) throws ServletException {

		ServletContext context = config.getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		tokenStrategyExecutor = (TokenStrategyExecutor) ctx.getBean("tokenStrategyExecutor");

	}

	/**
	 * 获取当前线程的request.
	 * 
	 * @return the requestLocal
	 */
	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) REQUEST_LOCAL.get();
	}

	/**
	 * 获取当前线程的response.
	 * 
	 * @return the responseLocal
	 */
	public static HttpServletResponse getResponse() {
		return (HttpServletResponse) RESPONSE_LOCAL.get();
	}

	public static void setCurrentToken(String token) {

		CURRENT_TOKEN_LOCAL.set(token);
	}

	public static String getCurrentToken() {

		return CURRENT_TOKEN_LOCAL.get();
	}

	/**
	 * 
	 */
	private void clearThreadLocal() {

		REQUEST_LOCAL.remove();
		RESPONSE_LOCAL.remove();

		CURRENT_TOKEN_LOCAL.remove();

		SessionHolder.clearThreadLocal();

		// 清理掉数据源相关的
		DataSourceManager.clearAllThreadLocal();
	}

}