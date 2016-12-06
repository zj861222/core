package com.framework.core.cache.redis.proxy.jedis;


import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 
 * @author zhangjun
 *
 */
public class JedisExcuteTemplate {

    /**
     * connection factpry
     */
	private RedisConnectionFactory connectionFactory;
	
	


	public RedisConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}


	public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}


	
	/**
	 * 获取链接，从 twmproxy 获取
	 * @return
	 */
	public RedisConnection getConnection() {
		
        try {

        	RedisConnection conenction = connectionFactory.getConnection();

            return conenction;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
		
	}
	

	/**
	 * 释放连接池中的链接
	 * @param connection
	 */
	 public void closeconnection(RedisConnection connection) {
		 
		 if(connection!=null)
		     connection.close();
		 
	 }
	


    public Object excute(Callback executeCallback) {

    	RedisConnection connection = getConnection();

        if (connection == null) {
            return null;
        }
        
        
        try {
            // 通过回调方法执行具体执行
            return executeCallback.command(connection);
        } catch (Exception e) {
        	e.printStackTrace();
    
        } finally {
            // 释放资源
        	closeconnection(connection);
//        	returnResource(shardedJedis);
        }
        return null;
    }

    /**
     *
     *
     * @author zhangwei_david
     * @version $Id: RedisExecuteTemplate.java, v 0.1 2015年6月6日 下午7:45:58 zhangwei_david Exp $
     */
    public interface Callback {

        public Object command(RedisConnection connection);
//        public Object command(ShardedJedis shardedJedis);

    }
}
