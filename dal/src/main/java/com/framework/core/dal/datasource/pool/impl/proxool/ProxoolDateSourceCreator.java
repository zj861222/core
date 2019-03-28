package com.framework.core.dal.datasource.pool.impl.proxool;

import java.sql.Connection;

import javax.sql.DataSource;
import org.logicalcobwebs.proxool.ProxoolDataSource;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;

import com.framework.core.dal.datasource.model.DataSourceUnit;
import com.framework.core.dal.datasource.pool.DataSourceCreator;
import com.framework.core.dal.exception.DalErrorCode;
import com.framework.core.error.exception.BizException;

/**
 * 
 * proxool 数据源创建
 * 
 * @author zhangjun
 *
 */
public class ProxoolDateSourceCreator extends DataSourceCreator {

	/**
	 * 增加数据库连接重试机制
	 */
	private boolean testBeforeUse = true;

	/**
	 * 测试sql
	 */
	private String houseKeepingTestSql = "select 1";

	/**
	 * 同一时刻允许申请最大连接数
	 */
	private int simultaneousBuildThrottle = 50;

	public boolean isTestBeforeUse() {
		return testBeforeUse;
	}

	public void setTestBeforeUse(boolean testBeforeUse) {
		this.testBeforeUse = testBeforeUse;
	}

	public String getHouseKeepingTestSql() {
		return houseKeepingTestSql;
	}

	public void setHouseKeepingTestSql(String houseKeepingTestSql) {
		this.houseKeepingTestSql = houseKeepingTestSql;
	}

	public int getSimultaneousBuildThrottle() {
		return simultaneousBuildThrottle;
	}

	public void setSimultaneousBuildThrottle(int simultaneousBuildThrottle) {
		this.simultaneousBuildThrottle = simultaneousBuildThrottle;
	}

	
	
	
	/**
	 * 创建数据源
	 * 
	 * @param unit
	 * @return
	 */
	protected DataSource createDataSource(DataSourceUnit unit) {

		ProxoolDataSource pds = new ProxoolDataSource();

		pds.setAlias(unit.getAlias());

		pds.setDriverUrl(unit.getDbUrl());

		pds.setDriver(unit.getDbDriver());
		pds.setUser(unit.getUserName());
		pds.setPassword(unit.getPassWord());

		pds.setMinimumConnectionCount(unit.getMinIdle());
		pds.setMaximumConnectionCount(unit.getMaxActive());

		// 空闲数据库连接断开的时间使用数据库的sleep_keep_time
		pds.setMaximumConnectionLifetime(unit.getMaxConnectionLifetime());

		if (unit.getCheckInterval() > 0) {
			pds.setHouseKeepingSleepTime(unit.getCheckInterval());
		}

		// 设置 druid的特殊属性
		setSpecialAttrbute(pds);

		return pds;
	}

	/**
	 * 设置druid 特有的属性
	 * 
	 * @param ds
	 */
	private void setSpecialAttrbute(ProxoolDataSource pds) {

		// 同一时刻允许申请最大连接数
		pds.setSimultaneousBuildThrottle(simultaneousBuildThrottle);
		// 增加数据库连接重试机制
		pds.setTestBeforeUse(testBeforeUse);
		pds.setHouseKeepingTestSql(houseKeepingTestSql);
		
	}

	@Override
	public void destoryDataSourcePool(DataSource ds) {
		
		try {
		    ProxoolDataSource pds = (ProxoolDataSource)ds;
		
			ProxoolFacade.removeConnectionPool(pds.getAlias());
		} catch (Exception e) {
			e.printStackTrace();
			throw new BizException(DalErrorCode.EX_SYSTEM_DB_FALTE_EX_DESTORY_DATASOURCE_FAILED.getCode());
		}
	}

	@Override
	public void killConnection(Connection conn) {
		
		try {
			ProxoolFacade.killConnecton(conn, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}