package com.view.asim.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.view.asim.comm.Constant;
import com.view.asim.R;
import com.view.asim.model.LoginConfig;
import com.view.asim.service.AUKeyService;
import com.view.asim.service.IMChatService;
import com.view.asim.service.IMContactService;
import com.view.asim.service.ReConnectService;
import com.view.asim.util.FileUtil;
import com.view.asim.util.LogcatHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Actity 工具支持类
 * 
 * @author shimiso
 * 
 */
public class ActivitySupport extends Activity implements IActivitySupport {
	
	private static final String TAG = "ActivitySupport";

	protected Context context = null;
	protected SharedPreferences preferences;
	protected AsimApplication eimApplication;
	protected ProgressDialog pg = null;
	protected LoginConfig mLoginCfg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		preferences = getSharedPreferences(Constant.LOGIN_SET, 0);
		pg = new ProgressDialog(context);
		eimApplication = (AsimApplication) getApplication();
		eimApplication.addActivity(this);
		
		mLoginCfg = getLoginConfig();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(this.toString(), "onStart");

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(this.toString(), "onResume");

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(this.toString(), "onStop");

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(this.toString(), "onStop");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(this.toString(), "onDestroy");

	}

	@Override
	public ProgressDialog getProgressDialog() {
		return pg;
	}

	@Override
	public void startService() {
		// 好友联系人服务
		Intent server = new Intent(context, IMContactService.class);
		context.startService(server);
		
		// 聊天服务
		Intent chatServer = new Intent(context, IMChatService.class);
		context.startService(chatServer);
		// 自动恢复连接服务
		Intent reConnectService = new Intent(context, ReConnectService.class);
		context.startService(reConnectService);
		
		Intent keyService = new Intent(context, AUKeyService.class);
		context.startService(keyService);

	}

	/**
	 * 
	 * 销毁服务.
	 * 
	 * @author xuweinan
	 * @update 2012-5-16 下午12:16:08
	 */
	@Override
	public void stopService() {
		// 好友联系人服务
		Intent server = new Intent(context, IMContactService.class);
		context.stopService(server);
		// 聊天服务
		Intent chatServer = new Intent(context, IMChatService.class);
		context.stopService(chatServer);

		// 自动恢复连接服务
		Intent reConnectService = new Intent(context, ReConnectService.class);
		context.stopService(reConnectService);

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
		String a = preferences.getString(Constant.XMPP_HOST, null);
		String b = getResources().getString(R.string.xmpp_host);
		loginConfig.setXmppHost(preferences.getString(Constant.XMPP_HOST,
				getResources().getString(R.string.xmpp_host)));
		loginConfig.setXmppPort(preferences.getInt(Constant.XMPP_PORT,
				getResources().getInteger(R.integer.xmpp_port)));
		loginConfig.setUsername(preferences.getString(Constant.USERNAME, null));
		loginConfig.setPassword(preferences.getString(Constant.PASSWORD, null));
		loginConfig.setXmppServiceName(preferences.getString(
				Constant.XMPP_SERVICE_NAME,
				getResources().getString(R.string.xmpp_service_name)));
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
}
