package com.framework.core.cache.redis.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 序列化工具
 * 
 * @author zhangjun
 * @version
 */
public class SerializationUtil {

	// 序列化
	public static byte[] serialize(Object obj) {
		ObjectOutputStream obi = null;
		ByteArrayOutputStream bai = null;
		try {
			bai = new ByteArrayOutputStream();
			obi = new ObjectOutputStream(bai);
			obi.writeObject(obj);
			byte[] byt = bai.toByteArray();
			return byt;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 反序列化
	public static Object deserizlize(byte[] byt) {

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

			e.printStackTrace();
		}

		return null;
	}

}
