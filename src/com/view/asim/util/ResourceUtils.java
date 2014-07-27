package com.view.asim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.yixia.camera.util.StringUtils;

public class ResourceUtils {
	private final static String TAG = "ResourceUtils";

	/** ä»?assets ???ä»¶å¤¹ä¸???·å?????ä»¶å¹¶è¯»å????°æ?? */
	public static String getTextFromAssets(final Context context, String fileName) {
		String result = "";
		try {
			InputStream in = context.getResources().getAssets().open(fileName);
			// ??·å?????ä»¶ç??å­???????
			int lenght = in.available();
			// ???å»?byte??°ç??
			byte[] buffer = new byte[lenght];
			// å°????ä»¶ä¸­?????°æ??è¯»å??byte??°ç??ä¸?
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");
			in.close();
		} catch (Exception e) {
			Log.e(TAG, "Assert:" + fileName);
			e.printStackTrace();
		}
		return result;
	}

	/** ??·è??èµ?æº????sdcard */
	public static boolean copyToSdcard(final Context ctx, String fileName, String target) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = ctx.getAssets().open(fileName);
			out = new FileOutputStream(target);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {

			}
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {

			}
		}
		return true;
	}

	public static Drawable loadImageFromAsserts(final Context ctx, String fileName) {
		try {
			InputStream is = ctx.getResources().getAssets().open(fileName);
			return Drawable.createFromStream(is, null);
		} catch (IOException e) {
			if (e != null) {
				Log.e(TAG, "Assert:" + fileName);
				e.printStackTrace();
			}
		} catch (OutOfMemoryError e) {
			if (e != null) {
				Log.e(TAG, "Assert:" + fileName);
				e.printStackTrace();
			}
		} catch (Exception e) {
			if (e != null) {
				Log.e(TAG, "Assert:" + fileName);
				e.printStackTrace();
			}
		}
		return null;
	}

	/** ä»?Assetä»????è½½å?¾ç?? */
	public static void loadImageFromAsserts(final Context ctx, ImageView view, String fileName) {
		try {
			if (ctx != null && !StringUtils.isEmpty(fileName)) {
				InputStream is = ctx.getResources().getAssets().open(fileName);
				view.setImageDrawable(Drawable.createFromStream(is, null));
			}
		} catch (IOException e) {
			if (e != null) {
				Log.e(TAG, "Assert:" + fileName);
				e.printStackTrace();
			}
		} catch (OutOfMemoryError e) {
			if (e != null) {
				Log.e(TAG, "Assert:" + fileName);
				e.printStackTrace();
			}
		} catch (Exception e) {
			if (e != null) {
				Log.e(TAG, "Assert:" + fileName);
				e.printStackTrace();
			}
		}
	}

	/** ??·è????°æ??åº? */
	public static void copyDatabase(final Context ctx, String dbName) {
		if (ctx != null) {
			File f = ctx.getDatabasePath(dbName);
			if (!f.exists()) {

				// æ£?æµ?databases???ä»¶å¤¹??????å·²å??å»?
				if (!f.getParentFile().exists())
					f.getParentFile().mkdir();

				try {
					InputStream in = ctx.getAssets().open(dbName);
					OutputStream out = new FileOutputStream(f.getAbsolutePath());

					byte[] buffer = new byte[1024];
					int length;
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					in.close();
					out.close();
					Log.i(TAG, "Database copy successed! " + f.getPath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
