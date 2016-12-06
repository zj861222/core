package com.framework.core.common.view;



import java.io.Serializable;

/**
 * 
 * @author zhangjun
 *
 */
public class JwtInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7336077510294984135L;
	
	
	/**
	 * 生成的jwtStr
	 */
	private String jwtStr;
	
	/**
	 * token创建时间
	 */
	private long createTimeMills;
	
	/**
	 * 超时时间
	 */
	private long expireTimeMills;

	public String getJwtStr() {
		return jwtStr;
	}

	public void setJwtStr(String jwtStr) {
		this.jwtStr = jwtStr;
	}



	public long getCreateTimeMills() {
		return createTimeMills;
	}

	public void setCreateTimeMills(long createTimeMills) {
		this.createTimeMills = createTimeMills;
	}

	public long getExpireTimeMills() {
		return expireTimeMills;
	}

	public void setExpireTimeMills(long expireTimeMills) {
		this.expireTimeMills = expireTimeMills;
	}
	

}