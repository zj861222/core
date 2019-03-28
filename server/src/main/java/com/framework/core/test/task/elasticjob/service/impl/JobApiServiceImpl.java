//package com.framework.core.test.task.elasticjob.service.impl;
//
//import org.springframework.stereotype.Service;
//
//
//import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
//import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
//import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
//import com.dangdang.ddframe.job.lite.lifecycle.api.JobStatisticsAPI;
//import com.dangdang.ddframe.job.lite.lifecycle.api.ServerStatisticsAPI;
//import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
//import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingStatisticsAPI;
//import com.framework.core.test.task.elasticjob.service.JobApiService;
//import com.google.common.base.Optional;
//
//
//@Service
//public class JobApiServiceImpl implements JobApiService {
//
//
//    @Override
//    public JobSettingsAPI getJobSettingsAPI() {
//        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
//        return JobAPIFactory.createJobSettingsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
//    }
//    
//    @Override
//    public JobOperateAPI getJobOperatorAPI() {
//        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
//        return JobAPIFactory.createJobOperateAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
//    }
//    
//    @Override
//    public ShardingOperateAPI getShardingOperateAPI() {
//        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
//        return JobAPIFactory.createShardingOperateAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
//    }
//    
//    @Override
//    public JobStatisticsAPI getJobStatisticsAPI() {
//        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
//        return JobAPIFactory.createJobStatisticsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
//    }
//    
//    @Override
//    public ServerStatisticsAPI getServerStatisticsAPI() {
//        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
//        return JobAPIFactory.createServerStatisticsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
//    }
//    
//    @Override
//    public ShardingStatisticsAPI getShardingStatisticsAPI() {
//        RegistryCenterConfiguration regCenterConfig = SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
//        return JobAPIFactory.createShardingStatisticsAPI(regCenterConfig.getZkAddressList(), regCenterConfig.getNamespace(), Optional.fromNullable(regCenterConfig.getDigest()));
//    }
//	
//}