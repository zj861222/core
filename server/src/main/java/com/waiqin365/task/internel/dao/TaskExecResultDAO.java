//package com.waiqin365.task.internel.dao;
//
//import java.util.List;
//
//import com.waiqin365.task.internel.model.TaskExecResult;
//
///**
// * 
// * @author zhangjun
// *
// */
//public interface TaskExecResultDAO {
//	
//	
//	
//	/**
//	 * 批量插入
//	 * @param list
//	 */
//	void insertBatchTaskExecInfo(List<TaskExecResult> list);
//	
//	
//	/**
//	 * 更新记录的状态为成功
//	 * @param jobName jobname
//	 * @param batchNo 批次号
//	 * @param tenantId 企业id
//	 * @param status 状态
//	 * @return
//	 */
//	int updateToSuccess(String jobName,int batchNo,long tenantId);
//	
//	/**
//	 * 状态为失败，并且失败次数加1
//	 * 
//	 * @param jobName
//	 * @param batchNo
//	 * @param tenantId
//	 * @return
//	 */
//	int updateToFail(String jobName,int batchNo,long tenantId);
//	
//	
//	/**
//	 * 状态为放弃执行，失败次数加1
//	 * 
//	 * @param jobName
//	 * @param batchNo
//	 * @param tenantId
//	 * @return
//	 */
//	int updateToGiveUp(String jobName,int batchNo,long tenantId);
//
//	
//	
//	/**
//	 * 根据总分片数和当前分片信息查询 状态为 0-初始化的数据
//	 * @param jobName
//	 * @param batchNo
//	 * @param totalSharding
//	 * @param sharding
//	 * @param batchLimit
//	 * @return
//	 */
//	List<TaskExecResult> queryWaitToExecRecordBySharding(String jobName,int batchNo,int totalSharding,int sharding,int batchLimit);
//	
//	/**
//	 * 根据总分片数和当前分片信息查询查询状态为失败，而不是giveup的数据，
//	 * @param jobName
//	 * @param batchNo
//	 * @param totalSharding
//	 * @param sharding
//	 * @param batchLimit
//	 * @return
//	 */
//	List<TaskExecResult> queryExecFailedRecordBySharding(String jobName,int batchNo,int totalSharding,int sharding,int batchLimit);
//
//	
//}