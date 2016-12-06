//package com.framework.core.common.exception;
//
//
//import static java.text.MessageFormat.format;
//
//import java.text.MessageFormat;
//
//
//
///**
// * 业务通用异常
// * 
// * @author zhangjun
// */
//public class BizException extends Exception {
//
//    private static final long serialVersionUID = -7924878864085184620L;
//
//    /**
//     * 错误码
//     */
//    private  int  errorCode;
//
//    /**
//     * 抛出的异常
//     * 
//     * @param errorCode
//     * 错误码
//     * @param errFormat
//     * 错误描述模板，模板规则与{@link MessageFormat#format(String, Object...)}一致
//     * @param args
//     * 错误描述参数
//     */
//    public BizException(int errorCode, String errFormat, Object... args){
//        super(errorCode + "|" + format(errFormat, args));
//        this.errorCode = errorCode;
//
//    }
//
//    /**
//     * 抛出的异常
//     * 
//     * @param cause 错误原因
//     * @param errorCode 错误码
//     * @param errFormat 错误描述模板，模板规则与{@link MessageFormat#format(String, Object...)}一致
//     * @param args 错误描述参数
//     */
//    public BizException(Throwable cause, int errorCode, String errFormat, Object... args){
//        super(errorCode + "|" + format(errFormat, args), cause);
//        this.errorCode = errorCode;
//    }
//
//    /**
//     * 抛出的异常
//     * 
//     * @param cause 错误原因
//     * @param message 错误信息
//     */
//    public BizException(Throwable cause, String message){
//        super(message,cause);
//        this.errorCode = BizCodeEnum.EX_SYSTEM_UNKNOW.getCode();
//    }
//    
//
//
//    /**
//     * 抛出的异常
//     * 
//     * @param cause 源异常
//     */
//    public BizException(Throwable cause){
//        
//        super(cause);
//        
//        this.errorCode = BizCodeEnum.EX_SYSTEM_UNKNOW.getCode();
//    }
//
//    /**
//     * 抛出的异常
//     * 
//     * @param cause 源异常
//     */
//    public BizException(BizException cause){
//        super(cause.getMessage(), cause.getCause());
//        BizException ex = (BizException) cause;
//        this.errorCode = ex.errorCode;
//    }
//    
//    /**
//     * 抛出的异常
//     * @param errorCode 异常码
//     * @param message message
//     * @param cause 源异常
//     */
//    public BizException(int errorCode,String message,Throwable cause)
//    {
//        super(message, cause);
//        this.errorCode = errorCode;
//    }
//
//    
//    /**
//     * 抛出的异常
//     * @param message message
//     * @param cause 源异常
//     */
//    public BizException(String message)
//    {
//        super(message);
//        this.errorCode = BizCodeEnum.EX_SYSTEM_UNKNOW.getCode();
//        
//    }
//    
//    /**
//     * 抛出的异常
//     * @param message message
//     * @param cause 源异常
//     */
//    public BizException(String message,Throwable cause)
//    {
//        super(message,cause);
//        this.errorCode = BizCodeEnum.EX_SYSTEM_UNKNOW.getCode();
//    }
//    
//    
//    /**
//     * 抛出的异常
//     * @param message message
//     * @param cause 源异常
//     */
//    public BizException(int errorCode,String message)
//    {
//        super(message);
//        this.errorCode = errorCode;
//        
//    }
//    
//    
//    
//    
//    /**
//     * 获取异常码
//     * @return String
//     */
//    public int getErrorCode() {
//        return errorCode;
//    }
//    
//    
//    
//    public BizException(BizCodeEnum ex)
//    {
//        super(ex.getDesc());
//        this.errorCode = ex.getCode();
//    }
//    
//    
//    
//    /**
//     * 抛出的异常
//     * @param errorCode 异常码
//     * @param message message
//     * @param cause 源异常
//     */
//    public BizException(BizCodeEnum pee,Throwable cause)
//    {
//        super(pee.getDesc(), cause);
//        this.errorCode = pee.getCode();
//    }
//
//    
//}