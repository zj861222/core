package com.framework.core.web.hystrix.command;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.core.web.hystrix.audit.ServiceCallAudit;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

public class InnerServiceHystrixCommond<T> extends HystrixCommand<T> {

	protected final ServletRequest request;

	protected final ServletResponse response;

	private String serviceName;

	private String url;

	private FilterChain chain;

	private T fallback;

	private ServiceCallAudit audit;

	private Long tenantId;

	/**
	 * 源自那个服务
	 */
	private String srcServiceName;

	public InnerServiceHystrixCommond(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Long tenantId, String context) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(context))
				.andCommandKey(HystrixCommandKey.Factory.asKey(request.getRequestURI())));

		this.srcServiceName = null;

		this.serviceName = request.getRequestURI();

		this.chain = chain;

		this.tenantId = tenantId;

		this.request = request;

		this.response = response;

	}

	@Override
	protected T run() throws Exception {
		
		
		chain.doFilter(request, response);

		return null;
	}

}