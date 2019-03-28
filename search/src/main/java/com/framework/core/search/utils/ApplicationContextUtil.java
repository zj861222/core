package com.framework.core.search.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 */
@Component
@Lazy(value = false)
public class ApplicationContextUtil implements ApplicationContextAware {
	
	protected static ApplicationContext context = null;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

	public static ApplicationContextUtil getInstance() {
		return (ApplicationContextUtil)context.getBean("applicationContextUtil");
	}

    public static Object getBean(String name){
        return context.getBean(name);
    }

    public static Object getBean(Class classz){
        return context.getBean(classz);
    }
}
