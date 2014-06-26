package com.view.asim.activity;


import java.util.HashSet;
import java.util.Random;

import org.jivesoftware.smack.SmackAndroid;

import com.view.asim.comm.Constant;
import com.view.asim.dbg.CrashHandler;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.service.ConnectService;
import com.view.asim.task.LoginTask;
import com.view.asim.util.FileUtil;

import android.graphics.drawable.AnimationDrawable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.view.asim.R;

/**
 * 
 * Splash screen.
 * 
 * @author xuweinan
 */
public class SplashActivity extends ActivitySupport {
	private final static String TAG = "SplashActivity";
	private String to = null;

	private Button signUpBtn = null;
	private Button loginBtn = null;
	private Button leftBtn = null;
	private Button rightBtn = null;
	private LinearLayout screen = null;
	private View bottomLayout = null;
	private View errorOperLayout = null;
	private View normalOperLayout = null;
	private ImageView loginImg = null;
	private ImageView bottomBackgroundImg = null;
	private TextView errTxt = null;
	
	private int[] backgroundImg = {
			R.drawable.smartisan_lockscreen_6,
			R.drawable.smartisan_lockscreen_8,
			R.drawable.smartisan_lockscreen_10,
			R.drawable.smartisan_lockscreen_11,
			R.drawable.smartisan_lockscreen_12,
			R.drawable.background
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.splash);

		init();
	}

	/**
	 * 
	 * ��ʼ��.
	 * 
	 * @author allen
	 * @update 2014-4-8
	 */
	protected void init() {
		getEimApplication().addActivity(this);

		screen = (LinearLayout) findViewById(R.id.splashup_screen);
		screen.setBackgroundResource(backgroundImg[getRandom()]);
		
		bottomLayout = findViewById(R.id.bottom_navi_layout);
		errorOperLayout = findViewById(R.id.error_oper_layout);
		normalOperLayout = findViewById(R.id.normal_oper_layout);
		
		loginImg = (ImageView) findViewById(R.id.loading_img);
		bottomBackgroundImg = (ImageView) findViewById(R.id.bottom_background_img);
		errTxt = (TextView) findViewById(R.id.error_msg_txt);

		leftBtn = (Button) findViewById(R.id.left_btn);
		rightBtn = (Button) findViewById(R.id.right_btn);
		
		signUpBtn = (Button) findViewById(R.id.signup_btn);
		loginBtn = (Button) findViewById(R.id.login_btn);

		signUpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, SignUpActivity.class);
				startActivity(intent);
				finish();
			}
		});

		loginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// �������Ͱ汾
		if(checkInternet()) {

			// У��SD��
			if(checkMemoryCard()) {
				normalOperation();
			}
		}
	}
	
	private int getRandom(){  
        Random random = new Random();  
        return random.nextInt(6); 
    }
	
	/*
	private boolean checkMemoryCard() {
		final String sdcardPath = FileUtil.getSDCardRootDirectory();
		if (sdcardPath == null) {
			showErrorSDCardView();
			return false;
		}
		else {
			String oldPath = getDataRootPath();
			
			// ����ǵ�һ������ App�������õ� SD ��·��������������
			if (oldPath == null) {
				Log.d(TAG, "save sdcard path: " + sdcardPath);
				mLoginCfg.setRootPath(sdcardPath);
				saveLoginConfig(mLoginCfg);
				Constant.SDCARD_ROOT_PATH = sdcardPath;
			}
			else {
				Constant.SDCARD_ROOT_PATH = oldPath;

				if(oldPath.equals(sdcardPath)) {
					Log.d(TAG, "sdcard path " + sdcardPath + " has been saved.");
				}
				else {
					Log.d(TAG, "sdcard path " + sdcardPath + " has changed, the saved path is " + oldPath);
					// ����������з��� SD ��·����֮ǰ��¼�Ĳ�ͬ���ж�֮ǰ��·���Ƿ���ã���������ã��˳� App
					if(!FileUtil.checkPathValid(oldPath)) {
						showSDCardChangedView(sdcardPath);
						return false;
					}
				}
			}
		}
		return true;
	}
	*/
	
	private boolean checkMemoryCard() {
		if (Constant.SDCARD_ROOT_PATH == null) {
			showErrorSDCardView();
			return false;
		}
		else {
			String oldPath = getDataRootPath();
			
			/* ����ǵ�һ������ App�������õ� SD ��·�������������� */
			if (oldPath == null) {
				Log.d(TAG, "save sdcard path: " + Constant.SDCARD_ROOT_PATH);
				mLoginCfg.setRootPath(Constant.SDCARD_ROOT_PATH);
				saveLoginConfig(mLoginCfg);
			}
			else {
				if(oldPath.equals(Constant.SDCARD_ROOT_PATH)) {
					Log.d(TAG, "sdcard path " + Constant.SDCARD_ROOT_PATH + " has been saved.");
				}
				else {
					Log.d(TAG, "sdcard path " + Constant.SDCARD_ROOT_PATH + " has changed, the saved path is " + oldPath);
					/* ����������з��� SD ��·����֮ǰ��¼�Ĳ�ͬ���ж�֮ǰ��·���Ƿ���ã���������ã��˳� App */
					if(!FileUtil.checkPathValid(oldPath)) {
						showSDCardChangedView(Constant.SDCARD_ROOT_PATH);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean checkInternet() {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			showErrorInternetView();
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
		
		showErrorInternetView();
		return false;
	}
	
	private void showErrorInternetView() {
		errTxt.setText("û�п��õ���������");
		leftBtn.setText("�˳�");
		leftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eimApplication.exit();
			}
		});	
		
		rightBtn.setText("����");
		rightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(intent);
				finish();
			}
		});
		
		bottomBackgroundImg.setVisibility(View.VISIBLE);
		errorOperLayout.setVisibility(View.VISIBLE);
		bottomLayout.setVisibility(View.VISIBLE);
	}
	
	private void showErrorSDCardView() {
		errTxt.setText("û�п��õĴ洢��");
		leftBtn.setText("�˳�");
		leftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eimApplication.exit();
			}
		});	
		
		rightBtn.setText("����");
		rightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.ACTION_SETTINGS);
				startActivity(intent);
				finish();
			}
		});
		
		bottomBackgroundImg.setVisibility(View.VISIBLE);
		errorOperLayout.setVisibility(View.VISIBLE);
		bottomLayout.setVisibility(View.VISIBLE);
	}
	
	private void showSDCardChangedView(String newPath) {
		final String path = newPath;
		errTxt.setText("ԭ�洢������λ�������������õĴ洢��");
		leftBtn.setText("�˳�");
		leftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eimApplication.exit();
			}
		});	
		
		rightBtn.setText("ʹ���´洢��");
		rightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoginCfg.setRootPath(path);
				saveLoginConfig(mLoginCfg);
				Constant.SDCARD_ROOT_PATH = path;
				normalOperation();
			}
		});
		
		bottomBackgroundImg.setVisibility(View.VISIBLE);
		errorOperLayout.setVisibility(View.VISIBLE);
		bottomLayout.setVisibility(View.VISIBLE);
	}
	
	public void normalOperation() {
		
		LogcatHelper.getInstance(context).stop();
		LogcatHelper.getInstance(context).start();

		stopService();
		startService();
		
		bottomBackgroundImg.setVisibility(View.GONE);
		errorOperLayout.setVisibility(View.GONE);
		normalOperLayout.setVisibility(View.VISIBLE);
		bottomLayout.setVisibility(View.VISIBLE);

		// ����ѱ����û��������룬�Զ���¼
		if (mLoginCfg.getUsername() != null) {
			loginImg.setVisibility(View.VISIBLE);
			LoginTask loginTask = new LoginTask(SplashActivity.this, 
					mLoginCfg,
					(AnimationDrawable) loginImg.getBackground());
			loginTask.execute();
		} else {
			signUpBtn.setVisibility(View.VISIBLE);
			loginBtn.setVisibility(View.VISIBLE);
		}
		
	}
	
}
