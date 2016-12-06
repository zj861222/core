package com.framework.core.test.token;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.core.error.exception.BizException;
import com.framework.core.web.session.filter.SessionFilter;

public class TestSessionFilter extends SessionFilter {

	@Override
	protected void redirctToLoginPage(HttpServletRequest request, HttpServletResponse response, BizException e)
			throws IOException {

		String url = response.encodeRedirectURL(request.getRequestURL().toString()) ;
		
		response.sendRedirect("https://cloud.waiqin365.com?returnUrl="+url);

	}
	
}