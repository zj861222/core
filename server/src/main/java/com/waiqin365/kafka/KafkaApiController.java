package com.waiqin365.kafka;


import javax.annotation.Resource;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.core.common.view.BizResult;
import com.framework.core.kafka.KafkaMessageSender;
import com.framework.core.search.index.service.IndexService;
import com.framework.core.search.model.SearchParam;
import com.framework.core.search.model.SearchResult;

@Controller
public class KafkaApiController{
	
	
	private String TOPIC = "tp_test_01";
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	@RequestMapping(value = "testSendMsg")
	@ResponseBody
	public BizResult testSendMsg()
	{
		
		KafkaMessageSender.sendMessage(TOPIC, "22", "data2");
		
		return BizResult.success();
	}
	

}

