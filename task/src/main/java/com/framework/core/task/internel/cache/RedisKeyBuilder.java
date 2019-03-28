package com.framework.core.task.internel.cache;


public class RedisKeyBuilder {
	

	/**
	 *  job 执行前，准备这一批次的企业数据，提前lock
	 * @param jobName
	 * @return
	 */
	public static String getJobPreparBatchLockKey(String jobName){
		
		
		return "task:lock:jobprepar:name:"+jobName;
	}
	
	
	
	
	/**
	 * 根据job名获取等待队列的名字
	 * @param jobName
	 * @return
	 */
	public static String getTenantIdsWaitQueueKeyByJobName(String jobName) {
		
		
		return "task:remotequeue:wait:jobName"+jobName;
	}
	
	
	
	
	
	
	
	/**
	 * 根据job名获取执行队列的名字
	 * @param jobName
	 * @return
	 */
	public static String getTenantIdsRunningQueueKeyByJobName(String jobName) {
		
		
		return "task:remotequeue:running:jobName"+jobName;
	}
	
	
	
	/**
	 * 根据job名获取执行结果队列的名字
	 * @param jobName
	 * @return
	 */
	public static String getTenantIdsResultQueueKeyByJobName(String jobName) {
		
		
		return "task:remotequeue:result:jobName"+jobName;
	}
	
	

	/**
	 *  job 执行前，准备这一批次的企业数据，用作标注 init的状态
	 * @param jobName
	 * @return
	 */
	public static String getJobPreparStatKey(String jobName){
		
		
		return "task:lock:jobprepar:stat:"+jobName;
	}
	
	
	
	/**
	 *  当前job的批次号
	 * @param jobName
	 * @return
	 */
	public static String getJobCurrentBatchNoKey(String jobName){
		
		
		return "task:jobprepar:batchNo:"+jobName;
	}
	
}