package com.framework.core.dal.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * add by zhangjun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForceReleaseConn {

	/**
	 * 执行完 强制kill数据库连接池里的连接
	 * 
	 * @return
	 */
	boolean forceKillAfterComplete() default true;

}
