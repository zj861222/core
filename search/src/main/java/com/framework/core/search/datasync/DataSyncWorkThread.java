package com.framework.core.search.datasync;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.model.IndexData;



/**
 * 数据同步线程
 * 
 * @author zhangjun
 *
 */
public class DataSyncWorkThread implements Runnable
{

	private static final Logger log = LoggerFactory.getLogger(DataSyncWorkThread.class);

	private String name;

	private DataSyncJob job;

	private WorkThreadResultCallBack callBack;

	private DataSyncCallBack<IndexData> service;

	private IndexClient indexClient;

	public DataSyncWorkThread(String name, DataSyncJob job, DataSyncCallBack<IndexData> service,
			WorkThreadResultCallBack callBack, IndexClient indexClient)
	{

		this.name = name;
		this.job = job;
		this.callBack = callBack;
		this.indexClient = indexClient;
		this.service = service;

	}

	@Override
	public void run()
	{

		try
		{
			doExecute();

		}
		catch (Exception e)
		{
			log.error("DataSyncWorkThread threadName=" + Thread.currentThread().getName() + " indexName=" + name
					+ "with exception!!!", e);

			callBack.notify(name,job,false,e);
		}
	}

	private void doExecute()
	{

		long begin = System.currentTimeMillis();

		List<IndexData> list = service.queryDataByPage((job.getPageNo() - 1) * job.getPageSize(), job.getPageSize());

		//
		if (CollectionUtils.isEmpty(list))
		{
			callBack.notify(name,job,true,null);
		}

		long readFromMysql = System.currentTimeMillis() - begin;

		indexClient.addIndexDataList(name, list);

		log.info("DataSyncWorkThread threadName=" + Thread.currentThread().getName() + " indexName=" + name + " pageNo="
				+ job.getPageNo() + " resultSize=" + list.size() + " fromMysql=" + readFromMysql + " intoIndex="
				+ (System.currentTimeMillis() - begin - readFromMysql));

		callBack.notify(name,job,true,null);

	}

}