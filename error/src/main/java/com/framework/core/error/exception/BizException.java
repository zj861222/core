package com.framework.core.error.exception;

import com.framework.core.error.exception.code.impl.BaseCode;
import com.framework.core.error.exception.internel.ErrorCodeLoader;

/**
 * 业务通用异常
 * 
 * @author zhangjun
 */
public class BizException extends Exception {

	private static final long serialVersionUID = -7924878864085184620L;

	/**
	 * 错误码
	 */
	private int errorCode;

	/**
	 * 抛出的异常,非预期异常，错误码为 BaseCode.EX_SYSTEM_UNKNOW
	 * 
	 * @param cause
	 *            错误原因
	 * @param message
	 *            错误信息
	 */
	public BizException(Throwable cause, String message) {
		super(message, cause);
		this.errorCode = BaseCode.EX_SYSTEM_UNKNOW.getCode();
	}

	/**
	 * 抛出的异常
	 * 
	 * @param cause
	 *            源异常
	 * 
	 * @throws BizException
	 */
	public BizException(Throwable cause) {

		super(ErrorCodeLoader.getErrorMessageByCode(BaseCode.EX_SYSTEM_UNKNOW.getCode()), cause);

		this.errorCode = BaseCode.EX_SYSTEM_UNKNOW.getCode();

	}

	/**
	 * 抛出的异常
	 * 
	 * @param errorCode
	 *            异常码
	 * @param message
	 *            message
	 * @param cause
	 *            源异常
	 */
	public BizException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * 抛出的异常非预期异常
	 * 
	 * @param message
	 *            message
	 * @param cause
	 *            源异常
	 * 
	 */
	public BizException(String message, Throwable cause) {
		super(message, cause);

		this.errorCode = BaseCode.EX_SYSTEM_UNKNOW.getCode();
	}

	/**
	 * 抛出的异常
	 * 
	 * @param message
	 *            message
	 * @param cause
	 *            源异常
	 */
	public BizException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;

	}
	
	
	

	/**
	 * 抛出的异常
	 * 
	 * @param errorCode errorCode
	 * @throws BizException 
	 */
	public BizException(int errorCode)  {
		super(ErrorCodeLoader.getErrorMessageByCode(BaseCode.EX_SYSTEM_UNKNOW.getCode()));
		this.errorCode = errorCode;

	}
	
	
	

    /**
     * 
     * @param errorCode
     * @param cause
     */
	public BizException(int errorCode, Throwable cause)  {
		super(ErrorCodeLoader.getErrorMessageByCode(BaseCode.EX_SYSTEM_UNKNOW.getCode()),cause);
		this.errorCode = errorCode;

	}	
	
	

	/**
	 * 获取异常码
	 * 
	 * @return String
	 */
	public int getErrorCode() {
		return errorCode;
	}

}