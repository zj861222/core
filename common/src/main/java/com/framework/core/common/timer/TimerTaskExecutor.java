package com.framework.core.common.timer;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.framework.core.common.timer.TimerInstance;


/**
 * 
 * @author zhangjun
 *
 */
public class TimerTaskExecutor implements ApplicationContextAware
{

	protected Logger logger = LoggerFactory.getLogger(getClass());

	
	private final Timer timer = new Timer();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{

		logger.info("TimerTaskExecutor schudel timer task start *********************!");
		
		Map<String, TimerInstance> map = applicationContext.getBeansOfType(TimerInstance.class);

		if (map.isEmpty())
		{
			return;
		}

		for (Entry<String, TimerInstance> entry : map.entrySet())
		{

			final TimerInstance instance = entry.getValue();

			timer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{

					instance.innterExecute();

				}
			}, instance.getDelayTime(), instance.getExecInterval());
			
			
			logger.info("TimerTaskExecutor schudel timer task, [TimerInstance]:"+instance.getClass().getName());

		}

	}

	
	public synchronized void close()
	{

		this.timer.cancel();
	}

}
