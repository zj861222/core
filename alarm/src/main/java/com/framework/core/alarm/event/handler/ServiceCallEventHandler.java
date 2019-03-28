package com.framework.core.alarm.event.handler;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.framework.core.alarm.event.ServiceCallEvent;



/**
 *   service access event：所有的服务访问事件
 *
 * Created by zhangjun.
 */
public class ServiceCallEventHandler extends AbstractEventHandler<ServiceCallEvent> {

    /**
     * 设置参数
     */
    @Override
    protected  void addArgs(ServiceCallEvent event, Map<String, Object> args){


        //堆栈
        if(event.getException()!=null) {
        	args.put("stack",  ExceptionUtils.getStackTrace(event.getException()));
        }

        //是否成功
    	args.put("status",  event.getStatus());
    	
    	//消耗时间
    	args.put("cost",  event.getCost());
    	
    	//设置的超时时间
    	args.put("conf_timeout",  event.getTimeout());


    	/**
    	 * 附加参数
    	 */
    	if(StringUtils.isNotBlank(event.getAdditionInfo())) {
    	    args.put("s_addition",  event.getAdditionInfo());
    	}


    }


    /**
     * 设置tags
     */
    @Override
    protected  void addTags(ServiceCallEvent event, Map<String, String> tags){
    	
        tags.put("caller_src",  event.getCallSource());
        
        tags.put("module",  event.getModuleName());
        
        //context这里不需要
        tags.remove("context");
        
        
        String  url = tags.get("event");
        
        //url不为null ,
        if(StringUtils.isNotBlank(url) && url.startsWith("http")) {
        	tags.put("event", getDomainFromGetUrl(url));
        }
  
    }

    @Override
    protected String getDatabase() {
        return SERVER_EVENT;
    }

    @Override
    String getCatalog(ServiceCallEvent event) {
        return "service_call";
    }
    
    
    
    
	/**
	 * GET请求 去掉参数
	 * @param getUrl
	 * @return
	 */
	private static String getDomainFromGetUrl(String getUrl){
		
		int idx = getUrl.indexOf("?");
		
		if(idx > -1) {
			getUrl = getUrl.substring(0, idx);
		}
		
		return getUrl;
	}
}
