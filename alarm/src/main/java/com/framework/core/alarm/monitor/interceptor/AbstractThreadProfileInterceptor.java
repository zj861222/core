package com.framework.core.alarm.monitor.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.framework.core.alarm.event.ServiceAccessEvent;
import com.framework.core.alarm.monitor.RequestStatistic;
import com.framework.core.alarm.monitor.ThreadProfile;



/**
 * 
 * service access 拦截器抽象类，因为要适配 struts2 和  spring，所以吧具体的逻辑抽象出来
 * 
 * @author zhangjun
 *
 */
public abstract class AbstractThreadProfileInterceptor implements ApplicationEventPublisherAware {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	// 多少毫秒会打印异常日志
	private int threshold = 500;

//	public final static String HTTP_HEADER_SERVICE_NAME = "X-365-SERVICE-NAME";

	// 服务的方法名
	private final static ThreadLocal<String> serviceNameThreadLocal = new ThreadLocal<>();

	// 本服务内的方法名称
	private final static ThreadLocal<String> localServiceNameThreadLocal = new ThreadLocal<>();

	// publisher
	private ApplicationEventPublisher publisher;

	/**
	 * 获取服务名，请求的连接地址
	 * 
	 * @return
	 */
	private String getRequestName(HttpServletRequest request) {

		return request.getRequestURI();
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	private void setupServiceName(final HttpServletRequest request) {

//		String serviceName = request.getHeader(HTTP_HEADER_SERVICE_NAME);
//
//		// 头部没有服务名称，则认为是来自gateway的请求
//		if (StringUtils.isEmpty(serviceName)) {
//			serviceName = getRequestName(request);
//		}
		
		String serviceName = getRequestName(request);

		serviceNameThreadLocal.set(serviceName);

		// local
		localServiceNameThreadLocal.set(request.getRequestURI());
	}

	/**
	 * 获取服务名称
	 * 
	 * @return 服务名称
	 */
	public static String getServiceName() {
		String name = serviceNameThreadLocal.get();
		return StringUtils.isEmpty(name) ? "unknown" : name;
	}

	/**
	 * 获取本地服务名称
	 * 
	 * @return 服务名称
	 */
	public static String getLocalServiceName() {
		String name = localServiceNameThreadLocal.get();
		return StringUtils.isEmpty(name) ? "unknown" : name;
	}

	
	/**
	 * 请求执行前之后执行的操作
	 * @param request
	 */
	protected void doPreHandle(HttpServletRequest request) {
		
		try {
			// 设置服务名称
			this.setupServiceName(request);

			// 需要记录请求处理时长以及堆栈
			ThreadProfile.start(request.getRequestURI(), this.threshold);

			ThreadProfile.enter(this.getClass().getName(), "doPreHandle");
		} catch (Exception e) {
			logger.error("AbstractThreadProfileInterceptor execute doPreHandle failed!", e);
		}

		// //数据库操作,默认从master,slave分摊查询操作, 与database.yml中读readOnlyInSlave一起判断
		// DatabaseRouting.masterOrSlave();
	}

	/**
	 * 请求执行完之后执行的操作
	 * @param request
	 * @param response
	 */
	protected void doAfterCompletion(HttpServletRequest request, HttpServletResponse response) {

		try {
			/** remove all threadlocal */
			String srcServiceName = this.getServiceName();
			serviceNameThreadLocal.remove();
			localServiceNameThreadLocal.remove();

			// //remove master or salve
			// DatabaseRouting.remove();

			ThreadProfile.exit();

			/** do stat */
			Pair<String, Long> duration = ThreadProfile.stop();
			if (duration != null) {

				// 统计响应时长分布
				RequestStatistic.put(request.getRequestURI(), duration.getRight().intValue());

				// 服务调用的统计
				ServiceAccessEvent event = new ServiceAccessEvent(request.getRequestURI(), duration.getRight(),
						response.getStatus(), duration.getLeft());
				event.setSrcService(srcServiceName);
				this.publisher.publishEvent(event);
			}

		} catch (Exception e) {
			logger.error("AbstractThreadProfileInterceptor execute doAfterCompletion failed!", e);

		}
	}

}