package com.view.asim.util;

import java.util.regex.Pattern;

public class IsUtils {

	private static final String PATTERN_ALPHABETIC_OR_NUMBERIC = "[A-Za-z0-9]*";
	private static final String PATTERN_NUMBERIC = "\\d*\\.{0,1}\\d*";

	/**
	 * �?�?串�???????��????��????��??�????
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isAlphabeticOrNumberic(String str) {
		return Pattern.compile(PATTERN_ALPHABETIC_OR_NUMBERIC).matcher(str).matches();
	}

	/**
	 * �?�?串�??????????��??
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		return Pattern.compile(PATTERN_NUMBERIC).matcher(str).matches();
	}

	/**
	 * ??��??�?�?串�?????为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String str) {
		return (str == null || str.length() == 0);
	}

	/**
	 * ??��??对象??????为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(final Object str) {
		return (str == null || str.toString().length() == 0);
	}

	/**
	 * ??��??�?�?�?�?串�????????�?�?为空
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean isNullOrEmpty(final String... strs) {
		if (strs == null || strs.length == 0) {
			return true;
		}
		for (String str : strs) {
			if (str == null || str.length() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ??��??�?�?�?串�??????????��?��?��??�?�?�?串中
	 * 
	 * @param str
	 * @param c
	 * @return
	 */
	public static boolean find(String str, String c) {
		if (isNullOrEmpty(str)) {
			return false;
		}
		return str.indexOf(c) > -1;
	}

	public static boolean findIgnoreCase(String str, String c) {
		if (isNullOrEmpty(str)) {
			return false;
		}
		return str.toLowerCase().indexOf(c.toLowerCase()) > -1;
	}
	
	/**
	 * �?�?两个�?�?串�????????
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean equals(String str1, String str2) {
		if (str1 == str2)
			return true;

		if (str1 == null)
			str1 = "";
		return str1.equals(str2);
	}
}
