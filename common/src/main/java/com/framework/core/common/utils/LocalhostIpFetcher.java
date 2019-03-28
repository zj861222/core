package com.framework.core.common.utils;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  获取本地IP地址
 * 
 */
public class LocalhostIpFetcher
{

	private static final Logger logger = LoggerFactory.getLogger(LocalhostIpFetcher.class);

	private static String LOCAL_IP = "127.0.0.1";

	private static String INVAILED_IP = "0.0.0.0";

	public static String fetchLocalIP()
	{

		String ip = "127.0.0.1";

		LocalhostIpFetcher instance = new LocalhostIpFetcher();

		ip = instance.fetchLocalIPBySocket();

		if (instance.isInvalidIp(ip))
		{

			ip = instance.fetchLocalIPByNetworkInterface();

		}

		// System.out.println("*********************************local ip =
		// "+ip);

		return ip;

	}

	/**
	 * 此种方法使用绝大多数场景，但是对多网卡支持有问题
	 * @return
	 */
	private String fetchLocalIPBySocket()
	{

		String localIP = "127.0.0.1";
		DatagramSocket sock = null;
		try
		{
			SocketAddress socket_addr = new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 1);
			sock = new DatagramSocket();
			sock.connect(socket_addr);

			localIP = sock.getLocalAddress().getHostAddress();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			sock.disconnect();
			sock.close();
			sock = null;
		}
		return localIP;

	}

	/**
	 * 是否有效ip
	 * @param ip
	 * @return
	 */
	private boolean isInvalidIp(String ip)
	{

		if (StringUtils.isEmpty(ip) || ip.trim().equals(LOCAL_IP) || ip.trim().equals(INVAILED_IP))
		{
			return true;
		}
		return false;
	}

	/**
	 * 根据网卡获取当前ip
	 * @return
	 */
	private String fetchLocalIPByNetworkInterface()
	{
		try
		{
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements())
			{
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp())
				{
					continue;
				}
				else
				{
					Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
					while (addresses.hasMoreElements())
					{
						ip = addresses.nextElement();
						if (ip != null && ip instanceof Inet4Address)
						{
							return ip.getHostAddress();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("IP地址获取失败", e);
		}
		return "";
	}

	/**
	 * 获取本机的端口号
	 * @return
	 * @throws MalformedObjectNameException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public static String tryFetchServerPort() 
	{

		try
		{
			ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

			if (CollectionUtils.isEmpty(servers))
			{
				return null;
			}

			MBeanServer mBeanServer = servers.get(0);

			Set<ObjectName> names = servers.get(0).queryNames(new ObjectName("Catalina:type=Connector,*"), null);

			if (names == null || names.size() == 0)
			{
				return null;
			}

			for (Iterator<ObjectName> i = names.iterator(); i.hasNext();)
			{

				ObjectName obj = i.next(); // 下载

				String protocol = (String) mBeanServer.getAttribute(obj, "protocol");


				if (StringUtils.isNotBlank(protocol) && protocol.indexOf("Http11NioProtocol") > -1)
				{
					int port = (Integer) mBeanServer.getAttribute(obj, "port");

					return port + "";
				}

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();

		}

		return null;
	}

}
