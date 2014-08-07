package com.view.asim.utils;

import android.content.Context;
import android.util.TypedValue;

public class ConvertToUtils {

	private static final String EMPTY_STRING = "";

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String toString(String str) {
		if (IsUtils.isNullOrEmpty(str)) {
			return EMPTY_STRING;
		} else {
			return str;
		}
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static String toString(Object o) {
		if (IsUtils.isNullOrEmpty(o)) {
			return EMPTY_STRING;
		} else {
			return o.toString();
		}
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºint
	 * 
	 * @param str
	 * @return
	 */
	public static int toInt(String str) {
		return toInt(str, 0);
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºint
	 * 
	 * @param str
	 * @param def é»?è®¤å??
	 * @return
	 */
	public static int toInt(String str, int def) {
		if (IsUtils.isNullOrEmpty(str)) {
			return def;
		}
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºboolean
	 * 
	 * @param str
	 * @return
	 */
	public static boolean toBoolean(String str) {
		return toBoolean(str, false);
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºboolean
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static boolean toBoolean(String str, boolean def) {
		if (IsUtils.isNullOrEmpty(str)) {
			return def;
		}
		if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
			return false;
		} else if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
			return true;
		} else {
			return def;
		}
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºfloat
	 * 
	 * @param str
	 * @return
	 */
	public static float toFloat(String str) {
		return toFloat(str, 0F);
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºfloat
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static float toFloat(String str, float def) {
		if (IsUtils.isNullOrEmpty(str)) {
			return def;
		}
		try {
			return Float.parseFloat(str);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºlong
	 * 
	 * @param str
	 * @return
	 */
	public static long toLong(String str) {
		return toLong(str, 0L);
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºlong
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static long toLong(String str, long def) {
		if (IsUtils.isNullOrEmpty(str)) {
			return def;
		}
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºshort
	 * 
	 * @param str
	 * @return
	 */
	public static short toShort(String str) {
		return toShort(str, (short) 0);
	}

	/**
	 * è½???¢å??ç¬?ä¸²ä¸ºshort
	 * 
	 * @param str
	 * @param def
	 * @return
	 */
	public static short toShort(String str, short def) {
		if (IsUtils.isNullOrEmpty(str)) {
			return def;
		}
		try {
			return Short.parseShort(str);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * px = dp * (dpi / 160)
	 * 
	 * @param ctx
	 * @param dip
	 * @return
	 */
	public static int dipToPX(final Context ctx, float dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ctx.getResources().getDisplayMetrics());
	}
}
