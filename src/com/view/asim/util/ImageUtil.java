/**   
 * ��һ�仰�������ļ���ʲô.
 * @title ImageUtil.java
 * @package com.viewiot.asim.util
 * @author xuwenan  
 * @update 2014/4/12
 */
package com.view.asim.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

/**
 * ͼƬ����������.
 * 
 * @author  xuweinan
 */

public class ImageUtil {
	/**
	 * ��bitmapת�����ֽ�����
	 * 
	 * @param bitmap����
	 * @return �ֽ���������
	 */
	public static byte[] bitmapToByte(Bitmap b) {
	    if (b == null) {
	        return null;
	    }

	    ByteArrayOutputStream o = new ByteArrayOutputStream();
	    // ѹ�����ƶ���.PNG��ʽ
	    b.compress(Bitmap.CompressFormat.PNG, 100, o);
	    return o.toByteArray();
	}

	/**
	 * ���Լ�����ת����Bitmap
	 * 
	 * @param �ֽ���������
	 * @return bitmap����
	 */
	public static Bitmap byteToBitmap(byte[] b) {
	    return (b == null || b.length == 0) ? null : BitmapFactory
	            .decodeByteArray(b, 0, b.length);
	}

	/**
	 * ��Drawable����ת����Bitmap
	 * 
	 * @param Drawable����
	 * @return bitmap����
	 */
	public static Bitmap drawableToBitmap(Drawable d) {
	    return d == null ? null : ((BitmapDrawable) d).getBitmap();
	}

	/**
	 * �� Bitmap ����ת���� Drawable
	 * 
	 * @param bitmap����
	 * @return Drawable����
	 */
	public static Drawable bitmapToDrawable(Bitmap b) {
	    return b == null ? null : new BitmapDrawable(b);
	}

	/**
	 * �� Drawable����ת���� �ֽ�����
	 * 
	 * @param Drawable����
	 * @return �ֽ�����
	 */
	public static byte[] drawableToByte(Drawable d) {
	    return bitmapToByte(drawableToBitmap(d));
	}

	/**
	 * ���ֽ�����ת���� Drawable
	 * 
	 * @param �ֽ�����
	 * @return Drawable
	 */
	public static Drawable byteToDrawable(byte[] b) {
	    return bitmapToDrawable(byteToBitmap(b));
	}

	/**
	 * ����url��ȡ�����������ز��ر�������
	 * 
	 * @param ����url
	 * @param ��ȡ��ʱ
	 * @return ������
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromUrl(String imageUrl,
	        int readTimeOutMillis) {
	    InputStream stream = null;
	    try {
	        URL url = new URL(imageUrl);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        if (readTimeOutMillis > 0) {
	            con.setReadTimeout(readTimeOutMillis);
	        }
	        stream = con.getInputStream();
	    } catch (MalformedURLException e) {
	        closeInputStream(stream);
	        throw new RuntimeException("MalformedURLException occurred. ", e);
	    } catch (IOException e) {
	        closeInputStream(stream);
	        throw new RuntimeException("IOException occurred. ", e);
	    }
	    return stream;
	}

	/**
	 * ��ȥurl�����ݲ�ת����drawable
	 * 
	 * @param ���ݵ�url
	 * @param ��ȡ��ʱʱ��
	 * @return
	 */
	public static Drawable getDrawableFromUrl(String imageUrl,
	        int readTimeOutMillis) {
	    InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOutMillis);
	    Drawable d = Drawable.createFromStream(stream, "src");
	    closeInputStream(stream);
	    return d;
	}

	/**
	 * ͨ������url��ȡbitmap����
	 * 
	 * @param ͼƬurl
	 * @return bitmap����
	 */
	public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut) {
	    InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOut);
	    Bitmap b = BitmapFactory.decodeStream(stream);
	    closeInputStream(stream);
	    return b;
	}
	
	public static byte[] getBytesFromUrl(String fileUrl, int readTimeOut) {
		 ByteArrayOutputStream outStream;
		 
		try {
		    InputStream inputStream = getInputStreamFromUrl(fileUrl, readTimeOut);
		    
		    outStream = new ByteArrayOutputStream();  
	        byte[] buffer = new byte[1024];  
	        int len = 0;  
	        while((len = inputStream.read(buffer)) != -1){  
	            outStream.write(buffer, 0, len);  
	        }  
		    closeInputStream(inputStream);
		    outStream.close();
		}
		catch (IOException e) {
			return null;
		}
        return outStream.toByteArray();  
	}
	
	 /** 
     * ����ͼƬ���ļ� 
     * @param bm 
     * @param fileName 
     * @throws IOException 
     */  
    public static void saveImage(Bitmap bm, String fileName) {  
    	try {
	        File myCaptureFile = new File(fileName);  
	        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));  
	        bm.compress(Bitmap.CompressFormat.JPEG, 85, bos);  
	        bos.flush();  
	        bos.close();  
    	} catch (IOException e) {
	        throw new RuntimeException("IOException occurred when saving image: " + fileName, e);
    	}
    } 
    
	 /** 
     * �����ļ� (byte[])
     * @param bm 
     * @param fileName 
     * @throws IOException 
     */  
    public static void saveFile(Context cntx, byte[] bytes, String fileName) throws IOException { 
    	FileOutputStream fout = null;
        
    	try {
	    	File file = new File(fileName);
	    	fout = new FileOutputStream(file);
	    	fout.write(bytes);
	    	fout.close(); 
    	} catch (IOException e) {
	        throw new RuntimeException("IOException occurred when saving file: " + fileName, e);
    	}
    } 
    
	/**
	 * ͨ����߳�������ͼƬ
	 * 
	 * @param bitmap����
	 * @param �µĿ��
	 * @param �µĸ߶�
	 * @return ���ź��bitmap
	 */
	public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
	    return scaleImage(org, (float) newWidth / org.getWidth(),
	            (float) newHeight / org.getHeight());
	}

	/**
	 * ͨ����ߵı�������ͼƬ
	 * 
	 * @param bitmap����
	 * @param ��ȵı���
	 * @param �߶ȵı���
	 * @return ���ź��bitmap
	 */
	public static Bitmap scaleImage(Bitmap org, float scaleWidth,
	        float scaleHeight) {
	    if (org == null) {
	        return null;
	    }

	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(),
	            matrix, true);
	}

	
	/**
	* ����ָ����ͼ��·���ʹ�С����ȡ����ͼ
	* �˷���������ô���
	* 1. ʹ�ý�С���ڴ�ռ䣬��һ�λ�ȡ��bitmapʵ����Ϊnull��ֻ��Ϊ�˶�ȡ��Ⱥ͸߶ȣ�
	* �ڶ��ζ�ȡ��bitmap�Ǹ��ݱ���ѹ������ͼ�񣬵����ζ�ȡ��bitmap����Ҫ������ͼ��
	* 2. ����ͼ����ԭͼ������û�����죬����ʹ����2.2�汾���¹���ThumbnailUtils��ʹ
	* ������������ɵ�ͼ�񲻻ᱻ���졣
	* @param imagePath ͼ���·��
	* @param width ָ�����ͼ��Ŀ��
	* @param height ָ�����ͼ��ĸ߶�
	* @return ���ɵ�����ͼ
	*/
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// ��ȡ���ͼƬ�Ŀ�͸ߣ�ע��˴���bitmapΪnull
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // ��Ϊ false
		// �������ű�
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// ���¶���ͼƬ����ȡ���ź��bitmap��ע�����Ҫ��options.inJustDecodeBounds ��Ϊ false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// ����ThumbnailUtils����������ͼ������Ҫָ��Ҫ�����ĸ�Bitmap����
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
		ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	* ��ȡ��Ƶ������ͼ
	* ��ͨ��ThumbnailUtils������һ����Ƶ������ͼ��Ȼ��������ThumbnailUtils������ָ����С������ͼ��
	* �����Ҫ������ͼ�Ŀ�͸߶�С��MICRO_KIND��������Ҫʹ��MICRO_KIND��Ϊkind��ֵ���������ʡ�ڴ档
	* @param videoPath ��Ƶ��·��
	* @param width ָ�������Ƶ����ͼ�Ŀ��
	* @param height ָ�������Ƶ����ͼ�ĸ߶ȶ�
	* @param kind ����MediaStore.Images.Thumbnails���еĳ���MINI_KIND��MICRO_KIND��
	* ���У�MINI_KIND: 512 x 384��MICRO_KIND: 96 x 96
	* @return ָ����С����Ƶ����ͼ
	*/
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
		ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	/**
	 * �ر�������
	 * 
	 * @param ������
	 */
	private static void closeInputStream(InputStream s) {
	    if (s == null) {
	        return;
	    }

	    try {
	        s.close();
	    } catch (IOException e) {
	        throw new RuntimeException("IOException occurred. ", e);
	    }
	}

}
