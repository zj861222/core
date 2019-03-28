package com.framework.core.test.service;



/**
 * 测试service
 * @author zhangjun
 *
 */
public class TestService {
	

	private Test2Service test2Service;
	

	public Test2Service getTest2Service()
	{
		return test2Service;
	}



	public void setTest2Service(Test2Service test2Service)
	{
		this.test2Service = test2Service;
	}



	public void test(){
		
		
		System.out.println("test");
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0;i < 20;i++){
			test2Service.test2();
		}
		
		
		for(int i = 0;i < 20;i++){
			test2Service.test3();
		}
		

		for(int i = 0;i < 30;i++){
			test2Service.test4();
		}
	}
	
}