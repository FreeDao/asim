/**   
 * 锟斤拷一锟戒话锟斤拷锟斤拷锟斤拷锟侥硷拷锟斤拷什么.
 * @title DateUtil.java
 * @package com.sinsoft.android.util
 * @author shimiso  
 * @update 2012-6-26 锟斤拷锟斤拷9:57:56  
 */
package com.view.asim.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.view.asim.R;
import com.view.asim.comm.ApplicationContext;

import android.app.Application;

/**
 * 锟斤拷锟节诧拷锟斤拷锟斤拷锟斤拷锟斤拷.
 * 
 * @author shimiso
 */

public class DateUtil {

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss SSS";

	public static Date str2Date(String str) {
		return str2Date(str, null);
	}

	public static Date str2Date(String str, String format) {
		if (str == null || str.length() == 0) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;

	}

	public static Calendar str2Calendar(String str) {
		return str2Calendar(str, null);

	}

	public static Calendar str2Calendar(String str, String format) {

		Date date = str2Date(str, format);
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c;

	}

	public static String date2Str(Calendar c) {// yyyy-MM-dd HH:mm:ss
		return date2Str(c, null);
	}

	public static String date2Str(Calendar c, String format) {
		if (c == null) {
			return null;
		}
		return date2Str(c.getTime(), format);
	}

	public static String date2Str(Date d) {// yyyy-MM-dd HH:mm:ss
		return date2Str(d, null);
	}

	public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
		if (d == null) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String s = sdf.format(d);
		return s;
	}

	public static String getCurDateStr() {
		Calendar c = Calendar.getInstance();
		return date2Str(c);
	}
	
	public static long getCurDateLong() {
		Calendar c = Calendar.getInstance();
		return c.getTimeInMillis();
	}

	/**
	 * 锟斤拷玫锟角帮拷锟斤拷诘锟斤拷址锟斤拷锟斤拷锟绞�
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurDateStr(String format) {
		Calendar c = Calendar.getInstance();
		return date2Str(c, format);
	}

	// 锟斤拷式锟斤拷锟斤拷
	public static String getMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(time);

	}

	// 锟斤拷式锟斤拷锟斤拷
	public static String getDay(long time) {

		return new SimpleDateFormat("yyyy-MM-dd").format(time);

	}

	// 锟斤拷式锟斤拷锟斤拷锟斤拷
	public static String getSMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(time);

	}
	
	public static String getMDHM(long time) {
		String format = ApplicationContext.get().getResources().getString(R.string.date_format);
		return new SimpleDateFormat(format).format(time);

	}
}
