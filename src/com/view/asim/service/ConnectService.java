package com.view.asim.service;


import java.util.HashMap;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.R;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.CallLogManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.worker.Worker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * �����ӷ���.
 * 
 * @author xuweinan
 */
public class ConnectService extends Service {
	private static final String TAG = "ConnectService";
	private ConnectivityManager connectivityManager;
	private NetworkInfo info;
	private XmppConnectionManager mXmppManager = null;
	private SmackAndroid mSmackAndroid;
	private Context context;
	@Override
	public void onCreate() {
		Log.d(TAG, "service create");
		context = this;
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(Constant.ACTION_RECONNECT_STATE);

		registerReceiver(reConnectionBroadcastReceiver, mFilter);
				
		mXmppManager = XmppConnectionManager.getInstance();
		mSmackAndroid = SmackAndroid.init(ApplicationContext.get());
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(reConnectionBroadcastReceiver);
		try {
			mSmackAndroid.onDestroy();
		} catch(Exception e) {
			e.printStackTrace();
		}
//		Intent xmpp = new Intent(context, ConnectService.class);
//		context.startService(xmpp);
		super.onDestroy();
	}

	BroadcastReceiver reConnectionBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				XMPPConnection connection = mXmppManager.getConnection();
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					Log.d(TAG, "network connection available.");

					if (connection != null && !connection.isConnected()) {
						new ConnThread().start();
					}
				} else {
					Log.d(TAG, "network connection lost.");
				}
			}
			else if (action.equals(Constant.ACTION_RECONNECT_STATE)) {
				String status = intent.getStringExtra(Constant.RECONNECT_STATE);
				if (status.equals(XmppConnectionManager.CONNECTING)) {
					new ConnThread().start();
				}
			}
		}

	};

	private class ConnThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
				AppConfigManager.getInstance().resolvServer();
				
				mXmppManager.reconnect();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
