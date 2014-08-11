package com.view.asim.activity.im;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.LoginActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.NoticeAdapter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 关于密信界面
 * 
 * @author xuweinan
 */
public class AboutAsimActivity extends ActivitySupport {
	protected static final String TAG = "AboutAsimActivity";
	private TextView mBackTxt = null;
	private TextView mVerTxt = null;
	private AKeyReceiver receiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_asim);
		init();
	}

	@Override
	protected void onPause() {
		// 卸载广播接收器
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();

		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		// 好友请求
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);
		
		registerReceiver(receiver, filter);
		refresh();
	}

	private void init() {
		getEimApplication().addActivity(this);

		mBackTxt = (TextView) findViewById(R.id.title_back_btn);
		mBackTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mVerTxt = (TextView) findViewById(R.id.app_ver_txt);
		mVerTxt.setText("V" + getVersion());
		
		receiver = new AKeyReceiver();
	}

	private class AKeyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
		}
	}

	private void refresh() {
		refreshViewOnAUKeyStatusChange();
	}
	
	private void refreshViewOnAUKeyStatusChange() {
		View titleBar = findViewById(R.id.main_head);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			mBackTxt.setTextColor(getResources().getColor(R.color.white));
			mBackTxt.setBackgroundResource(R.drawable.title_clickable_background_black);
		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			mBackTxt.setTextColor(getResources().getColor(R.color.darkgray));
			mBackTxt.setBackgroundResource(R.drawable.title_clickable_background);
		}
			
	}
}
