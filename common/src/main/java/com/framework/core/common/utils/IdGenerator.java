package com.framework.core.common.utils;

import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class IdGenerator {

	
    private static char[] BASE64 = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789".toCharArray();

    public static String generateUUID1() {
        UUID uuid = UUID.randomUUID();
        char[] chs = new char[22];
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        int high = (int)((most >> 13) ^ (least >> 31)) & 0x3c;
        int k = chs.length - 1;
        for(int i = 0; i < 10; i++, least >>>= 6) {
            chs[k--] = BASE64[(int)(least & 0x3f)];
        }
        chs[k--] = BASE64[(int)((least & 0x3f) | (most & 0x30))];
        most >>>= 2;
        for(int i = 0; i < 10; i++, most >>>= 6) {
            chs[k--] = BASE64[(int)(most & 0x3f)];
        }
        chs[k--] = BASE64[(int)(high | most)];
        return new String(chs);
    }	
    
    
	private final static char[] DIGITS64 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();
	
	public static String generateUUID2() {
		UUID u = UUID.randomUUID();
//		return u.toString();
		return toIDString(u.getMostSignificantBits()) + toIDString(u.getLeastSignificantBits());
	}
 
	private static String toIDString(long l) {
		char[] buf = "00000000000".toCharArray(); // 限定11位长度
		int length = 11;
		long least = 63L; // 0x0000003FL
		do {
			buf[--length] = DIGITS64[(int) (l & least)]; // l & least取低6位
			/* 无符号的移位只有右移，没有左移
			 * 使用“>>>”进行移位
			 * 为什么没有无符号的左移呢，知道原理的说一下哈
			 */
			l >>>= 6;
		} while (l != 0);
		return new String(buf);
	}

    
	/**
	 * 获取Long型的UUID（唯一）.
	 * 
	 * @return the UUID least bits
	 */
	public static long getUUID2Long()
	{
		return java.util.UUID.randomUUID().getLeastSignificantBits() * -1;
	}
	
	
	/**
	 * 获取Long型的UUID（唯一）.
	 * 
	 * @return the UUID least bits
	 */
	public static long getUUID2Long2()
	{
		return java.util.UUID.randomUUID().getMostSignificantBits() * -1;
	}
    
    
    public static void main(String[] args) {
    	
    	long start1 = System.currentTimeMillis();
    	for(int i = 0 ; i < 1000000;i++) {
    		
    		generateUUID1();
//    		System.out.println(generateUUID1());
    		
    	}
    	
    	long cost1 = System.currentTimeMillis()-start1;
    	
    	
    	
    	long start2 = System.currentTimeMillis();
    	System.out.println("start2:"+start2);
    	
    	for(int i = 0 ; i < 1000000;i++) {
    		
    		generateUUID2();
//    		System.out.println(generateUUID2());
    		
    	}
    	
    	
    	
    	for(int i = 0;i<100;i++){
    		System.out.println(getUUID2Long());
    	}
    	
    	
    	long cost2 = System.currentTimeMillis()-start2;

    	
    	System.out.println("cost:"+cost1);
    	
    	System.out.println("cost2:"+cost2);

    	
    	
    }
	
	
//	
//    /** 
//     * 
//     *把UUID 转为 22位长字符串 
//     */  
//    public String shorter(String s) {  
//    	
//    	Base64.
//        char[] res = Base64.encodeBase64(asBytes(s));  
//        return new String(res,0,res.length-2);  
//    }  
//  
//   /** 
//     * 
//     *把22位长字符串转为 UUID 
//     */  
//    public String recover(String s) {  
//        int len = s.length();  
//        char[] chars = new char[len+2];  
//        chars[len]=chars[len+1]='_';  
//        for(int i=0;i<len;i++){  
//            chars[i]=s.charAt(i);  
//        }  
//        return toUUID(Base64.decode(chars)).toString();  
//    }  
//    public static byte[] asBytes(String id) {  
//        UUID uuid=UUID.fromString(id);  
//        long msb = uuid.getMostSignificantBits();  
//        long lsb = uuid.getLeastSignificantBits();  
//        byte[] buffer = new byte[16];  
//  
//        for (int i = 0; i < 8; i++) {  
//                buffer[i] = (byte) (msb >>> 8 * (7 - i));  
//        }  
//        for (int i = 8; i < 16; i++) {  
//                buffer[i] = (byte) (lsb >>> 8 * (7 - i));  
//        }  
//        return buffer;  
//  
//    }  
//  
//    public static UUID toUUID(byte[] byteArray) {  
//        long msb = 0;  
//        long lsb = 0;  
//        for (int i = 0; i < 8; i++)  
//                msb = (msb << 8) | (byteArray[i] & 0xff);  
//        for (int i = 8; i < 16; i++)  
//                lsb = (lsb << 8) | (byteArray[i] & 0xff);  
//        UUID result = new UUID(msb, lsb);  
//  
//        return result;  
//    }  

//	14+19
//	2018 1212 11:22:30 xxxxxx
}