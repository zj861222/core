package com.framework.core.alarm.event.handler;


import java.util.Map;

import com.framework.core.alarm.EventTypeEnum;
import com.framework.core.alarm.event.ElasticJobFailedEvent;

/**
 * 
 *  
 * @author zhangjun
 *
 */
public class ElasticJobFailedEventHandler extends AbstractEventHandler<ElasticJobFailedEvent> {

	@Override
	String getCatalog(ElasticJobFailedEvent event)
	{
		return  EventTypeEnum.EVENT_TYPE_ES_JOB_FAILED.getMeasurements();
	}
	
	
    /**
     * 设置参数
     */
    @Override
    protected  void addArgs(ElasticJobFailedEvent event, Map<String, Object> map){
    	
    	map.put("batch_no", event.getBatchNo());
    	
    	map.put("type", event.getType() == 0?"未执行":"执行失败");
    	
    	map.put("create_time", event.getCreateTime());
    	
    	map.put("trigger_time", event.getNowTime());
    	
    	map.put("fail_times", event.getFailTimes());
    	
    	map.put("sharding_no", event.getShardingNo());
    	
    	map.put("total_sharding", event.getTotalSharding());
    	
    	map.put("stack", event.getStack());

    }
	
    /**
     * 设置tags
     */
    @Override
    protected  void addTags(ElasticJobFailedEvent event, Map<String, String> tags){
    	//删掉抽象类中默认的 event tag，保留 context
    	tags.remove("event");
    	
    	tags.put("job_name", event.getJobName());
    	
    	tags.put("tenant_id", event.getTenantId());

    }

    @Override
    protected String getDatabase() {
        return EventTypeEnum.EVENT_TYPE_ES_JOB_FAILED.getDbName();
    }

}
