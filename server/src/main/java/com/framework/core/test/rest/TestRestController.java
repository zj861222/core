package com.framework.core.test.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dangdang.ddframe.job.lite.internal.sharding.ExecutionService;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.internal.reg.RegistryCenterFactory;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.framework.core.cache.redis.lock.RedisComplexLock;
import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.rest.client.GlobalServiceCaller;
import com.framework.core.test.service.CustomizeElasticSearchRepository;
import com.framework.core.test.service.TestService;
import com.framework.core.view.ApiResponse;
import com.framework.core.web.common.biz.BizDataFetcher;
import com.google.common.base.Optional;

@Controller
public class TestRestController
{

	private Object lock = new Object();

	@Resource
	private GlobalServiceCaller globalServiceCaller;

	@Resource
	private BizDataFetcher bizDataFetcher;

	@Resource
	RedisComplexLock redisComplexLock;

	@Resource
	private CustomizeElasticSearchRepository customizeElasticSearchRepository;

//	@Resource
//	private IndexLoader indexLoader;

	@Resource
	private TestService testService;

	@RequestMapping(value = "testRest")
	@ResponseBody
	public String testRest(int index)
	{

		try
		{

			Map<String, String> map = new HashMap<>();

			map.put("test1", "test2");

			String response = globalServiceCaller.get("test", "http://127.0.0.1:8080/testRest2.action?index=" + index,
					map, String.class, null, 2, true);

			return response;

		}
		catch (Exception e)
		{
			System.out.println("error:" + e.getMessage() + ",Threadid:" + Thread.currentThread().getId());

			return e.getMessage();
		}
	}

	@RequestMapping(value = "testRest2")
	@ResponseBody
	public String testRest2(int index, HttpServletRequest request)
	{

		String header = request.getHeader("test1");

		System.out.println("time=" + System.currentTimeMillis() + "----------enter testRest2-----------index=" + index
				+ ",header=" + header);

		bizDataFetcher.getCurrentLoginUserId();

		if (index > 10 && index < 30)
		{

			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return index + ",testRest22222222222222222";
		}
		else
		{
			return index + ",testRest2222";

		}
	}

	@RequestMapping(value = "testRest3")
	@ResponseBody
	public String testRest3()
	{

		ExecutorService pool = Executors.newFixedThreadPool(5);

		int count = 0;
		while (true)
		{

			if (count > 200)
			{
				break;
			}

			for (int i = 0; i < 10; i++)
			{

				count++;

				final int index = count;
				pool.submit(new Runnable()
				{

					@Override
					public void run()
					{
						try
						{

							System.out.println(
									"time=" + System.currentTimeMillis() + "*******index=" + index + ",***********");

							String response = testRest(index);

							System.out.println("time=" + System.currentTimeMillis() + "*******index=" + index
									+ ",****response =*******" + response);

						}
						finally
						{
						}

					}
				});
			}

			try
			{
				if (count > 100)
				{
					Thread.sleep(3000);
				}
				else
				{
					Thread.sleep(1000);

				}
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return "success";

	}

	@RequestMapping(value = "testRest4")
	@ResponseBody
	public String testRest4()
	{

		String token = redisComplexLock.tryLock("test", 3, TimeUnit.MINUTES);

		System.out.println("token" + token);

		if (StringUtils.isEmpty(token))
		{

			boolean isSuccess = redisComplexLock.releaseLock("test", "0cdd02f7b9380ec5465305d8074467d1");
			System.out.println("isSuccess:" + isSuccess);

		}

		// testService.test();

		return "success";
	}

	@RequestMapping(value = "testSearch")
	@ResponseBody
	public String testSearch()
	{

//		try
//		{
//
//			customizeElasticSearchRepository.createIndex(Order.class);
//
//			indexLoader.createIndex("brand", true);
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
		return "111";

	}

	

}
