package com.view.asim.util;

import java.util.regex.Pattern;

public class IsUtils {

	private static final String PATTERN_ALPHABETIC_OR_NUMBERIC = "[A-Za-z0-9]*";
	private static final String PATTERN_NUMBERIC = "\\d*\\.{0,1}\\d*";

	/**
	 * å­?ç¬?ä¸²æ???????±å????¢æ????°å??ç»????
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isAlphabeticOrNumberic(String str) {
		return Pattern.compile(PATTERN_ALPHABETIC_OR_NUMBERIC).matcher(str).matches();
	}

	/**
	 * å­?ç¬?ä¸²æ??????????°ç??
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		return Pattern.compile(PATTERN_NUMBERIC).matcher(str).matches();
	}

	/**
	 * ??¤æ??å­?ç¬?ä¸²æ?????ä¸ºç©º
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String str) {
		return (str == null || str.length() == 0);
	}

	/**
	 * ??¤æ??å¯¹è±¡??????ä¸ºç©º
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(final Object str) {
		return (str == null || str.toString().length() == 0);
	}

	/**
	 * ??¤æ??ä¸?ç»?å­?ç¬?ä¸²æ????????ä¸?ä¸?ä¸ºç©º
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
	 * ??¤æ??å­?å­?ç¬?ä¸²æ??????????ºç?°å?¨æ??å®?å­?ç¬?ä¸²ä¸­
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
	 * æ¯?è¾?ä¸¤ä¸ªå­?ç¬?ä¸²æ????????
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
