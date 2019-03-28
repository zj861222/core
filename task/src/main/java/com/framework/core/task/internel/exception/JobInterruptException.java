package com.framework.core.task.internel.exception;


/**
 * 异常中断job
 * @author zhangjun
 *
 */
public class JobInterruptException extends Exception {

	
	public JobInterruptException(String message) {
		
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7638610050757186194L;
	
}