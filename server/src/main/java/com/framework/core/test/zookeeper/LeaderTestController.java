package com.framework.core.test.zookeeper;


import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.framework.core.common.utils.JwtBuilderUtils;
import com.framework.core.test.zookeeper.listener.TestLeaderListener;
import com.framework.core.zookeeper.model.CreateMode;
import com.framework.core.zookeeper.util.ZookeeperClientHelper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;


@Controller
public class LeaderTestController {
	
    @Resource
	private TestLeaderListener testLeaderListener;
    
    @Resource
    private JwtBuilderUtils jwtBuilderUtils;
    
    
    @RequestMapping(value = "test3")
	@ResponseBody
	public String test3() {
    	
    	boolean result = testLeaderListener.isLeader();
    	
//    	ZookeeperClientHelper.deleteNode("/waiqin365/leader_latch");
    	
    	
    	
    	ZookeeperClientHelper.createNode("/waiqin365/test", CreateMode.PERSISTENT, "你是谁");
    	
    	
    	System.out.println("node data:"+ZookeeperClientHelper.getData("/waiqin365/test"));
    	
		return "Test:"+result;
    	
    }
	
    
    @RequestMapping(value = "test4")
	@ResponseBody
	public String test4() {
    	
    	
    	String value = ZookeeperClientHelper.getData("/waiqin365/datasource");

    	ZookeeperClientHelper.deleteNode("/waiqin365/leader_latch");
    	
		return "Test:";
    	
    }    
    
    
    @RequestMapping(value = "test5")
	@ResponseBody
    public String test5() {
    	
    	String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqd3QiLCJpYXQiOjE1MjE0MzY2MTgsInN1YiI6IntcImxvZ2luVHlwZVwiOlwiU09VUkNFX1RZUEVfQ0xJRU5UXCIsXCJyZWFsUmVmcmVzaFRva2VuRXhwaXJlTWludXRlXCI6NDMyMDAsXCJyZWFsVG9rZW5FeHBpcmVNaW51dGVcIjoxNDQwMCxcInJlZnJlc2hUb2tlbkV4cGlyZUhvdXJzXCI6NzIwLFwidGVuYW50SWRcIjo4OTEzMTUwODE3Nzc1Mjc5MDM4LFwidG9rZW5FeHBpcmVIb3Vyc1wiOjI0MCxcInVzZXJJZFwiOjg2ODY1MjAzNTE0MTYxOTQ5NDF9IiwiaXNzIjoiODY4NjUyMDM1MTQxNjE5NDk0MV84OTEzMTUwODE3Nzc1Mjc5MDM4X0NMSUVOVCIsImV4cCI6MTUyMjMwMDYxOH0.tT78NdHgfUFA4bE1NO-fn8HSrNksbuVL20DTVdTRkgQ";
    	
    	
    	try {
    		Pair<Boolean, Claims> result = jwtBuilderUtils.parseJWTEx(token);
    		
    		System.out.println("Claims="+JSON.toJSONString(result.getRight()));
    		
    		System.out.println(result.getRight().getExpiration());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	return null;
    }
	
	
}