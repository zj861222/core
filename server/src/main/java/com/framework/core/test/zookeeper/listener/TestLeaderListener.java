package com.framework.core.test.zookeeper.listener;




import org.springframework.stereotype.Component;

import com.framework.core.zookeeper.election.ZkLeaderLatcherListener;

/**
 * 
 * @author zhangjun
 *
 */
@Component
public class TestLeaderListener extends ZkLeaderLatcherListener {

	
	@Override
    public boolean isLeader(){
		
	      System.out.println("TestLeaderListener  isLeader:"+isLeader);

        return this.isLeader;
    }

	@Override
    public void setupLeader(boolean isLeader){
		
      this.isLeader = isLeader;
      System.out.println("TestLeaderListener  setupLeader:"+isLeader);
    }
	
	
	
	@Override
	public String getLatchType() {

		return "Test"+Thread.currentThread().getId();
	}

	
}