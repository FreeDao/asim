package com.view.asim.activity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import com.avos.avoscloud.AVAnalytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.view.asim.R;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.service.AUKeyService;
import com.view.asim.service.IMChatService;
import com.view.asim.service.IMContactService;
import com.view.asim.service.OTAService;
import com.view.asim.service.ConnectService;
import com.view.asim.sip.api.SipManager;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.FileUtil;
import com.view.asim.utils.StringUtil;
import com.yixia.camera.model.MediaObject;
import com.yixia.camera.model.MediaObject.MediaPart;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
 * 
 * 
 * @author xuweinan
 * 
 */
public class ActivitySupport extends Activity implements IActivitySupport {
	
	private static final String TAG = "ActivitySupport";

	protected Context context = null;
	protected AsimApplication eimApplication;
	protected ProgressDialog pg = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		pg = new ProgressDialog(context);
		pg.setCancelable(false);
		eimApplication = (AsimApplication) getApplication();
		eimApplication.addActivity(this);
		
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
		AVAnalytics.onResume(this);
		Log.d(this.toString(), "onResume on " + DateUtil.getCurDateStr());

	}

	@Override
	protected void onPause() {
		super.onPause();
		AVAnalytics.onPause(this);
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
		Intent xmpp = new Intent(context, ConnectService.class);
		context.startService(xmpp);

	}

	@Override
	public void startGeneralService() {
		Intent ota = new Intent(context, OTAService.class);
		context.startService(ota);
		Intent server = new Intent(context, IMContactService.class);
		context.startService(server);
		
		Intent chatServer = new Intent(context, IMChatService.class);
		context.startService(chatServer);
		
		Intent keyService = new Intent(context, AUKeyService.class);
		context.startService(keyService);

	}
	

	/**
	 * 
	 * ???锟�???????.
	 * 
	 * @author xuweinan
	 */
	@Override
	public void stopService() {
		// OTA??�线???级�?????
		Intent ota = new Intent(context, OTAService.class);
		context.stopService(ota);
		
		// 好�?????系人??????
		Intent server = new Intent(context, IMContactService.class);
		context.stopService(server);
		
		// ???天�?????
		Intent chatServer = new Intent(context, IMChatService.class);
		context.stopService(chatServer);
		
		// XMPP�????��?��????????
		Intent xmpp = new Intent(context, ConnectService.class);
		context.stopService(xmpp);
		
		// �????��?��????��?��????????
		Intent keyService = new Intent(context, AUKeyService.class);
		context.stopService(keyService);
	}

	@Override
	public void isExit() {
		new AlertDialog.Builder(context).setTitle(getResources().getString(R.string.settings_exit_btn))
				.setNeutralButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						stopService();
						LogcatHelper.getInstance().stop();
						eimApplication.exit();
					}
				})
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
	 * ??�示toast
	 * 
	 * @param text
	 * @param longint
	 * @author xuweinan
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
	 * ??��????????�??�??
	 * 
	 * @author xuweinan
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
	public  void changeLayout(){
	}
	public boolean isLoginActivity(){
		return false;
	}
	public boolean isSignUpActivity(){
		return false;
	}
}
