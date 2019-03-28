package com.waiqin365.task.biz;


import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.framework.core.common.utils.DateUtil;
import com.framework.core.error.exception.BizException;
import com.framework.core.task.internel.exception.MasterException;
import com.framework.core.task.internel.model.TaskExecResult;
import com.framework.core.task.internel.template.DsSwitchMultiTenantJobTemplate;


/**
 * 
 * @author zhangjun
 *
 */
@Component
public class EsJobTest1 extends DsSwitchMultiTenantJobTemplate {

	@Override
	public void doBeforePcocess(ShardingContext context, TaskExecResult record)
	{
		System.out.println("EsJobTest1  doBeforePcocess，time="+DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT));
		
	}

	@Override
	public void doAfterPcocess(ShardingContext context, TaskExecResult record)
	{
//		System.out.println("EsJobTest1  doAfterPcocess,time="+DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT));

		
	}

	@Override
	protected boolean switchAndDoProcessData(ShardingContext context, TaskExecResult record)
	{
//		System.out.println("EsJobTest1  switchAndDoProcessData,[tenantid]="+record.getTenantId()+",[jobName]"+record.getJobName()+",[batch]:"+record.getBatchNo()+",[shard]:"+context.getShardingItem()+",time="+DateUtil.date2String(new Date(), DateUtil.DATETIME_PATTENT));
		
//		
//		try
//		{
//			Thread.sleep(4000);
//		}
//		catch (InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		if(true) {
			throw new BizException(1111);
		}
		
		return true;
	}

	@Override
	public int getConcurrentTenantNum()
	{
		return 5;
	}
	
	

}