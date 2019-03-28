package com.framework.core.task.internel.constants;


/**
 * 定时任务执行模式
 * @author zhangjun
 *
 */
public enum TaskRunningMode {
	
	/**
	 * 每次执行一个企业的同步方式
	 */
	RUNNING_MODE_SYNC_ADN_SINGLE(0),
	
	/**
	 * 多企业异步并发
	 */
	RUNNING_MODE_ASYNC_ADN_MUTITENANT(1),
	
	;
	
	private int code;
	
	
	
	public int getCode() {
		return code;
	}



	public void setCode(int code) {
		this.code = code;
	}



	private TaskRunningMode(int code) {
		this.code = code;
	}
}