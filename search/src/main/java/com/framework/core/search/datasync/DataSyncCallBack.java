package com.framework.core.search.datasync;

import java.util.List;

/**
 * 数据同步回调
 * 
 * @author zhangjun
 *
 */
public abstract class  DataSyncCallBack<T> {
	
	
	private static final long DEFAULT_PAGE_SIZE = 1000;
	
	/**
	 * 获取总的数据量
	 * @return
	 */
	public abstract long count();
	
	
	/**
	 * 分页查询
	 * @param offset
	 * @param pageSize
	 * @return
	 */
	public abstract List<T> queryDataByPage(long offset,long pageSize);
	
	
	/**
	 * 默认的页数大小
	 * @return
	 */
	public long getPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
	

}