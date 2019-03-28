package com.framework.core.dal.datasource;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.InitializingBean;
import com.alibaba.fastjson.JSON;
import com.framework.core.dal.datasource.model.DataSourceInstance;

/**
 * 
 * 数据源管理
 * 
 * @author zhangjun
 *
 */
public class DataSourceManager implements InitializingBean{

	private static DataSourceManager instance;

	/** 企业数据源缓存 */
	private static final Map<String, DataSourceInstance> dataSources = new ConcurrentHashMap<String, DataSourceInstance>();

	/**
	 * lookup key
	 */
	private static ThreadLocal<Object> lookupKeyLocal = new ThreadLocal<Object>();

	/**
	 * 主库/从库标志
	 */
	private static ThreadLocal<Boolean> isMaster = new ThreadLocal<Boolean>();
	
	/**
	 * 强制释放数据库连接标志
	 */
	private static ThreadLocal<Boolean> forceReleaseLocal = new ThreadLocal<>();

	/**
	 * 事务标志
	 */
	private static ThreadLocal<Boolean> transactionLocal = new ThreadLocal<>();


	private DynamicDataSourceFactory dynamicDataSourceFactory;

	public DynamicDataSourceFactory getDynamicDataSourceFactory() {
		return dynamicDataSourceFactory;
	}

	public void setDynamicDataSourceFactory(DynamicDataSourceFactory dynamicDataSourceFactory) {
		this.dynamicDataSourceFactory = dynamicDataSourceFactory;
	}

	/**
	 * 获取当前的lookup key
	 * 
	 * @return
	 */
	public static Object getCurrentDateSourceLookupKey() {
		return lookupKeyLocal.get();
	}
	
	
	/**
	 * 设置当前的lookup key
	 * 
	 * @return
	 */
	public static void setCurrentDateSourceLookupKey(Object lookupKey) {
		 lookupKeyLocal.set(lookupKey);
	}	
	

	/**
	 * 当前是主库还是从库.
	 * 
	 * 
	 * @return true 表示 主库 false 表示从库
	 */
	public static boolean isMasterOrSlave() {

		return isMaster.get() == null ? false : isMaster.get();
	}
	
	
	/**
	 * 设置当前是 主库还是从库
	 * @param isMaster
	 */
	public static void setMasterOrSlave(boolean isMasterFlag) {
		
		isMaster.set(isMasterFlag);
	}
	
	

	/**
	 * 根据 lookup key 获取 DataSourceInstance对象
	 * 
	 * @param lookupKey
	 * @return
	 */
	public static DataSourceInstance getDataSourceInstance(Object lookupKey) {

		String jsonLookUpKey = JSON.toJSONString(lookupKey);

		return dataSources.get(jsonLookUpKey);
	}

	/**
	 * 注册数据源
	 * 
	 * @param lookupKey
	 * @param instance
	 */
	public static void registerDataSourceInstance(Object lookupKey, DataSourceInstance instance) {
		String jsonLookUpKey = JSON.toJSONString(lookupKey);

		dataSources.put(jsonLookUpKey, instance);
	}

	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	
	/**
	 * 创建数据源
	 * 
	 * @param lookupKey
	 * @return
	 */
	public static DataSourceInstance createDataSourceInstance(Object lookupKey) {
		return instance.dynamicDataSourceFactory.createDataSourceInstance(lookupKey);
	}
	
	
	/**
	 * 设置强制释放数据库连接的flag
	 * 
	 * @param flag
	 */
	public static void setForceRelease(boolean flag) {

		forceReleaseLocal.set(flag);

	}

	/**
	 * 删除flag
	 */
	public static void removeForceRelease() {

		forceReleaseLocal.remove();
	}

	/**
	 * 是否强制释放连接
	 * 
	 * @return
	 */
	public static boolean isForceReleaseConn() {

		return forceReleaseLocal.get() == null ? false : forceReleaseLocal.get();
	}

	/**
	 * 设置事务标志
	 * 
	 * @param flag
	 */
	public static void setTransactionFlag(boolean flag) {

		transactionLocal.set(flag);

	}

	/**
	 * 删除
	 */
	public static void removeTransactionFlag() {

		transactionLocal.remove();
	}

	/**
	 * 看是否在事务范围内
	 * 
	 * @return
	 */
	public static boolean isInTransaction() {

		return transactionLocal.get() == null ? false : transactionLocal.get();
	}

	/**
	 * 
	 */
	public static void clearAllThreadLocal() {

		transactionLocal.remove();
		forceReleaseLocal.remove();
		isMaster.remove();
		lookupKeyLocal.remove();
	}


	/**
	 * 根据lookupkey 清理数据源信息
	 */
	public static void destoryDataSourcePool(Object lookupKey) {
		
		String jsonLookUpKey = JSON.toJSONString(lookupKey);

		DataSourceInstance dataSourceInstance = dataSources.remove(jsonLookUpKey);
		
		if(dataSourceInstance == null) {
			return;
		}
		
		instance.dynamicDataSourceFactory.destoryDataSourceInstance(dataSourceInstance);
	}
	
	
	/**
	 * 释放连接，不回收到连接池
	 * 
	 * @param conn
	 */
	public static void killConnection(Connection conn) {
		
		instance.dynamicDataSourceFactory.killConnection(conn);
	}
}