package com.framework.core.dal.datasource.pool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;

import com.framework.core.dal.datasource.model.DataSourceInfo;
import com.framework.core.dal.datasource.model.DataSourceInstance;
import com.framework.core.dal.datasource.model.DataSourceUnit;

/**
 * 创建数据源
 * @author zhangjun
 *
 */
public abstract class DataSourceCreator {
	

	/**
	 * 创建数据源
	 * @param dbInfo
	 * @return
	 */
	public DataSourceInstance createDataSourceInstance(DataSourceInfo dbInfo) {
		
		DataSourceInstance  instance = new DataSourceInstance();
		
		instance.setMaster(createMaster(dbInfo));
		
		instance.setSlaves(createSlaves(dbInfo));
		
		return instance;
	}
	
	
	/**
	 * 创建主库
	 * @param dbInfo
	 * @return
	 */
	private DataSource createMaster(DataSourceInfo dbInfo){
		
		dbInfo.validate();
		
		return createDataSource(dbInfo.getMasterDataSource());
		
	}
	
	
	/**
	 * 创建从库
	 * @param dbInfo
	 * @return
	 */
	private List<DataSource> createSlaves(DataSourceInfo dbInfo) {
		
		if(CollectionUtils.isEmpty(dbInfo.getSlaveDataSources())){
			return null;
		}
		
		List<DataSource> slaves = new ArrayList<DataSource>();
		
		for(DataSourceUnit unit:dbInfo.getSlaveDataSources()) {	
			if(unit ==null) {
				continue;
			}
			slaves.add(createDataSource(unit));
			
		}
		
		return slaves;
		
	}
	
	
	/**
	 * 根据不同的连接池，创建数据源
	 * @param unit
	 * @return
	 */
	protected abstract DataSource createDataSource(DataSourceUnit unit);
	
	/**
	 * 摧毁数据源连接池
	 */
	public abstract void destoryDataSourcePool(DataSource ds);
	
    /**
     * 直接kill连接，而不是放回到连接池继续使用
     * @param conn
     */
	public abstract void killConnection(Connection conn);
}