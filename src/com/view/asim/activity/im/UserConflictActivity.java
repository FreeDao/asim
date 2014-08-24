package com.view.asim.activity.im;

import java.io.File;
import java.util.Calendar;

import com.view.asim.R;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.MainActivity;
import com.view.asim.comm.Constant;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.UpgradeRule;
import com.view.asim.model.User;
import com.view.asim.service.AUKeyService;
import com.view.asim.service.OTAService;
import com.view.asim.sip.api.ISipService;
import com.view.asim.sip.api.SipManager;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.FileUtil;
import com.view.asim.utils.PreferencesProviderWrapper;
import com.view.asim.utils.PreferencesWrapper;
import com.view.asim.utils.StringUtil;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.DownloadManager.Request;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class UserConflictActivity extends ActivitySupport {

	public static final String TAG = "UserConflictActivity";
	private Context context;
	private String mResource = null;
	private AlertDialog mAlert;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Transparent);
		context = this;
		getEimApplication().addActivity(this);    
	}


	@Override
	protected void onResume() {
		super.onResume();
		mResource = getIntent().getStringExtra(User.userResourceKey);
		Log.i(TAG, "recv new resource:" + mResource);
		
		String vendor = mResource.substring(0, mResource.indexOf("."));
		mResource = mResource.replace(vendor + ".", "");
		String device = mResource.substring(0, mResource.indexOf("."));

		String date = DateUtil.getMDHM(Calendar.getInstance().getTimeInMillis());
		
		if (mAlert != null) {
			if (mAlert.isShowing()) {
				return;
			} else {
				mAlert = null;
			}
		}
		
		AlertDialog mDialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(getResources().getString(R.string.login_notify));
		builder.setMessage(getResources().getString(R.string.attention) + " " + StringUtil.getCellphoneByName(ContacterManager.userMe.getName())
				+ " " + getResources().getString(R.string.on) + " " + date
				+ " " + getResources().getString(R.string.at) + vendor + " " + device + 
				getResources().getString(R.string.login_on_other_device)
				);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		mDialog = builder.create();
		mDialog.show();
		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		
		mAlert = mDialog;
		new StopAsimRun().start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mAlert) {
			mAlert = null;
		}
		
		if (pg != null) {
			pg.dismiss();
		}
		
		/*if (connection != null) {
			try {
				service.forceStopService();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			unbindService(connection);
		}
		
		NoticeManager.getInstance().clearAllMessageNotify();
		LogcatHelper.getInstance().stop();*/
		eimApplication.exit();
	}
	private class StopAsimRun extends Thread {
		
		@Override
		public void run() {		
				try {
					AppConfigManager.getInstance().setOnline(false);
					AppConfigManager.getInstance().saveLoginConfig();
		            Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
		            stopService(serviceIntent); 
					Thread.sleep(2000);
					stopService();
					LogcatHelper.getInstance().stop();
					NoticeManager.getInstance().clearAllMessageNotify();
					XmppConnectionManager.getInstance().disconnect();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}catch (RuntimeException e) {
					e.printStackTrace();
				}
			}

			
		
	}
}
