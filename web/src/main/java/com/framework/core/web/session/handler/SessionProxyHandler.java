package com.framework.core.web.session.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.event.SessionExceptionEvent;
import com.framework.core.error.exception.BizException;
import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.web.session.SessionHolder;
import com.framework.core.web.session.filter.SessionFilter;
import com.framework.core.web.session.token.constants.SessionAttrbutes;

/**
 * 拦截httpsession的setattrbute和getattrbute方法
 * 
 * @author zhangjun
 *
 */
public class SessionProxyHandler implements InvocationHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private HttpSession session = null;

	private SessionProxyHandler(HttpSession httpSession) {
		this.session = httpSession;
	};

	public static HttpSession getInstance(HttpSession httpSession) {

		if (httpSession == null)
			return null;

		InvocationHandler handler = new SessionProxyHandler(httpSession);
		return (HttpSession) Proxy.newProxyInstance(httpSession.getClass().getClassLoader(),
				httpSession.getClass().getInterfaces(), handler);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("setAttribute".equals(method.getName()) || "putValue".equals(method.getName())) {

			// 本地先存储一份
			method.invoke(session, args);

			
			String sessionid = SessionHolder.fetchTokenSessionId();
			
			if (StringUtils.isNotEmpty(sessionid)) {

				try {
					// arg[0]是 hashkey，args[1]是value
					SessionHolder.setAttrbute(sessionid, args[0], args[1]);

				} catch (Exception e) {
					// 如果抛出异常，记录日志，还从老的session逻辑
					logger.error("SessionProxyHandler ---SessionHolder.setAttrbute failed,[sessionId]="+sessionid +"[arg0]="+args[0]+",[arg1]="+args[1], e);

					String methodInfo = "SessionProxyHandler=>[setAttribute]";
					reportEvent(methodInfo, args, e);

				}

			}

			return null;

		} else if ("getAttribute".equals(method.getName())) {

			// 本地session先尝试获取
			Object obj = method.invoke(session, args);

			boolean isTokenRefer = isTokenRefer(String.valueOf(args[0]));

			// 如果本地能获取到，直接返回，token相关的例外，这个必须从redis优先取
			if (obj != null && !isTokenRefer) {
				return obj;
			}

			String sessionid = SessionHolder.fetchTokenSessionId();

			if (StringUtils.isNotEmpty(sessionid)) {
				try {
					return SessionHolder.getAttrbute(sessionid, args[0]);
				} catch (Exception e) {
					// 如果抛出异常，记录日志，还从老的session逻辑
					
					logger.error("SessionProxyHandler ---SessionHolder.getAttribute failed,[sessionId]="+sessionid +"[arg0]="+args[0], e);

					String methodInfo = "SessionProxyHandler=>[getAttribute]";
					reportEvent(methodInfo, args, e);

				}
			}

			// 如果时token相关的，并且之前从session取不到
			if (isTokenRefer) {
				return obj;
			}

			return null;

		} else if ("removeAttribute".equals(method.getName())) {

			method.invoke(session, args);

			String sessionid = SessionHolder.fetchTokenSessionId();

			if (StringUtils.isNotEmpty(sessionid)) {
				try {
					SessionHolder.removeAttrbute(sessionid, args[0]);
				} catch (Exception e) {

					// 如果抛出异常，记录日志，还从老的session逻辑
					
					logger.error("SessionProxyHandler ---SessionHolder.removeAttrbute failed,[sessionId]="+sessionid +"[arg0]="+args[0], e);

					String methodInfo = "SessionProxyHandler=>[removeAttribute]";
					reportEvent(methodInfo, args, e);

				}
			}

			return null;

		}

		else if ("invalidate".equals(method.getName())) {
			
			method.invoke(session, args);

			String sessionid = SessionHolder.fetchTokenSessionId();

			if (StringUtils.isNotEmpty(sessionid)) {

				try {
					SessionHolder.invalidate(sessionid);
				} catch (Exception e) {

					// 如果抛出异常，记录日志，还从老的session逻辑

					logger.error("SessionProxyHandler ---SessionHolder.invalidate failed,[sessionId]="+sessionid, e);
					
				    String methodInfo = "SessionProxyHandler=>[invalidate]";
					// 上报事件
					reportEvent(methodInfo, args, e);

				}

			}

			return null;

		}

		return method.invoke(session, args);
	}

	/**
	 * 上报事件
	 * 
	 * @param methodInfo
	 * @param args
	 * @param e
	 */
	private void reportEvent(String methodInfo, Object[] args, Exception e) {

		int errorcode = BaseCode.EX_SYSTEM_UNKNOW.getCode();

		if (e instanceof BizException) {
			errorcode = ((BizException) e).getErrorCode();
		}

		SessionExceptionEvent event = new SessionExceptionEvent(SessionExceptionEvent.TYPE_REDIS_EX,
				SessionFilter.getRequest().getRequestURI(), methodInfo, JSON.toJSONString(args), errorcode, e);
		EventPublisherUtils.reportEvent(event);
	}

	/**
	 * 是否token相关的
	 * 
	 * @param key
	 * @return
	 */
	private boolean isTokenRefer(String key) {

		if (key.equals(SessionAttrbutes.SESSION_REFER_REFRESH_TOKEN)
				|| key.equals(SessionAttrbutes.SESSION_REFER_TOKEN)) {
			return true;
		} else {
			return false;
		}

	}

}