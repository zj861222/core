package com.framework.core.test.task.elasticjob.dataflow;


import java.util.List;

import org.springframework.stereotype.Service;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.framework.core.task.internel.model.TaskExecResult;


@Service
public class TssJob implements DataflowJob<TaskExecResult> {


	@Override
	public List<TaskExecResult> fetchData(ShardingContext shardingContext)
	{
		
		System.out.println("TssJob  fetchData ");
		
		return null;
	}

	@Override
	public void processData(ShardingContext shardingContext, List data)
	{
		System.out.println("TssJob  processData ");

	}
	
}