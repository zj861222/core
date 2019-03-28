package com.framework.core.test.hystrix;


import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.EventReporter;
import com.framework.core.alarm.event.ServiceAccessEvent;
import com.framework.core.cache.redis.GracefulRedisTemplate;
import com.framework.core.test.hystrix.dao.HystrixTestDao;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

@Controller
public class HystrixTestController {
	
	@Resource
	private HystrixTestDao hystrixTestDao;
	
	@Resource
	private GracefulRedisTemplate gracefulRedisTemplate;
	

    @RequestMapping(value = "test2")
	@ResponseBody
	public String test2() {
    	
		System.out.println("main thread id is:"+Thread.currentThread().getId());

		ServiceAccessEvent event = new ServiceAccessEvent("1111111111", 20000,
				500, "tst stack");
		event.setSrcService("1111111111111111111");	
		
		event.setTenantId(123);
		event.setUserId(123);
		
		
		EventPublisherUtils.reportEvent(event);
		
//		
//		hystrixTestDao.getTestString();
//		
//    	TestCommand tc1 = new TestCommand(1002);
//
//    	tc1.execute();
//    	
//    	try {
//    		
////    		testMethod();
//    		
//			SessionHolder.setAttrbute("11", "22", "33");
//			
//			SessionHolder.setAttrbute("11", "42", "36");
//
//			
//			String value = (String)SessionHolder.getAttrbute("11", "22");
//			
//			System.out.println("value="+value);
//			
//		} catch (BizException e) {
//			System.out.println("222222="+111);
//
//		} catch(Exception e) {
//			
//			System.out.println("111111="+111);
//
//		}
//    	
//
//    	String result = hystrixTestDao.getTestString();
//    	
//    	System.out.println("result="+result
//    			);
//    	
//    	
//    	return "11";
    	
//    	try {
//	    	TestCommand tc = new TestCommand(11);
//
//    	String result =  tc.execute();
//    	return result;
//
//    	
//    	}catch(Exception e) {
//    		e.printStackTrace();
//    		
//    		return "fail";
//    	}
  	

    	
//    	
//    	for(int index =9;index<10;index++) {
//    		
//    		if(index == 105) {
//    			try {
//					Thread.sleep(100000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    		}
//    		
//    		final int i = index;
//    		new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					
//					System.out.println("Runnable************index="+i+"*****************in run method，thread id is:"+Thread.currentThread().getId());
//
//					
//			    	TestCommand tc = new TestCommand(i);
//
//		    		tc.execute();					
//				}
//			}).start();
//    
//    	}
//    	
    	
    	
    	
    	return "the end";
    }
    
    
    
    
    
    

	
	@SuppressWarnings("unused")
	private class TestCommand extends HystrixCommand<String> {

        int index;
		
		public TestCommand(int index) {
			
			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("111"))
					.andCommandKey(HystrixCommandKey.Factory.asKey("222"))
					
//					.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
//					.andCommandPropertiesDefaults(  
//                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(5000)) 
//					.andCommandPropertiesDefaults(  
//                            HystrixCommandProperties.Setter().withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE))  
//					
					
					.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
							.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
							.withExecutionIsolationSemaphoreMaxConcurrentRequests(2)));			
					
			
			this.index = index;


		}

		@Override
		protected String run() throws Exception {

			System.out.println("************index="+index+"*****************in run method,Thread id is:"+Thread.currentThread().getId());

			if(true) {
				throw new Exception("23423424");
			}
			
			
			if(index>0&&index<10) {
				
//				System.out.println("pass************index="+index+"*****************in run method");

				return "22";
			}
			
			Thread.sleep(6000);
			
			return "11";
		}
		
		
		
	    @Override
	    public String getFallback() {
	    	
	    	System.out.println("-----------------getFallback");
	    	return "getfallback";
	
	    }
		
	}
	
	
	
	
    
    
//    
//    private void testMethod () throws BizException {
//    	throw new BizException(11,"test runtime");
//    }
//	
}