package com.framework.core.alarm.event;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.framework.core.alarm.EventTypeEnum;

/**
 *   
 *
 * 数据库查询结果集比较大的数据统计事件。
 * 
 */
public class BigSqlQueryResultEvent extends CommonEvent
{

	/**
	 * @Fields serialVersionUID
	 */
	private static final long serialVersionUID = 2370305262095540320L;

	/**
	 * 
	 * @param statament sql statement
	 * @param parameter 参数
	 * @param tenantId 企业id
	 * @param userId userId
	 * @param count 查询结果集大小
	 * @param type 0-ibatis 1-mybatis
	 */
	public BigSqlQueryResultEvent(String statament, Object parameter, long tenantId, long userId, int count, int type)
	{
		super(statament);

		Assert.notNull(statament);

		// 参数
		if (parameter != null)
		{
			this.parameter = JSON.toJSONString(parameter);
		}

		this.statament = statament;

		this.tenantId = tenantId;

		this.userId = userId;

		this.count = count;

		Assert.isTrue(type == 0 || type == 1);
		this.type = type;

	}

	/**
	 * 
	 * @param statament sql statement
	 * @param parameter 参数
	 * @param tenantId 企业id
	 * @param count 查询结果集大小
	 * @param type 0-ibatis 1-mybatis
	 */
	public BigSqlQueryResultEvent(String statament, Object parameter, long tenantId, int count, int type)
	{
		super(statament);

		Assert.notNull(statament);

		// 参数
		if (parameter != null)
		{
			this.parameter = JSON.toJSONString(parameter);
		}

		this.statament = statament;

		this.tenantId = tenantId;

		this.count = count;

		Assert.isTrue(type == 0 || type == 1);
		this.type = type;

	}

	/**
	 * 参数
	 */
	private String parameter;

	/**
	 * sql statement
	 */
	private String statament;

	/**
	 * 用户id
	 */
	private long userId = -1;

	/**
	 * 企业id
	 */
	private long tenantId = -1;

	/**
	 * 结果集大小
	 */
	private long count;

	/**
	 * ibatis = 0, mybatis = 1
	 */
	private int type;

	public String getParameter()
	{
		return parameter;
	}

	public void setParameter(String parameter)
	{
		this.parameter = parameter;
	}

	public String getStatament()
	{
		return statament;
	}

	public void setStatament(String statament)
	{
		this.statament = statament;
	}

	public long getUserId()
	{
		return userId;
	}

	public void setUserId(long userId)
	{
		this.userId = userId;
	}

	public long getTenantId()
	{
		return tenantId;
	}

	public void setTenantId(long tenantId)
	{
		this.tenantId = tenantId;
	}

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	@Override
	public EventTypeEnum getEventType()
	{
		return EventTypeEnum.EVENT_TYPE_BIG_SQL_QUERY_RESULT;
	}
}
