package com.waiqin365.task.biz;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.framework.core.task.internel.exception.MasterException;
import com.framework.core.task.internel.model.TaskExecResult;
import com.framework.core.task.internel.template.MultiTenantJobTemplate;


@Component
public class Test1 extends MultiTenantJobTemplate {

	@Override
	public int getConcurrentTenantNum() {
		return 2;
	}

	@Override
	protected boolean doProcessData(ShardingContext shardingContext, TaskExecResult data) {
		
		
		System.out.println("Test1 doProcessData start:shardingContext="+shardingContext.getShardingItem()+",[data]:"+JSON.toJSONString(data));
		
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	

	private long index = 0;
	
	
	@Override
	public Pair<Boolean,List<Long>> fetchWaitExecutTenantIds(String jobName, int batchSize, int pageNum) throws MasterException {

		if (index == 0) {

			List<Long> list = new ArrayList<>();

			
			for(long i =1;i<100;i++) {
				list.add(i);
			}
			
			index++;
			
			return Pair.of(true, list);

		} else {
			index = 0;
			return null;
		}

	}
	
}