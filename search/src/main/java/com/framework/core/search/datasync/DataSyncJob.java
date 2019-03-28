package com.framework.core.search.datasync;

/**
 * 数据同步的job
 * @author zhangjun
 *
 */
public class DataSyncJob
{

	/**
	 * 任务名
	 */
	private String jobName;

	/**
	 * 页数
	 */
	private int pageNo;

	/**
	 * 每页大小
	 */
	private int pageSize;

	/**
	 * job状态
	 */
	private int status;

	/**
	 * 任务描述
	 */
	private String desc;

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public int getPageNo()
	{
		return pageNo;
	}

	public void setPageNo(int pageNo)
	{
		this.pageNo = pageNo;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	/**
	 * 
	 * @param name job名
	 * @param pageNo 页数
	 * @param pageSize 每页大小
	 * @param status 状态
	 */
	public DataSyncJob(String name, int pageNo, int pageSize, int status)
	{

		this.jobName = name;

		this.pageNo = pageNo;

		this.pageSize = pageSize;

	}

	/**
	 * 
	 * @param name job名
	 * @param pageNo 页数
	 * @param pageSize 每页大小
	 * @param status 状态
	 * @param desc 描述
	 */
	public DataSyncJob(String name, int pageNo, int pageSize, int status, String desc)
	{

		this.jobName = name;

		this.pageNo = pageNo;

		this.pageSize = pageSize;

		this.desc = desc;

	}

}