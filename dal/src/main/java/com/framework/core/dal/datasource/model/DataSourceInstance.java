package com.framework.core.dal.datasource.model;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;

import com.framework.core.dal.datasource.strategy.SlaveChoiceStrategy;

/**
 * 数据源 实例
 * 包含1个主库，和多个从库
 * 
 * @author zhangjun
 *
 */
public class DataSourceInstance {
	
	/**
	 * 主数据源
	 */
	private DataSource master;
	
	/**
	 * 从库 list
	 */
	private List<DataSource> slaves;

	public DataSource getMaster() {
		return master;
	}

	public void setMaster(DataSource master) {
		this.master = master;
	}

	public List<DataSource> getSlaves() {
		return slaves;
	}

	public void setSlaves(List<DataSource> slaves) {
		this.slaves = slaves;
	}

	/**
	 * 获取从库实例.
	 * 如果 没有从库，那么获取从库时，取到的时主库；
	 * 如果 有从库，那么按照策略在主库获取
	 * 
	 * 
	 * @return
	 */
	public DataSource getSlave() {	
		
		return CollectionUtils.isEmpty(slaves)?master:SlaveChoiceStrategy.getSlave(slaves);
	}

	
}