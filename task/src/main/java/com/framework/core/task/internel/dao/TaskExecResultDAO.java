package com.framework.core.task.internel.dao;

import java.util.Date;
import java.util.List;

import com.framework.core.task.internel.model.TaskExecResult;


/**
 * 
 * 
 * job 执行结果
 * 
 * @author zhangjun
 *
 */
public interface TaskExecResultDAO {
	
	
	
	/**
	 * 批量插入
	 * @param list
	 */
	void insertBatchTaskExecInfo(List<TaskExecResult> list);
	
	
	/**
	 * 更新记录的状态为成功
	 * @param jobName jobname
	 * @param batchNo 批次号
	 * @param tenantId 企业id
	 * @param status 状态
	 * @return
	 */
	int updateToSuccess(String jobName,int batchNo,long tenantId);
	
	/**
	 * 状态为失败，并且失败次数加1
	 * 
	 * @param jobName
	 * @param batchNo
	 * @param tenantId
	 * @return
	 */
	int updateToFail(String jobName,int batchNo,long tenantId);
	
	
	/**
	 * 状态为放弃执行，失败次数加1
	 * 
	 * @param jobName
	 * @param batchNo
	 * @param tenantId
	 * @return
	 */
	int updateToGiveUp(String jobName,int batchNo,long tenantId);
	
	
	
	
	/**
	 * 状态为正在执行
	 * 
	 * @param jobName
	 * @param batchNo
	 * @param tenantId
	 * @return
	 */
	int updateToRunning(String jobName,int batchNo,long tenantId);

	
	
	/**
	 * 根据总分片数和当前分片信息查询 状态为 0-初始化的数据
	 * @param jobName
	 * @param batchNo
	 * @param totalSharding
	 * @param sharding
	 * @param batchLimit
	 * @return
	 */
	List<TaskExecResult> queryWaitToExecRecordBySharding(String jobName,int batchNo,int totalSharding,int sharding,int batchLimit);
	
	/**
	 * 根据总分片数和当前分片信息查询查询状态为失败，而不是giveup的数据，
	 * @param jobName
	 * @param batchNo
	 * @param totalSharding
	 * @param sharding
	 * @param batchLimit
	 * @return
	 */
	List<TaskExecResult> queryExecFailedRecordBySharding(String jobName,int batchNo,int totalSharding,int sharding,int batchLimit);

	
	/**
	 * 查询执行中状态的
	 * @param jobName
	 * @param batchNo
	 * @param totalSharding
	 * @param sharding
	 * @param batchLimit
	 * @return
	 */
	List<TaskExecResult> queryExecRunningRecordBySharding(String jobName,int batchNo,int totalSharding,int sharding,int batchLimit);
	
	
	/**
	 * 不分片，直接查询
	 * @param jobName
	 * @param batchNo
	 * @param batchLimit
	 * @param status
	 * @return
	 */
	List<TaskExecResult> queryExecRecordByStatus(String jobName, int batchNo, int batchLimit,int status) ;

	
	
	
	/**
	 * 查询某个时间点 n小时之前的待执行的
	 * @param fromDate
	 * @param nHoursBefore
	 * @return
	 */
	List<TaskExecResult> queryWaittingStatusRecordFromNHoursBefore(Date fromDate,int nHoursBefore);
	
	
}