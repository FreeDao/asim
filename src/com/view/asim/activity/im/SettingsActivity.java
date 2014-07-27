package com.view.asim.activity.im;

import java.io.InputStream;
import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.StringUtil;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 系统设置.
 * 
 * @author xuweinan
 */
public class SettingsActivity extends ActivitySupport {
	public static final String TAG = "SettingsActivity";

	private Button mLogoutBtn;
	private Button mExitBtn;
	private TextView mBackTxtBtn;
	private AlertDialog alertDg = null;
    protected ISipService service;
    protected ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            /*
             * timings.addSplit("Service connected"); if(configurationService !=
             * null) { timings.dumpToLog(); }
             */
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.settings);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);

		mBackTxtBtn = (TextView) findViewById(R.id.title_back_btn);
		mBackTxtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mLogoutBtn = (Button) findViewById(R.id.logout_btn);
		mLogoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				alertDg = new AlertDialog.Builder(context).setTitle("退出登录")
				.setNeutralButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new SecurityExitThread().start();
						dialog.cancel();
						pg.setMessage("正在退出密信");
						pg.show();
						
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
			}
		});
		
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);
		
	}
	
	@Override
	public void onDestroy() {
		if (alertDg != null) {
			alertDg.dismiss();
		}
		if (pg != null) {
			pg.dismiss();
		}
		super.onDestroy();

	}
	
	private class SecurityExitThread extends Thread {
		
		@Override
		public void run() {
			Log.d(TAG, "SecurityExitThread");
			mLoginCfg.setUsername(null);
			mLoginCfg.setPassword(null);
			mLoginCfg.setOnline(false);
			saveLoginConfig(mLoginCfg);
			try {
				service.forceStopService();
				Thread.sleep(2000);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			stopService();
			unbindService(connection);

			LogcatHelper.getInstance().stop();
			eimApplication.exit();
		}
	}

}
