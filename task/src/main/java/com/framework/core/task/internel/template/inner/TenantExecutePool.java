package com.framework.core.task.internel.template.inner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 * @author zhangjun
 *
 */
public class TenantExecutePool {
	
	
	private static ExecutorService executor = Executors.newFixedThreadPool(20);
	
	
	
	public static Future<?> schedule(Runnable runnable) {
		return executor.submit(runnable);
	}
	

}