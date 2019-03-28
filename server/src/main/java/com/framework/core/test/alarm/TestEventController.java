package com.framework.core.test.alarm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.event.BigSqlQueryResultEvent;

@Controller
public class TestEventController {
	
	
	
	@RequestMapping("/alarm")
	@ResponseBody
	public void testAlarm(){
		
		
		BigSqlQueryResultEvent event = new BigSqlQueryResultEvent("ps_mm", 11, 111L, 1, 0);
		
		EventPublisherUtils.reportEvent(event);
		
	}
	
}