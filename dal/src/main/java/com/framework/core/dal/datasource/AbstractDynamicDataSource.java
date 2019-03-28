package com.framework.core.dal.datasource;


import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

import com.framework.core.dal.datasource.custom.ConnectionWapper;
import com.framework.core.dal.datasource.model.DataSourceInstance;

/**
 * 动态数据源
 * @author zhangjun
 *
 */
public abstract class AbstractDynamicDataSource extends AbstractRoutingDataSource {
	

	/**
	 * 获取连接
	 */
	public Connection getConnection() throws SQLException {
		
		Connection conn =  determineTargetDataSource().getConnection();
		
		//动态代理conn
		return ConnectionWapper.getInstance(conn);
	}	
	
	
	/**
	 * 决定当前数据源，根据 lookupkey+主从库的标签
	 */
	protected DataSource determineTargetDataSource() {
		
		Object lookupKey = determineCurrentLookupKey();
		
		Assert.notNull(lookupKey, "DynamicDataSource lookupKey not set!!!");
		
		DataSourceInstance dataSourceInstance = DataSourceManager.getDataSourceInstance(lookupKey);
		
		if (dataSourceInstance == null ) {
			dataSourceInstance = DataSourceManager.createDataSourceInstance(lookupKey);
		}
		
		//如果创建失败
		if (dataSourceInstance == null) {
			throw new IllegalStateException("Cannot determine target dataSourceInstance for lookup key [" + lookupKey + "]");
		}
		
		//判断主还是从库
		boolean isMaster = DataSourceManager.isMasterOrSlave();
		
		DataSource ds = isMaster?dataSourceInstance.getMaster():dataSourceInstance.getSlave();
		
		if (ds == null) {
			throw new IllegalStateException("load dataSource failed for lookup key [" + lookupKey + "],isMaster:"+isMaster);
		}		

		return ds;
	}	
	

}
