package com.view.asim.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import com.csipsimple.api.SipManager;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.view.asim.comm.Constant;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.R;
import com.view.asim.model.LoginConfig;
import com.view.asim.service.AUKeyService;
import com.view.asim.service.IMChatService;
import com.view.asim.service.IMContactService;
import com.view.asim.service.OTAService;
import com.view.asim.service.ConnectService;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FileUtil;
import com.view.asim.util.StringUtil;
import com.yixia.camera.model.MediaObject;
import com.yixia.camera.model.MediaObject.MediaPart;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Actity 工具支持类
 * 
 * @author xuweinan
 * 
 */
public class ActivitySupport extends Activity implements IActivitySupport {
	
	private static final String TAG = "ActivitySupport";

	protected Context context = null;
	protected SharedPreferences preferences;
	protected SharedPreferences sipPreferences;
	protected AsimApplication eimApplication;
	protected ProgressDialog pg = null;
	protected LoginConfig mLoginCfg = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		preferences = getSharedPreferences(Constant.IM_SET_PREF, 0);
		sipPreferences = getSharedPreferences(Constant.SIP_SET_PREF, 0);
		pg = new ProgressDialog(context);
		pg.setCancelable(false);
		eimApplication = (AsimApplication) getApplication();
		eimApplication.addActivity(this);
		
		mLoginCfg = getLoginConfig();
		Log.d(this.toString(), "onCreate on " + DateUtil.getCurDateStr());

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(this.toString(), "onStart on " + DateUtil.getCurDateStr());

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(this.toString(), "onResume on " + DateUtil.getCurDateStr());

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(this.toString(), "onPause on " + DateUtil.getCurDateStr());

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(this.toString(), "onStop on " + DateUtil.getCurDateStr());
		hideProgress();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(this.toString(), "onDestroy on " + DateUtil.getCurDateStr());
		mLoginCfg = null;
		pg = null;
	}

	@Override
	public ProgressDialog getProgressDialog() {
		return pg;
	}

	public ProgressDialog showProgress(String title, String message) {
		return showProgress(title, message, -1);
	}

	public ProgressDialog showProgress(String title, String message, int theme) {
		if (pg == null) {
			if (theme > 0)
				pg = new ProgressDialog(this, theme);
			else
				pg = new ProgressDialog(this);
			pg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pg.requestWindowFeature(Window.FEATURE_NO_TITLE);
			pg.setCanceledOnTouchOutside(false);
			pg.setIndeterminate(true);
		}

		if (!StringUtils.isEmpty(title))
			pg.setTitle(title);
		pg.setMessage(message);
		pg.show();
		return pg;
	}

	public void hideProgress() {
		if (pg != null) {
			pg.dismiss();
		}
	}
	
	@Override
	public void startConnService() {
		// XMPP连接管理服务
		Intent xmpp = new Intent(context, ConnectService.class);
		context.startService(xmpp);

	}

	@Override
	public void startGeneralService() {
		// OTA在线升级服务
		Intent ota = new Intent(context, OTAService.class);
		context.startService(ota);
		
		// 好友联系人服务
		Intent server = new Intent(context, IMContactService.class);
		context.startService(server);
		
		// 聊天服务
		Intent chatServer = new Intent(context, IMChatService.class);
		context.startService(chatServer);
		
		// 安司盾监控管理服务
		Intent keyService = new Intent(context, AUKeyService.class);
		context.startService(keyService);

	}
	

	/**
	 * 
	 * 销毁服务.
	 * 
	 * @author xuweinan
	 */
	@Override
	public void stopService() {
		// OTA在线升级服务
		Intent ota = new Intent(context, OTAService.class);
		context.stopService(ota);
		
		// 好友联系人服务
		Intent server = new Intent(context, IMContactService.class);
		context.stopService(server);
		
		// 聊天服务
		Intent chatServer = new Intent(context, IMChatService.class);
		context.stopService(chatServer);
		
		// XMPP连接管理服务
		Intent xmpp = new Intent(context, ConnectService.class);
		context.stopService(xmpp);
		
		// 安司盾监控管理服务
		Intent keyService = new Intent(context, AUKeyService.class);
		context.stopService(keyService);
	}

	@Override
	public void isExit() {
		new AlertDialog.Builder(context).setTitle("确定退出吗?")
				.setNeutralButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						stopService();
						LogcatHelper.getInstance().stop();
						eimApplication.exit();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	@Override
	public boolean hasInternetConnected() {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager != null) {
			NetworkInfo network = manager.getActiveNetworkInfo();
			if (network != null && network.isConnectedOrConnecting()) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean hasLocationGPS() {
		LocationManager manager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (manager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean hasLocationNetWork() {
		LocationManager manager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (manager
				.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean validateInternet() {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			openWirelessSet();
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		openWirelessSet();
		return false;
	}

	public void openWirelessSet() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder
				.setTitle(R.string.prompt)
				.setMessage(context.getString(R.string.check_connection))
				.setPositiveButton(R.string.menu_settings,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								Intent intent = new Intent(
										Settings.ACTION_WIRELESS_SETTINGS);
								context.startActivity(intent);
							}
						})
				.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
		dialogBuilder.show();
	}

	/**
	 * 
	 * 显示toast
	 * 
	 * @param text
	 * @param longint
	 * @author shimiso
	 * @update 2012-6-28 下午3:46:18
	 */
	@Override
	public void showToast(String text, int longint) {
		Toast.makeText(context, text, longint).show();
	}

	@Override
	public void showToast(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	
	/**
	 * 
	 * 关闭键盘事件
	 * 
	 * @author xuweinan
	 * @update 2012-7-4 下午2:34:34
	 */
	public void closeInput() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null && this.getCurrentFocus() != null) {
			inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public SharedPreferences getLoginUserSharedPre() {
		return preferences;
	}

	@Override
	public void saveLoginConfig(LoginConfig loginConfig) {
		preferences.edit()
				.putString(Constant.XMPP_HOST, loginConfig.getXmppHost())
				.commit();
		preferences.edit()
				.putInt(Constant.XMPP_PORT, loginConfig.getXmppPort()).commit();
		preferences
				.edit()
				.putString(Constant.XMPP_SERVICE_NAME,
						loginConfig.getXmppServiceName()).commit();
		preferences.edit()
				.putString(Constant.USERNAME, loginConfig.getUsername())
				.commit();
		preferences.edit()
				.putString(Constant.PASSWORD, loginConfig.getPassword())
				.commit();
		preferences.edit()
				.putBoolean(Constant.IS_ONLINE, loginConfig.isOnline())
				.commit();
		preferences.edit()
				.putString(Constant.DATA_ROOT_PATH, loginConfig.getRootPath())
				.commit();
	}

	@Override
	public LoginConfig getLoginConfig() {
		LoginConfig loginConfig = new LoginConfig();
		loginConfig.setXmppHost(preferences.getString(Constant.XMPP_HOST,
				Constant.IM_SERVICE_HOST));
		loginConfig.setXmppPort(preferences.getInt(Constant.XMPP_PORT,
				Constant.IM_SERVICE_PORT));
		loginConfig.setUsername(preferences.getString(Constant.USERNAME, null));
		loginConfig.setPassword(preferences.getString(Constant.PASSWORD, null));
		loginConfig.setXmppServiceName(preferences.getString(
				Constant.XMPP_SERVICE_NAME,
				Constant.IM_SERVICE_NAME));
		loginConfig.setRootPath(preferences.getString(Constant.DATA_ROOT_PATH, null));
		return loginConfig;
	}

	@Override
	public boolean getUserOnlineState() {
		return preferences.getBoolean(Constant.IS_ONLINE, true);
	}

	@Override
	public void setUserOnlineState(boolean isOnline) {
		preferences.edit().putBoolean(Constant.IS_ONLINE, isOnline).commit();

	}
	
	@Override
	public String getDataRootPath() {
		return preferences.getString(Constant.DATA_ROOT_PATH, null);
	}

	@Override
	public void setDataRootPath(String path) {
		preferences.edit().putString(Constant.DATA_ROOT_PATH, path).commit();

	}

	@Override
	public AsimApplication getEimApplication() {
		return eimApplication;
	}
	
	@Override
	public String getVersion() {
		final String unknown = "Unknown";
		
		if (context == null) {
			return unknown;
		}
		
		try {
	    	String ret = context.getPackageManager()
			   .getPackageInfo(context.getPackageName(), 0)
			   .versionName;
	    	if (ret.contains(" + "))
	    		ret = ret.substring(0,ret.indexOf(" + "))+"b";
	    	return ret;
		} catch(NameNotFoundException ex) {}
		
		return unknown;		
	}
	
    protected void sipDisconnect(boolean quit) {
        Log.d(TAG, "True disconnection SIP service...");
        Intent intent = new Intent(SipManager.ACTION_OUTGOING_UNREGISTER);
        intent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(this, MainActivity.class));
        sendBroadcast(intent);
        if(quit) {
            finish();
        }
    }
    
	protected static MediaObject restoreMediaObject(String obj) {
		try {
			String str = FileUtils.readFile(new File(obj));
			Gson gson = new Gson();
			MediaObject result = gson.fromJson(str.toString(), MediaObject.class);
			result.getCurrentPart();
			preparedMediaObject(result);
			return result;
		} catch (Exception e) {
			if (e != null)
				Log.e("VCamera", "readFile", e);
		}
		return null;
	}

	public static void preparedMediaObject(MediaObject mMediaObject) {
		if (mMediaObject != null && mMediaObject.getMedaParts() != null) {
			int duration = 0;
			for (MediaPart part : mMediaObject.getMedaParts()) {
				part.startTime = duration;
				part.endTime = part.startTime + part.duration;
				duration += part.duration;
			}
		}
	}
	
	public static  boolean saveMediaObject(MediaObject mMediaObject) {
		if (mMediaObject != null) {
			try {
				if (!StringUtils.isNotEmpty(mMediaObject.getObjectFilePath())) {
					FileOutputStream out = new FileOutputStream(mMediaObject.getObjectFilePath());
					Gson gson = new Gson();
					out.write(gson.toJson(mMediaObject).getBytes());
					out.flush();
					out.close();
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
