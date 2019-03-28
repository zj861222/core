package com.framework.core.alarm;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import com.framework.core.common.utils.PropertiesUtil;

/**
 * 事件上报的开关
 * 
 * @author zhangjun
 *
 */
public class EventDeterminater implements InitializingBean
{
	
	
	private static final String IS_ALL_CLOSE = "alarm.all.close";

	private static EventDeterminater instance;

	private boolean serviceAcessEventOpen;

	private boolean exceptionEventOpen;

	private boolean sessionExceptionEventOpen;

	private boolean agentServiceAccessOpen;

	private boolean rabbitMqSendEventOpen;

	private Map<EventTypeEnum, Boolean> propertiesMap = new HashMap<>();

	public boolean isRabbitMqSendEventOpen()
	{
		return rabbitMqSendEventOpen;
	}

	public void setRabbitMqSendEventOpen(boolean rabbitMqSendEventOpen)
	{
		this.rabbitMqSendEventOpen = rabbitMqSendEventOpen;
	}

	public boolean isAgentServiceAccessOpen()
	{
		return agentServiceAccessOpen;
	}

	public void setAgentServiceAccessOpen(boolean agentServiceAccessOpen)
	{
		this.agentServiceAccessOpen = agentServiceAccessOpen;
	}

	public boolean isServiceAcessEventOpen()
	{
		return serviceAcessEventOpen;
	}

	public void setServiceAcessEventOpen(boolean serviceAcessEventOpen)
	{
		this.serviceAcessEventOpen = serviceAcessEventOpen;
	}

	public boolean isExceptionEventOpen()
	{
		return exceptionEventOpen;
	}

	public void setExceptionEventOpen(boolean exceptionEventOpen)
	{
		this.exceptionEventOpen = exceptionEventOpen;
	}

	public boolean isSessionExceptionEventOpen()
	{
		return sessionExceptionEventOpen;
	}

	public void setSessionExceptionEventOpen(boolean sessionExceptionEventOpen)
	{
		this.sessionExceptionEventOpen = sessionExceptionEventOpen;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		instance = new EventDeterminater();

		instance.serviceAcessEventOpen = this.serviceAcessEventOpen;

		instance.exceptionEventOpen = this.exceptionEventOpen;

		instance.sessionExceptionEventOpen = this.sessionExceptionEventOpen;

		instance.agentServiceAccessOpen = this.agentServiceAccessOpen;

		instance.rabbitMqSendEventOpen = this.rabbitMqSendEventOpen;
	}

	/**
	 * 判断此类事件是否需要上报
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isEventPublishOpen(EventTypeEnum type)
	{
		
		// 有可能未初始化
		if (instance == null)
		{
			return false;
		}
		
		//判断是否都关闭
		String isAllCloseVal = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, IS_ALL_CLOSE, "true");

		//如果告警都关闭
		if("true".equals(isAllCloseVal)) {
			return false;
		}
		

		switch (type)
		{

			case EVENT_TYPE_SESSION_EX:
				return instance.sessionExceptionEventOpen;

			case EVENT_TYPE_SERVICE_ACCESS:
				return instance.serviceAcessEventOpen;

			case EVENT_TYPE_SERVER_EX:
				return instance.exceptionEventOpen;

			case EVENT_TYPE_AGENT_SERVICE_ACCESS:
				return instance.agentServiceAccessOpen;

			case EVENT_TYPE_RABBIT_SEND:
				return instance.rabbitMqSendEventOpen;

			default:

				return instance.isOpen(type);

		}
	}

	/**
	 * 判断是否开启的
	 * @param type
	 * @return
	 */
	private boolean isOpen(EventTypeEnum type)
	{

		Boolean isOpen = propertiesMap.get(type);

		if (isOpen == null)
		{

			isOpen = isOpenAlarmFromProperties(type);

			propertiesMap.put(type, isOpen);

		}

		return isOpen;

	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private boolean isOpenAlarmFromProperties(EventTypeEnum type)
	{

		
		// alarm.serviceAcessEvent.open

		String key = "alarm." + type.getMeasurements() + ".open";

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, key, "true");

		return "true".equals(val);

	}

}