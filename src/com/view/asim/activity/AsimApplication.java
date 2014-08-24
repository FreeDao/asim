package com.view.asim.activity;


import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.view.asim.R;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.dbg.CrashHandler;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.sip.api.SipManager;
import com.view.asim.utils.FileUtil;
import com.yixia.camera.VCamera;
import com.yixia.camera.util.DeviceUtils;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * 密信 App
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
        
        ApplicationContext.getInstance().init(this);

        initAppConfig();
        initAVOSCloud();
        initRootPath();
        initImageLoader();
        initVCamera();
    }  
	
	private void initAppConfig() {
		AppConfigManager.getInstance();
	}
	
	private void initAVOSCloud() {
		AVOSCloud.initialize(this, "dfhujsucfnoecty9pl6fop0s7ta0bdtvl58a7pqh5pm2yau3", "dxery5x1wr0v40p6ceagxv99cftni0nivr5uc1yo72liejgf");
        AVAnalytics.enableCrashReport(this, true);
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
	
	private void initRootPath() {
		final String sdcardPath = FileUtil.getSDCardRootDirectory();
		if (sdcardPath == null) {
			Constant.SDCARD_ROOT_PATH = null;
		}
		else {
			String oldPath = AppConfigManager.getInstance().getRootPath();
			
			if (oldPath == null) {
				Log.d(TAG, "save sdcard path: " + sdcardPath);

				AppConfigManager.getInstance().setRootPath(sdcardPath);
				Constant.SDCARD_ROOT_PATH = sdcardPath;
			}
			else {
				Constant.SDCARD_ROOT_PATH = oldPath;

				if(!oldPath.equals(sdcardPath)) {
					Log.d(TAG, "sdcard path " + sdcardPath + " has changed, the saved path is " + oldPath);

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
	
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
	public void addService(Service service) {
		serviceList.add(service);
	}

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
		System.exit(0);
	}
	public void exitUserConflict() {
		if (XmppConnectionManager.getInstance() != null) {
			XmppConnectionManager.getInstance().destroy();
		}
		for (Activity activity : activityList) {
			activity.finish();
		}
		for (Service service : serviceList) {
			service.stopSelf();
		}
		Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        stopService(serviceIntent); 
		System.exit(0);
	}
}
