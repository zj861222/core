
package com.framework.core.search.index.factory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.framework.core.search.index.IndexMgr;
import com.framework.core.search.index.factory.Index;
import com.framework.core.search.index.factory.IndexClient;
import com.framework.core.search.index.factory.IndexClientFactory;
import com.framework.core.search.index.factory.config.ClientConfig;
import com.framework.core.search.index.factory.config.IndexConfig;
import com.framework.core.search.index.factory.config.IndexConfigs;
import com.framework.core.search.index.factory.config.JaxbBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * 创建ES的客户端提供请求。
 */

@Component
public class EsIndexClientFactory extends IndexClientFactory
{

	private final Logger logger = LoggerFactory.getLogger(EsIndexClientFactory.class);

	// 搜索引擎配置文件为 index.xml文件
	private String configFile = "index.xml";

	@Resource
	private IndexMgr indexMgr;

	/**
	 * 创建索引器，索引器负责处理具体的索引接口
	 *
	 * @param properties
	 * @return
	 */
	@SuppressWarnings("resource")
	@Override
	public IndexClient doCreateIndexClient(Map<String, String> properties)
	{

		String clusterName = properties.get("es.cluster.name");

		Assert.isTrue(StringUtils.isNotBlank(clusterName));

		String servers = properties.get("es.servers");

		Assert.isTrue(StringUtils.isNotBlank(servers));

		Settings settings = Settings.builder().put("cluster.name", clusterName)
				.put("client.transport.ping_timeout", "20s").build();

		// 3.X以后版本用法
		TransportClient client = new PreBuiltTransportClient(settings);

		// 2.x版本用法
		// TransportClient client =
		// TransportClient.builder().settings(settings).build();

		// 1.x 版本用法
		// TransportClient client = new TransportClient(settings);

		try
		{
			String[] serverArray = parseServers(servers);

			Assert.isTrue(serverArray != null && serverArray.length > 0);

			for (String server : serverArray)
			{

				String[] serverHostPort = server.trim().split(":");
				String serverHost = serverHostPort[0].trim();
				int serverPort = Integer.parseInt(serverHostPort[1]);
				try
				{
					client.addTransportAddress(
							new InetSocketTransportAddress(InetAddress.getByName(serverHost), serverPort));
				}
				catch (UnknownHostException e)
				{
					e.printStackTrace();

					logger.error("ElasticSearchIndexClientFactory  UnknownHostException!!!serverHost=" + serverHost, e);

					return null;
				}

			}

			logger.info(
					String.format("Setup transport client %s, cluster name %s", servers, settings.get("cluster.name")));

			return new EsIndexClient(client);

		}
		finally
		{

		}
	}

	/**
	 * 解析ip地址
	 * @param servers
	 * @return
	 */
	private String[] parseServers(String servers)
	{

		String[] args = servers.trim().split(",");

		return args;

	}

	@PostConstruct
	@Override
	public void initSearchEngine()
	{
		long begin = System.currentTimeMillis();
		logger.info("EsIndexClientFactory [method=init] [step=start]");
		this.configure(this.configFile);
		logger.info("EsIndexClientFactory [method=init] [step=end] [cost={}ms]", System.currentTimeMillis() - begin);
	}

	/**
	 * 根据配置文件初始化消息连接
	 *
	 * @param configFile
	 *            配置文件的路径
	 */
	public void configure(String configFile)
	{
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(configFile);

		try
		{

			// 解析index.xml 读取索引属性
			JaxbBinder jaxbBinder = new JaxbBinder(IndexConfigs.class);
			IndexConfigs configs = jaxbBinder.fromXML(is);

			// String defaultFactory =
			// PropertiesUtil.getProp(PropertiesUtil.FILE_SYSTEM,
			// "search.es.default.factory",
			// "com.waiqin365.crawler.search.index.impl.elasticsearch.ElasticSearchIndexClientFactory");
			// 初始化客户端
			for (ClientConfig clientConfig : configs.getClientConfigs())
			{

				// String factory = clientConfig.getFactory();
				//
				// if (StringUtils.isEmpty(factory))
				// {
				// factory = defaultFactory;
				// }
				//
				// IndexClientFactory indexClientFactory = (IndexClientFactory)
				// Class.forName(factory).newInstance();

				String factoryClass = clientConfig.getFactory();
				
				if(StringUtils.isEmpty(factoryClass)) {
					factoryClass = EsIndexClientFactory.class.getName();
				}
				
				IndexClientFactory indexClientFactory = (IndexClientFactory) Class.forName(factoryClass)
						.newInstance();

				IndexClient client = indexClientFactory.createIndexClient(clientConfig.getProperties());

				if (client == null)
				{
					continue;
				}

				client.setName(clientConfig.getName());

				indexMgr.registerIndexClient(client.getName(), client);

				if (clientConfig.getIndexConfigs() != null)
				{
					for (IndexConfig indexConfig : clientConfig.getIndexConfigs())
					{
						Index index = new EsIndex(indexConfig.getAliasName(), client, indexConfig.getProperties());
						// 构造数据提供者
						index.setIndexBuilderClassName(indexConfig.getBuilderClass());
						String mappingFile = indexConfig.getMappingFile();
						index.setMappingContent(readFile(mappingFile));

						indexMgr.registerIndex(index.getAliasName(), index);
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("初始化索引服务失败，Exception: ", e);
		}
	}

	// 读文件，返回字符串
	private String readFile(String path)
	{

		BufferedReader reader = null;
		String laststr = "";
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
			reader = new BufferedReader(new InputStreamReader(is));
			;
			String tempString = null;

			// int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null)
			{
				// 显示行号
				// logger.infoln("line " + line + ": " + tempString);
				laststr = laststr + tempString;
				// line++;
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e1)
				{
				}
			}
		}
		return laststr;
	}

}
