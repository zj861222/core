package com.framework.core.dal.datasource.model;

/**
 * 
 * @author zhangjun
 *
 */
public class DataSourceUnit {
	
	/**
	 * 数据库url
	 */
	private String dbUrl;
	
	/**
	 * 驱动
	 */
	private String dbDriver;
	
	/**
	 * 用户名
	 */
	private String userName;
	
	/**
	 * 密码
	 */
	private String passWord;
	
	/**
	 * 别名
	 */
	private String alias;
	
	/**
	 * 最大活动连接数
	 */
	private int maxActive;
	
	/**
	 * 最小idle 连接数
	 */
	private int minIdle;
	
	/**
	 * 定时任务检测连接是否可用的间隔
	 */
	private int checkInterval;
	
	/**
	 * 空闲数据连接断开的时间
	 */
	private int maxConnectionLifetime;
	

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public int getMaxConnectionLifetime() {
		return maxConnectionLifetime;
	}

	public void setMaxConnectionLifetime(int maxConnectionLifetime) {
		this.maxConnectionLifetime = maxConnectionLifetime;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
	


	
}