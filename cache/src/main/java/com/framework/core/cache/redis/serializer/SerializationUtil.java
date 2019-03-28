package com.framework.core.cache.redis.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.framework.core.cache.redis.exception.RedisErrorCode;
import com.framework.core.error.exception.BizException;

/**
 * 序列化工具
 * 
 * @author zhangjun
 * @version
 */
public class SerializationUtil {

	private final static Logger logger = LoggerFactory.getLogger(SerializationUtil.class);

	// 序列化
	public static byte[] serialize(Object obj)  {
		ObjectOutputStream obi = null;
		ByteArrayOutputStream bai = null;
		try {
			bai = new ByteArrayOutputStream();
			obi = new ObjectOutputStream(bai);
			obi.writeObject(obj);
			byte[] byt = bai.toByteArray();
			return byt;
		} catch (Exception e) {
			
			logger.error("redis SerializationUtil serialize failed,message is {}",e.getMessage(),e);
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_SERIAL_FAIL.getCode(),e);
		}
	}

	// 反序列化
	public static Object deserizlize(byte[] byt)  {

		if (byt == null || byt.length == 0) {
			return null;
		}

		ObjectInputStream oii = null;
		ByteArrayInputStream bis = null;
		bis = new ByteArrayInputStream(byt);
		try {
			oii = new ObjectInputStream(bis);
			Object obj = oii.readObject();
			return obj;
		} catch (Exception e) {
			logger.error("redis SerializationUtil deserialize failed,message is {}",e.getMessage(),e);
			throw new BizException(RedisErrorCode.EX_SYS_REDIS_DESERIAL_FAIL.getCode(),e);
		}

	}

}
