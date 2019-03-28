package com.framework.core.task.internel.template;

import org.springframework.stereotype.Component;

/**
 * 
 * @author zhangjun
 *
 */

@Component
public  abstract class  MultiTenantJobTemplate extends BaseTenantTemplate {


	/**
	 * 并发企业数限制
	 * 
	 * @return
	 */
	@Override
	public int getConcurrentLimit() {
		
		int num = getConcurrentTenantNum();
		
		return num>1?num:super.getConcurrentLimit();
	}
	
	/**
	 * 并发的企业数
	 * @return
	 */
	public abstract int getConcurrentTenantNum();
	
}