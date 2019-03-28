package com.framework.core.task.internel.model;

import java.util.Date;

public class TaskSchedule {
	
	
	/**
	 * 初始化
	 */
	public static int STAT_INIT = 0;
	
	/**
	 * 执行中
	 */
	public static int STAT_EXEC = 1;
	
	/**
	 * 完成
	 */
	public static int STAT_COMPLETE = 2;


	
	/**
	 * job名
	 */
	private String jobName;
	
	
	/**
	 * 批次号
	 */
	private Integer batchNo;

	

	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	
	/**
	 * 修改时间
	 */
	private Date modifyTime;


	public String getJobName() {
		return jobName;
	}


	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	public Integer getBatchNo() {
		return batchNo;
	}


	public void setBatchNo(Integer batchNo) {
		this.batchNo = batchNo;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public Date getModifyTime() {
		return modifyTime;
	}


	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	
	
	/**
	 * 创建task
	 * 
	 * @param jobName
	 * @param batchNo
	 * @return
	 */
	public static TaskSchedule createTask(String jobName,int batchNo) {
		Date now = new Date();
		TaskSchedule record = new TaskSchedule();
		record.setJobName(jobName);
		record.setBatchNo(batchNo);
		record.setCreateTime(now);
		record.setModifyTime(now);
		return record;
	}
	
}