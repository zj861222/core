//package com.framework.core.common.exception;
//
///**
// * 返回码枚举类
// * 
// * @author zhangjun
// *
// */
//public enum BizCodeEnum
//{
//	//正常请求状态码为 200
//	EX_SYSTEM_NORMAL (200,"请求成功"),    
//    EX_SYSTEM_UNKNOW (1000,"系统异常"),
//    
//    EX_SYSTEM_DB_ERROR (1001,"数据库异常"),    
//    EX_SYS_INFLUX_MAPPING_FAILED (2001,"influxDB返回结果映射错误！"),    
//    
//    
//    
//    //redis 
//    EX_SYS_REDIS_SET_EXPIRE_FAIL (3001,"Redis set expire 操作失败"), 
//    EX_SYS_REDIS_SET_FAIL (3002,"Redis set 操作失败"),     
//    EX_SYS_REDIS_GET_FAIL (3003,"Redis get 操作失败"),     
//    EX_SYS_REDIS_DELETE_FAIL (3004,"Redis delete 操作失败"),     
//    EX_SYS_REDIS_LOCK_FAIL (3005,"Redis 加锁失败"), 
//
//    
//    //token
//    EX_LOGIN_GENERATE_TOKEN_FAILED (11001,"登录生成token失败"),    
//    EX_LOGIN_PARSE_TOKEN_FAILED (11002,"解析token失败"),    
//    EX_LOGIN_IEAGLLE_REFRESH_TOKEN (11003,"非法的refresh token参数"),    
//    EX_LOGIN_REFRESH_TOKEN_EXPIRE (11004,"refresh token过期"),    
//    EX_LOGIN_TOKEN_EXPIRE (11005,"token过期"),    
//    EX_LOGIN_REFRESH_TOKEN_FAILED (11006,"刷新token失败"),    
//    EX_LOGIN_IEAGLLE_TOKEN (11007,"无效的token信息"),
//    EX_LOGIN_IEAGLLE_TOKEN_EXPIRE_TIME (11008,"token超时时间设置不合法"),
//    EX_LOGIN_IEAGLLE_TOKEN_4_SESSION_EXPIRE (11009,"session已失效"),
//    
//
//
//    
//    //zookeeper
//    EX_ZK_UNEXPECTED_EX (12000,"zookeeper 非预期异常"),
//    EX_ZK_NOT_START (12001,"zookeeper 未启动或者连接丢失"),    
//    EX_ZK_CREATE_NODE_FAIL (12002,"zookeeper 创建节点失败"),    
//    EX_ZK_NODE_FAIL_NODE_EXIST (12003,"zookeeper 节点已存在"),    
//    EX_ZK_NODE_FAIL_NOT_EXIST (12004,"zookeeper 节点不存在"),
//    EX_ZK_GET_NODE_DATA_FAIL (12005,"zookeeper 获取节点数据失败"),
//    EX_ZK_UPDATE_NODE_DATA_FAIL (12006,"zookeeper 修改节点数据失败"),
//    EX_ZK_DELETE_NODE_DATA_FAIL (12007,"zookeeper 删除节点失败"),
//
//
//
//
//
//
//
//    
//    ;
//
//    /**
//     * 异常码
//     */
//    private int code;
//    
//    /**
//     * 异常描述
//     */
//    private String desc;
//    
//    private BizCodeEnum(int code, String desc){
//        this.code = code;
//        this.desc = desc;
//    }
//
//    
//    public int getCode() {
//    
//        return code;
//    }
//
//    
//    public void setCode(int code) {
//    
//        this.code = code;
//    }
//
//    
//    public String getDesc() {
//    
//        return desc;
//    }
//
//    
//    public void setDesc(String desc) {
//    
//        this.desc = desc;
//    }
//    
//
//    
//}