package com.framework.core.alarm.event.handler;


import java.util.Map;

import com.framework.core.alarm.event.ServiceAccessEvent;



/**
 *   service access event：所有的服务访问事件
 *
 * Created by zhangjun.
 */
public class ServiceAccessEventHandler extends AbstractEventHandler<ServiceAccessEvent> {

    /**
     * 设置参数
     */
    @Override
    protected  void addArgs(ServiceAccessEvent event, Map<String, Object> args){

        if(event.getStack() != null){
            args.put("stack", event.getStack());
        }

        args.put("cost", event.getCost());
    }


    /**
     * 设置tags
     */
    @Override
    protected  void addTags(ServiceAccessEvent event, Map<String, String> tags){
        tags.put("status_code", String.valueOf(event.getResponse_code()));
        tags.put("src_service",  event.getSrcService());
        //设置标签，标志这个请求是否属于执行慢的请求
        tags.put("is_slow", event.getCost()>500?"1":"0");

    }

    @Override
    protected String getDatabase() {
        return SERVER_EVENT;
    }

    @Override
    String getCatalog(ServiceAccessEvent event) {
        return "service_access";
    }
}
