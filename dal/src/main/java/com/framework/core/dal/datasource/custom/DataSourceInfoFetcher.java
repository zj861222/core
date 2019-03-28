package com.framework.core.dal.datasource.custom;

import com.framework.core.dal.datasource.model.DataSourceInfo;

/**
 * 获取数据源信息
 * 
 * @author zhangjun
 *
 */
public interface DataSourceInfoFetcher {

	/**
	 * 获取主库和从库的数据源信息
	 * 
	 * @param lookupKey
	 * @return
	 */
	DataSourceInfo fetchBizDataSourceInfo(Object lookupKey);

}