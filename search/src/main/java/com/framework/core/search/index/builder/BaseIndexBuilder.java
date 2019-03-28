package com.framework.core.search.index.builder;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.framework.core.search.index.factory.IndexClient;


/**
 * 全量构建索引接口
 */
public abstract class BaseIndexBuilder
{

	/**
	 * 全量索引
	 */
	public abstract boolean buildAll(final String tempIndexName, final String indexName, final IndexClient indexClient)
		throws Exception;

	/**
	 * 获取时间间隔毫秒秒数
	 * 
	 * @param unit
	 * @param interval
	 * @return seconds
	 */
	public long getInterval(int unit, int interval)
	{
		Calendar calendar = new GregorianCalendar();
		long start = calendar.getTimeInMillis();
		calendar.add(unit, interval);
		long end = calendar.getTimeInMillis();

		return end - start;
	}
}
