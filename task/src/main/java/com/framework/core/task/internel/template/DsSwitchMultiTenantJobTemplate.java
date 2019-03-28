package com.framework.core.task.internel.template;


import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.framework.core.task.internel.model.TaskExecResult;
import com.framework.core.task.internel.template.MultiTenantJobTemplate;


@Component
public abstract class DsSwitchMultiTenantJobTemplate extends MultiTenantJobTemplate {


	@Override
	protected boolean doProcessData(ShardingContext context, TaskExecResult record) {
		
		try {
			//切换数据源
			doBeforePcocess(context, record);
			
			return switchAndDoProcessData(context, record);
			
		} finally {
			//删掉数据源信息
			doAfterPcocess(context, record);
		}
	}
	
	
	/**
	 * 处理企业数据之前
	 * @param context
	 * @param record
	 */
	public abstract void doBeforePcocess(ShardingContext context, TaskExecResult record);
	
	
	
	/**
	 * 企业数据处理之后
	 * @param context
	 * @param record
	 */
	public abstract void doAfterPcocess(ShardingContext context, TaskExecResult record);
	
	
	/**
	 * 切换到企业的数据源，处理企业的job
	 * @param context
	 * @param record
	 * @return
	 */
	abstract protected boolean switchAndDoProcessData(ShardingContext context, TaskExecResult record);
	
}