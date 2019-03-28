package com.framework.core.web.alarm.extend;


import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.framework.core.alarm.event.ServiceAccessEvent;
import com.framework.core.alarm.event.extend.ServiceAccessBizExtend;
import com.framework.core.web.common.biz.BizDataFetcher;


/**
 * service access 事件的业务扩展属性设置
 * 
 * @author zhangjun
 *
 */
public class ServiceAccessBizExtendHandler implements ServiceAccessBizExtend ,ApplicationContextAware {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	
	private BizDataFetcher bizDataFetcher;

	public BizDataFetcher getBizDataFetcher() {
		return bizDataFetcher;
	}

	public void setBizDataFetcher(BizDataFetcher bizDataFetcher) {
		this.bizDataFetcher = bizDataFetcher;
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		Map<String, BizDataFetcher> list = applicationContext.getBeansOfType(BizDataFetcher.class);

		//判断是否为空
		if(MapUtils.isEmpty(list) || list.size()>1) {
			
			logger.warn("使用ServiceAccessBizExtendHandler请先实现BizDataFetcher接口(注册为bean,接口只实现一次)!");
			
			return;
		}

		for(Entry<String,BizDataFetcher> entry:list.entrySet()) {
			bizDataFetcher = entry.getValue();
		}		
	}

	@Override
	public void setBizExtendAttrbute(ServiceAccessEvent event,HttpServletRequest request) {
		
		setTenantId(event);
		
		setLoginUserId(event);
	}
	
	/**
	 * 获取tenantid
	 * @param event
	 */
	private void setTenantId(ServiceAccessEvent event) {
		
		if(bizDataFetcher == null) {
			return;
		}
		
		Long tenantId = bizDataFetcher.getCurTenantId();
		
		//设置当前的企业id
		if(tenantId != null) {
			event.setTenantId(tenantId);
		}
		
	}
	
	/**
	 * 设置当前登录的用户
	 * 
	 * @param event
	 */
	private void setLoginUserId(ServiceAccessEvent event) {
		
		if(bizDataFetcher == null) {
			return;
		}
		
		Long userId = bizDataFetcher.getCurrentLoginUserId();
		
		if(userId != null) {
			event.setUserId(userId);
		}
		
	}
	
}