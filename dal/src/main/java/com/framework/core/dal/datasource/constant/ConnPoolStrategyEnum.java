package com.framework.core.dal.datasource.constant;

/**
 * 连接池枚举类
 * @author zhangjun
 *
 */
public enum ConnPoolStrategyEnum {
	
	CONN_POOL_PROXOOL("proxoolDateSourceCreator"),
	
	CONN_POOL_DRUID("druidDataSourceCreator"),
	
	;
	
	private String beanName;
	
	
	public String getBeanName() {
		return beanName;
	}


	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}


	private ConnPoolStrategyEnum(String beanName) {
		this.beanName = beanName;
	}
}