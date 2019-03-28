package com.framework.core.message.rabbit.factory;

import com.framework.core.message.rabbit.constants.RabbitConstants;

/**
 * 主题分发的消费者
 *
 *  所有的topic 消费者需要实现这个接口，并且加入到Spring的context中
 *
 * 
 */
public abstract class  TopicMessageConsumer implements MessageConsumer {

     /**
      * 返回默认的exchange
      * @return
      */
	 @Override
     public String getExchangName(){
    	 
    	 return RabbitConstants.DEFAULT_TOPIC_EXCHAGE;
     }
    
		 
     /**
      * 默认的并发数量
      * @return
      */
	 @Override
	 public int getConcurrentConsumers() {
    	 return 1;
     }
	 

}
