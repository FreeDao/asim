package com.view.asim.activity.im;

import java.io.InputStream;

import org.jivesoftware.smack.XMPPException;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.StringUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
				
				new AlertDialog.Builder(context).setTitle("退出登录")
				.setNeutralButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mLoginCfg.setUsername(null);
						mLoginCfg.setPassword(null);
						mLoginCfg.setOnline(false);
						saveLoginConfig(mLoginCfg);

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
		});
		

		
	}

}
