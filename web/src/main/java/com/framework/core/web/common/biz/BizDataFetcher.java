



package com.framework.core.web.common.biz;

/**
 *  BizDataFetcher的接口，由业务实现,获取业务数据
 *  
 *  
 *  
 * @author zhangjun
 *
 */
public interface BizDataFetcher {
	
	/**
	 * 获取当前的tenantid
	 * @return
	 */
	public Long getCurTenantId();
	
	
	
	/**
	 * 获取当前登录用户id
	 * @return
	 */
	public Long getCurrentLoginUserId();
	
}