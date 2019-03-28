package com.framework.core.dal.datasource.constant;

/**
 * 数据源类型，主/从
 * @author zhangjun
 *
 */
public enum DataSourceType {
	
	TYPE_MASTER(0),
	
	TYPE_SLAVE(1),
	;
	
	
	private int code ;
	
	
	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	private DataSourceType(int code) {
		this.code = code;
	}
	
}
