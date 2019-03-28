package com.framework.core.task.internel.template;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.framework.core.task.internel.model.TaskExecResult;

/**
 * 回调
 * @author zhangjun
 *
 */
public interface  TenantTaskThreadCallBack {
	
	/**
	 * 回调方法
	 * @param context
	 * @param data
	 * @param isSuccess
	 * @param e
	 */
	 void callback(ShardingContext context, TaskExecResult data,boolean isSuccess,Exception e);
	
	
}