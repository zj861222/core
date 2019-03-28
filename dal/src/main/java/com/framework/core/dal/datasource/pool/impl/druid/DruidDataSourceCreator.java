package com.framework.core.dal.datasource.pool.impl.druid;


import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.framework.core.dal.datasource.model.DataSourceUnit;
import com.framework.core.dal.datasource.pool.DataSourceCreator;
import com.framework.core.dal.exception.DalErrorCode;
import com.framework.core.error.exception.BizException;

/**
 * druid数据源创建
 * 
 * @author zhangjun
 *
 */
public class DruidDataSourceCreator extends DataSourceCreator {

	
	/**
	 * 最大等待连接数
	 */
	private int maxWait;

	/**
	 * 用来检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用
	 */
	private String validationQuery;
	
    /**
     * 有两个含义：
     * 1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接。
     * 2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
     */
    private long timeBetweenEvictionRunsMillis;
    
    /**
     * 连接保持空闲而不被驱逐的最长时间
     */
    private long minEvictableIdleTimeMillis;	
	
	/**
	 * 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效
	 */
	private boolean testWhileIdle = true;
	
	/**
	 * 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。。
	 */
	private boolean testOnBorrow = false;
	
	/**
	 * 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
	 */
	private boolean testOnReturn = false;

	/**
	 * 初始化连接数
	 */
	private int initSize = 5;
	
	/**
	 * 最大等待连接的时间
	 */
	private long maxWaitTimeMills;
	

	public int getInitSize() {
		return initSize;
	}



	public void setInitSize(int initSize) {
		this.initSize = initSize;
	}



	public long getMaxWaitTimeMills() {
		return maxWaitTimeMills;
	}



	public void setMaxWaitTimeMills(long maxWaitTimeMills) {
		this.maxWaitTimeMills = maxWaitTimeMills;
	}



	public int getMaxWait() {
		return maxWait;
	}



	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}



	public String getValidationQuery() {
		return validationQuery;
	}



	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}



	public long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}



	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}



	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}



	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}



	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}



	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}



	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}



	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}



	public boolean isTestOnReturn() {
		return testOnReturn;
	}



	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}



	
	/**
	 * 创建数据源
	 * 
	 * @param unit
	 * @return
	 */
	protected DataSource createDataSource(DataSourceUnit unit) {

		DruidDataSource ds = new DruidDataSource();
		
		ds.setUrl(unit.getDbUrl());

		ds.setDriverClassName(unit.getDbDriver());
		ds.setUsername(unit.getUserName());
		ds.setPassword(unit.getPassWord());
		
		ds.setMinIdle(unit.getMinIdle());
		ds.setMaxActive(unit.getMaxActive());
		
		//设置 druid的特殊属性
		setSpecialAttrbute(ds);
		
		return ds;
	}
	
	/**
	 * 设置druid 特有的属性
	 * @param ds
	 */
	private void setSpecialAttrbute(DruidDataSource ds) {
		
		ds.setMaxWait(maxWaitTimeMills);
		ds.setInitialSize(initSize);
		
		ds.setValidationQuery(validationQuery);
		ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		
		ds.setTestOnBorrow(testOnBorrow);
		ds.setTestOnReturn(testOnReturn);
		ds.setTestWhileIdle(testWhileIdle);
		
	}



	@Override
	public void destoryDataSourcePool(DataSource ds) {
		
		try {
			DruidDataSource dds = (DruidDataSource)ds;
		
			dds.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BizException(DalErrorCode.EX_SYSTEM_DB_FALTE_EX_DESTORY_DATASOURCE_FAILED.getCode());
		}
		
	}



	@Override
	public void killConnection(Connection conn) {
		
		try {
			DruidPooledConnection dpconn = (DruidPooledConnection)conn;

			dpconn.close();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
			throw new BizException(DalErrorCode.EX_SYSTEM_DB_FALTE_EX_DESTORY_DATASOURCE_FAILED.getCode());

		}
		
	}
	
}