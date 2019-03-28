package com.framework.core.task.internel.template;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * 
 * @author zhangjun
 *
 */
public class JobSemaphoreMgr
{

	private static Map<String, SemaphoreMgrInstance> jobSemaphoreMap = new HashMap<>();

	
	/**
	 * 获取job对应的信号量
	 * @param jobName
	 * @return
	 */
	public static boolean tryAcquireSemaphore(String jobName)
	{

		SemaphoreMgrInstance instance = getSemaphoreMgrInstance(jobName);
		
		return instance.tryAcquire();

	}
	
	
	/**
	 * 尝试释放信号量
	 * @param jobName
	 */
	public static void tryReleaseSemaphore(String jobName) {
		
		SemaphoreMgrInstance instance = getSemaphoreMgrInstance(jobName);
		
		if(instance.availablePermits() == 0){
			instance.release();
		}
		
	}
	
	
	
    
	private static SemaphoreMgrInstance getSemaphoreMgrInstance(String jobName)
	{

		SemaphoreMgrInstance instance = jobSemaphoreMap.get(jobName);

		if (instance == null)
		{

			initSemaphoreMgrInstance(jobName);

			instance = jobSemaphoreMap.get(jobName);

			Assert.notNull(instance);

		}

		return instance;

	}

	
	
	
	/**
	 * 
	 * 
	 * 初始化
	 * 
	 * 
	 * 
	 */
	private static synchronized void initSemaphoreMgrInstance(String jobName)
	{

		SemaphoreMgrInstance instance = jobSemaphoreMap.get(jobName);

		if (instance != null)
		{
			return;
		}

		instance = new SemaphoreMgrInstance(1);

		jobSemaphoreMap.put(jobName, instance);

	}

}