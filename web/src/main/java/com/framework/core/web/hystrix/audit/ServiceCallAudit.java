package com.framework.core.web.hystrix.audit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.framework.core.alarm.event.ServiceCallEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务调用统计,
 *
 * @author 
 */
public class ServiceCallAudit implements ApplicationEventPublisherAware{

    private ApplicationEventPublisher publisher;
    

    /**
     * 成功调用
     *
     * @param serviceName
     */
    public void audit(String serviceName,  String path) {
//        publisher.publishEvent(new ServiceCallEvent(serviceName,serviceName, path,  null));
    }


    /**
     * 调用失败
     *
     * @param serviceName 服务名称
     * @param e
     */
    public void auditFailed(String serviceName,  String path,Throwable e) {
//        publisher.publishEvent(new ServiceCallEvent(serviceName,serviceName, path,  e));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }
}
