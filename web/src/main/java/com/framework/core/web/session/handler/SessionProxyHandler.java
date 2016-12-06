package com.framework.core.web.session.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.framework.core.web.session.SessionHolder;

/**
 * 拦截httpsession的setattrbute和getattrbute方法
 * 
 * @author zhangjun
 *
 */
public class SessionProxyHandler implements InvocationHandler {

	private HttpSession session = null;

	private SessionProxyHandler(HttpSession httpSession) {
		this.session = httpSession;
	};

	public static HttpSession getInstance(HttpSession httpSession) {
		
		if(httpSession == null)
			return null;
		
		InvocationHandler handler = new SessionProxyHandler(httpSession);
		return (HttpSession) Proxy.newProxyInstance(httpSession.getClass().getClassLoader(),
				httpSession.getClass().getInterfaces(), handler);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("setAttribute".equals(method.getName()) || "putValue".equals(method.getName())) {

			String sessionid = SessionHolder.fetchTokenSessionId();

			// 获取不到token对应的session id
			if (StringUtils.isEmpty(sessionid)) {
				return method.invoke(session, args);
			}

			// arg[0]是 hashkey，args[1]是value
			SessionHolder.setAttrbute(sessionid, args[0], args[1]);

			return null;

		} else if ("getAttribute".equals(method.getName())) {

			String sessionid = SessionHolder.fetchTokenSessionId();

			// 获取不到token对应的session id
			if (StringUtils.isEmpty(sessionid)) {
				return method.invoke(session, args);
			}

			// arg[0]是 hashkey
			return SessionHolder.getAttrbute(sessionid, args[0]);
		} else if ("removeAttribute".equals(method.getName())) {

			String sessionid = SessionHolder.fetchTokenSessionId();

			// 获取不到token对应的session id
			if (StringUtils.isEmpty(sessionid)) {
				return method.invoke(session, args);
			}

			SessionHolder.removeAttrbute(sessionid, args[0]);
			return null;

		} 
//		
//		else if ("invalidate".equals(method.getName())) {
//
//			String sessionid = SessionHolder.fetchTokenSessionId();
//
//			// 获取不到token对应的session id
//			if (StringUtils.isEmpty(sessionid)) {
//				return method.invoke(session, args);
//			}
//
//			SessionHolder.invalidate(sessionid);
//			return null;
//
//		}

		return method.invoke(session, args);
	}

}