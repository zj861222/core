package com.framework.core.task.internel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.task.api.ElasticJobApi;
import com.google.common.base.Optional;


/**
 * 初始化，在所有的spring bean加载完毕之后读取参数，决定是否在本机开启es job
 * 
 * 
 * @author zhangjun
 *
 */
@Component
public class EsJobInitComponent implements SmartLifecycle
{

	private static Logger logger = LoggerFactory.getLogger(EsJobInitComponent.class);

	@Override
	public void start()
	{
		// 判断是否开启本机的分发job
		String enable = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "jobcenter.currentip.distribute.enable",
				"1");

		String ip = IpUtils.getIp();

		// 1-enable ，0-disable
		if ("1".equals(enable))
		{
			ElasticJobApi.enable(Optional.<String> absent(), Optional.of(ip));
			logger.info("*******************************************************************");
			logger.info("EsJobInitComponent decided to enable the job distribute for this ip!!");
		}
		else
		{
			ElasticJobApi.disable(Optional.<String> absent(), Optional.of(ip));
			logger.warn("*******************************************************************");
			logger.warn("EsJobInitComponent decided to disable the job distribute for this ip!!");

		}

	}

	@Override
	public void stop()
	{

	}

	@Override
	public boolean isRunning()
	{
		return false;
	}

	@Override
	public int getPhase()
	{
		return 0;
	}

	@Override
	public boolean isAutoStartup()
	{
		return true;
	}

	@Override
	public void stop(Runnable callback)
	{

	}

}