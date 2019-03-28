package com.framework.core.task.internel.exception;



public class MasterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7209343756502498934L;
	
	
	/**
	 * 
	 * @param jobName
	 */
	public MasterException(String jobName) {
		super(getErrorMsg(jobName));
	}
	
	/**
	 * 
	 * @param jobName
	 */
	public MasterException(String jobName,Throwable throwable) {
		super(getErrorMsg(jobName),throwable);
	}
	
	
	private static String getErrorMsg(String jobName) {
		return "fetch tenant ids for job failed,job name is:"+jobName;
	}
	
}