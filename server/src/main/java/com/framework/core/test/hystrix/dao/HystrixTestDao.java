package com.framework.core.test.hystrix.dao;

import org.springframework.stereotype.Component;

import com.framework.core.web.hystrix.annotation.HystrixDaoConfig;

/**
 * 
 * @author zhangjun
 *
 */
@Component
public class HystrixTestDao {
	
	
	@HystrixDaoConfig(useHystrix=true,timeout=3000)
	public String  getTestString() {
		
		System.out.println("HystrixTestDao-getTestString-start,time="+System.currentTimeMillis());
		
		try {
			Thread.currentThread().sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("HystrixTestDao-getTestString-end,time="+System.currentTimeMillis());

		return "TestString";
	}
	
}