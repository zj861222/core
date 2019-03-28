
package com.framework.core.search.datasync;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.model.IndexData;



/**
 * 数据同步执行器.
 * 
 * @author zhangjun
 *
 */
public class DataSyncExecutor
{

	private static final Logger log = LoggerFactory.getLogger(DataSyncExecutor.class);

	/**
	 * 索引名
	 */
	private final String indexName;

	/**
	 * 线程跑出异常，是否丢回Queue重新处理
	 */
	private boolean reProcessFailedJob = false;

	private DataSyncCallBack<IndexData> callBack;

	private IndexClient indexClient;

	// 线程并发控制器
	private AtomicInteger counter = new AtomicInteger(0);

	private BlockingQueue<DataSyncJob> blockingQueue = new LinkedBlockingQueue<>();

	/**
	 * 是否含有执行失败的线程
	 */
	private AtomicBoolean hasFail = new AtomicBoolean(false);

	private long checkInterval = 500L;

	public DataSyncExecutor(String indexName, IndexClient indexClient, DataSyncCallBack<IndexData> callBack,
			boolean reProcessFailedJob)
	{

		this.indexName = indexName;

		this.callBack = callBack;

		this.indexClient = indexClient;

		this.reProcessFailedJob = reProcessFailedJob;

	}

	public DataSyncExecutor(String indexName, IndexClient indexClient, DataSyncCallBack<IndexData> callBack,
			long checkInterval, boolean reProcessFailedJob)
	{

		this.indexName = indexName;

		this.callBack = callBack;

		this.indexClient = indexClient;

		this.checkInterval = checkInterval;

		this.reProcessFailedJob = reProcessFailedJob;
	}

	public boolean execute()
	{

		long begin = System.currentTimeMillis();

		try
		{

			return executeDataSync();

		}
		catch (Exception e)
		{

			log.info("********************************************************8");
			log.info("DataSyncExecutor: indexName=" + indexName + " exec failed end!! cost time :"
					+ (System.currentTimeMillis() - begin), e);
			log.info("********************************************************8");

			return false;
		}

	}

	/**
	 * 初始化blockqueue，，返回最大线程数
	 * @return
	 */
	private int initQueueAndGetThreadPoolCount()
	{

		// 获取总的记录数，设置每页取1条，因为无需拿数据
		long totalCount = callBack.count();

		final long limit = callBack.getPageSize();

		int totalPageNum = (int) ((totalCount - 1) / limit + 1); // 总页数

		// 线程数量
		int threadCount = getMaxThreadCount(); // 执行同步的最大线程数量
		// 最大线程数大于总页数，则需要调整最大线程数
		if (threadCount > totalPageNum)
		{
			threadCount = totalPageNum;
		}

		List<DataSyncJob> jobList = scheduleAllJobs(indexName, totalPageNum, callBack.getPageSize());

		Assert.isTrue(CollectionUtils.isNotEmpty(jobList));

		for (DataSyncJob job : jobList)
		{

			blockingQueue.offer(job);

		}

		return threadCount;
	}

	private boolean executeDataSync()
	{

		long begin = System.currentTimeMillis();

		int threadCount = initQueueAndGetThreadPoolCount();

		ExecutorService service = Executors.newFixedThreadPool(threadCount + 1);

		while (!blockingQueue.isEmpty())
		{

			// 如果有执行失败的，那么推出循环
			if (hasFail.get())
			{
				break;
			}

			// 分发job
			boolean success = dispatchJob(service, threadCount);

			// 如果上1个请求没分发出去，那么sleep 3s，因为这个时候线程跑满了
			if (!success)
			{

				try
				{
					Thread.sleep(checkInterval);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		// 等待结束
		waitForThreadEnd();

		if (!hasFail.get())
		{

			log.info("********************************************************");
			log.info("DataSyncExecutor: indexName=" + indexName + " exec success end!! cost time :"
					+ (System.currentTimeMillis() - begin));
			log.info("********************************************************");

			return true;
		}
		else
		{
			log.info("********************************************************");
			log.info("DataSyncExecutor: indexName=" + indexName
					+ " exec failed end (thread exec exception)!! cost time :" + (System.currentTimeMillis() - begin));
			log.info("********************************************************");

			return false;
		}

	}

	/**
	 * 等待结束
	 */
	private void waitForThreadEnd()
	{

		while (true)
		{

			if (counter.get() == 0)
			{
				break;
			}

			try
			{
				Thread.sleep(500L);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 分发job
	 * @param service
	 * @param stack
	 * @param threadCount
	 * @return
	 */
	private boolean dispatchJob(ExecutorService service, int threadCount)
	{

		if (counter.get() >= threadCount)
		{
			return false;

		}

		scheduleJobExec(blockingQueue.poll(), service);

		return true;
	}

	/**
	 * 投入到线程池执行
	 * 
	 * @param job
	 */
	private void scheduleJobExec(DataSyncJob job, ExecutorService service)
	{

		DataSyncWorkThread t = new DataSyncWorkThread(indexName, job, callBack, new WorkThreadResultCallBack()
		{

			@Override
			public void notify(String name, DataSyncJob job, boolean isSuccess, Exception e)
			{
				// 失败了，但是设置了可以重试
				if (!isSuccess && reProcessFailedJob)
				{

					blockingQueue.offer(job);

					// 失败了，不允许重试，直接抛出异常！
				}
				else if (!isSuccess)
				{

					// 设置为失败
					hasFail.set(true);

					log.error("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
					log.error("DataSyncExecutor [indexname]:" + name + " will failed to execute soon!!!!!!!", e);
					log.error("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

				}

				counter.decrementAndGet();

			}
		}, indexClient);

		counter.incrementAndGet();

		service.execute(t);

	}

	/**
	 * 
	 *  拆分job
	 * @param indexName
	 * @param totalPageNum
	 * @param pageSize
	 * @return
	 */
	private List<DataSyncJob> scheduleAllJobs(String indexName, int totalPageNum, long pageSize)
	{

		Assert.isTrue(totalPageNum > 0);

		List<DataSyncJob> jobs = new ArrayList<>();

		for (int i = 1; i <= totalPageNum; i++)
		{
			jobs.add(new DataSyncJob(indexName, i, Integer.parseInt(pageSize + ""), 0));
		}

		return jobs;
	}

	/**
	 * 获取最大并发线程限制
	 * @return
	 */
	private int getMaxThreadCount()
	{

		String maxThreadLimit = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "search.index.batch.max.thread.size", "10");
		
		Assert.notNull(maxThreadLimit);
		
		int threadCount = Integer.parseInt(maxThreadLimit); // 执行同步的最大线程数量

		return threadCount;
	}

}
