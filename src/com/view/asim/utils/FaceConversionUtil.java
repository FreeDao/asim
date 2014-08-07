package com.view.asim.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.view.asim.model.ChatEmoji;

import com.view.asim.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;

/**
 * 
 ****************************************** 
 * @author ï¿??ï¿??ï¿??
 * @???ä»¶ï¿½??ï¿?? : FaceConversionUtil.java
 * @???å»ºï¿½?ï¿½ï¿½?? : 2013-1-27 ï¿?????02:34:09
 * @???ä»¶ï¿½??ï¿?? : è¡?ï¿???ï¿?????å·¥ï¿½??
 ****************************************** 
 */
public class FaceConversionUtil {
	private final static String TAG = "FaceConversionUtil";

	/** ï¿??ï¿??é¡µè¡¨??????ï¿????? */
	private int pageSize = 17;

	private static FaceConversionUtil mFaceConversionUtil;

	/** ï¿??ï¿??ï¿?????ï¿??ï¿?????è¡?ï¿???HashMap */
	private HashMap<String, String> emojiMap = new HashMap<String, String>();

	/** ï¿??ï¿??ï¿?????ï¿??ï¿?????è¡?ï¿????????? */
	private List<ChatEmoji> emojis = new ArrayList<ChatEmoji>();

	/** è¡?ï¿??????é¡µï¿½??ï¿??????????? */
	public List<List<ChatEmoji>> emojiLists = new ArrayList<List<ChatEmoji>>();

	private FaceConversionUtil() {

	}

	public static FaceConversionUtil getInstace() {
		if (mFaceConversionUtil == null) {
			mFaceConversionUtil = new FaceConversionUtil();
		}
		return mFaceConversionUtil;
	}

	/**
	 * ï¿????ï¿½ï¿½??ï¿??SpanableStringå¯¹è±¡ï¿?????ï¿??ï¿????ï¿½ï¿½??ï¿??ï¿??ï¿??,å¹¶ï¿½??ï¿??æ­£ï¿½????ï¿½ï¿½??
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public SpannableString getExpressionString(Context context, String str, int size) {
		SpannableString spannableString = new SpannableString(str);
		// æ­£ï¿½??è¡¨è¾¾ï¿??ï¿?????ï¿??ï¿??ä¸²ï¿½??????????????è¡?ï¿???ï¿??ï¿??ï¿?? ???ï¿??[ï¿??ï¿??]???
		String zhengze = "\\[[^\\]]+\\]";
		// ???ï¿??ï¿????ï¿½ï¿½??æ­£ï¿½??è¡¨è¾¾ï¿????ï¿½ï¿½?????ï¿??ï¿??pattern
		Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(context, spannableString, sinaPatten, 0, size);
		} catch (Exception e) {
			Log.e("dealExpression", e.getMessage());
		}
		return spannableString;
	}

	/**
	 * æ·»ï¿½??è¡?ï¿???
	 * 
	 * @param context
	 * @param imgId
	 * @param spannableString
	 * @return
	 */
	public SpannableString addFace(Context context, int imgId,
			String spannableString) {
		if (TextUtils.isEmpty(spannableString)) {
			return null;
		}
		
		Drawable d = context.getResources().getDrawable(imgId);
		//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),imgId);
		//bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
		d.setBounds(0, 0, 60, 60);
		ImageSpan imageSpan = new ImageSpan(d);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, spannableString.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * ï¿??spanableStringï¿??ï¿??æ­£ï¿½????ï¿½ï¿½??ï¿??ï¿?????ï¿?????ï¿??ï¿??ï¿?????ä»¥è¡¨?????ï¿½ï¿½??ä»£ï¿½??
	 * 
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws Exception
	 */
	private void dealExpression(Context context,
			SpannableString spannableString, Pattern patten, int start, int size)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			// ï¿?????ï¿??ï¿??ï¿??ï¿??ï¿?????ç´?ï¿??????????????ï¿½ï¿½????ï¿½ä¸ªæ­£ï¿½??è¡¨è¾¾ï¿??,ture ???ç»§ç»­???ï¿??
			if (matcher.start() < start) {
				continue;
			}
			String value = emojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}
			int resId = context.getResources().getIdentifier(value, "drawable",
					context.getPackageName());
			// ???ï¿??ï¿????ï¿½ï¿½?ï¿½ï¿½??ï¿????ï¿½ï¿½??ï¿??ï¿??ä¸²ï¿½?ï¿½ï¿½???????ï¿½ï¿½??ï¿??ï¿??id
			// Field field=R.drawable.class.getDeclaredField(value);
			// int resId=Integer.parseInt(field.get(null).toString());
			if (resId != 0) {
				Drawable d = context.getResources().getDrawable(resId);
				//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
				//bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
				// ???ï¿????ï¿½ï¿½??ï¿??ï¿??id??ï¿½ï¿½?????bitmapï¿????ï¿½ï¿½??ï¿??ImageSpan??ï¿½ï¿½??ï¿??
				//ImageSpan imageSpan = new ImageSpan(bitmap);
				d.setBounds(0, 0,  size, size);
				ImageSpan imageSpan = new ImageSpan(d);
				// è®¡ï¿½??è¯¥ï¿½?ï¿½ï¿½?????ï¿???????ï¿½åº¦ï¿??ï¿??å°±ï¿½??ï¿????ï¿½ï¿½?ï¿½ï¿½??ï¿??ï¿??ä¸²ï¿½????ï¿½åº¦
				int end = matcher.start() + key.length();
				// ï¿??è¯¥ï¿½?ï¿½ï¿½????ï¿½ï¿½?ï¿½ï¿½??ï¿??ä¸²ä¸­ï¿??ï¿?????ï¿??ï¿??ï¿??
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					// ï¿???????ï¿½ä¸ªï¿??ï¿??ä¸²ï¿½?????ï¿??ï¿??ï¿??ï¿?????ç»§ç»­??????
					dealExpression(context, spannableString, patten, end, size);
				}
				break;
			}
		}
	}

	public void getFileText(Context context) {
		ParseData(FileUtil.getEmojiFile(context), context);
	}

	/**
	 * è§£ï¿½??ï¿??ï¿??
	 * 
	 * @param data
	 */
	private void ParseData(List<String> data, Context context) {
		if (data == null) {
			return;
		}
		ChatEmoji emojEentry;
		try {
			for (String str : data) {
				String[] text = str.split(",");
				String fileName = text[0]
						.substring(0, text[0].lastIndexOf("."));
				emojiMap.put(text[1], fileName);
				int resID = context.getResources().getIdentifier(fileName,
						"drawable", context.getPackageName());

				if (resID != 0) {
					emojEentry = new ChatEmoji();
					emojEentry.setId(resID);
					emojEentry.setCharacter(text[1]);
					emojEentry.setFaceName(fileName);
					emojis.add(emojEentry);
				}
			}
			
			int pageCount = (int) Math.ceil(emojis.size() / 17 + 0.1);
			Log.d(TAG, "all emojis: " + emojis.size() + " pages: " + pageCount);

			for (int i = 0; i < pageCount; i++) {
				emojiLists.add(getData(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ??ï¿½ï¿½?????é¡µï¿½?ï¿½ï¿½??
	 * 
	 * @param page
	 * @return
	 */
	private List<ChatEmoji> getData(int page) {
		int startIndex = page * pageSize;
		int endIndex = startIndex + pageSize;

		if (endIndex > emojis.size()) {
			endIndex = emojis.size();
		}
		// ï¿??ï¿??ï¿?????ï¿??ï¿?????viewpager???è½½ä¸­??ï¿½ï¿½????????ï¿??ï¿??å¸¸ï¿½?????ï¿??ï¿????ï¿½ï¿½??ä¸ºï¿½??ï¿??
		List<ChatEmoji> list = new ArrayList<ChatEmoji>();
		Log.d(TAG, "total size " + emojis.size() + ", start " + startIndex + ", end " + endIndex);
		
		list.addAll(emojis.subList(startIndex, endIndex));
		if (list.size() < pageSize) {
			for (int i = list.size(); i < pageSize; i++) {
				ChatEmoji object = new ChatEmoji();
				list.add(object);
			}
		}
		if (list.size() == pageSize) {
			ChatEmoji object = new ChatEmoji();
			object.setId(R.drawable.face_del_icon);
			list.add(object);
		}
		return list;
	}
}