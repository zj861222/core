package com.framework.core.search.index.factory;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.framework.core.common.utils.PropertiesUtil;
import com.framework.core.search.index.factory.IndexClient;

/**
 */

public abstract class IndexClientFactory {
	

    /**
     * 创建索引器，索引器负责处理具体的索引接口
     * 
     * @param properties
     * @return
     */
    public IndexClient createIndexClient(Map<String, String> properties){
    	
    	String isEnable = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "search.func.enable","0"); 
    	
    	if(!"1".equals(isEnable)){
    		return null;
    	}

    	return doCreateIndexClient(properties);
    }
    
    
    /**
     * 创建 index client
     * @param properties
     * @return
     */
    public abstract IndexClient doCreateIndexClient(Map<String, String> properties);
    
    
    /**
     * 初始化引擎
     */

    public abstract void initSearchEngine();
    

}
