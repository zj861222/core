package com.framework.core.dal.datasource.annotation.impl;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.framework.core.dal.datasource.DataSourceManager;
import com.framework.core.dal.datasource.annotation.ForceReleaseConn;


/**
 * Created by zhangjun
 */

public class ForceReleaseAnnotationInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {

		ForceReleaseConn annotation = methodInvocation.getMethod().getAnnotation(ForceReleaseConn.class);

		// 判断注解中的ForceReleaseConn取值，设置
		if (annotation != null && annotation.forceKillAfterComplete()) {

			DataSourceManager.setForceRelease(true);
		}

		try {
			return methodInvocation.proceed();

		} finally {
			DataSourceManager.removeForceRelease();
		}

	}

}
