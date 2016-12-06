package com.framework.core.web.session.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import com.framework.core.error.exception.BizException;
import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.web.session.token.TokenManager;

import io.jsonwebtoken.Claims;

/**
 * 
 * @author zhangjun
 *
 */
public class TokenStrategyExecutor implements ApplicationContextAware {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<TokenStrategy> strategyList = new ArrayList<TokenStrategy>();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		Map<String, TokenStrategy> strategys = applicationContext.getBeansOfType(TokenStrategy.class);

		if (strategys == null || strategys.isEmpty()) {
			return;
		}

		strategyList.addAll(strategys.values());

		Collections.sort(strategyList);
	}

	/**
	 * token 校验
	 * 
	 * @param request
	 * @param reponse
	 * @return
	 * @throws BizException
	 */
	public void tokenValidate(HttpServletRequest request, HttpServletResponse reponse) throws BizException {

		// 获取token，
		String token = TokenManager.fetchTokenFromRequest(request);

		if (StringUtils.isEmpty(token)) {
			return;
		}

		try {

			doValidateChain(token, request, reponse);

		} catch (BizException e) {

			throw e;

		} catch (Exception e) {

			logger.error(
					"TokenStrategyExecutor execute token validate unexpected failed!! message is :" + e.getMessage(),
					e);

			throw new BizException(BaseCode.EX_SYSTEM_UNKNOW.getCode());
		}

	}

	/**
	 * 
	 * @param claims
	 * @param request
	 * @param reponse
	 * @return
	 * @throws BizException
	 */
	private void doValidateChain(String token, HttpServletRequest request, HttpServletResponse reponse)
			throws BizException {

		if (CollectionUtils.isEmpty(strategyList)) {
			return;
		}

		for (TokenStrategy strategy : strategyList) {

			StrategyEnum result = strategy.vaidateToken(token, request, reponse);

			if (result == StrategyEnum.STRATEGY_VALIDATE_SUCCESS_ADN_BREAK) {
				break;

			}

		}

	}

}