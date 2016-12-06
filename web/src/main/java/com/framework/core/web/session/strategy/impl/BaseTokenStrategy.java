package com.framework.core.web.session.strategy.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;

import com.framework.core.error.exception.BizException;
import com.framework.core.web.session.strategy.StrategyEnum;
import com.framework.core.web.session.strategy.TokenStrategy;
import com.framework.core.web.session.token.TokenManager;

import io.jsonwebtoken.Claims;

/**
 * token 基本校验，是否过期
 * 
 * @author zhangjun
 *
 */
public class BaseTokenStrategy extends TokenStrategy {

	@Override
	public StrategyEnum vaidateToken(String token, HttpServletRequest request, HttpServletResponse reponse)
			throws BizException {

		TokenManager.isValidToken(token);

		return StrategyEnum.STRATEGY_VALIDATE_SUCCESS;

	}

	@Override
	public int getStrategyPriority() {
		return 5;
	}


}