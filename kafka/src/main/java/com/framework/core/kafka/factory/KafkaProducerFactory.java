package com.framework.core.kafka.factory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.error.exception.BizException;
import com.framework.core.kafka.exception.KafkaErrorCode;
import com.framework.core.kafka.factory.internal.AbstractProducerPropertiesBuilder;

/**
 * 
 * 
 *  kafka生产者 factory 
 *  
 *  
 * @author zhangjun
 *
 */

@Component
public class KafkaProducerFactory extends AbstractProducerPropertiesBuilder
{

	private static Logger logger = LoggerFactory.getLogger(KafkaProducerFactory.class);

	private Producer<String, String> producer = null;

	
	@PostConstruct
	public void initProducer(){
		
		String isEnable = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.kafka.enable", "1");

		if (!"1".equals(isEnable))
		{
			logger.warn("KafkaProducerFactory decide not to start producer ,[core.kafka.enable]" + isEnable);
			return;
		}


		// 生产者对象
		producer = new KafkaProducer<String, String>(this.getProducerProperties());
		
	}




	/**
	 * spring bean destroy
	 */
	@PreDestroy
	public void destroy()
	{
		logger.info("Shutting down KafkaProducerFactory start!!");

		// 关闭生产者
		closeProducer();

		logger.info("Shutting down KafkaProducerFactory success!!");

	}

	/**
	 * 关闭生产者
	 */
	private void closeProducer()
	{
		if (producer != null)
		{
			producer.close();
		}

	}



	/**
	 * 发送消息
	 * @param topic
	 * @param partitionKey
	 * @param data
	 */
	public void send(String topic, String partitionKey, String data)
	{

		if (producer == null || StringUtils.isEmpty(data) || StringUtils.isEmpty(topic))
		{
			return;
		}

		doSend(topic, partitionKey, data);
	}

	/**
	 * 发送消息
	 * @param topic
	 * @param partitionKey
	 * @param message
	 */
	private void doSend(String topic, String partitionKey, String data)
	{
		
		long start = System.currentTimeMillis();

		try
		{
			Future<RecordMetadata> future = producer
					.send(new ProducerRecord<String, String>(topic, partitionKey, data));

			RecordMetadata recordMetadata = future.get();

			// 打印日志
			if (recordMetadata != null)
			{

				logger.debug(
						"Kafka:ProducterRecord(topic = {}, partition = {}, offset = {}, partitionKey = {}, data = {}),{}ms",
						new Object[]
						{
							topic, recordMetadata.partition(), recordMetadata.offset(), partitionKey, data,
							System.currentTimeMillis() - start
						});
			}

		}
		catch (InterruptedException e)
		{
			logger.error("send kafka InterruptedException ,[topic]:" + topic + ",[partitionKey]:" + partitionKey
					+ ",[data]:" + data, e);
			throw new BizException(KafkaErrorCode.EX_KAFKA_SEND_MSG_FAIL.getCode(), "kafka send message failed!", e);

		}
		catch (ExecutionException e)
		{
			logger.error("send kafka ExecutionException ,[topic]:" + topic + ",[partitionKey]:" + partitionKey
					+ ",[data]:" + data, e);
			throw new BizException(KafkaErrorCode.EX_KAFKA_SEND_MSG_FAIL.getCode(), "kafka send message failed!", e);

		}
		catch (Exception e)
		{
			logger.error("send kafka unexpect exception ,[topic]:" + topic + ",[partitionKey]:" + partitionKey
					+ ",[data]:" + data, e);
			throw new BizException(KafkaErrorCode.EX_KAFKA_SEND_MSG_FAIL.getCode(), "kafka send message failed!", e);

		}

	}

	/**
	 * 批量发送消息
	 * @param topic
	 * @param partitionKey
	 * @param dataList
	 */
	public void sendMutiMessage(String topic, String partitionKey, List<String> dataList)
	{

		if (producer == null || StringUtils.isEmpty(topic) || CollectionUtils.isEmpty(dataList))
		{
			return;
		}

		for (String data : dataList)
		{
			doSend(topic, partitionKey, data);
		}

	}


}