package com.framework.core.test.zookeeper.listener;


import javax.annotation.Resource;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.framework.core.zookeeper.listener.custom.ZkNodeListener;
import com.framework.core.zookeeper.model.ZkNodeData;

@Component
public class DatesourceListener implements ZkNodeListener {
	


	@Override
	public void childUpdated(ZkNodeData nodeData) {
		
		System.out.println("DatesourceListener---childUpdated--,data is:"+JSON.toJSONString(nodeData));
		
	}

	@Override
	public void childAdded(ZkNodeData nodeData) {
		System.out.println("DatesourceListener---childAdded--,data is:"+JSON.toJSONString(nodeData));
		
	}

	@Override
	public void childDeleted(ZkNodeData nodeData) {
		System.out.println("DatesourceListener---childDeleted--,data is:"+JSON.toJSONString(nodeData));
		
	}

	@Override
	public void nodeUpdated(ZkNodeData nodeData) {
		System.out.println("DatesourceListener---nodeUpdatedeepe--,data is:"+JSON.toJSONString(nodeData));
		
	}

	@Override
	public boolean accept(ZkNodeData nodeData) {
		System.out.println("DatesourceListener---childUpdated--,data is:"+JSON.toJSONString(nodeData));
		return false;
	}

	@Override
	public String getMonitorPath() {
		return "/waiqin365/datasource";
	}
	
}