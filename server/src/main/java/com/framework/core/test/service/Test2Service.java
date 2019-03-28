package com.framework.core.test.service;

import java.util.Random;

/**
 * 测试service
 * @author zhangjun
 *
 */
public class Test2Service {
	
	
	public void test2(){
		
		try
		{
			Random rand =new Random(25);
			long time = rand.nextInt(50);
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void test3(){
		
		try
		{
			Random rand =new Random(25);
			long time = rand.nextInt(50);
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void test4(){
		
		try
		{
			Random rand =new Random(25);
			long time = rand.nextInt(50);
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}