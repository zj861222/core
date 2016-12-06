
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSON;
import com.framework.core.common.view.BizResult;
import com.framework.core.error.exception.BizException;
import com.framework.core.web.session.strategy.TokenStrategyExecutor;
import com.framework.core.web.session.token.TokenManager;
import com.framework.core.web.session.wrapper.SessionRequestWrapper;
import com.framework.core.web.session.wrapper.XSSRequestWrapper;

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

		// 看是否开启了token验证
		if (TokenManager.isTokenEnable()) {
			enableTokenValidateAndRedisSession(request, response, chain);
		} else {
			chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request), response);

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
	private void enableTokenValidateAndRedisSession(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		REQUEST_LOCAL.set(request);
		RESPONSE_LOCAL.set(response);

		try {
			// 校验token
			tokenStrategyExecutor.tokenValidate((HttpServletRequest) request, (HttpServletResponse) response);


		} catch (BizException e) {

			logger.info("SessionFilter token 校验失败,biz code :"+e.getErrorCode()+",message :"+e.getMessage(), e);
			// 对异常信息做处理
			redirctToLoginPage((HttpServletRequest)request,(HttpServletResponse) response, e);
			
			return;

		} catch (Exception e) {
			logger.error("SessionFilter token 校验失败，非预期异常,message : "+e.getMessage(), e);
			redirctToLoginPage((HttpServletRequest)request,(HttpServletResponse) response, new BizException(e, "token校验非预期异常"));

			return;

		}
		
		
		chain.doFilter(new SessionRequestWrapper((HttpServletRequest) request), response);

	}

	
	protected abstract void redirctToLoginPage(HttpServletRequest request,HttpServletResponse response, BizException e) throws IOException ;

	
//	/**
//	 * 
//	 * @return
//	 * @throws IOException 
//	 * @throws BizException 
//	 */
//	protected void redirctToLoginPage(HttpServletRequest request,HttpServletResponse response, BizException e) throws IOException {
//
//		String url = response.encodeRedirectURL(request.getRequestURL().toString()) ;
//		
//		response.sendRedirect("https://cloud.waiqin365.com?returnUrl="+url);
//
//	}

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

}