package com.framework.core.dal.datasource;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.fastjson.JSON;
import com.framework.core.common.lock.SegmentLock;
import com.framework.core.dal.datasource.constant.ConnPoolStrategyEnum;
import com.framework.core.dal.datasource.custom.DataSourceInfoFetcher;
import com.framework.core.dal.datasource.model.DataSourceInfo;
import com.framework.core.dal.datasource.model.DataSourceInstance;
import com.framework.core.dal.datasource.pool.DataSourceCreator;
import com.framework.core.dal.exception.DalErrorCode;
import com.framework.core.error.exception.BizException;

/**
 * 
 * DynamicDataSourceFactor
 * 
 * @author zhangjun
 *
 */
public class DynamicDataSourceFactory implements ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private SegmentLock<String> segmentLock = new SegmentLock<>(30, false);

	private DataSourceInfoFetcher dataSourceInfoFetcher;

	private DataSourceCreator dataSourceCreator;

	public DataSourceCreator getDataSourceCreator() {
		return dataSourceCreator;
	}

	public void setDataSourceCreator(DataSourceCreator dataSourceCreator) {
		this.dataSourceCreator = dataSourceCreator;
	}

	public DataSourceInfoFetcher getDataSourceInfoFetcher() {
		return dataSourceInfoFetcher;
	}

	public void setDataSourceInfoFetcher(DataSourceInfoFetcher dataSourceInfoFetcher) {
		this.dataSourceInfoFetcher = dataSourceInfoFetcher;
	}

	/**
	 * 创建数据源信息
	 * 
	 * @param lookupKey
	 * @return
	 */
	public DataSourceInstance createDataSourceInstance(Object lookupKey) {

		String jsonLookUpKey = JSON.toJSONString(lookupKey);
		// 尝试获取锁去执行 数据源初始化
		if (!segmentLock.lock(jsonLookUpKey, 60, TimeUnit.SECONDS)) {
			return null;
		}

		try {
			// 获取到锁之后，再检查下，数据源是否已经初始化成功了。
			DataSourceInstance dataSourceInstance = DataSourceManager.getDataSourceInstance(lookupKey);
			if (dataSourceInstance != null) {
				return dataSourceInstance;
			}

			if (dataSourceInfoFetcher == null) {
				throw new BizException(DalErrorCode.EX_SYSTEM_DB_FALTE_EX_DB_INFO_FATCHER_NOT_IMPLEMENT.getCode());
			}

			// 从业务获取数据源信息
			DataSourceInfo info = dataSourceInfoFetcher.fetchBizDataSourceInfo(jsonLookUpKey);
			info.validate();

			DataSourceInstance instance = dataSourceCreator.createDataSourceInstance(info);

			// 如果不为null，注册进去
			if (instance != null) {
				DataSourceManager.registerDataSourceInstance(lookupKey, instance);
			}

			return instance;

		} finally {
			try {
				segmentLock.unlock(jsonLookUpKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 决定当前使用的 数据库连接池技术，proxool,druid 还是其他,默认使用druid，可以重写此方法
	 * 
	 * @return
	 */
	public ConnPoolStrategyEnum determinConnPoolStrategy() {

		return ConnPoolStrategyEnum.CONN_POOL_PROXOOL;

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		initDataSourceInfoFetcher(applicationContext);

		determinCurrentDataSourceCreator(applicationContext);

	}

	/**
	 * 初始化 bean，获取实现 数据源 信息的实例
	 * 
	 * @param applicationContext
	 */
	private void initDataSourceInfoFetcher(ApplicationContext applicationContext) {

		Map<String, DataSourceInfoFetcher> map = applicationContext.getBeansOfType(DataSourceInfoFetcher.class);

		if (MapUtils.isEmpty(map) || map.size() > 1) {
			log.warn(
					"DynamicDataSourceFactory-----setApplicationContext: getBeansOfType DataSourceInfoFetcher result is:"
							+ (MapUtils.isEmpty(map) ? 0 : map.size()));
			return;
		}

		for (Entry<String, DataSourceInfoFetcher> entry : map.entrySet()) {
			dataSourceInfoFetcher = entry.getValue();
			break;
		}
	}

	/**
	 * 决定当前datasource的创建源
	 * 
	 * @param applicationContext
	 */
	private void determinCurrentDataSourceCreator(ApplicationContext applicationContext) {

		Map<String, DataSourceCreator> map = applicationContext.getBeansOfType(DataSourceCreator.class);

		if (MapUtils.isEmpty(map)) {

			throw new RuntimeException("There is no DataSourceCreator instance which is registered as bean");
		}

		ConnPoolStrategyEnum strategy = determinConnPoolStrategy();

		dataSourceCreator = map.get(strategy.getBeanName());

		if (dataSourceCreator == null) {

			throw new RuntimeException(
					"There is no DataSourceCreator instance which is registered with the bean name :[ "
							+ strategy.getBeanName() + "]");

		}

	}
	
	/**
	 * 清理掉 数据库连接池
	 * @param DataSourceInstance
	 */
	public void destoryDataSourceInstance(DataSourceInstance dataSourceInstance) {
		
		DataSource master = dataSourceInstance.getMaster();
		dataSourceCreator.destoryDataSourcePool(master);
		
		List<DataSource> slaves = dataSourceInstance.getSlaves();
		
		if(CollectionUtils.isEmpty(slaves)){
			return;
		}
		
		for(DataSource ds:slaves){
			dataSourceCreator.destoryDataSourcePool(ds);
		}
	}
	
	
	/**
	 * 强制kill连接池中的连接，而不是放回到连接池
	 * @param conn
	 */
	public void killConnection(Connection conn) {
		
	}
	

}