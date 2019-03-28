package com.framework.core.cache.redis.message.constants;



/**
 * topic 匹配的类型
 * @author zhangjun
 *
 */
public enum TopicMatchTypeEnum {
	
	/**
	 * Channel topic implementation (maps to a Redis channel).
	 */
	TYPE_CHANNEL("channel_topic"),
	
	/**
	 * Pattern topic (matching multiple channels).
	 */
	TYPE_PATTERN_TOPIC("pattern_topic")
	;
	
	
	private String topicType;
	
	
	public String getTopicType()
	{
		return topicType;
	}


	public void setTopicType(String topicType)
	{
		this.topicType = topicType;
	}



	private TopicMatchTypeEnum(String topicType) {
		
		this.topicType = topicType;
		
	}
}