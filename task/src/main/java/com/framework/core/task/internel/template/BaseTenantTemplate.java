package com.framework.core.task.internel.template;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.event.ElasticJobFailedEvent;
import com.framework.core.cache.redis.lock.RedisComplexLock;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.error.exception.BizException;
import com.framework.core.task.elasticjob.template.AbstractDataFlowJobTemplate;
import com.framework.core.task.internel.cache.RedisKeyBuilder;
import com.framework.core.task.internel.dao.TaskExecResultDAO;
import com.framework.core.task.internel.dao.TaskScheduleDAO;
import com.framework.core.task.internel.exception.JobErrorCode;
import com.framework.core.task.internel.exception.MasterException;
import com.framework.core.task.internel.model.TaskExecResult;
import com.framework.core.task.internel.model.TaskSchedule;
import com.framework.core.task.internel.template.inner.JobThreadLocal;
import com.framework.core.task.internel.template.inner.TenantExecutePool;
import com.framework.core.task.internel.template.inner.TenantIdFetcher;

@Component
public abstract class BaseTenantTemplate extends AbstractDataFlowJobTemplate<TaskExecResult>
{

	private static Logger logger = LoggerFactory.getLogger(BaseTenantTemplate.class);
	/**
	 * 查询分片一次查20个
	 */
	private static final int SHARDING_BATCH_LIMIT = 20;

	/**
	 * 一批1000个
	 */
	private static final int batchSize = 1000;

	/**
	 * 并发限制
	 */
	private static final int DEFAULT_CONCURRENT_LIMIT = 10;

	/**
	 * 2分钟的时间
	 */
	private static final int TWO_MINUTES = 2 * 60 * 1000;

	/**
	 * 执行失败次数限制
	 */
	private static final int EXEC_FAIL_TIMES_LIMIT = 3;

	@Resource
	private RedisComplexLock redisComplexLock;

	@Resource
	private RedisHelper redisHelper;

	@Resource
	private TaskScheduleDAO taskScheduleDAO;

	@Resource
	private TaskExecResultDAO taskExecResultDAO;

	@Resource
	private TenantIdFetcher tenantIdFetcher;

	/**
	 * 
	 */
	private SemaphoreMgrInstance semaphoreMgrInstance;

	@Override
	public List<TaskExecResult> fetchData(ShardingContext shardingContext)
	{

		try
		{
			List<TaskExecResult> result = doRealFetchData(shardingContext);

			// 为null时，本次job结束了，那么清理掉threadlocal
			if (CollectionUtils.isEmpty(result))
			{
				// 清理掉本次的threadlocal
				JobThreadLocal.clearCurrentBatchNoLocal();
			}

			return result;

		}
		catch (Exception e)
		{
			logger.error("FATTLE ERROR! TenantDataFlowTemplate fetchData failed,[shardingContext]:"
					+ JSON.toJSONString(shardingContext), e);

			// 清理掉本次的threadlocal
			JobThreadLocal.clearCurrentBatchNoLocal();

			return null;
		}

	}

	@Override
	public void processData(ShardingContext shardingContext, List<TaskExecResult> dataList)
	{

		logger.info("BaseTenantTemplate  processData begin,[shardingContext]:" + JSON.toJSONString(shardingContext)
				+ ",[dataList]:" + (dataList == null ? "null" : JSON.toJSONString(dataList)));
		try
		{
			determinAndRunning(shardingContext, dataList);
		}
		catch (Exception e)
		{
			logger.error("FATTLE ERROR! TenantDataFlowTemplate processData failed,[shardingContext]:"
					+ JSON.toJSONString(shardingContext) + ",[dataList]:" + JSON.toJSONString(dataList), e);
		}
	}

	/**
	 * 
	 * @param shardingContext
	 * @param dataList
	 */
	private void determinAndRunning(ShardingContext shardingContext, List<TaskExecResult> dataList)
	{

		asyncProcess(shardingContext, dataList);

	}

	@Override
	public void beforeJobExecuted(ShardingContexts shardingContexts)
	{

	}

	@Override
	public void afterJobExecuted(ShardingContexts shardingContexts)
	{

	}

	/**
	 * 打印日志
	 * 
	 * @param shardingContexts
	 * @param message
	 */
	private String printMsg(ShardingContext shardingContext, String message)
	{

		return "[msg]:" + message + ",[jobName]:" + shardingContext.getJobName() + ",[taskId]:"
				+ shardingContext.getTaskId() + ",[jobParameter]:" + shardingContext.getJobParameter() + ",[shardItem]:"
				+ shardingContext.getShardingItem() + ",[time]:" + System.currentTimeMillis();
	}

	/**
	 * 计划job
	 * 
	 * @param jobName
	 * @return batchno
	 */
	private int scheduleJob(ShardingContext shardingContext, String jobName)
	{

		// 分页查询哪些企业要执行job
		int pageNum = 1;

		// 获取当前的批次号,left 是操作是否成功，不成功不需要继续了，说明有别的并发操作了；right是 批次号
		Pair<Boolean, Integer> result = generateCurrentBatchInfo(jobName);

		if (!result.getLeft())
		{

			logger.info(printMsg(shardingContext, "beforeJobExecuted scheduleJob return ,and not init record!"));

			// 获取最新的一条记录
			int batchNo = getCurrentBatchNo(jobName);

			return batchNo;
		}

		// 获取批次号
		int batchNo = result.getRight();

		logger.info(printMsg(shardingContext, "beforeJobExecuted scheduleJob ,get batchNum, batchNum is:" + batchNo));

		List<TaskExecResult> insertList = new ArrayList<TaskExecResult>();

		while (true)
		{

			Pair<Boolean, List<Long>> idsPair = doFetchWaitExecutTenantIds(jobName, getFetchTenantBatchSize(), pageNum);

			// 如果idsPair 为null，或者 idsPair的left为false，表示不需要继续轮训
			if (idsPair == null || !idsPair.getLeft())
			{

				logger.info(printMsg(shardingContext,
						"beforeJobExecuted scheduleJob end, is not need to loop query tenant ,[batchNum]:" + batchNo
								+ ",[TotalPage]" + (pageNum - 1)));
				break;
			}

			// 获取的ids列表
			List<Long> ids = idsPair.getRight();

			pageNum++;

			// 不为空，存入库；为空可能是独立部署的企业，还需要继续轮询
			if (CollectionUtils.isNotEmpty(ids))
			{
				insertList = TaskExecResult.createBatchRecords(jobName, batchNo, ids);
				taskExecResultDAO.insertBatchTaskExecInfo(insertList);
				insertList.clear();
			}

		}

		logger.info(printMsg(shardingContext,
				"beforeJobExecuted scheduleJob success end ,[batchNum]:" + batchNo + ",[TotalPage]" + (pageNum - 1)));

		return batchNo;
	}

	/**
	 * 生成批次信息,并发问题，靠之前的redis分布式锁控制
	 * 
	 * @param jobName
	 * @return
	 */
	private Pair<Boolean, Integer> generateCurrentBatchInfo(String jobName)
	{

		TaskSchedule record = taskScheduleDAO.queryTaskScheduleByJobName(jobName);
		// 初始化为1
		int currentBatchNum = 1;

		if (record == null)
		{
			// 创建第一批数据
			record = TaskSchedule.createTask(jobName, currentBatchNum);

			int isSuccess = taskScheduleDAO.insertTaskSchedule(record);

			return Pair.of(isSuccess > 0, currentBatchNum);

		}
		else
		{

			// 之前的批次号加1
			currentBatchNum = record.getBatchNo() + 1;

			int isSuccess = taskScheduleDAO.updateTaskSchedule(jobName, currentBatchNum);

			return Pair.of(isSuccess > 0, currentBatchNum);

		}
	}

	public int getFetchTenantBatchSize()
	{

		return batchSize;
	}

	/**
	 * 
	 * 
	 * @param jobName
	 * @param batchSize
	 * @param pageNum
	 * @return
	 */
	protected Pair<Boolean, List<Long>> doFetchWaitExecutTenantIds(String jobName, int batchSize, int pageNum)
	{

		boolean isSuccess = false;

		int retryTimes = 0;

		// 如果失败了，会尝试重新获取几次
		while (!isSuccess)
		{

			try
			{

				Pair<Boolean, List<Long>> pair = fetchWaitExecutTenantIds(jobName, batchSize, pageNum);
				isSuccess = true;
				return pair;
			}
			catch (MasterException e)
			{

				if (retryTimes >= 3)
				{
					throw new BizException(JobErrorCode.EX_JOB_CENTER_FATECH_EXECUTE_JOBTENANT_IDS_FAIL.getCode());
				}

				retryTimes++;
			}

		}

		return null;

	}

	/**
	 * 
	 * @param jobName
	 * @param batchSize
	 * @param pageNum
	 * @return Pair<Boolean,List<Long>>  left表示是否需要继续轮训，
	 * @throws MasterException
	 */
	public Pair<Boolean, List<Long>> fetchWaitExecutTenantIds(String jobName, int batchSize, int pageNum)
		throws MasterException
	{
		return tenantIdFetcher.fetchWaitExecutTenantIds(jobName, batchSize, pageNum);

	}

	/**
	 * 根据分片，一批查多少数据
	 * 
	 * @return
	 */
	public int getShardingBatchLimit()
	{
		return SHARDING_BATCH_LIMIT;
	}

	/**
	 * 
	 * @param shardingContext
	 * @return
	 */
	private List<TaskExecResult> doRealFetchData(ShardingContext shardingContext)
	{

		int batchNo = getBatchNoBeforeFetchData(shardingContext);

		logger.info("fetchData begin，[batchNo]" + batchNo + ",[job]:" + shardingContext.getJobName() + ",[sharding]:"
				+ shardingContext.getShardingItem() + ",time="
				+ DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT));

		// 总共的分片
		int totalShardings = shardingContext.getShardingTotalCount();
		// 当前的分片
		int sharding = shardingContext.getShardingItem();

		List<TaskExecResult> list = (List<TaskExecResult>) taskExecResultDAO.queryWaitToExecRecordBySharding(
				shardingContext.getJobName(), batchNo, totalShardings, sharding, getShardingBatchLimit());

		// 取出等待执行的数据
		if (CollectionUtils.isNotEmpty(list))
		{

			logger.info("BaseTenantTemplate  fetchData result,[shardingContext]:" + JSON.toJSONString(shardingContext)
					+ ",[batchNo]:" + batchNo + ",[list]:" + (list == null ? "null" : JSON.toJSONString(list)));

			return list;
		}

		// 对之前执行失败的数据，再给几次机会重新执行
		list = (List<TaskExecResult>) taskExecResultDAO.queryExecFailedRecordBySharding(shardingContext.getJobName(),
				batchNo, totalShardings, sharding, getShardingBatchLimit());

		// 有失败的记录需要 执行的
		if (CollectionUtils.isNotEmpty(list))
		{

			logger.info("BaseTenantTemplate  fetchData result2,[shardingContext]:" + JSON.toJSONString(shardingContext)
					+ ",[batchNo]:" + batchNo + ",[list]:" + (list == null ? "null" : JSON.toJSONString(list)));
			return list;
		}

		List<TaskExecResult> runningRecord = (List<TaskExecResult>) taskExecResultDAO.queryExecRunningRecordBySharding(
				shardingContext.getJobName(), batchNo, totalShardings, sharding, getShardingBatchLimit());
		// 没有需要等待的，这个是最理想情况
		if (CollectionUtils.isEmpty(runningRecord))
		{
			logger.info("BaseTenantTemplate  fetchData result3,[shardingContext]:" + JSON.toJSONString(shardingContext)
					+ ",[batchNo]:" + batchNo + ",[list]:null");

			return null;
		}

		// 如果有需要等待的，那么等待，直到counter 为0
		waitUntilRunningIsZero(shardingContext.getJobName(), runningRecord.get(0).getBatchNo());

		logger.info("BaseTenantTemplate  fetchData result4,[shardingContext]:" + JSON.toJSONString(shardingContext)
				+ ",[batchNo]:" + batchNo + ",[list]:null");

		return null;
	}

	/**
	 * 获取当前的批次号
	 * 
	 * @param jobName
	 * @return
	 */
	private int getCurrentBatchNo(String jobName)
	{

		TaskSchedule schedule = taskScheduleDAO.queryTaskScheduleByJobName(jobName);

		if (schedule == null)
		{
			return 0;
		}
		else
		{
			return schedule.getBatchNo();
		}
	}

	/**
	 * 如果查到有正在运行的异步线程，等待直到为0；
	 */
	private void waitUntilRunningIsZero(String jobname, int batchNum)
	{

		long start = System.currentTimeMillis();

		// 等待直到可用信号量和并发线程数相等
		while (getSemaphoreMgrInstance().availablePermits() < getConcurrentLimit())
		{

			try
			{
				Thread.sleep(30000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long now = System.currentTimeMillis();

			long sizeHour = 6 * 60 * 60 * 1000;

			if ((now - start) > sizeHour)
			{
				// 等待超过6小时了，强制退出
				logger.error("**********************warning!!!!!!*****************************");
				logger.error("fetchData waitting for running thread exec end for a long time,force to exit!!![jobname]:"
						+ jobname + ",[batchNum]:" + batchNum);
				logger.error("**********************warning!!!!!!*****************************");

				break;
			}

		}
	}

	/**
	 * 异步模式执行
	 * 
	 * @param shardingContext
	 * @param dataList
	 */
	private void asyncProcess(ShardingContext shardingContext, List<TaskExecResult> dataList)
	{

		if (CollectionUtils.isEmpty(dataList))
		{
			return;
		}

		// 并发企业数限制
		int concurrentLimit = getConcurrentLimit();

		logger.info("BaseTenantTemplate  asyncProcess begin,[shardingContext]:" + JSON.toJSONString(shardingContext)
				+ ",[concurrentLimit]:" + concurrentLimit);

		for (TaskExecResult data : dataList)
		{

			// 阻塞申请信号量，
			try
			{
				boolean isSuccess = getSemaphoreMgrInstance().tryAcquire(6, TimeUnit.HOURS);

				// 如果等待6小时都申请不到
				if (!isSuccess)
				{
					throw new RuntimeException(
							"BaseTenantTemplate wait for  Semaphore for more than 6 hours, [ShardingContext]:"
									+ JSON.toJSONString(shardingContext) + ",[data]:" + JSON.toJSONString(data));
				}

				// 申请到信号量的执行业务逻辑
				asyncProcessDataInternal(shardingContext, data);

			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		}

	}

	/**
	 * 并发企业数限制
	 * 
	 * @return
	 */
	public int getConcurrentLimit()
	{
		return DEFAULT_CONCURRENT_LIMIT;
	}

	/**
	 * 异步处理单条记录
	 * 
	 * @param shardingContext
	 * @param data
	 * @return
	 */
	protected void asyncProcessDataInternal(ShardingContext context, TaskExecResult taskExecResult)
	{

		logger.info("BaseTenantTemplate  asyncProcessDataInternal begin,[shardingContext]:" + JSON.toJSONString(context)
				+ ",[taskExecResult]:" + JSON.toJSONString(taskExecResult));

		// 状态先改为执行中
		taskExecResultDAO.updateToRunning(context.getJobName(), taskExecResult.getBatchNo(),
				taskExecResult.getTenantId());

		switch (taskExecResult.getStatus())
		{
			// 初始化的
			case TaskExecResult.STAT_INIT:
				// 失败重新执行的
			case TaskExecResult.STAT_FAIL:

				logger.info("BaseTenantTemplate  asyncProcessDataInternal schedule job,[shardingContext]:"
						+ JSON.toJSONString(context) + ",[taskExecResult]:" + JSON.toJSONString(taskExecResult));

				// 线程放到线程池执行
				TenantTaskThread thread = new TenantTaskThread(context, taskExecResult, threadCallBack(), this);
				TenantExecutePool.schedule(thread);

				break;

			default:

		}

	}

	/**
	 * 业务执行定时任务的数据
	 * 
	 * @param shardingContext
	 * @param data
	 * @return
	 */
	protected boolean processDataInternal(ShardingContext shardingContext, TaskExecResult data)
	{

		try
		{

			boolean result = doProcessData(shardingContext, data);

			logger.info(
					"BaseTenantTemplate  processDataInternal ,[shardingContext]:" + JSON.toJSONString(shardingContext)
							+ ",[taskExecResult]:" + JSON.toJSONString(data) + ",[success]:" + result);

			return result;

		}
		finally
		{

		}
	}

	/**
	 * 执行结果记录入库
	 * 
	 * @param shardingContext
	 * @param data
	 */
	private void flushExecResultToDB(ShardingContext shardingContext, int batchNo, TaskExecResult data,
			boolean isSuccess)
	{

		if (isSuccess)
		{
			taskExecResultDAO.updateToSuccess(shardingContext.getJobName(), batchNo, data.getTenantId());
			return;
		}

		// 失败次数限制
		int failTimeLimit = getExecFailTimesLimit();

		int failTimes = data.getFailTimes() + 1;

		if (failTimes >= failTimeLimit)
		{
			taskExecResultDAO.updateToGiveUp(shardingContext.getJobName(), batchNo, data.getTenantId());
		}
		else
		{
			taskExecResultDAO.updateToFail(shardingContext.getJobName(), batchNo, data.getTenantId());
		}

	}

	/**
	 * 执行失败次数限制
	 * 
	 * @return
	 */
	public int getExecFailTimesLimit()
	{

		return EXEC_FAIL_TIMES_LIMIT;
	}

	/**
	 * 业务执行定时任务的数据
	 * 
	 * @param shardingContext
	 * @param data
	 * @return
	 */
	protected abstract boolean doProcessData(ShardingContext shardingContext, TaskExecResult data);

	/**
	 * 线程执行完的回调
	 * @return
	 */
	private TenantTaskThreadCallBack threadCallBack()
	{

		return new TenantTaskThreadCallBack()
		{

			@Override
			public void callback(ShardingContext shardingContext, TaskExecResult data, boolean isSuccess, Exception e)
			{

				try
				{

					// 成功处理
					if (isSuccess)
					{
						logger.info(
								printMsg(shardingContext, "processData sucess !!!![data]:" + JSON.toJSONString(data)));
						flushExecResultToDB(shardingContext, data.getBatchNo(), data, true);

					}
					else
					{

						if (e == null)
						{
							logger.info(printMsg(shardingContext,
									"processData failed !!!![data]:" + JSON.toJSONString(data)));
						}
						else
						{
							logger.error(printMsg(shardingContext,
									"processData with Exception !!!![data]:" + JSON.toJSONString(data)), e);
						}

						flushExecResultToDB(shardingContext, data.getBatchNo(), data, false);

						// 上报失败job
						reportElasticJobFailedEvent(shardingContext, data, isSuccess, e);
					}

				}
				catch (Exception e1)
				{

					logger.error("TenantTaskThreadCallBack exception ,[ShardingContext]:"
							+ JSON.toJSONString(shardingContext) + ",[TaskExecResult]:" + JSON.toJSONString(data)
							+ ",[isSuccess]:" + isSuccess, e1);

				}
				finally
				{

					// 释放信号量
					getSemaphoreMgrInstance().release();

				}

			}
		};
	}

	private SemaphoreMgrInstance getSemaphoreMgrInstance()
	{

		if (semaphoreMgrInstance == null)
		{

			int concurrentlimit = getConcurrentLimit();
			semaphoreMgrInstance = new SemaphoreMgrInstance(concurrentlimit);
		}

		return semaphoreMgrInstance;
	}

	/**
	 * 上报失败job
	 * @param shardingContext
	 * @param data
	 * @param isSuccess
	 * @param e
	 */
	public void reportElasticJobFailedEvent(ShardingContext shardingContext, TaskExecResult data, boolean isSuccess,
			Exception e)
	{

		try
		{
			String createTime = DateUtil.date2String(data.getCreateTime(), DateUtil.DATETIME_PATTENT);

			String modifyTime = DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT);

			String tenantId = (data.getTenantId() == null ? "0" : data.getTenantId().toString());
			ElasticJobFailedEvent event = new ElasticJobFailedEvent(tenantId, data.getJobName(), 1, data.getBatchNo(),
					shardingContext.getShardingItem(), shardingContext.getShardingTotalCount(), e,
					data.getFailTimes() + 1, createTime, modifyTime);

			EventPublisherUtils.reportEvent(event);

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * 在doSchdyleTask之前执行。
	 * 
	 * @param shardingContext
	 */
	private Integer getBatchNoBeforeFetchData(ShardingContext shardingContext)
	{

		Pair<String, Integer> pair = JobThreadLocal.getCurrentBatchNo();

		// 如果本地local里有，并且jobname一致，那么直接获取threadlocal里的信息，
		if (pair != null && shardingContext.getJobName().equals(pair.getLeft()))
		{

			logger.info("fetchData from threadlocal,[batchNo]" + pair.getRight() + ",time="
					+ DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT));

			return pair.getRight();
		}

		// 如果当前job名和theadlocal里的名字不一致，那么清理掉threadlocal变量
		if (pair != null && !shardingContext.getJobName().equals(pair.getLeft()))
		{
			JobThreadLocal.clearCurrentBatchNoLocal();
		}

		// 初始化并且获取到批次号
		return initAndFetchBatchNo(shardingContext);
	}

	/**
	 *  初始化并且获取批次号
	 * @param shardingContext
	 */
	private int initAndFetchBatchNo(ShardingContext shardingContext)
	{

		String lockKey = RedisKeyBuilder.getJobPreparBatchLockKey(shardingContext.getJobName());

		// token
		String token = redisComplexLock.tryLock(lockKey, 5, TimeUnit.MINUTES);

		String batchNoKey = RedisKeyBuilder.getJobCurrentBatchNoKey(shardingContext.getJobName());

		// 没有获取到token
		if (StringUtils.isEmpty(token))
		{
			// 阻塞等待批次号生成好
			int batchNo = blockAndWaitForBatchNo(shardingContext.getJobName(), batchNoKey);

			// 设置当前批次号
			JobThreadLocal.setCurrentBatchNo(shardingContext.getJobName(), batchNo);

			return batchNo;
		}

		try
		{
			// 批次号
			String val = redisHelper.valueGet(batchNoKey);

			if (StringUtils.isNotBlank(val))
			{

				int batchNo = Integer.parseInt(val);

				JobThreadLocal.setCurrentBatchNo(shardingContext.getJobName(), batchNo);

				return batchNo;
			}

			// 计划job到数据库
			int batchNo = scheduleJob(shardingContext, shardingContext.getJobName());

			// 设置当前批次号到threadlocal
			JobThreadLocal.setCurrentBatchNo(shardingContext.getJobName(), batchNo);

			// 设置到redis缓存
			redisHelper.valueSet(batchNoKey, String.valueOf(batchNo), 1, TimeUnit.MINUTES);

			logger.info("***************************************************");
			logger.info("fetchData from db，[batchNo]" + batchNo + ",time="
					+ DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT));

			return batchNo;
		}
		catch (Exception e)
		{
			logger.error(
					"initAndFetchBatchNo failed for  exception![shardingconext]:" + JSON.toJSONString(shardingContext),
					e);

			// 从数据库读取最新的一批次
			int batchNo = getCurrentBatchNo(shardingContext.getJobName());

			printMsg(shardingContext,
					"initAndFetchBatchNo fatle error, and will return the nearest batchNo,[batchno]:" + batchNo);

			return batchNo;
		}
		finally
		{

			// // 释放锁
			redisComplexLock.releaseLock(lockKey, token);

			// 这里不直接删除，而是设置30s之后自动过期
			redisHelper.expire(batchNoKey, 30, TimeUnit.SECONDS);

			printMsg(shardingContext, "initAndFetchBatchNo release lock ,lock key is " + lockKey + "[token]=" + token);
		}

	}

	/**
	 *  等待batch no 生成
	 * @param jobName
	 * @param batchNoKey
	 * @return
	 */
	private int blockAndWaitForBatchNo(String jobName, String batchNoKey)
	{

		long total = 0;

		long sleepTime = 100;

		while (true)
		{

			// 休眠100毫秒
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			total = total + sleepTime;

			// 获取val
			String val = redisHelper.valueGet(batchNoKey);

			// 值不为空就可以退出本次循环
			if (StringUtils.isNotEmpty(val))
			{
				return Integer.parseInt(val);
			}

			// 大于2分钟,自动退出， 从数据库读取最新的值
			if (total > TWO_MINUTES)
			{
				break;
			}

		}

		// 从数据库读取
		int batchNo = getCurrentBatchNo(jobName);

		return batchNo;

	}

}