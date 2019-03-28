package com.waiqin365.task;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BeanManager implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		applicationContext = context;

	}

	
	/**
	 * 根据bean名字获取bean
	 * 
	 * @param beanName
	 * @return
	 */
	public static Object getBeanByName(String beanName) {

		return applicationContext.getBean(beanName);
	}

}