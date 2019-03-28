//package com.waiqin365.task.internel.dao;
//
//import java.util.List;
//
//import com.waiqin365.task.internel.model.TaskSchedule;
//
///**
// * task批次信息dao
// * @author zhangjun
// *
// */
//public interface TaskScheduleDAO {
//	
//	
//	/**
//	 * 根据job name 查询
//	 * @param jobName
//	 * @return
//	 */
//	TaskSchedule queryTaskScheduleByJobName(String jobName);
//	
//	
//	/**
//	 * 插入或者删除
//	 * @param taskSchedule
//	 */
//	int insertOrUpdateTaskSchedule(TaskSchedule taskSchedule);
//	
//	/**
//	 * 更新批次号
//	 * @param jobName
//	 * @param batchNo
//	 * @return
//	 */
//	int updateTaskSchedule(String jobName,int batchNo);
//	
//
//}