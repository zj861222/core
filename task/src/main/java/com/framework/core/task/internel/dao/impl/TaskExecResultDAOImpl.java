package com.framework.core.task.internel.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.dangdang.ddframe.job.util.env.IpUtils;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.task.internel.dao.BaseDao;
import com.framework.core.task.internel.dao.TaskExecResultDAO;
import com.framework.core.task.internel.model.TaskExecResult;


public class TaskExecResultDAOImpl extends BaseDao implements TaskExecResultDAO {

	@Override
	public void insertBatchTaskExecInfo(List<TaskExecResult> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		sqlSessionTemplate.insert(getSqlName("insertBatchTaskExecInfo"), list);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int updateToSuccess(String jobName, int batchNo, long tenantId) {

		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);
		map.put("tenantId", tenantId);
		map.put("ip", IpUtils.getIp());

		map.put("status", TaskExecResult.STAT_SUCCESS);

		return sqlSessionTemplate.update(getSqlName("updateStatus"), map);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int updateToFail(String jobName, int batchNo, long tenantId) {

		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);
		map.put("tenantId", tenantId);
		map.put("ip", IpUtils.getIp());

		map.put("status", TaskExecResult.STAT_FAIL);

		return sqlSessionTemplate.update(getSqlName("updateToFailOrGiveUp"), map);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int updateToGiveUp(String jobName, int batchNo, long tenantId) {

		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);
		map.put("tenantId", tenantId);
		map.put("ip", IpUtils.getIp());

		map.put("status", TaskExecResult.STAT_GIVE_UP);

		return sqlSessionTemplate.update(getSqlName("updateToFailOrGiveUp"), map);
	}

	@Override
	public List<TaskExecResult> queryWaitToExecRecordBySharding(String jobName, int batchNo, int totalSharding,
			int sharding, int batchLimit) {

		return queryByStatus(jobName, batchNo, totalSharding, sharding, batchLimit, TaskExecResult.STAT_INIT);

	}

	@Override
	public List<TaskExecResult> queryExecFailedRecordBySharding(String jobName, int batchNo, int totalSharding,
			int sharding, int batchLimit) {

		return queryByStatus(jobName, batchNo, totalSharding, sharding, batchLimit, TaskExecResult.STAT_FAIL);

	}

	@Override
	public List<TaskExecResult> queryExecRunningRecordBySharding(String jobName, int batchNo, int totalSharding,
			int sharding, int batchLimit) {

		return queryByStatus(jobName, batchNo, totalSharding, sharding, batchLimit, TaskExecResult.STAT_RUNNING);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<TaskExecResult> queryByStatus(String jobName, int batchNo, int totalSharding, int sharding,
			int batchLimit, int status) {
		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);
		map.put("totalSharding", totalSharding);
		map.put("sharding", sharding);
		map.put("batchLimit", batchLimit);
		map.put("status", status);

		return sqlSessionTemplate.selectList(getSqlName("queryExecRecordByShardingByStatus"), map);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int updateToRunning(String jobName, int batchNo, long tenantId) {
		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);
		map.put("tenantId", tenantId);
		map.put("ip", IpUtils.getIp());

		map.put("status", TaskExecResult.STAT_RUNNING);

		return sqlSessionTemplate.update(getSqlName("updateStatus"), map);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<TaskExecResult> queryExecRecordByStatus(String jobName, int batchNo, int batchLimit, int status) {
		Map map = new HashMap();
		map.put("jobName", jobName);
		map.put("batchNo", batchNo);

		map.put("batchLimit", batchLimit);
		map.put("status", status);

		return sqlSessionTemplate.selectList(getSqlName("queryExecRecordByStatus"), map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TaskExecResult> queryWaittingStatusRecordFromNHoursBefore(Date date,int nHoursBefore)
	{
		
		Date fromDate = DateUtil.getDateBeforeMinutes(date, nHoursBefore*60);
		
		@SuppressWarnings("rawtypes")
		Map map = new HashMap();
		map.put("startTime", fromDate);
		map.put("status", 0);

		
		return sqlSessionTemplate.selectList(getSqlName("queryWaittingStatusRecordFromNHoursBefore"), map);
		
	}

}