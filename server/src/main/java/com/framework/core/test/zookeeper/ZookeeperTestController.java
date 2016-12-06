package com.framework.core.test.zookeeper;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class ZookeeperTestController {
	

    @RequestMapping(value = "test", method = RequestMethod.GET)
	@ResponseBody
	public String test() {
    	
//    	
//    	try {
//    		
//			ZookeeperClientHelper.createNode("/waiqin333/123", CreateMode.PERSISTENT, "111");
//
//    		
//    		boolean result  = ZookeeperClientHelper.isNodeExist("/waiqin333");
//    		
//    		boolean result2  = ZookeeperClientHelper.isNodeExist("/waiqin365");
//
//    		
//    		
//			ZookeeperClientHelper.createNode("/waiqin365/test", CreateMode.PERSISTENT, "111");
//			
//			String data = ZookeeperClientHelper.getData("/waiqin365/test");
//			
//			System.out.println("data is {}"+data);
//			
//			ZookeeperClientHelper.updateNodeDate("/waiqin365/test", "222");
//			
//			String data2 = ZookeeperClientHelper.getData("/waiqin365/test");
//			
//			System.out.println("data2 is {}"+data2);
//			
//			
//			ZookeeperClientHelper.deleteNode("/waiqin365/test");
//			
//			
//			String data3 = ZookeeperClientHelper.getData("/waiqin365/test");
//			
//			
//			System.out.println("data3 is {}"+data3);
//
//
//
//
//
//			
//			
//		} catch (BizException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	
 
    	try {

    		Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	

    	return "test123";
		
	}
    
    
    
    
	
}