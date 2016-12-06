package com.framework.core.web.common.interceptor;

import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.ClassUtils;

import com.framework.core.dal.exception.DalErrorCode;
import com.framework.core.error.exception.BizException;
import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.web.common.biz.TenantIdDetermin4Dao;
import com.framework.core.web.hystrix.annotation.HystrixDaoConfig;
import com.framework.core.zookeeper.listener.custom.ZkNodeListener;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * dao拦截器
 * 
 * @author zhangjun
 *
 */
public class DaoInterceptor implements MethodInterceptor, InitializingBean, ApplicationEventPublisherAware,ApplicationContextAware {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationEventPublisher publisher;

	// 数据库慢操作阈值，超过150ms打印运行日志
	private static final int DATABASE_THRESHOLD = 150;

	// 数据库默认操作timeout阈值，超过1000ms 认为失败
	private static final int DATABASE_TIME_OUT = 1000;
	
	//web context , for ex: appsvr
	private String context;
	
	/**
	 * 决定dao的 tenantid
	 */
	private TenantIdDetermin4Dao tenantIdDetermin4Dao;

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		HystrixDaoConfig config = invocation.getMethod().getAnnotation(HystrixDaoConfig.class);

		//加了配置标签，并且设置为不走hystrix发送
		if (config == null || !config.useHystrix()) {
			return doRealWork(invocation);
		} else {
			return executeWithHystrix(invocation, config);
		}

	}

	/**
	 * 直接执行
	 * 
	 * @param invocation
	 * @return
	 * @throws Throwable
	 */
	private Object doRealWork(MethodInvocation invocation) throws Throwable {

		// 数据库操作返回结果
		Object result = null;
		try {

			// 执行数据库操作
			result = invocation.proceed();

		} catch (Exception e) {
			e.printStackTrace();
			// logger.error("database operation
			// exception,dao[{}],statement[{}],begintime[{}],endtime[{}]. ",
			// mapperNamespace,
			// statementId,
			// beginTime,
			// System.currentTimeMillis(),
			// e);
		} finally {

		}

		return result;
	}

	/**
	 * 以hystrix command 运行
	 * 
	 * @param invocation
	 * @return
	 */
	private Object executeWithHystrix(MethodInvocation invocation, HystrixDaoConfig config) {

		Class<?> clazz = invocation.getThis().getClass();

		// dao中的方法名称
		String statementId = invocation.getMethod().getName();

		// 获取实际调用mapper erp
		Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(clazz, clazz.getClassLoader());

		String mapperNamespace = null;
		
		//dao不是接口实现
		if(targetInterfaces == null||targetInterfaces.length ==0) {
		
			mapperNamespace = clazz.getName();
		//接口实现的dao	
		} else {
			// dao完整名称，同mapping.xml中的namespace
			mapperNamespace = targetInterfaces[0].getName();
		}

		// group name :
		String groupName = context+"_"+tenantIdDetermin4Dao.getCurTenantId();
		
		// 完整的dao名称 com.xxx.xxx.xxxx.insert
		String dao = mapperNamespace + "." + statementId;
		logger.debug("call dao method [{}.{}]", dao);

		// group key : context+tengenid key: 完整的dao名称,比如
		// com.xxx.xxx.xxxx.insert

		HystrixDaoCommand cmd = new HystrixDaoCommand(invocation, groupName, dao, config.timeout());

		return cmd.execute();

	}

	/**
	 * hystrix command，
	 * 
	 * @author zhangjun
	 *
	 */
	public class HystrixDaoCommand extends HystrixCommand<Object> {

		private MethodInvocation invocation;

		protected HystrixDaoCommand(MethodInvocation invocation, String groupKey, String key, int timeout) {

			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
					.andCommandKey(HystrixCommandKey.Factory.asKey(key)).andCommandPropertiesDefaults(
							// we default to a 500ms timeout for db operation
							HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeout)));

			this.invocation = invocation;
		}

		protected HystrixDaoCommand(MethodInvocation invocation, String groupKey, String key) {

			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
					.andCommandKey(HystrixCommandKey.Factory.asKey(key)).andCommandPropertiesDefaults(
							// we default to a 500ms timeout for db operation
							HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(DATABASE_TIME_OUT)));

			this.invocation = invocation;
		}

		@Override
		protected Object run() throws Exception {

			try {
				return doRealWork(invocation);
			} catch (Throwable e) {

				logger.error("HystrixDaoCommand run fail!,message is:" + e.getMessage(), e);
				throw new BizException(DalErrorCode.EX_SYSTEM_DB_ERROR.getCode(), e.getMessage(), e);
			}
		}

		@Override
		public Object getFallback() {
			System.out.println("HystrixDaoCommand-getFallback-start，time=" + System.currentTimeMillis());

			return null;

		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		Map<String, TenantIdDetermin4Dao> list = applicationContext.getBeansOfType(TenantIdDetermin4Dao.class);

		//判断是否为空
		if(MapUtils.isEmpty(list) || list.size()>1) {
			throw new RuntimeException("使用DaoInterceptor请先实现TenantIdDetermin4Dao接口(注册为bean,接口只实现一次)!");
		}

		
		for(Entry<String,TenantIdDetermin4Dao> entry:list.entrySet()) {
			tenantIdDetermin4Dao = entry.getValue();
		}
	}

}