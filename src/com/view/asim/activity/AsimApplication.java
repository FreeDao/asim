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
import com.yixia.camera.VCamera;
import com.yixia.camera.util.DeviceUtils;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * 完整的退出应用.
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
        initVCamera();
    }  
	
	private void initVCamera() {
		/*
		File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (DeviceUtils.isZte()) {
			if (dcim.exists()) {
				VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
			} else {
				VCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/Camera/VCameraDemo/");
			}
		} else {
			VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
		}
		*/
		VCamera.setVideoCachePath(FileUtil.getGlobalCachePath());
		VCamera.setDebugMode(true);
		VCamera.initialize(this);
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
        crashHandler.init(this);
	}
	
	private void initRootPath() {
		SharedPreferences preferences = getSharedPreferences(Constant.IM_SET_PREF, 0);

		final String sdcardPath = FileUtil.getSDCardRootDirectory();
		if (sdcardPath == null) {
			Constant.SDCARD_ROOT_PATH = null;
		}
		else {
			String oldPath = preferences.getString(Constant.DATA_ROOT_PATH, null);
			
			/* 如果是第一次运行 App，将可用的 SD 卡路径保存在配置中 */
			if (oldPath == null) {
				Log.d(TAG, "save sdcard path: " + sdcardPath);

				preferences.edit().putString(Constant.DATA_ROOT_PATH, sdcardPath).commit();
				Constant.SDCARD_ROOT_PATH = sdcardPath;
			}
			else {
				Constant.SDCARD_ROOT_PATH = oldPath;

				if(!oldPath.equals(sdcardPath)) {
					Log.d(TAG, "sdcard path " + sdcardPath + " has changed, the saved path is " + oldPath);

					/* 如果本次运行发现 SD 卡路径和之前记录的不同，判断之前的路径是否可用，如果不可用，退出 App */
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
	
	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
	// 添加Service到容器中
	public void addService(Service service) {
		serviceList.add(service);
	}

	// 安全彻底的退出整个 App
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
