package com.framework.core.test.task.elasticjob.dataflow;



public class DataSupporter {
	

	/**
	 * 
	 * @return
	 */
	public static String getData() {
		

		double num = Math.random()*100;
		
		if(num>50&&num<60) {
			return null;
		}
		
		return String.valueOf(num);
	}
	
	
}