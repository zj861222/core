package com.framework.core.message.rabbit.factory;


public abstract class FanoutMessageConsumer  implements MessageConsumer {
		 
    /**
     * 默认的并发数量
     * @return
     */
	 @Override
	 public int getConcurrentConsumers() {
   	     return 1;
     }
	 
}