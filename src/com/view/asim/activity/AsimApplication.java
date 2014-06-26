package com.view.asim.activity;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.view.asim.R;
import com.view.asim.comm.Constant;
import com.view.asim.dbg.CrashHandler;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.util.FileUtil;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 
 * �������˳�Ӧ��.
 * 
 * @author xuweinan
 */
public class AsimApplication extends Application {
	private final static String TAG = "AsimApplication";
	private List<Activity> activityList = new LinkedList<Activity>();
	private List<Service> serviceList = new LinkedList<Service>();

	@Override  
    public void onCreate() {  
        super.onCreate();
        
        initCrashHandler();
        initImageLoader();
    }  
	
	private void initImageLoader() {
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		ImageLoader.getInstance().init(config);
	}
	
	private void initCrashHandler() {
        initRootPath();
        
        CrashHandler crashHandler = CrashHandler.getInstance();  
        //crashHandler.init(this);
	}
	
	private void initRootPath() {
		SharedPreferences preferences = getSharedPreferences(Constant.IM_SET_PREF, 0);

		final String sdcardPath = FileUtil.getSDCardRootDirectory();
		if (sdcardPath == null) {
			Constant.SDCARD_ROOT_PATH = null;
		}
		else {
			String oldPath = preferences.getString(Constant.DATA_ROOT_PATH, null);
			
			/* ����ǵ�һ������ App�������õ� SD ��·�������������� */
			if (oldPath == null) {
				Log.d(TAG, "save sdcard path: " + sdcardPath);

				preferences.edit().putString(Constant.DATA_ROOT_PATH, sdcardPath).commit();
				Constant.SDCARD_ROOT_PATH = sdcardPath;
			}
			else {
				Constant.SDCARD_ROOT_PATH = oldPath;

				if(!oldPath.equals(sdcardPath)) {
					Log.d(TAG, "sdcard path " + sdcardPath + " has changed, the saved path is " + oldPath);

					/* ����������з��� SD ��·����֮ǰ��¼�Ĳ�ͬ���ж�֮ǰ��·���Ƿ���ã���������ã��˳� App */
					if(!FileUtil.checkPathValid(oldPath)) {
						Constant.SDCARD_ROOT_PATH = sdcardPath;
					}
				}
				else {
					Log.d(TAG, "sdcard path " + sdcardPath + " has been saved.");

				}
			}
		}
		
		File cacheFolder = new File(FileUtil.getGlobalCachePath());
		File logFolder = new File(FileUtil.getGlobalLogPath());

		FileUtil.createDir(cacheFolder);
		FileUtil.createDir(logFolder);

	}
	
	// ���Activity��������
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
	// ���Service��������
	public void addService(Service service) {
		serviceList.add(service);
	}

	// ��ȫ���׵��˳����� App
	public void exit() {
		if (XmppConnectionManager.getInstance() != null) {
			XmppConnectionManager.getInstance().disconnect();
			XmppConnectionManager.getInstance().destroy();
		}
		
		for (Activity activity : activityList) {
			activity.finish();
		}
		
		for (Service service : serviceList) {
			service.stopSelf();
		}
	}
}
