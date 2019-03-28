package com.framework.core.common.timer;

/**
 * 
 * @author zhangjun
 *
 */
public abstract class TimerInstance
{

	/**
	 * 延迟20s默认
	 */
	private long delayTime = 1000*20;

	public long getDelayTime()
	{
		return delayTime;
	}

	/**
	 * 执行的事件间隔，单位：毫秒
	 * @return
	 */
	public abstract long getExecInterval();
	

	
	public void innterExecute(){
		
		try {
			doExecute();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 执行业务逻辑
	 */
	public abstract void doExecute();

}