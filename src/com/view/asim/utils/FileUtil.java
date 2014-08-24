package com.view.asim.utils;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.model.User;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 
 * �ļ�������
 * 
 * @author xuweinan
 */
public class FileUtil {
	private static final String TAG = "FileUtil";

	/**
	 * �����ļ�
	 * 
	 * @param fromFile
	 * @param toFile
	 * @throws IOException
	 */
	public static void copyFile(File fromFile, String toFile)
			throws IOException {

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
		}
	}

	/**
	 * �����ļ�
	 * 
	 * @param file
	 * @return
	 */
	public static File createNewFile(File file) {

		try {

			if (file.exists()) {
				return file;
			}

			File dir = file.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			Log.e(TAG, "", e);
			return null;
		}
		return file;
	}

	/**
	 * �����ļ�
	 * 
	 * @param path
	 */
	public static File createNewFile(String path) {
		File file = new File(path);
		return createNewFile(file);
	}// end method createText()

	/**
	 * ɾ���ļ�
	 * 
	 * @param path
	 */
	public static void deleteFile(String path) {
		File file = new File(path);
		deleteFile(file);
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
		}
		file.delete();
	}

	/**
	 * ��Text�ļ���д������
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean write(String path, String content) {
		return write(path, content, false);
	}

	public static boolean write(String path, String content, boolean append) {
		return write(new File(path), content, append);
	}

	public static boolean write(File file, String content) {
		return write(file, content, false);
	}

	public static boolean write(File file, String content, boolean append) {
		if (file == null || StringUtil.empty(content)) {
			return false;
		}
		if (!file.exists()) {
			file = createNewFile(file);
		}
		FileOutputStream ops = null;
		try {
			ops = new FileOutputStream(file, append);
			ops.write(content.getBytes());
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		} finally {
			try {
				ops.close();
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
			ops = null;
		}

		return true;
	}

	/**
	 * ����ļ���
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		if (StringUtil.empty(path)) {
			return null;
		}
		File f = new File(path);
		String name = f.getName();
		f = null;
		return name;
	}

	/**
	 * ��ȡ�ļ����ݣ��ӵ�startLine�п�ʼ����ȡlineCount��
	 * 
	 * @param file
	 * @param startLine
	 * @param lineCount
	 * @return �������ֵ�list,���list.size<lineCount��˵�������ļ�ĩβ��
	 */
	public static List<String> readFile(File file, int startLine, int lineCount) {
		if (file == null || startLine < 1 || lineCount < 1) {
			return null;
		}
		if (!file.exists()) {
			return null;
		}
		FileReader fileReader = null;
		List<String> list = null;
		try {
			list = new ArrayList<String>();
			fileReader = new FileReader(file);
			LineNumberReader lnr = new LineNumberReader(fileReader);
			boolean end = false;
			for (int i = 1; i < startLine; i++) {
				if (lnr.readLine() == null) {
					end = true;
					break;
				}
			}
			if (end == false) {
				for (int i = startLine; i < startLine + lineCount; i++) {
					String line = lnr.readLine();
					if (line == null) {
						break;
					}
					list.add(line);

				}
			}
		} catch (Exception e) {
			Log.e(TAG, "read log error!", e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	/**
	 * �����ļ���
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean createDir(File dir) {
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return true;
		} catch (Exception e) {
			Log.e(TAG, "create dir error", e);
			return false;
		}
	}

	/**
	 * ��SD���ϴ���Ŀ¼
	 * 
	 * @param dirName
	 */
	public static File creatSDDir(String dirName) {
		File dir = new File(dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * �ж�SD���ϵ��ļ��Ƿ����
	 */
	public static boolean isFileExist(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	/**
	 * ��һ��InputStream���������д�뵽SD����
	 */
	public static File write2SDFromInput(String path, String fileName,
			InputStream input) {
		File file = null;
		OutputStream output = null;
		try {
			creatSDDir(path);
			file = createNewFile(path + "/" + fileName);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			int len = -1;
			while ((len = input.read(buffer)) != -1) {
				output.write(buffer, 0, len);
			}
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	// ���ļ���һ��һ�еĶ�ȡ�ļ�
	public static String readFile(File file) {
		Reader read = null;
		String content = "";
		String string = "";
		BufferedReader br = null;
		try {
			read = new FileReader(file);
			br = new BufferedReader(read);
			while ((content = br.readLine().toString().trim()) != null) {
				string += content + "\r\n";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				read.close();
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("string=" + string);
		return string.toString();
	}
	
	public static List<String> getEmojiFile() {
		try {
			List<String> list = new ArrayList<String>();
			InputStream in = ApplicationContext.get().getResources().getAssets().open("emoji");
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}

			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getSDCardRootDirectory() {
		
		/* ������� SDCard ���ã�ֱ�ӷ�����·�� */
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		
		/* ����ͨ�� mount ��¼�������� SDCard */
		String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// �����뵱ǰ Java Ӧ�ó�����ص�����ʱ����
        try {
            Process p = run.exec(cmd);// ������һ��������ִ������
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // �������ִ�к��ڿ���̨�������Ϣ
                Log.d(TAG, lineStr);
                if (lineStr.contains("sdcard")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 3) {
                        String result = strArray[1].trim();
                        return result;
                    }
                }
                // ��������Ƿ�ִ��ʧ�ܡ�
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0��ʾ����������1������������
                    Log.e(TAG, "check mount info failed");
                    return null;
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
        
        return null;
	}
	
	public static boolean checkPathValid(String path) {
		StatFs s = null;
		try {
			s = new StatFs(path);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if (s.getBlockCount() * s.getBlockSize() != 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean checkFileValid(String path, String size, String sha1) {
		File downloadFile = new File(path);
		
		
		if (downloadFile.length() == Long.parseLong(size)) {
			StringBuffer hexString = new StringBuffer();
			
			try {
	            MessageDigest md = MessageDigest.getInstance("SHA-1");
	            FileInputStream fis = new FileInputStream(path);
	            byte[] dataBytes = new byte[4096];
	            int nread = 0;
	            while ((nread = fis.read(dataBytes)) != -1) {
	                md.update(dataBytes, 0, nread);
	            }
	            byte[] mdbytes = md.digest();
	            
	            for (int i = 0; i < mdbytes.length; i++) {
	                hexString.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	            }
	            //compare with sha hash code
	            Log.d(TAG, "rule SHA-1: " + hexString.toString()  + ", download SHA1 = " + sha1);
	            
	        } catch (NoSuchAlgorithmException e) {
	            Log.e(TAG, "There is no SHA-1 algorithm");
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            Log.e(TAG, "Update package does not exists");
	            e.printStackTrace();
	        } catch (IOException e) {
	            Log.e(TAG, "Read file error. Maybe file is corrupted");
	            e.printStackTrace();
	        }
			
			if(sha1 != null && sha1.equalsIgnoreCase(hexString.toString())){
                return true;
            } else {
                return false;
            }
		}
		else {
            Log.e(TAG, "rule size: " + size  + ", download size = " + downloadFile.length());

			return false;
		}
	}
	
	public static String getGlobalLogPath() {
		if (Constant.SDCARD_ROOT_PATH == null) {
			return null;
		}
		return Constant.SDCARD_ROOT_PATH + Constant.LOG_PATH + File.separator;
	}

	public static String getGlobalCachePath() {
		if (Constant.SDCARD_ROOT_PATH == null) {
			return null;
		}

		return Constant.SDCARD_ROOT_PATH + Constant.CACHE_PATH + File.separator;
	}
	
	
	public static String getUserRootPath() {
		if (Constant.SDCARD_ROOT_PATH == null ||  AppConfigManager.getInstance().getUsername()== null) {
			return null;
		}

		return Constant.SDCARD_ROOT_PATH + Constant.DATA_PATH + File.separator + 
				ContacterManager.userMe.getName();
	}
	
	public static String getUserDBPath() {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null) {
			return null;
		}

		return getUserRootPath() + Constant.DB_PATH + File.separator;
	}
	
	public static String getUserTempPath() {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null) {
			return null;
		}

		return getUserRootPath() + Constant.TEMP_PATH + File.separator;
	}
	
	public static String getUserImagePath() {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null) {
			return null;
		}

		return getUserRootPath() + Constant.IMAGE_PATH + File.separator;
	}
	
	public static String getUserAudioPath() {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null) {
			return null;
		}

		return getUserRootPath() + Constant.AUDIO_PATH + File.separator;
	}
	
	public static String getUserVideoPath() {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null) {
			return null;
		}

		return getUserRootPath() + Constant.VIDEO_PATH + File.separator;
	}
	
	public static String getUserFilePath() {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null) {
			return null;
		}

		return getUserRootPath() + Constant.FILE_PATH + File.separator;
	}
		
	public static String getImagePathByWith(String with) {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null || with == null) {
			return null;
		}

		return getUserImagePath() + StringUtil.getUserNameByJid(with) + File.separator;
	}
	
	public static String getAudioPathByWith(String with) {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null || with == null) {
			return null;
		}

		return getUserAudioPath() + StringUtil.getUserNameByJid(with) + File.separator;
	}
	
	public static String getVideoPathByWith(String with) {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null || with == null) {
			return null;
		}

		return getUserVideoPath() + StringUtil.getUserNameByJid(with) + File.separator;
	}
	
	public static String getFilePathByWith(String with) {
		if (Constant.SDCARD_ROOT_PATH == null || AppConfigManager.getInstance().getUsername() == null || with == null) {
			return null;
		}

		return getUserFilePath() + StringUtil.getUserNameByJid(with) + File.separator;
	}
	
	/**
	 * ��Դ�ļ������淶��
	 *   ��ͨ��־�ļ���normal-ʱ���.log
	 *   ������־�ļ���crash-ʱ���.log���ϴ���������ʱ���ϻ��ͺͰ汾��Ϣ��Ϊǰ׺������-�汾-(�û���)-crash-ʱ���.log��
	 *   ���ͱ������������ļ����ļ�ԭ��
	 *   �����������շ��͵���Ƭ�������û���-�Է��û���-image-ʱ���.src
	 *   ��������¼Ӱ���͵���Ƶ�������û���-�Է��û���-video-ʱ���.src
	 *   ��������¼Ӱ���͵���Ƶ����ͼ�������û���-�Է��û���-video-ʱ���.thumb
	 *   ���յ����ѷ�������Ƭ���Է��û���-�����û���-image-ʱ���.src
	 *   ���յ����ѷ�������Ƭ����ͼ���Է��û���-�����û���-image-ʱ���.thumb
	 *   ���յ����ѷ�������Ƶ���Է��û���-�����û���-video-ʱ���.src
	 *   ���յ����ѷ�������Ƶ����ͼ���Է��û���-�����û���-video-ʱ���.thumb
	 *   ��������¼�����͵������������û���-�Է��û���-audio-ʱ���.src
	 *   ���յ����ѷ������������Է��û���-�����û���-audio-ʱ���.src
	 *   ���ͺͽ��յ���ͨ�ļ����ļ�ԭ�� 
	 *   
	 */
	public static String genSipLogFileName() {
		return Constant.SIPLOG_PREFIX + "-" + DateUtil.getCurDateStr("yyyy-MM-dd-HH-mm-ss") + ".log";
	}
	
	public static String genLogFileName() {
		return Constant.LOG_PREFIX + "-" + DateUtil.getCurDateStr("yyyy-MM-dd-HH-mm-ss") + ".log";
	}

	public static String genCrashFileName() {
		return Constant.CRASH_PREFIX + "-" + DateUtil.getCurDateStr("yyyy-MM-dd-HH-mm-ss") + ".log";
	}	
	
	public static String genAvatarTempImageName() {
		return Constant.IMAGE_PREFIX + "-" + DateUtil.getCurDateStr("yyyy-MM-dd-HH-mm-ss") + ".jpg";
	}
	
	public static String genCaptureImageName(String with) {
		return ContacterManager.userMe.getName() + "-" + 
				StringUtil.getUserNameByJid(with) + "-" + 
				Constant.IMAGE_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.FILE_SUFFIX;
	}
	
	public static String genCaptureMicroVideoName() {
		return ContacterManager.userMe.getName() + "-" + 
				Constant.VIDEO_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
	
	public static String genRecvImageName(String with) {
		return StringUtil.getUserNameByJid(with) + "-" +
				ContacterManager.userMe.getName() + "-" + 
				Constant.IMAGE_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.FILE_SUFFIX;
	}
	
	public static String genRecvImageThumbName(String with) {
		return StringUtil.getUserNameByJid(with) + "-" +
				ContacterManager.userMe.getName() + "-" + 
				Constant.IMAGE_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.THUMB_SUFFIX;
	}
	
	public static String genRecvVideoName(String with) {
		return StringUtil.getUserNameByJid(with) + "-" +
				ContacterManager.userMe.getName() + "-" + 
				Constant.VIDEO_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.FILE_SUFFIX;
	}
	
	public static String genRecvVideoThumbName(String with) {
		return StringUtil.getUserNameByJid(with) + "-" +
				ContacterManager.userMe.getName() + "-" + 
				Constant.VIDEO_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.THUMB_SUFFIX;
	}
	
	public static String genCaptureAudioName(String with) {
		return ContacterManager.userMe.getName() + "-" + 
				StringUtil.getUserNameByJid(with) + "-" + 
				Constant.AUDIO_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.FILE_SUFFIX;
	}
	
	public static String genRecvAudioName(String with) {
		return StringUtil.getUserNameByJid(with) + "-" +
				ContacterManager.userMe.getName() + "-" + 
				Constant.AUDIO_PREFIX + "-" +
				String.valueOf(Calendar.getInstance().getTimeInMillis())
				+ Constant.FILE_SUFFIX;
	}
	
}
