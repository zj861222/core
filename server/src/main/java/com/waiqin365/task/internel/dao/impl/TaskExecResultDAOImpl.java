//package com.waiqin365.task.internel.dao.impl;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.stereotype.Component;
//
//import com.waiqin365.task.internel.dao.TaskExecResultDAO;
//import com.waiqin365.task.internel.model.TaskExecResult;
//
//
//@Component
//
//public class TaskExecResultDAOImpl implements TaskExecResultDAO {
//
//	@Override
//	public void insertBatchTaskExecInfo(List<TaskExecResult> list) {
//		
//	}
//
//	@Override
//	public int updateToSuccess(String jobName, int batchNo, long tenantId) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public int updateToFail(String jobName, int batchNo, long tenantId) {
//		return 0;
//	}
//
//	@Override
//	public int updateToGiveUp(String jobName, int batchNo, long tenantId) {
//		return 0;
//	}
//
//	@Override
//	public List<TaskExecResult> queryWaitToExecRecordBySharding(String jobName, int batchNo,int totalSharding, int sharding,
//			int batchLimit) {
//		
//		
//		double num = Math.random();
//		
//		if(num>0.8){
//			return null;
//		}
//		
//		List<TaskExecResult> list = new ArrayList<TaskExecResult>();
//		
//		TaskExecResult result1 = TaskExecResult.createRecord(jobName, batchNo, 11111);
//		
//		TaskExecResult result2 = TaskExecResult.createRecord(jobName, batchNo, 22222);
//		
//		TaskExecResult result3 = TaskExecResult.createRecord(jobName, batchNo, 33333);
//
//		list.add(result1);
//		list.add(result2);
//		list.add(result3);
//
//		return list;
//	}
//
//	@Override
//	public List<TaskExecResult> queryExecFailedRecordBySharding(String jobName,int batchNo, int totalSharding, int sharding,
//			int batchLimit) {
//		
//		
//		double num = Math.random();
//		
//		if(num>0.8){
//			return null;
//		}
//		
//		TaskExecResult result2 = TaskExecResult.createRecord(jobName, batchNo, 22222);
//		result2.setStatus(TaskExecResult.STAT_FAIL);
//		result2.setFailTimes(1);
//		
//		
//		List<TaskExecResult> list = new ArrayList<TaskExecResult>();
//		list.add(result2);
//
//		
//		return list;
//	}
//	
//}