package com.framework.core.dal.datasource.model;

import java.util.List;

import com.framework.core.dal.exception.DalErrorCode;
import com.framework.core.error.exception.BizException;


/**
 * 
 *  数据源信息，同一个数据源，包含1主多从
 * @author zhangjun
 *
 */
public class DataSourceInfo {
	
	/**
	 * 主库数据源信息
	 */
	private DataSourceUnit masterDataSource;
	
	/**
	 * 从库数据源信息
	 */
	private List<DataSourceUnit> slaveDataSources;

	public DataSourceUnit getMasterDataSource() {
		return masterDataSource;
	}

	public void setMasterDataSource(DataSourceUnit masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public List<DataSourceUnit> getSlaveDataSources() {
		return slaveDataSources;
	}

	public void setSlaveDataSources(List<DataSourceUnit> slaveDataSources) {
		this.slaveDataSources = slaveDataSources;
	}
	
	/**
	 * 校验数据源信息
	 */
	public void validate(){
		
		//主数据源必须设置
		if(masterDataSource == null) {
			throw new BizException(DalErrorCode.EX_SYSTEM_DB_FALTE_EX_MASTER_DB_INFO_IDEGGLE.getCode());
		}
	}
}