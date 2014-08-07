package com.view.asim.service;


import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.activity.IActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.worker.Worker;

import android.hardware.usb.*;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 安司盾服务
 * 
 * @author xuweinan
 */
public class AUKeyService extends Service {
	
	private static final String TAG = "AUKeyService";
	private boolean daemonRun = false;
	private Context context;
	private static final int VID = 0x0029;
	private static final int PID = 0x0015;

	@Override
	public void onCreate() {
		Log.d(TAG, "service create");

		context = this;
		super.onCreate();
		daemonRun = true;
		new AUKeyDaemonThread().start();
	}


	private class AUKeyDaemonThread extends Thread {
		
		@Override
		public void run() {
			boolean findKey = false;
			Log.d(TAG, "AUKeyDaemonThread");
			UsbManager mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
			
			while(true) {
				findKey = false;
				
				if (daemonRun == false) {
					return;
				}
				try {
					HashMap<String, UsbDevice> map = mUsbManager.getDeviceList();
					for(UsbDevice dev : map.values()){
						if(VID == dev.getVendorId() && PID == dev.getProductId()){
							AUKeyManager.getInstance().setAUKeyStatus(context, AUKeyManager.ATTACHED);	
							findKey = true;
							break;
						}
					}
					
					if (findKey == false) {
						AUKeyManager.getInstance().setAUKeyStatus(context, AUKeyManager.DETACHED);
					}

					Thread.sleep(5000);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
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
		super.onDestroy();
		daemonRun = false;
	}

}
