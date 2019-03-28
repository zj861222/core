package com.framework.core.task.internel.dao.impl;

import java.util.HashMap;
import java.util.Map;


import com.framework.core.task.internel.dao.BaseDao;
import com.framework.core.task.internel.dao.TaskScheduleDAO;
import com.framework.core.task.internel.model.TaskSchedule;

public class TaskScheduleDAOImpl  extends BaseDao implements TaskScheduleDAO {

	@Override
	public TaskSchedule queryTaskScheduleByJobName(String jobName) {

		return sqlSessionTemplate.selectOne(getSqlName("queryTaskScheduleByJobName"), jobName);
	}

	@Override
	public int insertTaskSchedule(TaskSchedule taskSchedule) {
		return sqlSessionTemplate.insert(getSqlName("insertOrUpdateTaskSchedule"), taskSchedule);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int updateTaskSchedule(String jobName, int batchNo) {
		
		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);
		
		return sqlSessionTemplate.update(getSqlName("updateTaskSchedule"), map);
	}
	
}