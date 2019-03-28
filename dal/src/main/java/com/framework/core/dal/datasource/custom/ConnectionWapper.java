package com.framework.core.dal.datasource.custom;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import org.logicalcobwebs.proxool.ProxoolFacade;

import com.framework.core.dal.datasource.DataSourceManager;


/**
 * 某些特殊场景下：
 * 连接池中的连接释放到连接池之后，还要等定时任务来执行释放，这个中间过程会有很多连接还处在连接保持状态，当一个服务器装了 几万个数据库之后，可能会导致连接数不够。
 * 
 * 场景举例: 定时任务高并发的给几万个企业刷新指标数据
 * 
 * @author zhangjun
 *
 */
public class ConnectionWapper implements InvocationHandler {

	private static final String CLOSE_METHOD = "close";

	private Connection conn = null;

	private ConnectionWapper(Connection conn) {
		this.conn = conn;
	};

	public static Connection getInstance(Connection conn) {

		if (conn == null)
			return null;

		InvocationHandler handler = new ConnectionWapper(conn);

		return (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(), conn.getClass().getInterfaces(),
				handler);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (CLOSE_METHOD.equals(method.getName())) {

			Object obj = method.invoke(conn, args);

			if (isNeedForceRemoveConn()) {
				killConn(conn);
			}

			return obj;
		}

		return method.invoke(conn, args);
	}

	/**
	 * 是否强制释放连接 1. 在标签范围内 2. 不在事务范围内
	 * 
	 * @return
	 */
	private boolean isNeedForceRemoveConn() {
		// 如果在强制释放连接的标签范围内，并且不在事务里
		if(DataSourceManager.isForceReleaseConn()&&!DataSourceManager.isInTransaction()){
			return true;
		}
		
		return false;
	}

	/**
	 * 强制释放连接
	 * 
	 * @param conn
	 */
	private void killConn(Connection conn) {

		try {
			ProxoolFacade.killConnecton(conn, true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}