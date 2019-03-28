/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.framework.core.test.task.elasticjob.dataflow;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyDataFlowJob implements DataflowJob<Foo>, ElasticJobListener {
    
    private FooRepository fooRepository = FooRepositoryFactory.getFooRepository();
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
//        System.out.println(String.format("Item: %s | Time: %s | Thread: %s | %s",
//                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "DATAFLOW FETCH"));
        List<Foo> list =  fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
        
       String taskId =  shardingContext.getTaskId();
       
       
       System.out.println("MyDataFlowJob ----------------info:+"+JSON.toJSONString(shardingContext));
        
//        System.out.println("fetchData ::::[time]="+new Date().toLocaleString()+"[Sharding]="+shardingContext.getShardingParameter()+",[result]="+JSON.toJSONString(list));
        return list;
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {

        for (Foo each : data) {  
            fooRepository.setCompleted(each.getId());
        }
        
        
//        System.out.println("processData::: [time]="+new Date().toLocaleString()+"[Sharding]="+shardingContext.getShardingParameter()+",[result]="+JSON.toJSONString(data));

    }

    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
    	
    	System.out.println("SimpleListener ----------beforeJobExecuted");
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
    	System.out.println("SimpleListener ----------afterJobExecuted");

    }
}
