package com.framework.core.test.task.elasticjob.service;


import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ServerStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingStatisticsAPI;

public interface JobApiService {
    
    JobSettingsAPI getJobSettingsAPI();
    
    JobOperateAPI getJobOperatorAPI();
    
    ShardingOperateAPI getShardingOperateAPI();
    
    JobStatisticsAPI getJobStatisticsAPI();
    
    ServerStatisticsAPI getServerStatisticsAPI();
    
    ShardingStatisticsAPI getShardingStatisticsAPI();
    
}
