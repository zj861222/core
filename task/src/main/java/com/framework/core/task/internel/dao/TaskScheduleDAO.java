package com.framework.core.task.internel.dao;

import com.framework.core.task.internel.model.TaskSchedule;

/**
 * task批次信息dao
 * @author zhangjun
 *
 */
public interface TaskScheduleDAO {
	
	
	/**
	 * 根据job name 查询
	 * @param jobName
	 * @return
	 */
	TaskSchedule queryTaskScheduleByJobName(String jobName);
	
	
	/**
	 * 插入
	 * @param taskSchedule
	 */
	int insertTaskSchedule(TaskSchedule taskSchedule);
	
	/**
	 * 更新批次号
	 * @param jobName
	 * @param batchNo
	 * @return
	 */
	int updateTaskSchedule(String jobName,int batchNo);
	

}