package com.framework.core.test.hystrix;


import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.core.error.exception.BizException;
import com.framework.core.test.hystrix.dao.HystrixTestDao;
import com.framework.core.web.session.SessionHolder;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommand.Setter;

@Controller
public class HystrixTestController {
	
	@Resource
	private HystrixTestDao hystrixTestDao;
	
    @RequestMapping(value = "test2")
	@ResponseBody
	public String test2() {
    	
    	
    	
    	try {
			SessionHolder.setAttrbute("11", "22", "33");
			
			SessionHolder.setAttrbute("11", "42", "36");

			
						String value = (String)SessionHolder.getAttrbute("11", "22");
			
			System.out.println("value="+value);
			
		} catch (BizException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

    	String result = hystrixTestDao.getTestString();
    	
    	System.out.println("result="+result
    			);
    	
    	
    	return "11";
//    	
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
////    	
    	
    	
    	
//    	for(int index =9;index<11;index++) {
//    		
//    		final int i = index;
//    		new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					
//					System.out.println("Runnable************index="+i+"*****************in run method");
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
//    	
//    	
//    	
//    	return "the end";
    }
    
    
    
    
    
    

//	
//	@SuppressWarnings("unused")
//	private class TestCommand extends HystrixCommand<String> {
//
//        int index;
//		
//		public TestCommand(int index) {
//			
//			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("111"))
//					.andCommandKey(HystrixCommandKey.Factory.asKey("222"))
//					.andCommandPropertiesDefaults(  
//                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(5000))  
//					
//					);
//			
//			this.index = index;
//
//
//		}
//
//		@Override
//		protected String run() throws Exception {
//
//			
//			System.out.println("************index="+index+"*****************in run method");
//			
//			if(index>0&&index<10) {
//				
////				System.out.println("pass************index="+index+"*****************in run method");
//
//				return "22";
//			}
//			
//			Thread.sleep(6000);
//			
//			return "11";
//		}
//		
////		
////		
////	    @Override
////	    public String getFallback() {
////	    	
////	    	return null;
////	
////	    }
//		
//	}
	
}