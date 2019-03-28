package com.waiqin365.task.common.cache;


public class RedisKeyBuilder {
	
	
	
	
	/**
	 *  job 执行前，准备这一批次的企业数据，提前lock
	 * @param jobName
	 * @return
	 */
	public static String getJobPreparBatchLockKey(String jobName){
		
		
		return "task:lock:jobprepar:name:"+jobName;
	}
	
}