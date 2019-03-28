package com.framework.core.search.datasync;



/**
 * 回调
 * @author zhangjun
 *
 */
public interface WorkThreadResultCallBack {
	
	
	/**
	 *  线程是否执行 成功
	 * @param name
	 * @param job
	 * @param isSuccess
	 * @param e
	 */
	void notify(String name,DataSyncJob job, boolean isSuccess,Exception e);
	
}