package com.framework.core.common.utils;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.framework.core.common.utils.jwt.CustomJwtParser;
import com.framework.core.common.view.JwtInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtBuilderUtils {



	public static final String DEFAULT_JWT_SECRET = "7786df7fc3a34e26a61c034d5ec123as";

	/**
	 * jwt token超时时间,1天
	 */
	public static final int JWT_TTL = 24 * 60 * 60 * 1000; // millisecond
	/**
	 * jwt refresh token 超时时间，7天
	 */
	public static final int JWT_REFRESH_TTL = 7 * 24 * 60 * 60 * 1000; // millisecond

	private String tokenSecretKey;

	/**
	 * 由字符串生成加密key
	 * 
	 * @return
	 */
	public SecretKey generalKey() {

		if (StringUtils.isEmpty(tokenSecretKey)) {
			tokenSecretKey = DEFAULT_JWT_SECRET;
		}

		byte[] encodedKey = Base64.decodeBase64(tokenSecretKey);
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

	/**
	 * 创建jwt
	 * 
	 * @param id
	 *            token的标志信息，比如 auth_jwt_id,
	 * @param iss The issuer of the token，token 是给谁的          
	 *            
	 * @param subject
	 *            关键信息，可以是一个对象的json格式
	 * @param ttlMillis
	 *            超时时间
	 * @return
	 * @throws Exception
	 */
	public JwtInfo createJWT(String id, String iss,String subject, long ttlMillis) throws Exception {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).signWith(signatureAlgorithm,
				key).setIssuer(iss);
		
		JwtInfo info = new JwtInfo();
		
		info.setCreateTimeMills(nowMillis);
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
			
			info.setExpireTimeMills(expMillis);
		}
		
		info.setJwtStr(builder.compact());
	
		return info;
	}

	/**
	 * 解密jwt
	 * 
	 * @param jwtToken
	 *            jwtToken
	 * @return
	 * @throws Exception
	 */
	public Claims parseJWT(String jwtToken) throws Exception {
		SecretKey key = generalKey();
		
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken).getBody();
		return claims;
	}
	
	/**
	 *  解密jwt，即使过期，也不抛出异常
	 * @param jwtToken
	 * @return boolean里true表示过期；false表示没过期
	 * @throws Exception
	 */
	public Pair<Boolean,Claims> parseJWTEx(String jwtToken) throws Exception {
		SecretKey key = generalKey();
		
		Claims claims = new CustomJwtParser().setSigningKey(key).parseClaimsJws(jwtToken).getBody();
		
		if(claims == null) {
			throw new IllegalArgumentException("illegal argument token:"+jwtToken);
		}
		
        Date exp = claims.getExpiration();
        
        if (exp != null) {
        	Date now = new Date();
            if (now.equals(exp) || now.after(exp)) {
     
            	//表示过期
            	return Pair.of(true, claims);
            }
        }
        
        return  Pair.of(false, claims);
		
	}
		
	


	public String getTokenSecretKey() {
		return tokenSecretKey;
	}

	public void setTokenSecretKey(String tokenSecretKey) {
		this.tokenSecretKey = tokenSecretKey;
	}

	/**
	 * 判断时间是否过期
	 * 
	 * @param jwtToken
	 * @return
	 * @throws Exception
	 */
	public boolean checkIsExpire(String jwtToken) throws Exception {

		Claims claims = parseJWT(jwtToken);

		Date expireDate = claims.getExpiration();

		Date now = new Date();

		return now.after(expireDate);
	}
}
