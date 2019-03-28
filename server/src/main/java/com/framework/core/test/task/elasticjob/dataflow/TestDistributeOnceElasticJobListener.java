package com.framework.core.test.task.elasticjob.dataflow;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;

public final class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
    
    private final ElasticJobListenerCaller caller;
    
    public TestDistributeOnceElasticJobListener(final ElasticJobListenerCaller caller) {
        super(1L, 1L);
        this.caller = caller;
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
        caller.before();
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
        caller.after();
    }
}
