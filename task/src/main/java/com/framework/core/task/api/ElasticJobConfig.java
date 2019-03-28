package com.framework.core.task.api;

import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.internal.reg.RegistryCenterFactory;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.framework.core.task.internel.utils.PropertiesUtil;
import com.google.common.base.Optional;

/**
 * 
 * @author zhangjun
 *
 */
public class ElasticJobConfig
{

	private static final String NAME_SPACE_KEY = "elasticjob.namespace";

	private static final String ZK_ADDR_KEY = "zkAddress";

	private static final String nameSpace = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, NAME_SPACE_KEY,
			"waiqin365/jobcenter");

	private static String zkAddr = null;

	private static CoordinatorRegistryCenter coordinatorRegistryCenter;

	private static JobOperateAPI jobOperateAPI;

	private static ShardingOperateAPI shardingOperateAPI;

	/**
	 * 获取name space
	 * @return
	 */
	public static String getNameSpace()
	{
		return nameSpace;
	}

	/**
	 * zk地址
	 * @return
	 */
	public static String getZkAddr()
	{

		if (zkAddr == null)
		{
			zkAddr = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, ZK_ADDR_KEY, "127.0.0.1:2181");
		}
		return zkAddr;
	}

	/**
	 * 获取CoordinatorRegistryCenter
	 * @return
	 */
	public static CoordinatorRegistryCenter getCoordinatorRegistryCenter()
	{

		if (coordinatorRegistryCenter == null)
		{

			initCoordinatorRegistryCenter();
		}

		return coordinatorRegistryCenter;

	}

	private static synchronized void initCoordinatorRegistryCenter()
	{

		if (coordinatorRegistryCenter == null)
		{

			coordinatorRegistryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter(getZkAddr(),
					getNameSpace(), Optional.fromNullable("root"));
		}

	}

	/**
	 * 获取JobOperateAPI
	 * @return
	 */
	public static JobOperateAPI getJobOperateAPI()
	{

		if (null == jobOperateAPI)
		{

			initJobOperateAPI();
		}

		return jobOperateAPI;
	}

	private static synchronized void initJobOperateAPI()
	{
		if (null == jobOperateAPI)
		{
			jobOperateAPI = JobAPIFactory.createJobOperateAPI(getZkAddr(), getNameSpace(),
					Optional.fromNullable("root"));
		}
	}

	/**
	 * 获取ShardingOperateAPI 
	 * @return
	 */
	public static ShardingOperateAPI getShardingOperateAPI()
	{

		if (shardingOperateAPI == null)
		{

			initShardingOperateAPI();

		}

		return shardingOperateAPI;

	}

	private static synchronized void initShardingOperateAPI()
	{

		if (null == shardingOperateAPI)
		{

			shardingOperateAPI = JobAPIFactory.createShardingOperateAPI(getZkAddr(), getNameSpace(),
					Optional.fromNullable("root"));
		}
	}

}