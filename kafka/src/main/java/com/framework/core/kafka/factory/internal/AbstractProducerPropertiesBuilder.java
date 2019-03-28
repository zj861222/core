package com.framework.core.kafka.factory.internal;

import java.util.Properties;
import org.springframework.util.Assert;
import com.framework.core.common.utils.PropertiesUtil;

/**
 * 
 * 生产者属性builder
 * https://blog.csdn.net/louisliaoxh/article/details/51516077
 * 
 * @author zhangjun
 *
 */
public abstract class AbstractProducerPropertiesBuilder
{

	private Properties producerProperties;
	
	/**
	 * 在启动consumer时配置的broker地址的。不需要将cluster中所有的broker都配置上，因为启动后会自动的发现cluster所有的broker。
	 *
	 *  它配置的格式是：host1:port1;host2:port2…
	 *  
	 * @return
	 */
	public String getBootstrapServers()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.bootstrap.servers",
				"localhost:9092");

		Assert.notNull(val);

		return val;

	}

	/**
	 * key序列化
	 * @return
	 */
	public String getKeyScrializer()
	{
		return "org.apache.kafka.common.serialization.StringSerializer";
	}

	/**
	 * value序列化
	 * @return
	 */
	public String getValueScrializer()
	{
		return "org.apache.kafka.common.serialization.StringSerializer";
	}

	/**
	 * producer需要server接收到数据之后发出的确认接收的信号，此项配置就是指procuder需要多少个这样的确认信号。
	 * 此配置实际上代表了数据备份的可用性。以下设置为常用选项：（
	 * 1）acks=0： 设置为0表示producer不需要等待任何确认收到的信息。副本将立即加到socket buffer并认为已经发送。没有任何保障可以保证此种情况下server已经成功接收数据，同时重试配置不会发生作用（因为客户端不知道是否失败）回馈的offset会总是设置为-1；
	 * 2）acks=1： 这意味着至少要等待leader已经成功将数据写入本地log，但是并没有等待所有follower是否成功写入。这种情况下，如果follower没有成功备份数据，而此时leader又挂掉，则消息会丢失。
	 * 3）acks=all： 这意味着leader需要等待所有备份都成功写入日志，这种策略会保证只要有一个备份存活就不会丢失数据。这是最强的保证。
	 * 
	 * 
	 * @return
	 */
	public String getAcks()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.acks", "all");

		Assert.notNull(val);

		return val;
	}

	/**
	 * 重试次数
	 * 设置大于0的值将使客户端重新发送任何数据，一旦这些数据发送失败。
	 * 注意，这些重试与客户端接收到发送错误时的重试没有什么不同。允许重试将潜在的改变数据的顺序，
	 * 如果这两个消息记录都是发送到同一个partition，则第一个消息失败第二个发送成功，则第二条消息会比第一条消息出现要早
	 * 
	 * @return
	 */
	public String getRetries()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.retries", "5");

		Assert.notNull(val);

		return val;
	}

	/**
	 * 当这batch.size和 linger.ms
	 * 两个参数同时设置的时候，只要两个条件中满足一个就会发送。比如说batch.size设置16kb，linger.ms设置50ms，那么当消息积压达到16kb就会发送，如果没有到达16kb，那么在第一个消息到来之后的50ms之后消息将会发送。
	 * 通过这个参数来设置批量提交的数据大小，默认是16k,当积压的消息达到这个值的时候就会统一发送（发往同一分区的消息）
	 * 
	 * @return
	 */
	public String getBatchSize()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.batch.size", "16384");

		Assert.notNull(val);

		return val;

	}

	/**
	 * 
	 * 这个设置是为发送设置一定是延迟来收集更多的消息，默认大小是0ms（就是有消息就立即发送）
	 * 当这batch.size和 linger.ms两个参数同时设置的时候，只要两个条件中满足一个就会发送。
	 * 比如说batch.size设置16kb，linger.ms设置50ms，那么当消息积压达到16kb就会发送，
	 * 如果没有到达16kb，那么在第一个消息到来之后的50ms之后消息将会发送。
	 * 
	 * 
	 * @return
	 */
	public String getLingerMs()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.linger.ms", "1");

		Assert.notNull(val);

		return val;
	}

	/**
	 * 控制block的时长,当buffer空间不够或者metadata丢失时产生block
	 * 
	 * 默认6000，即6s
	 * @return
	 */
	public String getMaxBlockMs()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.max.block.ms",
				"6000");

		Assert.notNull(val);

		return val;
	}

	/**
	 * 请求的最大字节数。这也是对最大记录尺寸的有效覆盖。
	 * 注意：server具有自己对消息记录尺寸的覆盖，这些尺寸和这个设置不同。
	 * 此项设置将会限制producer每次批量发送请求的数目，以防发出巨量的请求
	 * 
	 * @return
	 */
	public String getMaxRequestSize()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.max.request.size",
				"1048576");

		Assert.notNull(val);

		return val;

	}

	/**
	 * 客户端将等待请求的响应的最大时间,如果在这个时间内没有收到响应，客户端将重发请求;超过重试次数将抛异常
	 * @return
	 */
	public String getRequestTimeoutMs()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.request.timeout.ms",
				"3000");

		Assert.notNull(val);

		return val;

	}

	/**
	 * producer可以用来缓存数据的内存大小。如果数据产生速度大于向broker发送的速度，producer会阻塞或者抛出异常，以“block.on.buffer.full”来表明。
	 * 
	 * 这项设置将和producer能够使用的总内存相关，但并不是一个硬性的限制，
	 * 因为不是producer使用的所有内存都是用于缓存。一些额外的内存会用于压缩（如果引入压缩机制），同样还有一些用于维护请求
	 * 默认32m
	 * @return
	 */
	public String getBufferMemory()
	{

		String val = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.producer.buffer.memory",
				"33554432");

		Assert.notNull(val);

		return val;

	}
	
	
	/**
	 * 生产者属性
	 * @return
	 */
	public Properties getProducerProperties() {
		
		if(producerProperties == null) {
			
			producerProperties = buildProducerProperties();
		}
		
		return producerProperties;
	}
	
	
	/**
	 * 构建生产者的属性
	 * 
	 */
	private Properties  buildProducerProperties(){
		
		Properties propProduct = new Properties();
		
		propProduct.put("bootstrap.servers", getBootstrapServers());
		propProduct.put("key.serializer", getKeyScrializer());
		propProduct.put("value.serializer", getValueScrializer());

		propProduct.put("acks", getAcks());
		propProduct.put("retries", getRetries());
		propProduct.put("batch.size", getBatchSize());
		propProduct.put("linger.ms", getLingerMs());
		propProduct.put("max.block.ms", getMaxBlockMs());
		propProduct.put("request.timeout.ms", getRequestTimeoutMs());
		propProduct.put("buffer.memory", getBufferMemory());
		//调整消息体允许的大小
		propProduct.put("max.request.size", getMaxRequestSize());

		return propProduct;
	}

	// connections.max.idle.ms 关闭连接空闲时间,默认 540000
	// partitioner.class 分区类
	// receive.buffer.bytes socket的接收缓存空间大小,当阅读数据时使用
	// send.buffer.bytes 发送数据时的缓存空间大小
	// timeout.ms
	// 此配置选项控制server等待来自followers的确认的最大时间。如果确认的请求数目在此时间内没有实现，则会返回一个错误。这个超时限制是以server端度量的，没有包含请求的网络延迟
	// max.in.flight.requests.per.connection
	// kafka可以在一个connection中发送多个请求，叫作一个flight,这样可以减少开销，但是如果产生错误，可能会造成数据的发送顺序改变,默认是5
	// (修改）
	// metadata.fetch.timeout.ms
	// 是指我们所获取的一些元素据的第一个时间数据。元素据包含：topic，host，partitions。此项配置是指当等待元素据fetch成功完成所需要的时间，否则会跑出异常给客户端
	// metadata.max.age.ms 以微秒为单位的时间，是在我们强制更新metadata的时间间隔。即使我们没有看到任何partition
	// leadership改变。
	// metric.reporters
	// 类的列表，用于衡量指标。实现MetricReporter接口，将允许增加一些类，这些类在新的衡量指标产生时就会改变。JmxReporter总会包含用于注册JMX统计
	// metrics.num.samples 用于维护metrics的样本数
	// metrics.sample.window.ms metrics系统维护可配置的样本数量，在一个可修正的window
	// size。这项配置配置了窗口大小，例如。我们可能在30s的期间维护两个样本。当一个窗口推出后，我们会擦除并重写最老的窗口
	// reconnect.backoff.ms 连接失败时，当我们重新连接时的等待时间。这避免了客户端反复重连
	// retry.backoff.ms 在试图重试失败的produce请求之前的等待时间。避免陷入发送-失败的死循环中

}