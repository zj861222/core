package com.framework.core.web.hystrix.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * hystrix dao层的配置，控制是否用hystrix控制，以及控制超时时间
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HystrixDaoConfig {

    /**
     * 是否使用 hystrix 管理
     * 
     * @return
     */
    boolean useHystrix() default true ;

    /**
     * 指定 这个dao操作的超时时间
     * 
     * @return
     */
    int timeout() default 1000;

}
