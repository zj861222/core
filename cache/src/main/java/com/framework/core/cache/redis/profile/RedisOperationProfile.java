package com.framework.core.cache.redis.profile;


import com.alibaba.fastjson.JSON;
import com.framework.core.common.utils.StackTraceHelper;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;

/**
 * 
 * @author zhangjun
 * 
 *
 */
public class RedisOperationProfile implements MethodInterceptor,InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RedisOperationProfile.class);

    //redis超过50ms打印出来
    private static final long threshold = 50 ;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        String methodName = methodInvocation.getMethod().getName();
        String className = methodInvocation.getMethod().getDeclaringClass().getName();

        long begin = (new Date()).getTime();

        try{
            return methodInvocation.proceed();
        }
        finally {
            /**
             * 统计延时,redis操作超过50ms打印日志
             */
            long end = (new Date()).getTime();
            if( (end - begin) > threshold ){

                String arguments = StackTraceHelper.getArguments(methodInvocation.getArguments());

                RedisStat stat = new RedisStat(className,methodName,arguments,(end - begin), begin,end );

                logger.warn("{}", JSON.toJSON(stat));
            }
        }

    }
}

class RedisStat{
    long delay ;
    String methodName ;
    String className ;
    String arguments ;
    long begin;
    long end ;

    RedisStat(String className,String methodName,String arguments,long delay,long begin,long end){
        this.className = className;
        this.methodName = methodName ;
        this.delay = delay ;
        this.arguments = arguments ;
        this.begin = begin ;
        this.end = end ;
    }

}
