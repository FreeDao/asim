package com.view.asim.util;

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
 * @author �?�?�?
 * @???件�??�? : FaceConversionUtil.java
 * @???建�?��?? : 2013-1-27 �????02:34:09
 * @???件�??�? : 表�??�????工�??
 ****************************************** 
 */
public class FaceConversionUtil {
	private final static String TAG = "FaceConversionUtil";

	/** �?�?页表??????�???? */
	private int pageSize = 17;

	private static FaceConversionUtil mFaceConversionUtil;

	/** �?�?�????�?�????表�??HashMap */
	private HashMap<String, String> emojiMap = new HashMap<String, String>();

	/** �?�?�????�?�????表�???????? */
	private List<ChatEmoji> emojis = new ArrayList<ChatEmoji>();

	/** 表�?????页�??�?????????? */
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
	 * �???��??�?SpanableString对象�????�?�???��??�?�?�?,并�??�?正�????��??
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public SpannableString getExpressionString(Context context, String str, int size) {
		SpannableString spannableString = new SpannableString(str);
		// 正�??表达�?�????�?�?串�??????????????表�??�?�?�? ???�?[�?�?]???
		String zhengze = "\\[[^\\]]+\\]";
		// ???�?�???��??正�??表达�???��?????�?�?pattern
		Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(context, spannableString, sinaPatten, 0, size);
		} catch (Exception e) {
			Log.e("dealExpression", e.getMessage());
		}
		return spannableString;
	}

	/**
	 * 添�??表�??
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
	 * �?spanableString�?�?正�????��??�?�????�????�?�?�????以表?????��??代�??
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
			// �????�?�?�?�?�????索�?????????????��????�个正�??表达�?,ture ???继续???�?
			if (matcher.start() < start) {
				continue;
			}
			String value = emojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}
			int resId = context.getResources().getIdentifier(value, "drawable",
					context.getPackageName());
			// ???�?�???��?��??�???��??�?�?串�?��???????��??�?�?id
			// Field field=R.drawable.class.getDeclaredField(value);
			// int resId=Integer.parseInt(field.get(null).toString());
			if (resId != 0) {
				Drawable d = context.getResources().getDrawable(resId);
				//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
				//bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
				// ???�???��??�?�?id??��?????bitmap�???��??�?ImageSpan??��??�?
				//ImageSpan imageSpan = new ImageSpan(bitmap);
				d.setBounds(0, 0,  size, size);
				ImageSpan imageSpan = new ImageSpan(d);
				// 计�??该�?��?????�??????�度�?�?就�??�???��?��??�?�?串�????�度
				int end = matcher.start() + key.length();
				// �?该�?��????��?��??�?串中�?�????�?�?�?
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					// �??????�个�?�?串�?????�?�?�?�????继续??????
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
	 * 解�??�?�?
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
	 * ??��?????页�?��??
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
		// �?�?�????�?�????viewpager???载中??��????????�?�?常�?????�?�???��??为�??�?
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