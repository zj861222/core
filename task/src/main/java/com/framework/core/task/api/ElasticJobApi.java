package com.framework.core.task.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.dangdang.ddframe.job.lite.internal.sharding.ExecutionService;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.google.common.base.Optional;

/**
 * 
 * @author zhangjun
 *
 */
public class ElasticJobApi
{

	private static Map<String, ExecutionService> executionServiceMap = new HashMap<>();

	/**
	 * 判断是否还有执行中的作业.
	 */
	public static boolean hasRunningItems(String jobName)
	{

		ExecutionService service = getExecutionService(jobName);

		Assert.notNull(service);

		return service.hasRunningItems();

	}

	private static ExecutionService getExecutionService(String jobName)
	{

		ExecutionService service = executionServiceMap.get(jobName);

		if (service == null)
		{
			service = new ExecutionService(ElasticJobConfig.getCoordinatorRegistryCenter(), jobName);
			executionServiceMap.put(jobName, service);
		}

		return service;

	}

	/**
	 * 作业禁用.
	 * 
	 * <p>会重新分片.</p>
	 *
	 * @param jobName 作业名称
	 * @param serverIp 作业服务器IP地址
	 */
	public static void disable(Optional<String> jobName, Optional<String> serverIp)
	{

		JobOperateAPI jobOperateAPI = ElasticJobConfig.getJobOperateAPI();

		Assert.notNull(jobOperateAPI);

		jobOperateAPI.disable(jobName, serverIp);
	}

	/**
	 * 作业启用.
	 *
	 * @param jobName 作业名称
	 * @param serverIp 作业服务器IP地址
	 */
	public static void enable(Optional<String> jobName, Optional<String> serverIp)
	{

		JobOperateAPI jobOperateAPI = ElasticJobConfig.getJobOperateAPI();

		Assert.notNull(jobOperateAPI);

		jobOperateAPI.enable(jobName, serverIp);

	}

	/**
	 * 作业关闭.
	 *
	 * @param jobName 作业名称
	 * @param serverIp 作业服务器IP地址
	 */
	public static void shutdown(Optional<String> jobName, Optional<String> serverIp)
	{

		JobOperateAPI jobOperateAPI = ElasticJobConfig.getJobOperateAPI();

		Assert.notNull(jobOperateAPI);

		jobOperateAPI.shutdown(jobName, serverIp);

	}

	/**
	 * 作业删除.
	 * 
	 * @param jobName 作业名称
	 * @param serverIp 作业服务器IP地址
	 */
	public static void remove(Optional<String> jobName, Optional<String> serverIp)
	{

		JobOperateAPI jobOperateAPI = ElasticJobConfig.getJobOperateAPI();

		Assert.notNull(jobOperateAPI);

		jobOperateAPI.remove(jobName, serverIp);
	}

}