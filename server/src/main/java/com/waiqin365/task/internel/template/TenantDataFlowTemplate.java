//package com.waiqin365.task.internel.template;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import javax.annotation.Resource;
//
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.fastjson.JSON;
//import com.dangdang.ddframe.job.api.ShardingContext;
//import com.dangdang.ddframe.job.executor.ShardingContexts;
//import com.framework.core.cache.redis.lock.RedisSimpleLock;
//import com.framework.core.cache.redis.utils.RedisHelper;
//import com.framework.core.task.elasticjob.template.AbstractDataFlowJobTemplate;
//import com.waiqin365.task.common.cache.RedisKeyBuilder;
//import com.waiqin365.task.internel.dao.TaskExecResultDAO;
//import com.waiqin365.task.internel.dao.TaskScheduleDAO;
//import com.waiqin365.task.internel.model.TaskExecResult;
//import com.waiqin365.task.internel.model.TaskSchedule;
//
//@Component
//public abstract class TenantDataFlowTemplate extends AbstractDataFlowJobTemplate<TaskExecResult> {
//
//	private static Logger logger = LoggerFactory.getLogger(TenantDataFlowTemplate.class);
//
//	/**
//	 * 查询分片一次查20个
//	 */
//	private static final int SHARDING_BATCH_LIMIT = 20;
//
//	/**
//	 * 执行失败次数限制
//	 */
//	private static final int EXEC_FAIL_TIMES_LIMIT = 3;
//	
//
//	/**
//	 * 一批1000个
//	 */
//	private int batchSize = 1000;
//
//	@Resource
//	private RedisHelper redisHelper;
//
//	@Resource
//	private RedisSimpleLock redisSimpleLock;
//
//	@Resource
//	private TaskScheduleDAO taskScheduleDAO;
//
//	@Resource
//	private TaskExecResultDAO taskExecResultDAO;
//
//
//	/**
//	 * 批次号
//	 */
//	private int batchNo;
//
//	@Override
//	public List<TaskExecResult> fetchData(ShardingContext shardingContext) {
//
//		// 总共的分片
//		int totalShardings = shardingContext.getShardingTotalCount();
//		// 当前的分片
//		int sharding = shardingContext.getShardingItem();
//
//		List<TaskExecResult> list = (List<TaskExecResult>) taskExecResultDAO.queryWaitToExecRecordBySharding(
//				shardingContext.getJobName(),batchNo, totalShardings, sharding, getShardingBatchLimit());
//
//		// 取出等待执行的数据
//		if (CollectionUtils.isNotEmpty(list)) {
//			return list;
//		}
//
//		// 对之前执行失败的数据，再给几次机会重新执行
//		list = (List<TaskExecResult>) taskExecResultDAO.queryExecFailedRecordBySharding(shardingContext.getJobName(),
//				batchNo,totalShardings, sharding, getShardingBatchLimit());
//
//		return list;
//	}
//
//	@Override
//	public void processData(ShardingContext shardingContext, List<TaskExecResult> dataList) {
//
//		if (CollectionUtils.isEmpty(dataList)) {
//			return;
//		}
//
//		for (TaskExecResult data : dataList) {
//
//			try {
//
//				switch (data.getStatus()) {
//				// 初始化的
//				case TaskExecResult.STAT_INIT:
//					// 失败重新执行的
//				case TaskExecResult.STAT_FAIL:
//
//					boolean isSuccess = doProcessData(shardingContext, data);
//
//					flushExecResultToDB(shardingContext, data, isSuccess);
//
//					if (isSuccess) {
//						logger.info(
//								printMsg(shardingContext, "processData sucess !!!![data]:" + JSON.toJSONString(data)));
//
//					} else {
//						logger.info(
//								printMsg(shardingContext, "processData failed !!!![data]:" + JSON.toJSONString(data)));
//
//					}
//					
//					break;
//				default:
//
//				}
//
//			} catch (Exception e) {
//
//				flushExecResultToDB(shardingContext, data, false);
//
//				logger.error(
//						printMsg(shardingContext, "processData with Exception !!!![data]:" + JSON.toJSONString(data)));
//			}
//		}
//
//	}
//
//	/**
//	 * 执行结果记录入库
//	 * 
//	 * @param shardingContext
//	 * @param data
//	 */
//	private void flushExecResultToDB(ShardingContext shardingContext, TaskExecResult data, boolean isSuccess) {
//
//		if (isSuccess) {
//			taskExecResultDAO.updateToSuccess(shardingContext.getJobName(), batchNo, data.getTenantId());
//			return;
//		}
//
//		// 失败次数限制
//		int failTimeLimit = getExecFailTimesLimit();
//
//		int failTimes = data.getFailTimes() + 1;
//
//		if (failTimes >= failTimeLimit) {
//			taskExecResultDAO.updateToGiveUp(shardingContext.getJobName(), batchNo, data.getTenantId());
//		} else {
//			taskExecResultDAO.updateToFail(shardingContext.getJobName(), batchNo, data.getTenantId());
//		}
//
//	}
//
//	/**
//	 * 业务执行定时任务的数据
//	 * 
//	 * @param shardingContext
//	 * @param data
//	 * @return
//	 */
//	protected abstract boolean doProcessData(ShardingContext shardingContext, TaskExecResult data);
//
//	@Override
//	public void beforeJobExecuted(ShardingContexts shardingContexts) {
//
//		logger.info(printMsg(shardingContexts, "beforeJobExecuted start!"));
//
//		String jobName = shardingContexts.getJobName();
//
//		String lockKey = RedisKeyBuilder.getJobPreparBatchLockKey(jobName);
//
//		boolean isLocked = redisSimpleLock.tryLock(lockKey, 5, TimeUnit.MINUTES);
//
//		if (!isLocked) {
//
//			logger.info(printMsg(shardingContexts, "beforeJobExecuted with out lock and end!"));
//
//			return;
//		}
//
//		logger.info(printMsg(shardingContexts, "beforeJobExecuted get lock ,lock key is " + lockKey));
//
//		try {
//			scheduleJob(shardingContexts, jobName);
//
//		} catch (Exception e) {
//
//			logger.error("beforeJobExecuted ,while scheduleJob exception!", e);
//		} finally {
//			redisSimpleLock.releaseLock(lockKey);
//
//			logger.info(printMsg(shardingContexts, "beforeJobExecuted release lock ,lock key is " + lockKey));
//
//		}
//
//	}
//
//	@Override
//	public void afterJobExecuted(ShardingContexts shardingContexts) {
//
//	}
//
//	/**
//	 * 计划job
//	 * 
//	 * @param jobName
//	 * @return
//	 */
//	private void scheduleJob(ShardingContexts shardingContext, String jobName) {
//
//		// 分页查询哪些企业要执行job
//		int pageNum = 1;
//
//		// 获取当前的批次号,left 是操作是否成功，不成功不需要继续了，说明有别的并发操作了；right是 批次号
//		Pair<Boolean, Integer> result = generateCurrentBatchInfo(jobName);
//
//		if (!result.getLeft()) {
//
//			logger.info(printMsg(shardingContext, "beforeJobExecuted scheduleJob return ,and not init record!"));
//
//			return;
//		}
//
//		// 获取批次号
//		batchNo = result.getRight();
//
//		logger.info(printMsg(shardingContext, "beforeJobExecuted scheduleJob ,get batchNum, batchNum is:" + batchNo));
//
//		List<Long> ids = fetchWaitExecutTenantIds(jobName, getFetchTenantBatchSize(),pageNum);
//
//		if(CollectionUtils.isEmpty(ids)) {
//
//			logger.info(printMsg(shardingContext,
//					"beforeJobExecuted scheduleJob end, no tenant ids need to process ,[batchNum]:" + batchNo + ",[TotalPage]" + (pageNum - 1)));
//			
//			return;
//		}
//		
//		
//		List<TaskExecResult> insertList = new ArrayList<TaskExecResult>();
//
//		while (CollectionUtils.isNotEmpty(ids)) {
//
//			insertList = TaskExecResult.createBatchRecords(jobName, batchNo, ids);
//			taskExecResultDAO.insertBatchTaskExecInfo(insertList);
//			pageNum++;
//			insertList.clear();
//
//			// 下一个批次
//			ids = fetchWaitExecutTenantIds(jobName, batchSize, pageNum);
//		}
//
//		logger.info(printMsg(shardingContext,
//				"beforeJobExecuted scheduleJob success end ,[batchNum]:" + batchNo + ",[TotalPage]" + (pageNum - 1)));
//	}
//
//	/**
//	 * 生成批次信息,并发问题，靠之前的redis分布式锁控制
//	 * 
//	 * @param jobName
//	 * @return
//	 */
//	private Pair<Boolean, Integer> generateCurrentBatchInfo(String jobName) {
//
//		TaskSchedule record = taskScheduleDAO.queryTaskScheduleByJobName(jobName);
//		// 初始化为1
//		int currentBatchNum = 1;
//
//		if (record == null) {
//			// 创建第一批数据
//			record = TaskSchedule.createTask(jobName, currentBatchNum);
//
//			int isSuccess = taskScheduleDAO.insertOrUpdateTaskSchedule(record);
//
//			return Pair.of(isSuccess > 0, currentBatchNum);
//
//		} else {
//
//			// 之前的批次号加1
//			currentBatchNum = record.getBatchNo() + 1;
//
//			int isSuccess = taskScheduleDAO.updateTaskSchedule(jobName, currentBatchNum);
//
//			return Pair.of(isSuccess > 0, currentBatchNum);
//
//		}
//	}
//
//	/**
//	 * 获取需要执行job的企业id，分页查询
//	 * 
//	 * @param jobName
//	 * @param batchSize
//	 *            一页多少条
//	 * @param pageNum
//	 *            多少页
//	 * @return
//	 */
//	protected abstract List<Long> fetchWaitExecutTenantIds(String jobName, int batchSize, int pageNum);
//
//	/**
//	 * 打印日志
//	 * 
//	 * @param shardingContexts
//	 * @param message
//	 */
//	private String printMsg(ShardingContexts shardingContexts, String message) {
//		return "[msg]:" + message + ",[jobName]:" + shardingContexts.getJobName() + ",[taskId]:"
//				+ shardingContexts.getTaskId() + ",[jobParameter]:" + shardingContexts.getJobParameter()
//				+ ",[shardmap]:" + shardingContexts.getShardingItemParameters();
//	}
//
//	/**
//	 * 打印日志
//	 * 
//	 * @param shardingContexts
//	 * @param message
//	 */
//	private String printMsg(ShardingContext shardingContext, String message) {
//
//		return "[msg]:" + message + ",[jobName]:" + shardingContext.getJobName() + ",[taskId]:"
//				+ shardingContext.getTaskId() + ",[jobParameter]:" + shardingContext.getJobParameter() + ",[shardItem]:"
//				+ shardingContext.getShardingItem();
//	}
//
//	/**
//	 * 根据分片，一批查多少数据
//	 * 
//	 * @return
//	 */
//	public int getShardingBatchLimit() {
//		return SHARDING_BATCH_LIMIT;
//	}
//
//	/**
//	 * 执行失败次数限制
//	 * 
//	 * @return
//	 */
//	public int getExecFailTimesLimit() {
//
//		return EXEC_FAIL_TIMES_LIMIT;
//	}
//	
//	
//	
//	public int getFetchTenantBatchSize() {
//		
//		return batchSize;
//	}
//
//}