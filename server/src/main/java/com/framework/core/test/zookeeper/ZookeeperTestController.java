package com.framework.core.test.zookeeper;


import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.framework.core.common.utils.JwtBuilderUtils;
import com.framework.core.task.internel.exception.MasterException;
import com.framework.core.task.internel.template.inner.TenantIdFetcher;
import com.waiqin365.task.BeanManager;

import io.jsonwebtoken.Claims;


@Controller
public class ZookeeperTestController {
	
	@Resource
	private JwtBuilderUtils jwtBuilderUtils;
	
	@Resource
	private TenantIdFetcher tenantIdFetcher;
	

    @RequestMapping(value = "test", method = RequestMethod.GET)
	@ResponseBody
	public String test() {
    	
    	
    	try {
			
    		tenantIdFetcher.fetchWaitExecutTenantIds("testJob1", 1000, 0);
			
			
		} catch (MasterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
//    	Object obj = BeanManager.getBeanByName("test1");
//    	
//    	System.out.println(obj);
//    	
//   String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqd3QiLCJpYXQiOjE0ODg4Njk3MzUsInN1YiI6IntcImxvZ2luVHlwZVwiOlwiU09VUkNFX1RZUEVfV0VCXCIsXCJyZWFsUmVmcmVzaFRva2VuRXhwaXJlTWludXRlXCI6NDMyMCxcInJlYWxUb2tlbkV4cGlyZU1pbnV0ZVwiOjE0NDAsXCJyZWZyZXNoVG9rZW5FeHBpcmVIb3Vyc1wiOjcyLFwidGVuYW50SWRcIjo3MDQyMTQ3NzIxNTA2MTg4MTgxLFwidG9rZW5FeHBpcmVIb3Vyc1wiOjI0LFwidXNlcklkXCI6NTQ3NTQyMzg4NzI1MDE2Mjc4N30iLCJpc3MiOiI1NDc1NDIzODg3MjUwMTYyNzg3XzcwNDIxNDc3MjE1MDYxODgxODFfV0VCIiwiZXhwIjoxNDg4OTU2MTM1fQ.taFMz7Xcke8Bjpp8dTiT1-eIt5oJ4ymaCqg8eZq9_as"; 	
// //String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqd3QiLCJpYXQiOjE0ODc4MzA2NjAsInN1YiI6IntcImxvZ2luVHlwZVwiOlwiU09VUkNFX1RZUEVfV0VCXCIsXCJyZWFsUmVmcmVzaFRva2VuRXhwaXJlTWludXRlXCI6NDMyMCxcInJlYWxUb2tlbkV4cGlyZU1pbnV0ZVwiOjE0NDAsXCJyZWZyZXNoVG9rZW5FeHBpcmVIb3Vyc1wiOjcyLFwidGVuYW50SWRcIjo4MDY4NzkwNDIxMzQzOTE0OTY1LFwidG9rZW5FeHBpcmVIb3Vyc1wiOjI0LFwidXNlcklkXCI6Nzk2ODEyNzA2MjUzNTk4ODQ3MX0iLCJpc3MiOiI3OTY4MTI3MDYyNTM1OTg4NDcxXzgwNjg3OTA0MjEzNDM5MTQ5NjVfV0VCIiwiZXhwIjoxNDg3OTE3MDYwfQ.T8x0TDqmlZnlLNsBVvGxfGlYimAMPUoaGilIpj6CqqE";    	
//    	try {
//    		Pair<Boolean, Claims> pair = jwtBuilderUtils.parseJWTEx(token);
//    		
//    		System.out.println("pair:"+JSON.toJSONString(pair));
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
    	
    	
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