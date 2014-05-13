package com.view.asim.service;

import java.util.HashMap;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.R;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;

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
public class ReConnectService extends Service {
	private static final String TAG = "ReConnectService";
	private Context context;
	private XMPPConnection connection;
	private ConnectivityManager connectivityManager;
	private NetworkInfo info;
	private ConnThread connThead = null;
	protected SharedPreferences preferences;
	protected LoginConfig mLoginCfg = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "service create");

		context = this;
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(reConnectionBroadcastReceiver, mFilter);
		
		preferences = getSharedPreferences(Constant.LOGIN_SET, 0);
		mLoginCfg = getLoginConfig();
		
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
					sendBroadcastToUI(Constant.RECONNECT_STATE_SUCCESS);
					sendReconnBroadcastToContactService();
				}
				
			});
		}
		super.onCreate();
	}
	
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
		unregisterReceiver(reConnectionBroadcastReceiver);
		super.onDestroy();
	}

	BroadcastReceiver reConnectionBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				XMPPConnection connection = XmppConnectionManager.getInstance()
						.getConnection();
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					Log.d(TAG, "network connection available.");

					if (!connection.isConnected()) {
						if (connThead == null || !connThead.isAlive()) {
							connThead = new ConnThread(connection);
							connThead.start();	
						}
					} else {
						sendBroadcastToUI(Constant.RECONNECT_STATE_SUCCESS);
					}
				} else {
					Log.d(TAG, "network connection lost.");

					sendBroadcastToUI(Constant.RECONNECT_STATE_FAIL);
				}
			}

		}

	};
	
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

	private class ConnThread extends Thread {
		private XMPPConnection conn;
		public ConnThread(XMPPConnection conn) {
			this.conn = conn;
		}
		@Override
		public void run() {
			Presence presence = new Presence(Presence.Type.unavailable);
			conn.disconnect(presence);
			
			reConnect(conn);
		}
	}

	/**
	 * 
	 * 递归重连，直连上为止.
	 * 
	 * @author xuweinan
	 * @update 2012-7-10 下午2:12:25
	 */
	public void reConnect(XMPPConnection connection) {
		Log.d(TAG, "XMPP connection lost, reconn it.");

		try {
			
			//XmppConnectionManager.getInstance().init(mLoginCfg);
			XmppConnectionManager.getInstance().getConnection().connect();
			XmppConnectionManager.getInstance().getConnection().login(mLoginCfg.getUsername(), mLoginCfg.getPassword());
			
		} catch (XMPPException e) {
			Log.e(TAG, "XMPP reconnect failed!");
			reConnect(connection);
		}
		
		sendBroadcastToUI(Constant.RECONNECT_STATE_SUCCESS);
		sendReconnBroadcastToContactService();
	}

	private void sendBroadcastToUI(boolean isSuccess) {
		Log.d(TAG, "send connection state " + isSuccess + " broadcast to UI.");
		Intent intent = new Intent();
		SharedPreferences preference = getSharedPreferences(Constant.LOGIN_SET,
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

}
