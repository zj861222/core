package com.framework.core.alarm.event;

import com.framework.core.alarm.EventTypeEnum;

/**
 *  服务之间的调用事件
 *
 * Created by zhangjun
 */
public class ServiceCallEvent extends CommonEvent
{

	/**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = 6769811962872763165L;
	
	private final String callSource;

	private final String moduleName;
	
	
	private final String additionInfo;

	private final Throwable exception;

	private final int status;
	
	
	private final long cost;
	
	
	
	private final int timeout;


	
	/**
	 * 
	 * @param callSource 服务器context
	 * @param moduleName 模块名
	 * @param url url
	 * @param additionInfo 附加信息，参数，header等
	 * @param exception 异常
	 * @param status 状态 0-exception  1-正常  
	 */
	public ServiceCallEvent(String callSource, String moduleName, String url, long cost, String additionInfo,Throwable exception,int timeout)
	{
		super(url);
		
		this.callSource = callSource;
		this.moduleName = moduleName;
		this.exception = exception;

		this.additionInfo = additionInfo;

		if(exception != null) {
			this.status = 0;
		} else if(cost < timeout){
			this.status = 1;
		} else {
			this.status = 2;
		}
		
		this.timeout = timeout;
		
		this.cost = cost;

	}


	public Throwable getException()
	{
		return exception;
	}

	public String getCallSource()
	{
		return callSource;
	}



	public String getAdditionInfo()
	{
		return additionInfo;
	}



	public int getStatus()
	{
		return status;
	}


	public long getCost()
	{
		return cost;
	}


	public int getTimeout()
	{
		return timeout;
	}


	public String getModuleName()
	{
		return moduleName;
	}

	@Override
	public EventTypeEnum getEventType()
	{
		return EventTypeEnum.EVENT_TYPE_SERVICE_CALL;
	}
}
