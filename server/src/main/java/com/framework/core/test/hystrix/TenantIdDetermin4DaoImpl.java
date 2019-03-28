package com.framework.core.test.hystrix;


import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.framework.core.web.common.biz.TenantIdDetermin4Dao;

@Component
public class TenantIdDetermin4DaoImpl implements TenantIdDetermin4Dao {


	
	
	@Override
	@Transactional
	public Long getCurTenantId() {
		

		return 1000L;
	}
	
}