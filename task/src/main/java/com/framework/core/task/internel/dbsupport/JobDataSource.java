package com.framework.core.task.internel.dbsupport;

import javax.sql.DataSource;

import org.logicalcobwebs.proxool.ProxoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import com.framework.core.task.internel.utils.PropertiesUtil;

public class JobDataSource extends DelegatingDataSource
{

	protected static Logger logger = LoggerFactory.getLogger(JobDataSource.class);

	private DataSource dataSource;

	@Override
	public void afterPropertiesSet()
	{
		// do nothing
	}

	public DataSource getTargetDataSource()
	{

		if (dataSource != null)
		{
			return dataSource;
		}

		DataSource ds = initDataSource();

		logger.debug("DynamicDataSource getTargetDataSource:" + ds.toString());

		return ds;

	}

	/**
	 * 初始化
	 * @return
	 */
	private synchronized DataSource initDataSource()
	{

		if (dataSource != null)
		{
			return dataSource;
		}

		dataSource = initTaskCenterDateSource();

		return dataSource;
	}

	// /**
	// * 初始化任务库
	// * @return
	// */
	// public static DataSource initTaskCenterDateSource() {
	//
	// BasicDataSource ds = new BasicDataSource();
	// String propFile = PropertiesUtil.FILE_CORE_CONFIG;
	// ds.setDriverClassName(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.driver"));
	// ds.setUrl(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.url"));
	// ds.setUsername(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.username"));
	// ds.setPassword(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.password"));
	//
	// ds.setMaxActive(Integer.parseInt(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.maxActive","3")));
	// ds.setMinIdle(Integer.parseInt(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.minIdle","1")));
	// ds.setMaxIdle(Integer.parseInt(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.maxIdle","10")));
	// ds.setMaxWait(Long.parseLong(PropertiesUtil.getProp(propFile,
	// "jobcenter.jdbc.maxWait","3000")));
	//
	// ds.setTimeBetweenEvictionRunsMillis(1000 * 30);
	// ds.setMinEvictableIdleTimeMillis(1000 * 60 * 10);
	//
	// return ds;
	// }

	/**
	 * 初始化任务库
	 * @return
	 */
	public static DataSource initTaskCenterDateSource()
	{

		String propFile = PropertiesUtil.FILE_CORE_CONFIG;

		ProxoolDataSource pds = null;

		// athrun
		// 根据数据源信息切换数据库
		pds = new ProxoolDataSource();
		pds.setAlias("task_center");

		// 分别找到 主库和从库的url

		pds.setDriverUrl(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.url"));
		pds.setDriver(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.driver"));

		pds.setUser(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.username"));
		pds.setPassword(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.password"));
		pds.setMinimumConnectionCount(
				Integer.parseInt(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.minCount", "5")));
		pds.setMaximumConnectionCount(
				Integer.parseInt(PropertiesUtil.getProp(propFile, "jobcenter.jdbc.minCount", "20")));
		// 空闲数据库连接断开的时间使用数据库的sleep_keep_time
		pds.setHouseKeepingSleepTime(1000 * 2);
		// 同一时刻允许申请最大连接数
		pds.setSimultaneousBuildThrottle(50);
		// 增加数据库连接重试机制
		pds.setTestBeforeUse(false);
		pds.setHouseKeepingTestSql("select 1");
		// 连接最大保持时间
		pds.setMaximumConnectionLifetime(1000 * 60 * 10);

		return pds;

	}

}
