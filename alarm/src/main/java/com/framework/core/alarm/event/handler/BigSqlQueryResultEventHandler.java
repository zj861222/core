package com.framework.core.alarm.event.handler;

import java.util.Map;

import com.framework.core.alarm.EventTypeEnum;
import com.framework.core.alarm.event.BigSqlQueryResultEvent;

/**
 * sql查询大结果集 事件处理逻辑
 * 
 * @author zhangjun
 *
 */
public class BigSqlQueryResultEventHandler extends AbstractEventHandler<BigSqlQueryResultEvent>
{

	/**
	 * 设置参数
	 */
	@Override
	protected void addArgs(BigSqlQueryResultEvent event, Map<String, Object> args)
	{

		// sql参数
		args.put("parameter", event.getParameter());

		// sql结果集size
		args.put("count", event.getCount());

		if (event.getType() == 0)
		{
			args.put("type", "ibatis");
		}
		else
		{
			args.put("type", "mybatis");
		}

	}

	/**
	 * 设置tags
	 */
	@Override
	protected void addTags(BigSqlQueryResultEvent event, Map<String, String> tags)
	{

		// 删掉抽象类中默认的 event tag，保留 context
		tags.remove("event");

		// sql statement
		tags.put("statement", event.getStatament());

		// 企业id
		tags.put("tenantId", String.valueOf(event.getTenantId()));

		// 用户id
		tags.put("userId", String.valueOf(event.getUserId()));

	}

	/**
	 * 是否忽略这个事件的上报
	 * 
	 * @param event  事件
	 *           
	 * @return 是否忽略这个事件
	 */
	protected boolean isIgnore(final BigSqlQueryResultEvent event)
	{

		return false;

	}

	@Override
	String getCatalog(BigSqlQueryResultEvent event)
	{
		return EventTypeEnum.EVENT_TYPE_BIG_SQL_QUERY_RESULT.getMeasurements();

	}

}
