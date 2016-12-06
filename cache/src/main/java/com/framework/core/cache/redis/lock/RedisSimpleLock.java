package com.framework.core.cache.redis.lock;

import com.framework.core.cache.redis.GracefulRedisTemplate;
import com.framework.core.cache.redis.RedisValueOperations;
import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.cache.redis.exception.RedisLockFailedException;
import com.framework.core.error.exception.BizException;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

/**
 * redis  分布式锁
 * 
 * @author zhangjun
 *
 */
@Service
public class RedisSimpleLock {

    @Resource(name="gracefulRedisTemplate")
    GracefulRedisTemplate<String,String> gracefulRedisTemplate ;

    @Autowired
    RedisValueOperations<String,String> redisValueOperations ;

    /**
     * 加锁逻辑:
     *     使用redis点setNX命令,如果写入redis成功,返回true,如果已经存在返回false. 同时只会有一个线程\一个进程成功
     * 锁超时失效:
     *     如果不显示释放锁,默认 60s 失效
     *
     * @param key   锁的key
     * @throws RedisLockFailedException
     */
    public void tryLock( String key ) throws BizException{

        //加锁失败, 抛出异常
        if( Boolean.FALSE.equals(redisValueOperations.setIfAbsent(key, "lock")) ){
            throw new BizException(RedisErrorCode.EX_SYS_REDIS_LOCK_FAIL.getCode());
        }

        //加锁成功,设置expire时间,60s
        gracefulRedisTemplate.longExpire(key, 60, TimeUnit.SECONDS);
    }


    /**
     * 释放锁
     * @param key
     */
    public void releaseLock(String key){
    	gracefulRedisTemplate.delete(key);
    }



}
