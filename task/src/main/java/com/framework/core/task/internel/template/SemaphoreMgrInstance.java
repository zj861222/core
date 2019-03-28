package com.framework.core.task.internel.template;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 信号量控制器实例
 * @author zhangjun
 *
 */
public class SemaphoreMgrInstance {
	
	
	private final Semaphore semaphore ;
	
	
	public SemaphoreMgrInstance(int permits) { 
		
		semaphore = new Semaphore(permits, true);

	}
	
	
	
	/**
	 * 申请1个信号量，最多阻塞等待timeout的时间
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException{
		
		return semaphore.tryAcquire(timeout, unit);
		
	}
	
	
	
	/**
	 * 申请n个信号量，最多阻塞等待timeout的时间
	 * @param permits
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException{
		
		return semaphore.tryAcquire(permits,timeout, unit);
		
	}
	
	
	/**
	 * 释放1个信号量
	 */
	public void release(){
		
		semaphore.release();
		
	}
	
	/**
	 * 释放n个信号量
	 * @param permits
	 */
	public void release(int permits){
		
		semaphore.release(permits);
	}
	
	/**
	 * 当前可用的信号量
	 */
	public int availablePermits(){
		return semaphore.availablePermits();
	}
	
	/**
	 * 等待的数量
	 * @return
	 */
	public int getQueueLength(){
		return semaphore.getQueueLength();
	}
	
	
	/**
	 * 申请
	 * @return
	 */
	public boolean tryAcquire(){
		
		return semaphore.tryAcquire();
	}

	
}