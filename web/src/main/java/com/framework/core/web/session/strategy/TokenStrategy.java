package com.framework.core.web.session.strategy;


/**
 * 
 * @author zhangjun
 *
 */
public abstract class TokenStrategy implements Strategy, Comparable<TokenStrategy> {

	@Override
	public int compareTo(TokenStrategy strategy) {

		int priority = getStrategyPriority();

		return strategy.getStrategyPriority() - priority;

	}


}