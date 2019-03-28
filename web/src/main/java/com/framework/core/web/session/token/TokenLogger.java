package com.framework.core.web.session.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenLogger {
	
	
	private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);
	

	public static void printDebugInfo(String message) {
		
		logger.debug(message);
		
	}
}