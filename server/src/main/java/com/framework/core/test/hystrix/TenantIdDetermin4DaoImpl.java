package com.framework.core.test.hystrix;


import org.springframework.stereotype.Component;

import com.framework.core.web.common.biz.TenantIdDetermin4Dao;

@Component
public class TenantIdDetermin4DaoImpl implements TenantIdDetermin4Dao {

	@Override
	public Long getCurTenantId() {
		return 1000L;
	}
	
}