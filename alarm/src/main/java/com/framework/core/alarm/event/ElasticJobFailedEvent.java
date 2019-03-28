package com.framework.core.alarm.event;

import com.framework.core.alarm.EventTypeEnum;
import com.framework.core.common.utils.StackTraceHelper;

/**
 * elastic job 执行失败的事件。
 * 
 * @author zhangjun
 *
 */
public class ElasticJobFailedEvent extends CommonEvent
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -237047205883003847L;

	/**
	 * job名
	 */
	private String jobName;

	/**
	 *  0未执行，1执行失败
	 */
	private int type;

	/**
	 * 批次号
	 */
	private int batchNo;

	/**
	 * 分片号
	 */
	private int shardingNo;

	/**
	 * 总分片数
	 */
	private int totalSharding;

	/**
	 * 错误堆栈
	 */
	private String stack;

	/**
	 * 包括本次的失败次数
	 */
	private int failTimes;

	/**
	 * 创建事件
	 */
	private String createTime;

	/**
	 * 本次时间
	 */
	private String nowTime;

	/**
	 * 企业id
	 */
	private String tenantId;

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public int getBatchNo()
	{
		return batchNo;
	}

	public void setBatchNo(int batchNo)
	{
		this.batchNo = batchNo;
	}

	public int getShardingNo()
	{
		return shardingNo;
	}

	public void setShardingNo(int shardingNo)
	{
		this.shardingNo = shardingNo;
	}

	public int getTotalSharding()
	{
		return totalSharding;
	}

	public void setTotalSharding(int totalSharding)
	{
		this.totalSharding = totalSharding;
	}

	public String getStack()
	{
		return stack;
	}

	public void setStack(String stack)
	{
		this.stack = stack;
	}

	public int getFailTimes()
	{
		return failTimes;
	}

	public void setFailTimes(int failTimes)
	{
		this.failTimes = failTimes;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

	public String getNowTime()
	{
		return nowTime;
	}

	public void setNowTime(String nowTime)
	{
		this.nowTime = nowTime;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getTenantId()
	{
		return tenantId;
	}

	public void setTenantId(String tenantId)
	{
		this.tenantId = tenantId;
	}

	public ElasticJobFailedEvent(String tenantId, String jobName, int type, int batchNo, int shardingNo,
			int totalSharding, Exception e, int failTimes, String createTime, String nowTime)
	{
		super(jobName);

		this.jobName = jobName;

		this.type = type;

		this.batchNo = batchNo;

		this.shardingNo = shardingNo;

		this.totalSharding = totalSharding;

		this.stack = StackTraceHelper.getStackTrace(e);

		this.failTimes = failTimes;

		this.createTime = createTime;

		this.nowTime = nowTime;

		this.tenantId = tenantId;

	}

	@Override
	public EventTypeEnum getEventType()
	{
		return EventTypeEnum.EVENT_TYPE_ES_JOB_FAILED;
	}

}