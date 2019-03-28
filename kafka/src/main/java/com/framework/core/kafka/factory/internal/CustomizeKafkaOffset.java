package com.framework.core.kafka.factory.internal;

import org.apache.commons.lang3.StringUtils;

import com.framework.core.zookeeper.model.CreateMode;
import com.framework.core.zookeeper.util.ZookeeperClientHelper;


public class CustomizeKafkaOffset
{
	
	// Kafka Offset值
	public static final String KAFKA_OFFSET_FORMAT = "/waiqin365/appsvr/kafka/%s-%s-%s";
	
	public static final String PREFIX = "offset";

	
	/**
	 * 前缀
	 */
	private String prefix;
	
	/**
	 * topic
	 */
	private String topic;
	
	/**
	 * 分片信息
	 */
	private int partition;
	
//	/**
//	 * 偏移量
//	 */
//	private long offset;
	
	/**
	 * 路径
	 */
	private String path;

	
	public CustomizeKafkaOffset(String storagePrefix,String topic,int partition)
	{
		this.prefix = storagePrefix;
		
		this.topic = topic;
		
		this.partition = partition;
		
		this.path = String.format(KAFKA_OFFSET_FORMAT, prefix, topic, partition);
	}

	
	
	/**
	 * 保存offset
	 * @param offset
	 * @throws Exception
	 */
	public void saveOffsetInExternalStore(long offset) throws Exception
	{
		if(ZookeeperClientHelper.isNodeExist(path)) {
			ZookeeperClientHelper.updateNodeData(path, String.valueOf(offset));
		} else {
			ZookeeperClientHelper.createNode(path, CreateMode.PERSISTENT, String.valueOf(offset));
		}
	}


//	/**
//	 * Overwrite the offset for the topic in an external storage.
//	 * @param offset
//	 * @throws Exception
//	 */
//	public void setAndSaveOffsetInExternalStore(long offset) throws Exception
//	{
//		this.offset = offset;
//		saveOffsetInExternalStore();
//	}

	/**
	 * 
	 * 读取下一个offset的值
	 * @return 
	 */

	public long readNextOffsetFromExternalStore() throws Exception
	{
		
		long currentOffset = readCurrentOffsetData();
		
		return currentOffset+1;
		
	}

//	/**
//	 * 存储路径
//	 * @param topic
//	 * @param partition
//	 * @return
//	 */
//	private String getStoragePath()
//	{
//		if(StringUtils.isEmpty(path)) {
//			path = String.format(KAFKA_OFFSET_FORMAT, prefix, topic, partition);
//		}
//		return path;
//	}

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public int getPartition()
	{
		return partition;
	}

	public void setPartition(int partition)
	{
		this.partition = partition;
	}

//	public long getOffset()
//	{
//		return offset;
//	}
//
//	public void setOffset(long offset)
//	{
//		this.offset = offset;
//	}
//	
//	

	
	/**
	 * 读取offset数据
	 * @param topic
	 * @param partition
	 * @return
	 */
	public long readCurrentOffsetData(){
		
		
		boolean isExist = ZookeeperClientHelper.isNodeExist(path);
		
		//节点不存在返回0
		if(!isExist) {
			return 0;
		}
		
		String offset = ZookeeperClientHelper.getData(path);
		
		if(StringUtils.isNotBlank(offset)) {
			return Long.parseLong(offset);
		}else {
			return 0;
		}
		
	}

}
