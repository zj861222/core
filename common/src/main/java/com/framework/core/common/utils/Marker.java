package com.framework.core.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * 
 */
public class Marker {


    private final static String MARKER = "*****";


    /**
     * 模糊电话号码。 少于等于4个的不处理
     *  123456 --> 12*****56
     *  1234-->1234
     * @param number 电话号码
     * @return 模糊后的号码
     */
    public static String maskNumber(String number) {
        if(StringUtils.isEmpty(number)){
            return number;
        }

        //小于等于4个字符，不需要模糊
        if(number.length() <= 4){
            return number;
        }

        return number.substring(0,2) + MARKER + number.substring(number.length()-2, number.length());

    }


    /**
     * 模糊电话号码。 少于等于4个的不处理
     *  123456 --> 12*****56
     *  1234-->1234
     * @param address 电话号码
     * @return 模糊后的号码
     */
    public static String maskAddress(String address) {

        return maskNumber(address);
    }



}
