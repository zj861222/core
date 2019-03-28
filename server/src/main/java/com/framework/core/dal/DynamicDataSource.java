//package com.framework.core.dal;
//
//
//import com.framework.core.dal.datasource.AbstractDynamicDataSource;
//import com.framework.core.web.session.filter.SessionFilter;
//
//public class DynamicDataSource extends AbstractDynamicDataSource {
//
//
//	/** 用于存放当前线程的企业信息 适用于没有request对象的后台程序切换数据源 */
//	private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<Long>();
//	
//	//决定当前的sessionid 是多少
//	@Override
//	protected Object determineCurrentLookupKey() {
//		
//		// 获取企业ID
//		Long tenantId = (Long) SessionFilter.getRequest().getAttribute("tenant_id");
//		if (tenantId == null && currentTenantId != null)
//		{
//			tenantId = currentTenantId.get();
//		}
//		else if (currentTenantId.get() == null || currentTenantId.get() < 0)
//		{
//			currentTenantId.set(tenantId);
//		}
//		
//		
//		return tenantId;
//	}
//	
//	
//	
//	
//}