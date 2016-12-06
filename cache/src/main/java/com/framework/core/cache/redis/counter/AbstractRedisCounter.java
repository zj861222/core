package com.framework.core.cache.redis.counter;

import com.framework.core.cache.redis.GracefulRedisTemplate;
import com.framework.core.cache.redis.RedisValueOperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * 计数器, 多线程,多进程 对数据进行原子性增减, 比如生成排队号, 商品库存(抢购时防止超卖)
 */
@Component
public abstract class AbstractRedisCounter {

    @Resource(name="gracefulRedisTemplate")
    GracefulRedisTemplate<String,String> gracefulRedisTemplate ;

    @Autowired
    RedisValueOperations<String,String> redisValueOperations ;

    //创建单个线程
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    Logger logger = LoggerFactory.getLogger(AbstractRedisCounter.class);

    public AbstractRedisCounter(){

        //每隔一段时间,回调实现类, 更新数据库
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    try {
                        Thread.sleep( updateInterval() );
                        updateDatabase();
                    }catch(Exception e){}
                }

            }
        });

    }

    /**
     * 回调初始化数据
     * @return
     */
    protected abstract int initialize();

    /**
     * 计数器的唯一标识
     * @return
     */
    protected abstract String key();

    /**
     * 计数器失效时间
     * @return
     */
    protected abstract int expireInSeconds();

    /**
     * 定时回调,更新数据库
     */
    protected void persistent(String value){
        //do nothing
        //如果实现类不overwrite此方法, 则通过自己的方式,更新数据库,比如每次都update 减1
    };


    /**
     * 持久化时间间隔
     */
    protected int updateInterval(){
        return 500;
    }

    /**
     * 计数器,原子性加1
     * @return
     */
    public int increase(){

        return this.increaseBy(1) ;
    }

    /**
     * 计数器,原子性减1
     * @return
     */
    public int decrease(){

        return this.increaseBy(-1) ;
    }

    public int getValue(){
        String value =  redisValueOperations.get(key());
        if( value != null ){
            return Integer.parseInt(value);
        }
        return 0 ;
    }

    /**
     * 计数器,原子操作, 按照delta对数据进行增加\减少
     * @param delta 如果为正,则原子性增加,  如果为负,则原子性减少
     * @return
     */
    public int increaseBy(int delta){

        /**
         * 如果 key不存在, 则计数器第一次使用
         */
        if( !gracefulRedisTemplate.hasKey(key()) ){

            //回调 计数器使用者,  初始化计数器
            String initialValue = String.valueOf(initialize());

            //如果已经被初始化过, 则不用再初始化, 因为其他线程可能已经进行了 increase, 所以必须使用 setIfAbsent
            redisValueOperations.setIfAbsent(key(),initialValue);

            //设置有效期, redis所有的key, 必须设置有效期
            gracefulRedisTemplate.longExpire(key(), expireInSeconds(), TimeUnit.SECONDS);
        }

        //原子性对值修改delta数值
        return redisValueOperations.increment(key(),delta).intValue();
    }


    /**
     * 更新数据库
     */
    private void updateDatabase(){
        String value = redisValueOperations.get(key()) ;

        //如果key不存在,则不更新
        if( value != null ){
            persistent(value);
        }
    }


}
