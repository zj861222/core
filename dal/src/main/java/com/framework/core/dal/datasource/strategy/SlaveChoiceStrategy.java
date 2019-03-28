package com.framework.core.dal.datasource.strategy;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import com.framework.core.common.strategy.ChoiceStrategy;

/**
 * 从库选择策略
 * @author zhangjun
 *
 */
public class SlaveChoiceStrategy implements InitializingBean {
		
	private static SlaveChoiceStrategy instance;
	
	private ChoiceStrategy choiceStrategy;

	
	public ChoiceStrategy getChoiceStrategy() {
		return choiceStrategy;
	}


	public void setChoiceStrategy(ChoiceStrategy choiceStrategy) {
		this.choiceStrategy = choiceStrategy;
	}

	/**
	 * 获取从库
	 * @param slaves
	 * @return
	 */
	public static DataSource getSlave(List<DataSource> slaves) {
		
		if(CollectionUtils.isEmpty(slaves)){
			return null;
		}
		
		return instance.choiceStrategy.getInstance(slaves);
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		instance = new SlaveChoiceStrategy();
		instance.choiceStrategy = this.choiceStrategy;
	}
	
	
	
	
}