package com.framework.core.task.internel.template;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.framework.core.task.internel.model.TaskExecResult;

public class TenantTaskThread implements Runnable
{

	private ShardingContext shardingContext;

	private TaskExecResult data;

	private TenantTaskThreadCallBack tenantTaskThreadCallBack;

	private BaseTenantTemplate baseTenantTemplate;

	/**
	 * 
	 * @param context
	 * @param data
	 */
	public TenantTaskThread(ShardingContext context, TaskExecResult data, TenantTaskThreadCallBack callback,
			BaseTenantTemplate template)
	{

		this.data = data;

		this.shardingContext = context;

		this.tenantTaskThreadCallBack = callback;

		this.baseTenantTemplate = template;
	}

	@Override
	public void run()
	{

		try
		{
			boolean isSuccess = baseTenantTemplate.processDataInternal(shardingContext, data);

			tenantTaskThreadCallBack.callback(shardingContext, data, isSuccess, null);

		}
		catch (Exception e)
		{
			tenantTaskThreadCallBack.callback(shardingContext, data, false, e);

		}

	}



}