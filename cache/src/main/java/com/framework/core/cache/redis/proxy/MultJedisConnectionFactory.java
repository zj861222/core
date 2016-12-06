package com.framework.core.cache.redis.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.redis.ExceptionTranslationStrategy;
import org.springframework.data.redis.PassThroughExceptionTranslationStrategy;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

/**
 * 配置 多 twemproxy 的 jedis connection factory
 * 
 * @author zhangjun
 *
 */
public class MultJedisConnectionFactory implements InitializingBean, DisposableBean, RedisConnectionFactory {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final Method SET_TIMEOUT_METHOD;
	private static final Method GET_TIMEOUT_METHOD;

	static {

		Method setTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "setTimeout", int.class);
		if (setTimeoutMethodCandidate == null) {
			// Jedis V 2.7.x changed the setTimeout method to setSoTimeout
			setTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "setSoTimeout", int.class);
		}
		SET_TIMEOUT_METHOD = setTimeoutMethodCandidate;

		Method getTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "getTimeout");
		if (getTimeoutMethodCandidate == null) {
			getTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "getSoTimeout");
		}

		GET_TIMEOUT_METHOD = getTimeoutMethodCandidate;
	}

	private static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = new PassThroughExceptionTranslationStrategy(
			JedisConverters.exceptionConverter());

	private List<JedisShardInfo> shardInfoList = new ArrayList<>();

	private String tweproxyAddrs;

	private int timeout = Protocol.DEFAULT_TIMEOUT;

	private String password;

	private boolean usePool = true;

	private List<Pool<Jedis>> pools = new ArrayList<>();

	private JedisPoolConfig poolConfig = new JedisPoolConfig();

	private int dbIndex = 0;

	private boolean convertPipelineAndTxResults = true;

	private AtomicBoolean inited=new AtomicBoolean(false);

	public String getTweproxyAddrs() {
		return tweproxyAddrs;
	}

	public void setTweproxyAddrs(String tweproxyAddrs) {
		this.tweproxyAddrs = tweproxyAddrs;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Pool<Jedis>> getPools() {
		return pools;
	}

	public void setPools(List<Pool<Jedis>> pools) {
		this.pools = pools;
	}

	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(JedisPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public int getDbIndex() {
		return dbIndex;
	}

	public void setDbIndex(int dbIndex) {
		this.dbIndex = dbIndex;
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data
	 * type. If false, results of {@link JedisConnection#closePipeline()} and
	 * {@link JedisConnection#exec()} will be of the type returned by the Jedis
	 * driver
	 * 
	 * @return Whether or not to convert pipeline and tx results
	 */
	@Override
	public boolean getConvertPipelineAndTxResults() {
		return convertPipelineAndTxResults;
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data
	 * type. If false, results of {@link JedisConnection#closePipeline()} and
	 * {@link JedisConnection#exec()} will be of the type returned by the Jedis
	 * driver
	 * 
	 * @param convertPipelineAndTxResults
	 *            Whether or not to convert pipeline and tx results
	 */
	public void setConvertPipelineAndTxResults(boolean convertPipelineAndTxResults) {
		this.convertPipelineAndTxResults = convertPipelineAndTxResults;
	}

	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return EXCEPTION_TRANSLATION.translate(ex);
	}

	@Override
	public JedisConnection getConnection() {

		Pool<Jedis> pool = fetchJedisPool();

		Jedis jedis;
		if (pool != null)
			jedis = pool.getResource();
		else
			jedis = fetchJedisConnector();
		
		JedisConnection connection = (usePool ? new JedisConnection(jedis, pool, dbIndex)
				: new JedisConnection(jedis, null, dbIndex));
		connection.setConvertPipelineAndTxResults(convertPipelineAndTxResults);

		return connection;
	}

	/**
	 * 获取随机的 pool
	 * 
	 * @return
	 */
	private Pool<Jedis> fetchJedisPool() {

		if (usePool && !CollectionUtils.isEmpty(pools)) {

			// 获取随机index 0 ~ length－1
			int randomIndex = (int) (Math.random() * pools.size());

			return pools.get(randomIndex);
		}

		return null;

	}

	/**
	 * 获取连接的jedis
	 * 
	 * @return
	 */
	protected Jedis fetchJedisConnector() {
		try {
			if (usePool && !CollectionUtils.isEmpty(pools)) {
				// 获取随机index 0 ~ length－1
				int randomIndex = (int) (Math.random() * pools.size());

				return pools.get(randomIndex).getResource();
			}

			int randomIndex = (int) (Math.random() * shardInfoList.size());

			Jedis jedis = new Jedis(shardInfoList.get(randomIndex));
			// force initialization (see Jedis issue #82)
			jedis.connect();

			return jedis;
		} catch (Exception ex) {
			throw new RedisConnectionFailureException("Cannot get Jedis connection", ex);
		}
	}

	@Override
	public RedisSentinelConnection getSentinelConnection() {

		throw new InvalidDataAccessResourceUsageException("No Sentinels configured");

	}

	@Override
	public void destroy() throws Exception {
		
		
		if (usePool && !CollectionUtils.isEmpty(pools)) {
			
			for(Pool<Jedis> pool:pools) {
				
				try {
					pool.destroy();
				} catch (Exception ex) {
					logger.warn("Cannot properly close Jedis pool", ex);
				}
				
				pool = null;
			}
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		if(inited.compareAndSet(false, true))
		{
			Map<String, Integer> addrMap = getTwemproxyAddrs(tweproxyAddrs);
			
			initJedisConnectionFactory(addrMap);
		}

	}

	/**
	 * 解析 twemproxy ip 端口
	 * 
	 * @param twemProxyAddrs
	 * @return
	 * @throws RuntimeException
	 */
	private Map<String, Integer> getTwemproxyAddrs(String twemProxyAddrs) throws RuntimeException {

		logger.info("MutiJedisFactory  getTwemproxyAddrs, twemProxyAddrs value is {}", twemProxyAddrs);

		if (StringUtils.isEmpty(twemProxyAddrs)) {
			throw new RuntimeException("please check config.properties file Twemproxy address!");
		}

		String[] addrs = twemProxyAddrs.split(",");
		if (addrs == null || addrs.length == 0) {
			throw new RuntimeException("please check config.properties file Twemproxy address!");
		}

		Map<String, Integer> addrMap = new HashMap<>();

		for (String addr : addrs) {

			String[] addrUnit = addr.split(":");
			if (addrUnit == null || addrUnit.length != 2) {
				throw new RuntimeException("please check config.properties file Twemproxy address!");
			}
			
			logger.info("MutiJedisFactory  spilt address addrUnit[0] is {},addrUnit[1] is {}",addrUnit[0],addrUnit[1]);

			addrMap.put(addrUnit[0].trim(), NumberUtils.toInt(addrUnit[1].trim()));

		}

		return addrMap;
	}

	/**
	 * 初始化多个 Jedis pool
	 * 
	 * @param addrs
	 */
	private void initJedisConnectionFactory(Map<String, Integer> addrs) throws RuntimeException {
		
		for (Entry<String, Integer> entry : addrs.entrySet()) {
			
			logger.info("MutiJedisFactory  initJedisConnectionFactory, entry key is {},entry value is {}", entry.getKey(),entry.getValue());
			
			JedisShardInfo shardInfo = new JedisShardInfo(entry.getKey(), entry.getValue());
			
			
			if (StringUtils.hasLength(password)) {
				shardInfo.setPassword(password);
			}

			if (timeout > 0) {
				setTimeoutOn(shardInfo, timeout);
			}

			shardInfoList.add(shardInfo);

			if (usePool) {
				
				logger.info("MutiJedisFactory  initJedisConnectionFactory, host value is {},port value is {}", shardInfo.getHost(),shardInfo.getPort());
				
				Pool<Jedis> pool = new JedisPool(this.poolConfig, shardInfo.getHost(), shardInfo.getPort(),
						getTimeoutFrom(shardInfo), shardInfo.getPassword());
				if (pool != null) {
					pools.add(pool);
				}
			}

		}

	}

	private void setTimeoutOn(JedisShardInfo shardInfo, int timeout) {
		ReflectionUtils.invokeMethod(SET_TIMEOUT_METHOD, shardInfo, timeout);
	}

	private int getTimeoutFrom(JedisShardInfo shardInfo) {
		return (Integer) ReflectionUtils.invokeMethod(GET_TIMEOUT_METHOD, shardInfo);
	}

}