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

import com.alibaba.fastjson.JSON;
import com.framework.core.alarm.EventPublisherUtils;
import com.framework.core.alarm.event.SessionExceptionEvent;
import com.framework.core.error.exception.BizException;
import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.web.exception.TokenErrorCode;
import com.framework.core.web.session.filter.SessionFilter;
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
	 */
	public void tokenValidate(HttpServletRequest request, HttpServletResponse reponse)  {

		// 获取token，
		String token = TokenManager.fetchTokenFromRequest(request);

		if (StringUtils.isEmpty(token)) {
			return;
		}

		SessionFilter.setCurrentToken(token);
		
		try {

			doValidateChain(token, request, reponse);

		} catch (BizException e) {

			reportEvent(SessionExceptionEvent.TYPE_REFRESH_TOKEN_AUTH_FAILED, "TokenStrategyExecutor=>[tokenValidate]:bizException", "[token]="+token, e);

			throw e;

		} catch (Exception e) {

			
			reportEvent(SessionExceptionEvent.TYPE_REFRESH_TOKEN_AUTH_FAILED, "TokenStrategyExecutor=>[tokenValidate]:exception", "[token]="+token, e);
			
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
	 */
	private void doValidateChain(String token, HttpServletRequest request, HttpServletResponse reponse)
			 {

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

	

	/**
	 * 上报事件
	 * 
	 * @param methodInfo
	 * @param args
	 * @param e
	 */
	private static void reportEvent(int type, String methodInfo, String param, Exception e) {

		int errorcode = BaseCode.EX_SYSTEM_UNKNOW.getCode();
		
		if(e instanceof BizException) {
			errorcode = ((BizException) e).getErrorCode();
		}
		
		SessionExceptionEvent event = new SessionExceptionEvent(type, SessionFilter.getRequest().getRequestURI(),
				methodInfo, param, errorcode,e);
		EventPublisherUtils.reportEvent(event);
	}

	
}