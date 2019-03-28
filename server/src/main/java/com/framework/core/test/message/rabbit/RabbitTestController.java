package com.framework.core.test.message.rabbit;


import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.core.message.rabbit.factory.internal.producer.TopicProducerTemplate;
import com.framework.core.web.session.token.TokenManager;
import com.framework.core.web.session.token.constants.SourceTypeEnum;
import com.framework.core.web.session.token.view.BaseTokenSubject;


@Controller
public class RabbitTestController {
	
	@Resource
	private TopicProducerTemplate topicProducerTemplate;
//	
//	@Resource
//	private TopicConsumerTest topicConsumerTest;
//	
//	
    @RequestMapping(value = "rabbit/send")
	@ResponseBody
	public String send(String message) {
  
    	topicProducerTemplate.send("default_fanout", "123123213", message, null);

		return "rabbit/test";
    	
    }

}