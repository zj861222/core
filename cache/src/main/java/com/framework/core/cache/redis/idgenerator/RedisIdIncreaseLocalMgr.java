package com.framework.core.cache.redis.idgenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.cache.redis.register.HeartbeatParmHelper;
import com.framework.core.common.lock.SegmentLock;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.error.exception.BizException;

/**
 * 
 * redis 本地id自增管理
 * 
 * @author zhangjun
 *
 */
public class RedisIdIncreaseLocalMgr
{

	private static Map<String, LocalIdIncreaseUnit> indexMap = new HashMap<String, LocalIdIncreaseUnit>();

	/**
	 * hash 分段锁
	 */
	private static SegmentLock<String> segmentLock = new SegmentLock<String>();

	/**
	 *  redis出问题时，生成本地的9位 的id 后缀
	 *  
	 * @param idType
	 * @param date
	 * @return
	 */
	public static String gererate9BitsLocalIdSuffix(String idType, Date date)
	{

		// 当前服务器申报的index,3位字符串
		String currentServerIndex = HeartbeatParmHelper.getCurrentServerIndex();
		// 确保index不为空
		Assert.isTrue(StringUtils.isNotBlank(currentServerIndex));

		String dateformatStr = DateUtil.date2String(date, "yyyyMMddHHmm");

		boolean isLocked = false;
		try
		{
			// 尝试锁，最多等5秒
			isLocked = segmentLock.lock(idType, 5, TimeUnit.SECONDS);

			if (!isLocked)
			{
				throw new BizException(RedisErrorCode.EX_SYS_REDIS_GENERATE_ID_FAILED.getCode());
			}

			LocalIdIncreaseUnit unit = indexMap.get(idType);

			int id = -1;

			if (unit == null)
			{

				unit = new LocalIdIncreaseUnit(dateformatStr);
				id = unit.increase();

				indexMap.put(idType, unit);

			}
			else
			{

				if (!dateformatStr.equals(unit.getIdSegment()))
				{
					indexMap.remove(idType);
					unit = new LocalIdIncreaseUnit(dateformatStr);
					indexMap.put(idType, unit);
				}

				id = unit.increase();
			}

			Assert.isTrue(id > 0);

			// currentServerIndex3位，加6位的自增序列
			return fillZeroTo6Bits(id) + currentServerIndex;

		}
		finally
		{

			if (isLocked)
			{
				segmentLock.unlock(idType);
			}
		}

	}

	/**
	 * 补充满到6位
	 * @param num
	 * @return
	 */
	private static String fillZeroTo6Bits(int num)
	{
		// 保证6位以下
		Assert.isTrue(num > 0 && num < 1000000);

		int length = String.valueOf(num).length();

		if (length == 6)
		{
			return String.valueOf(num);
		}

		// 需要补充多少个0
		int needFillZeroCount = 6 - length;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < needFillZeroCount; i++)
		{
			sb.append("0");
		}
		sb.append(num);

		return sb.toString();
	}

	private static class LocalIdIncreaseUnit
	{

		public String idSegment;

		public AtomicInteger count = new AtomicInteger(0);

		public LocalIdIncreaseUnit(String idSegment)
		{
			this.idSegment = idSegment;
		}

		public String getIdSegment()
		{
			return idSegment;
		}

		public int increase()
		{

			return count.incrementAndGet();
		}
	}

}