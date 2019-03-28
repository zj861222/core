package com.framework.core.kafka.factory.internal;

import java.util.Properties;

import org.springframework.util.Assert;

import com.framework.core.common.utils.PropertiesUtil;

/**
 * 消费者属性builder
 * https://www.cnblogs.com/rainwang/p/7493742.html
 * 
 * @author zhangjun
 *
 */
public  abstract class AbstractConsumerPropertiesBuilder {
	
	
	/**
	 * consumer属性
	 */
	protected Properties kafkaConsumerProperties = null;
		
	/**
	 * 在启动consumer时配置的broker地址的。不需要将cluster中所有的broker都配置上，因为启动后会自动的发现cluster所有的broker。
     *
     *  它配置的格式是：host1:port1;host2:port2…
	 * 
	 * @return
	 */
	public String getBootStrapServers() {
		
		String val =  PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.bootstrap.servers", "localhost:9092");

		Assert.notNull(val);
		return val;
	}
	
	
	/**
	 * Consumer session 过期时间。这个值必须设置在broker configuration中的group.min.session.timeout.ms 与 group.max.session.timeout.ms之间。
     *   这里设置默认为 30s，而 kafka默认值是：10000 （10 s）.
     *   
	 * 
	 * @return
	 */
	public int getSessionTimeOutMs() {
		
		String val =  PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.consumer.session.timeout.ms", "30000");
		
		Assert.notNull(val);
		
		return Integer.parseInt(val.trim());
	}
	
	
	
	/**
	 *   ·enable.auto.commit
	 *
	 *    Consumer 在commit offset时有两种模式：自动提交，手动提交。手动提交在前面已经说过。自动提交：是Kafka Consumer会在后台周期性的去commit。
	 *
	 *   这里默认值是false。
	 *  @return
	 */
	public boolean isEnableAutoCommit() {
		
		return false;
	}
	
	

	/**
	 * ·max.partition.fetch.bytes
	 * 一次fetch请求，从一个partition中取得的records最大大小。如果在从topic中第一个非空的partition取消息时，如果取到的第一个record的大小就超过这个配置时，仍然会读取这个record，也就是说在这片情况下，只会返回这一条record
	 * broker、topic都会对producer发给它的message size做限制。所以在配置这值时，可以参考broker的message.max.bytes 和 topic的max.message.bytes的配置。
	 * @return
	 */
	public int getMaxPartitionFetchBytes(){
		
		String val =  PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.consumer.max.partition.fetch.bytes", "20000000");
		
		Assert.notNull(val);
		
		return Integer.parseInt(val.trim());
	}
	
	

	
	/**
	 * key反序列化解析器
	 * @return
	 */
	public String getKeyDescrializer(){
		return "org.apache.kafka.common.serialization.StringDeserializer";
	}
	
	
	/**
	 * value反序列化解析器
	 * @return
	 */
	public String getValueDescrializer(){
		return "org.apache.kafka.common.serialization.StringDeserializer";
	}
	
	
	/**
	 * 心跳间隔。心跳是在consumer与coordinator之间进行的。心跳是确定consumer存活，加入或者退出group的有效手段。
	 *
	 *  这个值必须设置的小于session.timeout.ms，因为：
	 *
	 *  当Consumer由于某种原因不能发Heartbeat到coordinator时，并且时间超过session.timeout.ms时，就会认为该consumer已退出，它所订阅的partition会分配到同一group 内的其它的consumer上。
	 *
	 *  通常设置的值要低于session.timeout.ms的1/3。
	 *
	 *  这里默认设置为9s，
	 *  kafka默认值是：3000 （3s）
	 *  
	 * @return
	 */
	public int getHeartbeatIntervalMs(){
		
		String val =  PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.consumer.heartbeat.interval.ms", "9000");
		
		Assert.notNull(val);
		
		return Integer.parseInt(val.trim());
		
	}
	
	
    /**
     * .group.id
     * 用于表示该consumer想要加入到哪个group中。默认值是 “”。
     * @return
     */
	public abstract String getGroupId();
	
	
	/**
	 * .client.id
	 * Consumer进程的标识。如果设置一个人为可读的值，跟踪问题会比较方便。
	 * @return
	 */
	public abstract String getClientId();

	
	
	/**
	 * ·max.poll.records
	 * Consumer每次调用poll()时取到的records的最大数。
	 * 默认每次最多取一条。
	 * @return
	 */
	public int getMaxPollRecords(){
		
		String val =  PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.consumer.max.poll.records", "1");
		
		Assert.notNull(val);
		
		return Integer.parseInt(val.trim());
	}
	
	  
//	/**
//	 * ·max.poll.interval.ms
//	 * 前面说过要求程序中不间断的调用poll()。如果长时间没有调用poll，且间隔超过这个值时，就会认为这个consumer失败了。
//	 * 内部Kafka流消费者max.poll.interval.ms 缺省值从300000到改变Integer.MAX_VALUE
//	 * @return
//	 */
//	public int getMaxPollIntervalMs(){
//		
//		String val =  PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.max.interval.ms", "300000");
//		
//		Assert.notNull(val);
//		
//		return Integer.parseInt(val.trim());
//	}
	
	

    
//
//    ·connections.max.idle.ms
//
//    连接空闲超时时间。因为consumer只与broker有连接（coordinator也是一个broker），所以这个配置的是consumer到broker之间的。
//
//    默认值是：540000 (9 min)
//
//     
//
//    ·fetch.max.wait.ms
//
//    Fetch请求发给broker后，在broker中可能会被阻塞的（当topic中records的总size小于fetch.min.bytes时），此时这个fetch请求耗时就会比较长。这个配置就是来配置consumer最多等待response多久。


	
	/**
	 * 获取kafka 消费者的属性信息
	 * @return
	 */
	public Properties getKafkaConsumerProperties()
	{

		if (kafkaConsumerProperties == null)
		{
			kafkaConsumerProperties = buildKafkaConsumerProperties();
		}

		return kafkaConsumerProperties;
	}
	
	
	/**
	 * 获取kafka consumer配置信息
	 * @return
	 */
	private Properties buildKafkaConsumerProperties()
	{

		Properties propConsumer = new Properties();

		propConsumer.put("bootstrap.servers", this.getBootStrapServers());
		propConsumer.put("group.id", this.getGroupId());
		propConsumer.put("auto.commit.interval.ms", "1000");
		propConsumer.put("session.timeout.ms", this.getSessionTimeOutMs());
		// 1次poll最大数据量
		propConsumer.put("max.poll.records", this.getMaxPollRecords());
		propConsumer.put("key.deserializer", this.getKeyDescrializer());
		propConsumer.put("value.deserializer", this.getValueDescrializer());

		propConsumer.put("enable.auto.commit", this.isEnableAutoCommit());
		propConsumer.put("max.partition.fetch.bytes", this.getMaxPartitionFetchBytes());
		propConsumer.put("heartbeat.interval.ms", this.getHeartbeatIntervalMs());
		propConsumer.put("client.id", this.getClientId());

		return propConsumer;
	}
	

/**
 * 
 * 其他默认的参数
 * 
 *    ·auto.offset.reset
 * 这个配置项，是告诉Kafka Broker在发现kafka在没有初始offset，或者当前的offset是一个不存在的值（如果一个record被删除，就肯定不存在了）时，该如何处理。它有4种处理方式：
 * 1） earliest：自动重置到最早的offset。
 * 2） latest：看上去重置到最晚的offset。
 * 3） none：如果边更早的offset也没有的话，就抛出异常给consumer，告诉consumer在整个consumer group中都没有发现有这样的offset。
 * 4） 如果不是上述3种，只抛出异常给consumer。
 * 默认值是latest。
 * 
 * 
 *    ·metadata.max.age.ms
 *  Metadata数据的刷新间隔。即便没有任何的partition订阅关系变更也行执行。
 *  范围是：[0, Integer.MAX]，默认值是：300000 （5 min） 
 * 
 * 
 *    ·interceptor.classes
 *  用户自定义interceptor。
 * 
 * 
 *    ·request.timeout.ms
 *  请求发起后，并不一定会很快接收到响应信息。这个配置就是来配置请求超时时间的。
 *  默认值是：305000 （305 s）
 *
 * 
 *    ·receive.buffer.byte
 *  Consumer receiver buffer （SO_RCVBUF）的大小。这个值在创建Socket连接时会用到。
 *  取值范围是：[-1, Integer.MAX]。默认值是：65536 （64 KB）
 *  如果值设置为-1，则会使用操作系统默认的值。
 * 
 * 
 * 
 *    ·fetch.max.bytes
 *  一次fetch请求，从一个broker中取得的records最大大小。如果在从topic中第一个非空的partition取消息时，如果取到的第一个record的大小就超过这个配置时，仍然会读取这个record，也就是说在这片情况下，只会返回这一条record。
 *  broker、topic都会对producer发给它的message size做限制。所以在配置这值时，可以参考broker的message.max.bytes 和 topic的max.message.bytes的配置。
 *  取值范围是：[0, Integer.Max]，默认值是：52428800 （5 MB）
 * 
 * 
 *    ·fetch.min.bytes
 *  当consumer向一个broker发起fetch请求时，broker返回的records的大小最小值。如果broker中数据量不够的话会wait，直到数据大小满足这个条件。
 *  取值范围是：[0, Integer.Max]，默认值是1。
 *  默认值设置为1的目的是：使得consumer的请求能够尽快的返回。
 * 
 * 
 *    ·fetch.max.wait.ms
 *  Fetch请求发给broker后，在broker中可能会被阻塞的（当topic中records的总size小于fetch.min.bytes时），此时这个fetch请求耗时就会比较长。这个配置就是来配置consumer最多等待response多久。
 *
 *
 *    ·connections.max.idle.ms
 *  连接空闲超时时间。因为consumer只与broker有连接（coordinator也是一个broker），所以这个配置的是consumer到broker之间的。
 *  默认值是：540000 (9 min)
 * 
 * 
 * 
 * 
 */


	
}