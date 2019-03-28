package com.framework.core.task.elasticjob.template;


import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;


/**
 * 集成 job的监听和执行流程
 * @author zhangjun
 *
 * @param <T>
 */
public abstract class AbstractDataFlowJobTemplate<T> implements DataflowJob<T>,ElasticJobListener {
	
}