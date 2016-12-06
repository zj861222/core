package com.framework.core.cache.redis.exception;

/**
 * 
 */
public class RedisLockFailedException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3021482018993879680L;

	public RedisLockFailedException(String message){
        super(message);
    }

}
