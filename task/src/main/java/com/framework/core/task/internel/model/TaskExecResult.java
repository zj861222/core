package com.framework.core.task.internel.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;



/**
 * 各企业执行job的结果
 * 
 * 
 * ALTER TABLE task_exec_result ADD sharding_filed int8 not null default 0;
 * 
 * update task_exec_result set sharding_filed =  tenant_id where status = 0 and create_time > '2018-09-10';
 * 
 * 
 * @author zhangjun
 *
 */
public class TaskExecResult {
	
	
	/**
	 * 初始化
	 */
	public static final int STAT_INIT = 0;
	
	/**
	 * 执行成功
	 */
	public static final int STAT_SUCCESS = 1;
	
	/**
	 * 执行失败
	 */
	public static final int STAT_FAIL = 2;
	
	/**
	 * 失败次数过多，放弃
	 */
	public static final int STAT_GIVE_UP = 3;
	
	/**
	 * 运行中
	 */
	public static final int STAT_RUNNING = 4;



	
	/**
	 * 任务名
	 */
	private String jobName;
	
	/**
	 * 企业id
	 */
	private Long tenantId;
	
	
	/**
	 * 批次号
	 */
	private Integer batchNo;
	
	/**
	 * 状态  0-等待执行  1-执行成功 2-执行失败 3-失败次数太多，放弃  4-执行中
	 */
	private Integer status;
	
	/**
	 * 失败次数
	 */
	private Integer failTimes;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 修改时间
	 */
	private Date modifyTime;
	
	
	/**
	 * 执行机器的ip
	 */
	private String ip;
	
	
	/**
	 * 用来计算分片的字段，这里用的是简单的逻辑:用 tenantId+job名的长度。
	 * 
	 * 为什么不直接用 tenantId，直接用tenantid会导致一个企业的job全部到一台机器上执行了
	 */
	private Long shardingFiled;
	
	
	public Long getShardingFiled()
	{
		return shardingFiled;
	}

	public void setShardingFiled(Long shardingFiled)
	{
		this.shardingFiled = shardingFiled;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Integer getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(Integer batchNo) {
		this.batchNo = batchNo;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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
	
	
	
	
	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public Integer getFailTimes() {
		return failTimes;
	}

	public void setFailTimes(Integer failTimes) {
		this.failTimes = failTimes;
	}

	/**
	 * 创建一个记录
	 * @param jobName
	 * @param batchNum
	 * @param tenantId
	 * @return
	 */
	public static TaskExecResult  createRecord(String jobName,int batchNum,long tenantId) {

		TaskExecResult record = new TaskExecResult();
		
		record.setTenantId(tenantId);
		record.setJobName(jobName);
		record.setBatchNo(batchNum);
		record.setFailTimes(0);
		record.setStatus(STAT_INIT);
		
		Date now = new Date();
		record.setCreateTime(now);
		record.setModifyTime(now);
		
		record.setIp("");
		
		long shardingField = tenantId+jobName.length();
		record.setShardingFiled(shardingField);
		
		return record;
	}
	
	
	/**
	 * 批量创建
	 * @param jobName
	 * @param batchNum
	 * @param tenantId
	 * @return
	 */
	public static List<TaskExecResult> createBatchRecords(String jobName,int batchNum,List<Long> tenantIds) {
		
		if(CollectionUtils.isEmpty(tenantIds)) {
			return null;
		}
		
		List<TaskExecResult> list = new ArrayList<TaskExecResult>();
		for(Long id:tenantIds) {
			list.add(createRecord(jobName, batchNum, id));
		}
		
		return list;
	}
	
	

}