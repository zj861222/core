package com.framework.core.web.tree;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.framework.core.cache.redis.lock.RedisComplexLock;
import com.framework.core.cache.redis.utils.RedisHelper;
import com.framework.core.web.tree.constants.RedisTreeCacheKeyBuilder;
import com.framework.core.web.tree.constants.TreeTypeEnum;

/**
 * 
 * 
 * 刷新时间handler
 * 
 * @author zhangjun
 *
 */
@Component
public class TreecacheRefreshEventHandler
{
    
    @Resource
    private TreeCacheService treeCacheService;
    
    @Resource
    private RedisHelper redisHelper;
    
    @Resource
    private RedisComplexLock redisComplexLock;
    
    /**
     * 发送刷新事件
     * <一句话功能简述>
     * <功能详细描述>
     * @param type
     * @see [类、类#方法、类#成员]
     */
    public void sendRefreshEvent(TreeTypeEnum type)
    {
        
        String lockkey = RedisTreeCacheKeyBuilder.getRefreshEventLockKey(type);
        
        String token = null;
        
        try
        {
            token = redisComplexLock.tryLock(lockkey, 2, TimeUnit.MINUTES);
            
            if(StringUtils.isNotBlank(token)) {
                treeCacheService.buildTreeCache(type);
            }
            
        }
        finally
        {
            if(StringUtils.isNotBlank(token)) {
                redisComplexLock.releaseLock(lockkey, token);
            }
        }
        
        
    }
    
}