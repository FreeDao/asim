package com.view.asim.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import android.util.Log;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Javaºº×Ö×ª»»ÎªÆ´Òô
 * 
 */
public class CharacterParser {
	public static String[] pinyin;
	
	public static String getPingYin(String src) {

		char[] t1 = null;		
		t1 = src.toCharArray();
		String[] t2;
		HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
		t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		t3.setVCharType(HanyuPinyinVCharType.WITH_V);
		String t4 = "";
		int t0 = t1.length;
		try {
			for (int i = 0; i < t0; i++) {
				// ÅÐ¶ÏÊÇ·ñÎªºº×Ö×Ö·û				
				if (java.lang.Character.toString(t1[i]).matches(
						"[\\u4E00-\\u9FA5]+")) {
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
					t4 += t2[0];
				} else
					t4 += java.lang.Character.toString(t1[i]);
			}
			return t4;
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
		return t4;
	}

	// ·µ»ØÖÐÎÄµÄÊ××ÖÄ¸
	public static String getPinYinHeadChar(String str) {
		String convert = "";
		for (int j = 0; j < str.length(); j++) {
			char word = str.charAt(j);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
			if (pinyinArray != null) {
				convert += pinyinArray[0].charAt(0);
			} else {
				convert += word;
			}
		}
		return convert;
	}

	public static String getnamecode(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == 97)
				sb.append("2");
			else if (s.charAt(i) == 98)
				sb.append("2");
			else if (s.charAt(i) == 99)
				sb.append("2");
			else if (s.charAt(i) == 100)
				sb.append("3");
			else if (s.charAt(i) == 101)
				sb.append("3");
			else if (s.charAt(i) == 102)
				sb.append("3");
			else if (s.charAt(i) == 103)
				sb.append("4");
			else if (s.charAt(i) == 104)
				sb.append("4");
			else if (s.charAt(i) == 105)
				sb.append("4");
			else if (s.charAt(i) == 106)
				sb.append("5");
			else if (s.charAt(i) == 107)
				sb.append("5");
			else if (s.charAt(i) == 108)
				sb.append("5");
			else if (s.charAt(i) == 109)
				sb.append("6");
			else if (s.charAt(i) == 110)
				sb.append("6");
			else if (s.charAt(i) == 111)
				sb.append("6");
			else if (s.charAt(i) == 112)
				sb.append("7");
			else if (s.charAt(i) == 113)
				sb.append("7");
			else if (s.charAt(i) == 114)
				sb.append("7");
			else if (s.charAt(i) == 115)
				sb.append("7");
			else if (s.charAt(i) == 116)
				sb.append("8");
			else if (s.charAt(i) == 117)
				sb.append("8");
			else if (s.charAt(i) == 118)
				sb.append("8");
			else if (s.charAt(i) == 119)
				sb.append("9");
			else if (s.charAt(i) == 120)
				sb.append("9");
			else if (s.charAt(i) == 121)
				sb.append("9");
			else if (s.charAt(i) == 122)
				sb.append("9");
		}
		return sb.toString();
	}
}
