//package com.framework.core.test.task.elasticjob.dataflow;
//
//import java.util.List;
//
//import org.springframework.stereotype.Component;
//
//import com.dangdang.ddframe.job.api.ShardingContext;
//import com.waiqin365.task.internel.model.TaskExecResult;
//import com.waiqin365.task.internel.template.TenantDataFlowTemplate;
//
//
//@Component
//public class TenantTestJob extends TenantDataFlowTemplate {
//
//	@Override
//	protected boolean doProcessData(ShardingContext shardingContext, TaskExecResult data) {
//		
//		
//		return false;
//	}
//
//	@Override
//	protected List<Long> fetchWaitExecutTenantIds(String jobName, int batchSize, int pageNum) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}