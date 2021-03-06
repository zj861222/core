package com.framework.core.test.task.elasticjob.common;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.framework.core.test.task.elasticjob.simple.MySimpleJob;

public class TestJob {

	
    public static void main(String[] args) {
        new JobScheduler(createRegistryCenter(), createJobConfiguration()).init();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("127.0.0.1:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static LiteJobConfiguration createJobConfiguration() {
    	
	    JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder("demoSimpleJob", "0/15 * * * * ?", 10).build();
	    // 定义SIMPLE类型配置
	    SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, MySimpleJob.class.getCanonicalName());
	    // 定义Lite作业根配置
	    LiteJobConfiguration simpleJobRootConfig = LiteJobConfiguration.newBuilder(simpleJobConfig).build();
	    
	    return simpleJobRootConfig;
    }
	
}