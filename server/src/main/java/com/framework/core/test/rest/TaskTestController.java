package com.framework.core.test.rest;

import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.Resource;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.sharding.ExecutionService;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.internal.reg.RegistryCenterFactory;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.framework.core.cache.redis.idgenerator.RedisIdGenerator;
import com.framework.core.cache.redis.message.publish.RedisMessagePublisher;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.task.api.ElasticJobApi;
import com.framework.core.task.internel.exception.MasterException;
import com.framework.core.task.internel.template.inner.TenantIdFetcher;
import com.framework.core.test.task.elasticjob.dataflow.TssJob;
import com.google.common.base.Optional;

@Controller
public class TaskTestController
{

	@Resource
	private TenantIdFetcher tenantIdFetcher;

	@Resource
	private TssJob tssJob;

	@Resource
	private RedisIdGenerator redisIdGenerator;

	@Resource
	private RedisMessagePublisher redisMessagePublisher;

	@RequestMapping(value = "testDisable")
	@ResponseBody
	public String testDisable(int flag)
	{

		String zkAddr = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "zkAddress");

		if (StringUtils.isEmpty(zkAddr))
		{

			System.out.println("zkAddr config can not be null!!!");
		}

		ShardingOperateAPI api = JobAPIFactory.createShardingOperateAPI(zkAddr, "waiqin365/jobcenter",
				Optional.fromNullable("root"));

		JobOperateAPI opApi = JobAPIFactory.createJobOperateAPI(zkAddr, "waiqin365/jobcenter",
				Optional.fromNullable("root"));

		String ip = IpUtils.getIp();

		// api.enable("Test1", "0");
		//
		// api.enable("Test1", "1");

		// opApi.disable(Optional.<String>absent(),
		// Optional.of("172.31.0.137"));

		if (flag == 1)
		{

			ElasticJobApi.enable(Optional.<String> absent(), Optional.of(ip));
			// opApi.enable(Optional.<String> absent(), Optional.of(ip));
		}
		else
		{
			ElasticJobApi.disable(Optional.<String> absent(), Optional.of(ip));
		}

		return "success";
		//
		//
		// @POST
		// @Path("/{jobName}/sharding/{item}/disable")
		// @Consumes(MediaType.APPLICATION_JSON)
		// public void disableSharding(@PathParam("jobName") final String
		// jobName, @PathParam("item") final String item) {
		// jobAPIService.getShardingOperateAPI().disable(jobName, item);
		// }
		//
		// @DELETE
		// @Path("/{jobName}/sharding/{item}/disable")
		// @Consumes(MediaType.APPLICATION_JSON)
		// public void enableSharding(@PathParam("jobName") final String
		// jobName, @PathParam("item") final String item) {
		// jobAPIService.getShardingOperateAPI().enable(jobName, item);
		// }
		//

		// jobAPIService.getJobOperatorAPI().disable(Optional.<String>absent(),
		// Optional.of(serverIp));

	}

	@RequestMapping(value = "testCheckIsRunning")
	@ResponseBody
	public String testCheckIsRunning()
	{

		String zkStr = "172.31.3.252:2181,172.31.3.252:2182,172.31.3.252:2183";

		CoordinatorRegistryCenter center = RegistryCenterFactory.createCoordinatorRegistryCenter(zkStr,
				"waiqin365/jobcenter", Optional.fromNullable("root"));

		ExecutionService service = new ExecutionService(center, "esJobTest1");

		int count = 0;
		while (true)
		{
			if (service != null)
			{
				boolean hasRunning = service.hasRunningItems();

				System.out.println("hasRunning:" + hasRunning);
			}

			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			count++;

			if (count == 200)
			{
				break;
			}

		}

		return "success";

	}

	@RequestMapping(value = "fetchWaitExecutTenantIds")
	@ResponseBody
	public String fetchWaitExecutTenantIds()
	{

		try
		{
			Pair<Boolean, List<Long>> pair = tenantIdFetcher.fetchWaitExecutTenantIds("notifyCountJob", 1000, 1);

			System.out.println("pair=" + JSON.toJSONString(pair));
		}
		catch (MasterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";

	}

	@RequestMapping(value = "sheduleJob")
	@ResponseBody
	public String sheduleJob()
	{

		String zkStr = "172.31.3.252:2181,172.31.3.252:2182,172.31.3.252:2183";

		CoordinatorRegistryCenter center = RegistryCenterFactory.createCoordinatorRegistryCenter(zkStr,
				"waiqin365/jobcenter", Optional.fromNullable("root"));

		String defaultShardPrams = "0=A,1=B,2=C";

		LiteJobConfiguration liteJobConfiguration = getLiteJobConfiguration(tssJob.getClass(), "0 0/2 * * * ?", 10,
				defaultShardPrams);

		JobScheduler jobScheduler = new SpringJobScheduler(tssJob, center, liteJobConfiguration);

		jobScheduler.init();

		return "success";

	}

	private LiteJobConfiguration getLiteJobConfiguration(final Class<? extends DataflowJob> jobClass, final String cron,
			final int defaultShardTotal, final String defaultShardPrams)
	{

		// LiteJobConfiguration.newBuilder(new
		// SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
		// jobClass.getName(), cron,
		// defaultShardTotal).shardingItemParameters(defaultShardPrams).build(),
		// jobClass.getCanonicalName())).overwrite(true).build();
		//

		return LiteJobConfiguration
				.newBuilder(
						new DataflowJobConfiguration(
								JobCoreConfiguration.newBuilder("tss2", cron, defaultShardTotal)
										.shardingItemParameters(defaultShardPrams).build(),
								jobClass.getCanonicalName(), true))
				.overwrite(true).build();
	}

	@RequestMapping(value = "sendRedisMessage")
	@ResponseBody
	public String sendRedisMessage()
	{

		redisMessagePublisher.sendMessage("tp:test:1", "222");

		return "success";

	}

	@RequestMapping(value = "generate19BitsIdFromRedis")
	@ResponseBody
	public String generate19BitsIdFromRedis()
	{

		try
		{

			for (int i = 0; i < 100000; i++)
			{

				String ports = redisIdGenerator.generate19BitsIdFromRedis("test1");

				System.out.println("id:" + ports);
			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";

	}

	@RequestMapping(value = "testport")
	@ResponseBody
	public String testport()
	{

		try
		{

			for (int i = 0; i < 100000; i++)
			{

				String ports = redisIdGenerator.generate19BitsIdFromLocal("test1");

				System.out.println("id:" + ports);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return "success";

	}

}