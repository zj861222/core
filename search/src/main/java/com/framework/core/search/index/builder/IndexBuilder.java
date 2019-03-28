package com.framework.core.search.index.builder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.framework.core.search.datasync.DataSyncCallBack;
import com.framework.core.search.datasync.DataSyncExecutor;
import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.model.IndexData;

/**
 * 用来全量重建索引
 * 
 * @author zhangjun
 *
 */
@Component
public abstract class IndexBuilder extends BaseIndexBuilder
{

	private static final long DEFAULT_PAGE_SIZE = 1000;

	@Override
	public boolean buildAll(String tempIndexName, String indexName, IndexClient indexClient) throws Exception
	{

		try
		{
			DataSyncExecutor executor = new DataSyncExecutor(tempIndexName, indexClient,
					new DataSyncCallBack<IndexData>()
					{
						@Override
						public List<IndexData> queryDataByPage(long offset, long pageSize)
						{
							return queryBizDataByPage(offset, pageSize);
						}

						@Override
						public long count()
						{
							return countBizData();
						}

						@Override
						public long getPageSize()
						{
							return getBizPageSize();
						}

					}, false);

			return executor.execute();

		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			throw e;
		}

	}

	/**
	 * 根据offset和分页size查询 业务数据
	 * 
	 * @param offset
	 * @param pageSize
	 * @return
	 */
	public abstract List<IndexData> queryBizDataByPage(long offset, long pageSize);

	/**
	 * 计算总共有多少条业务数据
	 * @return
	 */
	public abstract long countBizData();

	/**
	 * 一页取多少条
	 * @return
	 */
	public long getBizPageSize()
	{

		return DEFAULT_PAGE_SIZE;
	}

}