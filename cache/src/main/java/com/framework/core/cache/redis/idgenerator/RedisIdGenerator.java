package com.framework.core.cache.redis.idgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.error.exception.BizException;
import redis.clients.jedis.Jedis;

/**
 * 
 * 
 * 集成19位 id主键
 * 
 * @author zhangjun
 *
 */
@Component
public class RedisIdGenerator
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * increase 同时重置超时时间，保证原子性
	 */
	private static final String LUA_SCRIPT = "local current = redis.call('incrBy',KEYS[1],ARGV[1]); if current == tonumber(ARGV[1]) then redis.call('expire',KEYS[1],ARGV[2]) end;return current";

	/**
	 * id长度
	 */
	private static final int ID_LENGTH = 19;
	// local current = redis.call('incrBy',KEYS[1],ARGV[1]);" +
	// " if current == tonumber(ARGV[1]) then" +
	// " local t = redis.call('ttl',KEYS[1]);" +
	// " if t == -1 then " +
	// " redis.call('expire',KEYS[1],ARGV[2])" +
	// " end;" +
	// " end;" +
	// " return current";

	/**
	 * 30分钟
	 */
	private static final long THIRTY_MINUTES = 30 * 60;

	@Resource
	private RedisHelper redisHelper;

	/**
	 * 本地生成19位id，本地的id获取频率上限是1分钟1台机器999999条记录。
	 * 
	 * id示范 1809251314 766 000098。
	 * 
	 * 其中 1809251314 对应当前日期 2018-09-25 13:14
	 *     766 对应本机自动申请的context编号  766
	 *     000098  本机在2018-09-25 13:14 这个分钟内的自增序列
	 * 
	 * @param idType id类型
	 * @return
	 */
	public String generate19BitsIdFromLocal(String idType)
	{

		Date now = new Date();

		String dateStr = generateDateStr(now);

		String idStrSuffix = RedisIdIncreaseLocalMgr.gererate9BitsLocalIdSuffix(idType, now);

		String id = dateStr + idStrSuffix;

		return id;

	}

	/**
	 * 从redis生成19位的主键id。
	 * 
	 * 1.优先尝试从redis中获取自增id
	 * 2.当从redis获取失败，那么从本地获取自增id，本地的id获取频率上限是1分钟1台机器999999条记录。
	 * 
	 *  有可能有2种模版:
	 *  从redis获取的:    1809251314 000000128
	 *      其中 1809251314 对应当前日期 2018-09-25 13:14。
	 *      00000128是redis在这一分钟内的自增序列id
	 *  
	 *  
	 *  从本地获取的:id示范 1809251314 766 000098。
	 *     其中 1809251314 对应当前日期 2018-09-25 13:14
	 *         766 对应本机自动申请的context编号  766
	 *         000098  本机在2018-09-25 13:14 这个分钟内的自增序列
	 * 
	 * @param idType
	 */
	public String generate19BitsIdFromRedis(String idType)
	{

		Date now = new Date();

		String dateStr = generateDateStr(now);

		// 尝试从redis获取
		String idStrSuffix = fetchIdSuffixFromRedis(idType, dateStr);

		if (StringUtils.isEmpty(idStrSuffix))
		{
			// 尝试从本地获取
			idStrSuffix = RedisIdIncreaseLocalMgr.gererate9BitsLocalIdSuffix(idType, now);
		}

		if (StringUtils.isEmpty(idStrSuffix))
		{
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_GENERATE_ID_FAILED.getCode(), "id生成失败");
		}

		String id = dateStr + idStrSuffix;

		return id;
	}

	/**
	 * 从redis生成后缀
	 * @param type
	 * @param dateStr
	 * @return
	 */
	private String fetchIdSuffixFromRedis(String type, String dateStr)
	{

		String redisKey = buildRedisKey(type, dateStr);

		Jedis jedis = null;

		List<String> args = new ArrayList<>();

		// 设置increase 1
		args.add("1");
		// 30分钟有效期
		args.add(String.valueOf(THIRTY_MINUTES));

		int dateStrLength = dateStr.length();

		int suffixTargetLength = ID_LENGTH - dateStrLength;

		try
		{
			jedis = getJedis();

			Object result = jedis.eval(LUA_SCRIPT, Collections.singletonList(redisKey), args);

			if (result != null || result instanceof Long)
			{

				String suffix = result.toString();

				return fillingZeroToTargetStrLength(suffix, suffixTargetLength);

			}
			else
			{
				throw new BizException(RedisErrorCode.EX_SYS_REDIS_GENERATE_ID_FAILED.getCode(), "id生成失败");
			}

		}
		catch (Exception e)
		{

			logger.error("fetchIdSuffixFromRedis failed,[type]:" + type + ",[dateStr]:" + dateStr, e);

			return null;
		}
		finally
		{
			releaseConnection(jedis);
		}

	}

	/**
	 * 构建年月日+时分的 str，注意 不一定是完整的 201809251130，可能是忽略了前面2位(1809251130)
	 * 
	 * @return
	 */
	private String generateDateStr(Date date)
	{

		String dateStr = DateUtil.date2String(date, "yyyyMMddHHmm");

		int ignoreNum = getIgnoreDateStrPrefixNum();

		if (ignoreNum > 0)
		{
			Assert.isTrue(ignoreNum == 1 || ignoreNum == 2);
			dateStr = dateStr.substring(ignoreNum, dateStr.length());
		}

		return dateStr;
	}

	/**
	 * 忽略年月日前缀前面的几位，用来节省位数，防止超过long上线。
	 * 
	 * 比如 原来 201809251130,这里返回2 的话，表示忽略最前面2位 20 , 那么是 1809251130。
	 * 
	 * @return
	 */
	private int getIgnoreDateStrPrefixNum()
	{

		return 2;
	}

	/**
	 * 
	 * @return
	 */
	private Jedis getJedis()
	{

		// RedisConnection jedisConnection =
		// jedisConnectionFactory.getConnection();
		// Jedis jedis = (Jedis) jedisConnection.getNativeConnection();
		return redisHelper.getWriteJedis();
	}

	/**
	 * release jedis
	 * 
	 * @param jedis
	 */
	private void releaseConnection(Jedis jedis)
	{
		if (jedis != null)
		{
			jedis.close();
		}
	}

	/**
	 * 构建redis的key
	 * @param type
	 * @param dateStr
	 * @return
	 */
	private String buildRedisKey(String type, String dateStr)
	{

		return "idg:" + type + ":" + dateStr;

	}

	/**
	 * 填充0到目标长度
	 * @param data
	 * @param targetLength
	 * @return
	 */
	private String fillingZeroToTargetStrLength(String data, int targetLength)
	{

		int dataLength = data.length();

		Assert.isTrue(dataLength <= data.length());
		
		if (targetLength == data.length())
		{
			return data;
		}

		StringBuilder sb = new StringBuilder();

		int fillZeroLength = targetLength - data.length();

		// 位数差几位就补几个零
		for (int i = 0; i < fillZeroLength; i++)
		{
			sb.append("0");
		}

		sb.append(data);

		return sb.toString();
	}

}