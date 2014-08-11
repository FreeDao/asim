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
 * 重连接服务.
 * 
 * @author xuweinan
 */
public class ConnectService extends Service {
	private static final String TAG = "ConnectService";
	private ConnectivityManager connectivityManager;
	private NetworkInfo info;
	protected LoginConfig mLoginCfg = null;
	private Worker mConnWorker = null;
	private XmppConnectionManager mXmppManager = null;
	private SmackAndroid mSmackAndroid;

	@Override
	public void onCreate() {
		Log.d(TAG, "service create");

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(Constant.ACTION_RECONNECT_STATE);

		registerReceiver(reConnectionBroadcastReceiver, mFilter);
		
		mLoginCfg = AppConfigManager.getInstance().getLoginConfig();
		
		mConnWorker = new Worker();
		mConnWorker.initilize("XMPP Connection");

		mXmppManager = XmppConnectionManager.getInstance();
		mSmackAndroid = SmackAndroid.init(ApplicationContext.get());

		/*
		connection = XmppConnectionManager.getInstance().getConnection();
		if (connection.isConnected()) {
			connection.addConnectionListener(new ConnectionListener() {
	
				@Override
				public void connectionClosed() {
					Log.d(TAG, "ConnectionListener: connectionClosed");
					
				}
	
				@Override
				public void connectionClosedOnError(Exception arg0) {
					Log.d(TAG, "ConnectionListener: connectionClosedOnError, exception: "  + arg0);		
					
					
				}
	
				@Override
				public void reconnectingIn(int arg0) {
					Log.d(TAG, "ConnectionListener: reconnectingIn " + arg0 + " times");					
				}
	
				@Override
				public void reconnectionFailed(Exception arg0) {
					Log.d(TAG, "ConnectionListener: reconnectionFailed, exception: "  + arg0);
				}
	
				@Override
				public void reconnectionSuccessful() {
					Log.d(TAG, "ConnectionListener: reconnectionSuccessful");
					//sendBroadcastToUI(Constant.RECONNECT_STATE_SUCCESS);
					//sendReconnBroadcastToContactService();
				}
				
			});
		}
		*/
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
						/*
						if (connThead == null || !connThead.isAlive()) {
							connThead = new ConnThread(connection);
							connThead.start();	
						}*/
						new ConnThread().start();
					} else {
						//sendBroadcastToUI(Constant.RECONNECT_STATE_SUCCESS);
					}
				} else {
					Log.d(TAG, "network connection lost.");

					//sendBroadcastToUI(Constant.RECONNECT_STATE_FAIL);
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

	/*
	private class ConnStatusDaemonThread extends Thread {
		
		@Override
		public void run() {
			Log.d(TAG, "ConnStatusDaemonThread");
			
			while(true) {
				try {
					Thread.sleep(5000);
					XMPPConnection connection = XmppConnectionManager.getInstance()
							.getConnection();
					if(!connection.isConnected()) {
						if(connThead == null || !connThead.isAlive()) {
							connThead = new ConnThread(connection);
							connThead.start();	
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	*/

	private class ConnThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
				mXmppManager.reconnect(mLoginCfg);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	
	public void reConnect(XMPPConnection connection) {
		Log.d(TAG, "XMPP connection lost, reconn it.");

		try {
			Thread.sleep(5000);
			//XmppConnectionManager.getInstance().init(mLoginCfg);
			XmppConnectionManager.getInstance().getConnection().connect();
			//XmppConnectionManager.getInstance().getConnection().login(mLoginCfg.getUsername(), mLoginCfg.getPassword());
			
		} catch (Exception e) {
			Log.e(TAG, "XMPP reconnect failed!");
			reConnect(connection);
		}
		
		sendBroadcastToUI(Constant.RECONNECT_STATE_SUCCESS);
		sendReconnBroadcastToContactService();
	}
	*/

	/*
	private void sendBroadcastToUI(boolean isSuccess) {
		Log.d(TAG, "send connection state " + isSuccess + " broadcast to UI.");
		Intent intent = new Intent();
		SharedPreferences preference = getSharedPreferences(Constant.IM_SET_PREF,
				0);
		// 保存在线连接信息
		preference.edit().putBoolean(Constant.IS_ONLINE, isSuccess).commit();
		intent.setAction(Constant.ACTION_RECONNECT_STATE);
		intent.putExtra(Constant.RECONNECT_STATE, isSuccess);
		sendBroadcast(intent);
	}
	
	private void sendReconnBroadcastToContactService() {
		Log.d(TAG, "send reconnection success broadcast to Contact Service");
		Intent intent = new Intent(Constant.RECV_OFFLINE_MSG_ACTION);
		sendBroadcast(intent);
	}
	*/

}
