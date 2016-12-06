package com.framework.core.web.common.biz;

/**
 *  dao层选择tenantid的接口，由业务实现
 *  
 * @author zhangjun
 *
 */
public interface TenantIdDetermin4Dao {
	
	/**
	 * 获取当前的tenantid
	 * @return
	 */
	public  Long getCurTenantId();
	
}