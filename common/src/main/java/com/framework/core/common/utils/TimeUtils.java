package com.framework.core.common.utils;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 日期工具类
 * @author bo.sun
 *
 */
public final class TimeUtils {

	private static final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyyMMdd");

	private static final FastDateFormat timeFormat = FastDateFormat.getInstance("yyyyMMddHHmmss");

	public static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

	public static final int DATE_TO_TIME_MULTIPLE = 1000000;

	private TimeUtils() {
	}

	/**
	 * convert int yyyyMMddHHmmss to String yyyy-MM-dd HH:mm:ss
	 * @param time
	 * @return
	 */
	public static String longTimeToString(long time) {
		
		StringBuilder builder = new StringBuilder(19 + 1);
		
		/*
		 * 
		 * +---------------------------------+
		 * |       yyyy-MM-dd HH:mm:ss       |
		 * +---------------------------------+
		 *             ^  ^  ^  ^  ^
		 *             |  |  |  |  |
		 *             4  7  10 13 16
		 * 
		 */
		
		builder.append(time).insert(4, '-').insert(7, '-').insert(10, ' ').insert(13, ':').insert(16, ':');
		
		return builder.toString();
		
	}
	
	/**
	 * convert timeMillis to String yyyyMMddHHmmss
	 * @param time
	 * @return
	 */
	public static String dateTimeToString(Date time) {
		
		return timeFormat.format(time);
		
	}

	/**
	 * convert timeMillis to long yyyyMMddHHmmss
	 * @param time
	 * @return
	 */
	public static long dateTimeToLong(Date time) {
		
		return Long.parseLong(dateTimeToString(time));
		
	}

	/**
	 * convert timeMillis to String yyyyMMddHHmmss
	 * @param timeMillis
	 * @return
	 */
	public static String millisTimeToString(long timeMillis) {
		
		if(timeMillis == 0) {
			return "0";
		}
		
		return dateTimeToString(new Date(timeMillis));
		
	}

	/**
	 * convert timeMillis to int yyyyMMddHHmmss
	 * @param timeMillis
	 * @return
	 */
	public static long millisTimeToLong(long timeMillis) {
		
		if(timeMillis == 0) {
			return 0;
		}
		
		return Long.parseLong(millisTimeToString(timeMillis));
		
	}

	/**
	 * convert int yyyyMMdd to String yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public static String intDateToString(int date) {
		
		StringBuilder builder = new StringBuilder(10 + 1);
		
		/*
		 * 
		 * +------------------------+
		 * |       yyyy-MM-dd       |
		 * +------------------------+
		 *             ^  ^
		 *             |  |
		 *             4  7
		 * 
		 */
		
		builder.append(date).insert(4, '-').insert(7, '-');
		
		return builder.toString();
		
	}

	/**
	 * convert String yyyyMMdd to int yyyyMMdd
	 * or String yyyyMMddHHmmss to int yyyyMMddHHmmss
	 * @param date
	 * @return
	 */
	public static long stringDateTimeToLong(String date) {
		
		long l = 0;
		
		char ch = ' ';
		
		for(int i = 0, len = date.length(); i < len; i++) {
			
			ch = date.charAt(i);
			
			if(ch >= 0x30 && ch <= 0x39) {
				l = l * 10 + ch - 0x30;
			}
			
		}
		
		return l;
		
	}

	/**
	 * convert timeMillis to String yyyyMMdd
	 * @param date
	 * @return
	 */
	public static String dateDateToString(Date date) {
		
		return dateFormat.format(date);
		
	}

	/**
	 * convert timeMillis to int yyyyMMdd
	 * @param date
	 * @return
	 */
	public static int dateDateToInt(Date date) {
		
		return Integer.parseInt(dateDateToString(date));
		
	}

	/**
	 * convert timeMillis to String yyyyMMdd
	 * @param timeMillis
	 * @return
	 */
	public static String millisDateToString(long timeMillis) {
		
		if(timeMillis == 0) {
			return "0";
		}
		
		return dateDateToString(new Date(timeMillis));
		
	}

	/**
	 * convert timeMillis to int yyyyMMdd
	 * @param timeMillis
	 * @return
	 */
	public static int millisDateToInt(long timeMillis) {
		
		if(timeMillis == 0) {
			return 0;
		}
		
		return dateDateToInt(new Date(timeMillis));
		
	}

}
