package com.framework.core.test.task.elasticjob.simple;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.google.gson.stream.JsonWriter;


public class MySimpleJob implements SimpleJob {

	@Override
	public void execute(ShardingContext context) {
		
		
        switch (context.getShardingItem()) {
        case 0: 
    		System.out.println("MySimpleJob execute1 ! [context]="+JSON.toJSONString(context));
            break;
        case 1: 
    		System.out.println("MySimpleJob execute2 ! [context]="+JSON.toJSONString(context));
            break;
        case 2: 
    		System.out.println("MySimpleJob execute3 ! [context]="+JSON.toJSONString(context));
            break;
        // case n: ...
    }
		
		
//		System.out.println("MySimpleJob execute ! [context]="+JSON.toJSONString(context));
	}
	
}