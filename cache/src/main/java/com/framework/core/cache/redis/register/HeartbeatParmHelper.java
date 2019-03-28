package com.framework.core.cache.redis.register;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.framework.core.cache.redis.constants.RedisKeyBuilder;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.common.utils.LocalhostIpFetcher;

/**
 * 注册机器编号
 * @author zhangjun
 *
 */
public class HeartbeatParmHelper implements InitializingBean
{

	private static String LOCAL_IP = LocalhostIpFetcher.fetchLocalIP();

	private static String PORT = LocalhostIpFetcher.tryFetchServerPort();

	private String serverIndex;

	private static HeartbeatParmHelper instance;

	private String context;

	private RedisHelper redisHelper;

	public RedisHelper getRedisHelper()
	{
		return redisHelper;
	}

	public void setRedisHelper(RedisHelper redisHelper)
	{
		this.redisHelper = redisHelper;
	}

	public String getContext()
	{
		return context;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

	/**
	 * 获取server注册的key
	 * @return
	 */
	public static String getServerRegisterKey()
	{

		String key = instance.context + ":server_reg:ip:" + LOCAL_IP;

		return key;
	}

	/**
	 * 获取本次启动的token。
	 * 
	 * @return
	 */
	public static String getCurrentServerIndex()
	{

		return instance.serverIndex;

	}

	/**
	 * 获取context
	 * @return
	 */
	public static String getWebContext()
	{

		return instance.context;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{

		instance = new HeartbeatParmHelper();

		instance.context = this.context;

		instance.redisHelper = this.redisHelper;

		instance.serverIndex = gererate3BitsServerIndex(this.redisHelper);

	}

	/**
	 * 生成3位的index字符串，可能是  001 ，100，023这种
	 * @param redisHelper
	 * @return
	 */
	private String gererate3BitsServerIndex(RedisHelper redisHelper)
	{

		// 随机数的池子
		Set<String> randomPool = new HashSet<>();

		while (true)
		{

			int randomInt = new Random().nextInt(1000);

			if (randomPool.contains(String.valueOf(randomInt)))
			{
				continue;
			}

			String key = RedisKeyBuilder.generateServerRegisterIndexKey(context, String.valueOf(randomInt));

			boolean isDeclareSuccess = redisHelper.setNxWithTimeOut(key, LOCAL_IP + ":" + PORT, 10, TimeUnit.MINUTES);

			if (isDeclareSuccess)
			{
				randomPool.add(String.valueOf(randomInt));
				return fillZeroTo3Bits(randomInt);

			}
			else
			{
				randomPool.add(String.valueOf(randomInt));
			}
		}

	}

	/**
	 * 填充0，构建3位长度的字符串
	 * @param randomInt
	 * @return
	 */
	private static String fillZeroTo3Bits(int randomInt)
	{

		Assert.isTrue(randomInt >= 0 && randomInt < 1000);

		if (randomInt >= 100)
		{
			return String.valueOf(randomInt);
		}
		else if (randomInt >= 10)
		{
			return "0" + String.valueOf(randomInt);
		}
		else
		{
			return "00" + String.valueOf(randomInt);
		}

	}

}