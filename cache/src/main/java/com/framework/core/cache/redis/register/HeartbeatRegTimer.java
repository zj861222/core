package com.framework.core.cache.redis.register;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.framework.core.cache.redis.constants.RedisKeyBuilder;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.common.timer.TimerInstance;

@Component
public class HeartbeatRegTimer extends TimerInstance
{

	private final static Logger logger = LoggerFactory.getLogger(HeartbeatRegTimer.class);

	/**
	 * 10s 
	 */
	private static final long EXEC_INTERVAL = 10 * 1000;

	@Resource
	private RedisHelper redisHelper;

	@Override
	public long getExecInterval()
	{
		return EXEC_INTERVAL;
	}

	@Override
	public void doExecute()
	{

		execute();

	}

	private void execute()
	{

		try
		{
			if (redisHelper != null)
			{

				// 注册服务器的key
				String key = HeartbeatParmHelper.getServerRegisterKey();

				// 注册当前的server的token，有效期为2分钟
				redisHelper.valueSet(key, HeartbeatParmHelper.getCurrentServerIndex(), 2, TimeUnit.MINUTES);

				// 申报服务器 id 编号的key
				String index = HeartbeatParmHelper.getCurrentServerIndex();
				Assert.isTrue(StringUtils.isNotBlank(index));
				String indexRegisterKey = RedisKeyBuilder.generateServerRegisterIndexKey(HeartbeatParmHelper.getWebContext(), index);
				// 延长这台机器注册的index的有效时间，防止被其他机器注册了同样的index
				redisHelper.expire(indexRegisterKey, 10, TimeUnit.MINUTES);
			}

		}
		catch (Exception e)
		{

			logger.error("ServerRegister startDaemonThread failed", e);

		}

	}



}