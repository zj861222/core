package com.framework.core.test.hystrix;

import org.springframework.stereotype.Service;

import com.framework.core.web.common.biz.BizDataFetcher;

public class BizDataFetcherImpl implements BizDataFetcher {

	@Override
	public Long getCurTenantId() {
		// TODO Auto-generated method stub
		return 123123L;
	}

	@Override
	public Long getCurrentLoginUserId() {
		return 123L;
	}
	
}