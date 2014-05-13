/**   
 * 用一句话描述该文件做什么.
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
 * 图片操作工具类.
 * 
 * @author  xuweinan
 */

public class ImageUtil {
	/**
	 * 把bitmap转化成字节数组
	 * 
	 * @param bitmap数据
	 * @return 字节数组数据
	 */
	public static byte[] bitmapToByte(Bitmap b) {
	    if (b == null) {
	        return null;
	    }

	    ByteArrayOutputStream o = new ByteArrayOutputStream();
	    // 压缩成制定的.PNG格式
	    b.compress(Bitmap.CompressFormat.PNG, 100, o);
	    return o.toByteArray();
	}

	/**
	 * 把自己数组转换成Bitmap
	 * 
	 * @param 字节数组数据
	 * @return bitmap数据
	 */
	public static Bitmap byteToBitmap(byte[] b) {
	    return (b == null || b.length == 0) ? null : BitmapFactory
	            .decodeByteArray(b, 0, b.length);
	}

	/**
	 * 把Drawable数据转换成Bitmap
	 * 
	 * @param Drawable数据
	 * @return bitmap数据
	 */
	public static Bitmap drawableToBitmap(Drawable d) {
	    return d == null ? null : ((BitmapDrawable) d).getBitmap();
	}

	/**
	 * 把 Bitmap 数据转换成 Drawable
	 * 
	 * @param bitmap数据
	 * @return Drawable数据
	 */
	public static Drawable bitmapToDrawable(Bitmap b) {
	    return b == null ? null : new BitmapDrawable(b);
	}

	/**
	 * 把 Drawable数据转换成 字节数组
	 * 
	 * @param Drawable数据
	 * @return 字节数组
	 */
	public static byte[] drawableToByte(Drawable d) {
	    return bitmapToByte(drawableToBitmap(d));
	}

	/**
	 * 把字节数组转换成 Drawable
	 * 
	 * @param 字节数组
	 * @return Drawable
	 */
	public static Drawable byteToDrawable(byte[] b) {
	    return bitmapToDrawable(byteToBitmap(b));
	}

	/**
	 * 根据url获取输入流，返回并关闭输入流
	 * 
	 * @param 数据url
	 * @param 读取超时
	 * @return 输入流
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
	 * 过去url的数据并转换成drawable
	 * 
	 * @param 数据的url
	 * @param 读取超时时间
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
	 * 通过数据url获取bitmap数据
	 * 
	 * @param 图片url
	 * @return bitmap数据
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
     * 保存图片到文件 
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
     * 保存文件 (byte[])
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
	 * 通过宽高长度缩放图片
	 * 
	 * @param bitmap数据
	 * @param 新的宽度
	 * @param 新的高度
	 * @return 缩放后的bitmap
	 */
	public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
	    return scaleImage(org, (float) newWidth / org.getWidth(),
	            (float) newHeight / org.getHeight());
	}

	/**
	 * 通过宽高的倍数缩放图片
	 * 
	 * @param bitmap数据
	 * @param 宽度的倍数
	 * @param 高度的倍数
	 * @return 缩放后的bitmap
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
	* 根据指定的图像路径和大小来获取缩略图
	* 此方法有两点好处：
	* 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	* 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
	* 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
	* 用这个工具生成的图像不会被拉伸。
	* @param imagePath 图像的路径
	* @param width 指定输出图像的宽度
	* @param height 指定输出图像的高度
	* @return 生成的缩略图
	*/
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
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
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
		ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	* 获取视频的缩略图
	* 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	* 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	* @param videoPath 视频的路径
	* @param width 指定输出视频缩略图的宽度
	* @param height 指定输出视频缩略图的高度度
	* @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	* 其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	* @return 指定大小的视频缩略图
	*/
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
		ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	/**
	 * 关闭输入流
	 * 
	 * @param 输入流
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
