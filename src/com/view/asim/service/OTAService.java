package com.view.asim.service;

import java.io.File;
import java.util.Calendar;

import com.view.asim.R;
import com.view.asim.comm.Constant;
import com.view.asim.model.UpgradeRule;
import com.view.asim.util.FileUtil;
import com.view.asim.worker.BaseHandler;
import com.view.asim.worker.CheckOTAStatusHandler;
import com.view.asim.worker.OTACheckResultListener;
import com.view.asim.worker.Worker;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 
 * 在线升级服务.
 * 
 * @author xuweinan
 */
public class OTAService extends Service {
	private static final String TAG = "OTAService";
	
	public static final int MSG_SHOW_UPDATE_NOTIFY = 0;
	public static final int MSG_SHOW_INSTALL_NOTIFY = 1;

	class OTAStatusInfo {  
		// Status enumeration
		public static final String STATUS_IDLE = "idle";
		public static final String STATUS_CHECKING = "checking";
		public static final String STATUS_DOWNLOADING = "downloading";
		public static final String STATUS_DOWNLOADED = "downloaded";
		
		// Trigger enumeration
		public static final String TRIGGER_MANUAL = "manual";
		public static final String TRIGGER_AUTO = "auto";
		
        public String status;  
		public String version;
        public String trigger;  
        public UpgradeRule upgradeRule;  
        public long downloadId;  
        public String filename;  
        
        public OTAStatusInfo() {
        	init();
        	this.version = null;
        }
        
        public void init() {
        	this.status = STATUS_IDLE;
        	this.trigger = TRIGGER_AUTO;
        	this.upgradeRule = null;
        	this.downloadId = -1;
        	this.filename = null;
        }
  
        @Override  
        public String toString() {  
            String str = "status = " + status + ", version = " + version + ", trigger = " + trigger + ", upgradeRule = " + upgradeRule  
                    + ", downloadId = " + downloadId + ", filename = " + filename;  
            return str;
        }  
    }
	
	private Context context;
	private ConnectivityManager connectivityManager;
	private DownloadManager downloadManager;
	private NetworkInfo info;
	private UIHandler mHandler;
	private Worker mOTAProcWorker = null;
	private OTAStatusInfo mOTAStatus = new OTAStatusInfo();

	@Override
	public void onCreate() {
		Log.d(TAG, "service create");

		context = this;
		
		downloadManager = (DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
		mHandler = new UIHandler();
		
		initWorker();
		initVersion();
		initBroadcastReceiver();
		
		super.onCreate();
	}
	
	private void initWorker() {
		mOTAProcWorker = new Worker();
		mOTAProcWorker.initilize("OTA Process Worker");
	}
	private void initBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		filter.addAction(Constant.OTA_CHECK_ACTION);
		registerReceiver(receiver, filter);
	}
	
	private void initVersion() {
		// 获取 App 当前版本
		PackageInfo info = null;  
        try {  
            info = context.getPackageManager().getPackageInfo(  
                    context.getPackageName(), 0);  
        } catch (NameNotFoundException e) {  
        }  
        if (info == null) {
            info = new PackageInfo();  
        }
        
        mOTAStatus.version = info.versionName;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
		//return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	 private class UIHandler extends Handler{  
	        @Override  
	        public void handleMessage(Message msg) {  
	            super.handleMessage(msg);
	            
	            switch(msg.what) {
	            case MSG_SHOW_UPDATE_NOTIFY:
					showUpgradeNotify();

	            	break;
	            	
	            case MSG_SHOW_INSTALL_NOTIFY:
		            Bundle bd = msg.getData();  
		            showInstallNotify((Uri)bd.getParcelable("uri"), bd.getString("mime"));
	            	break;
	            }
	            
	        }  
	    }

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final Context cntx = context;
			boolean ifCheck = false;
			String action = intent.getAction();
			
			if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if(mOTAStatus.status.equals(OTAStatusInfo.STATUS_DOWNLOADING)) {
					if(id == mOTAStatus.downloadId) {
						String name = FileUtil.getGlobalCachePath() + 
	                			mOTAStatus.filename;
						
	                	Uri localUri = Uri.parse("file://" + name);
	                	
	                	if (FileUtil.checkFileValid(name, mOTAStatus.upgradeRule.getSize(), mOTAStatus.upgradeRule.getSha1())) {
		                	mOTAStatus.status = OTAStatusInfo.STATUS_DOWNLOADED;
		                	
		                	Bundle bd = new Bundle();
		                	bd.putParcelable("uri", localUri);
		                	bd.putString("mime", downloadManager.getMimeTypeForDownloadedFile(id));
		                	Message msg = new Message();
		                	msg.setData(bd);
		                	msg.what = MSG_SHOW_INSTALL_NOTIFY;
		                	mHandler.sendMessage(msg);
	                	}
	                	else {
	                		mOTAStatus.init();
	                	}
					}
				}
				return;
			}

			
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					Log.d(TAG, "check ota when network available.");
					ifCheck = true;
					mOTAStatus.init();
					mOTAStatus.trigger = OTAStatusInfo.TRIGGER_AUTO;
				}
			}
			else if (action.equals(Constant.OTA_CHECK_ACTION)) {
				Log.d(TAG, "check ota when manual operation");
				ifCheck = true;
				mOTAStatus.trigger = OTAStatusInfo.TRIGGER_MANUAL;

			}

			if (ifCheck) {
				clearOldDownloadTask();
				
				BaseHandler handler = null;
				
				mOTAStatus.status = OTAStatusInfo.STATUS_CHECKING;
				// 启动在线检查版本更新情况的 handler
				handler = (CheckOTAStatusHandler) new CheckOTAStatusHandler(context, mOTAStatus.version,
						new OTACheckResultListener() {
							@Override
							public void onCheckResult(boolean needUpgrade, UpgradeRule rule) {
								
								if(needUpgrade) {
									Log.d(TAG, "need upgrade, the rule: " + rule);
									mOTAStatus.upgradeRule = rule;
									Message msg = new Message();
				                	msg.what = MSG_SHOW_UPDATE_NOTIFY;
				                	mHandler.sendMessage(msg);
								}
								else {
									Log.d(TAG, "the version " + mOTAStatus.version + " is up-to-date, do not need upgrade");
									if(mOTAStatus.trigger.equals(OTAStatusInfo.TRIGGER_MANUAL)) {
										/* FIXME: 需要广播返回结果给触发模块
										Looper.prepare();
										Toast.makeText(cntx, "已经是最新版本了哦 :)", Toast.LENGTH_SHORT).show();
										Looper.loop();
										*/
									}
									mOTAStatus.init();
								}
								
							}
				
				});
				mOTAProcWorker.addHandler(handler);
				
			}
		
		}
	};
	
	private void clearOldDownloadTask() {
		Log.d(TAG, "clearOldDownloadTask, status info: " + mOTAStatus);
		String name;
		int status;
		long id;
	    DownloadManager.Query query = new DownloadManager.Query();
	    Cursor c = null;
	    try {
	        c = downloadManager.query(query);
	        if (c != null && c.moveToFirst()) {
	        	name = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
	        	status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
	        	id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));

	        	if (name.contains("asim")) {
	        		Log.d(TAG, "find a downloading task " + id + " on status " + status + ", named " + name + ", cancel it.");
	        		downloadManager.remove(id);
	        	}
	        }
	    } finally {
	        if (c != null) {
	            c.close();
	        }
	    }
	}

	private void showUpgradeNotify() {		
        AlertDialog mDialog = null;  
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("版本更新");  
        builder.setMessage("密信新版本 " + mOTAStatus.upgradeRule.getTgtVer() + " 已经发布，是否立即下载？");  
        builder.setPositiveButton(android.R.string.ok,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {
                    	
                    	Uri uri = Uri.parse("http://" + Constant.OTA_STORAGE_HOST + File.separator + mOTAStatus.upgradeRule.getName());
                    	DownloadManager.Request request = new Request(uri);  
                    	request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
                    	request.setTitle("密信 新版本 " + mOTAStatus.upgradeRule.getTgtVer() + " 下载中");
                    	
                    	mOTAStatus.filename = mOTAStatus.upgradeRule.getName().replace(".apk", "") + "_" + 
                    			Calendar.getInstance().getTimeInMillis() + ".apk";
                    	Uri localUri = Uri.parse("file://" + FileUtil.getGlobalCachePath() + 
                    			mOTAStatus.filename);
                    	request.setDestinationUri(localUri);
                    	mOTAStatus.downloadId = downloadManager.enqueue(request); 
                    	mOTAStatus.status = OTAStatusInfo.STATUS_DOWNLOADING;
                    	
						Log.d(TAG, "the new version file " + mOTAStatus.filename + " starts downloading.");
                    	
                    }  
                });  
        builder.setNegativeButton(android.R.string.cancel,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();
                        mOTAStatus.init();
                    }  
                });  
        mDialog = builder.create();  
        mDialog.getWindow().setType(  
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
        mDialog.show();  
	}
	
	private void showInstallNotify(Uri uri, String mimeType) {
		final String mime = mimeType;
		final Uri u = uri;
		
        AlertDialog mDialog = null;  
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("版本安装");  
        builder.setMessage("密信新版本 " + mOTAStatus.upgradeRule.getTgtVer() + " 下载完成，是否立即更新？");  
        builder.setPositiveButton(android.R.string.ok,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "the new version file " + u + " starts installing.");

						Intent intent = new Intent(Intent.ACTION_VIEW);  
					    intent.setDataAndType(u,  
					    		mime);  
					    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    startActivity(intent);  
					
                    }  
                });  
        builder.setNegativeButton(android.R.string.cancel,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();
                    }  
                });  
        mDialog = builder.create();  
        mDialog.getWindow().setType(  
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
        mDialog.show();  
	}


}
