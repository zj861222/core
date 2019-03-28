package com.framework.core.task.internel.template.inner;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * @author zhangjun
 *
 */
public class JobThreadLocal {
	
	
	private static final ThreadLocal<Pair<String,Integer>> CURRENT_BATCH_NO_LOCAL = new ThreadLocal<>();
	
	
	
	/**
	 * 设置当前的批次号
	 * @param batchNo
	 */
	public static void setCurrentBatchNo(String jobName,int batchNo) {
		
		CURRENT_BATCH_NO_LOCAL.set(Pair.of(jobName, batchNo));
	}
	
	
	/**
	 * 获取当前执行的批次号信息
	 * @return
	 */
	public static Pair<String,Integer> getCurrentBatchNo() {
		
		return CURRENT_BATCH_NO_LOCAL.get();
	}
	
	
	/**
	 * 清理掉 当前执行的批次号信息
	 */
	public static void clearCurrentBatchNoLocal(){
		
		CURRENT_BATCH_NO_LOCAL.remove();
	}

	
}