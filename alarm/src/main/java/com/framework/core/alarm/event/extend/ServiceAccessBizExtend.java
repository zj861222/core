package com.framework.core.alarm.event.extend;

import javax.servlet.http.HttpServletRequest;

import com.framework.core.alarm.event.ServiceAccessEvent;

/**
 * service access 扩展字段设置值，这个是为了与 业务添加的字段隔离
 * 
 * @author zhangjun
 *
 */
public interface ServiceAccessBizExtend {
		
	/**
	 * 设置 事件的业务扩展属性
	 * @param event
	 * @param request
	 */
	void setBizExtendAttrbute(ServiceAccessEvent event,HttpServletRequest request);
	
}