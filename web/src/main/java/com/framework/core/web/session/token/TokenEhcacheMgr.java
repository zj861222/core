package com.framework.core.web.session.token;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;


public class TokenEhcacheMgr {

	private static final String SESSION_CACHE_NAME = "core_session_cache";

	private Cache cache;

	public TokenEhcacheMgr(int maxElementsInMemory, int timeToIdleSeconds, int timeToLiveSeconds,
			int diskExpiryThreadIntervalSeconds) {

			init(maxElementsInMemory, timeToIdleSeconds, timeToLiveSeconds, diskExpiryThreadIntervalSeconds);

	}

	private void init(int maxElementsInMemory, int timeToIdleSeconds, int timeToLiveSeconds,
			int diskExpiryThreadIntervalSeconds) {

        CacheManager singletonManager = CacheManager.create();  

        Cache sessionCache = new Cache(SESSION_CACHE_NAME, maxElementsInMemory, false, false, timeToIdleSeconds, timeToLiveSeconds);  

        singletonManager.addCache(sessionCache);
		

		cache = singletonManager.getCache(SESSION_CACHE_NAME);// 获得缓存
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

}