package com.view.asim.activity;


import java.util.Random;

import com.newrelic.agent.android.NewRelic;
import com.view.asim.comm.Constant;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.task.LoginTask;
import com.view.asim.utils.FileUtil;

import android.graphics.drawable.AnimationDrawable;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
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
    private long mExitTime = 0;   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.splash);

		init();
		
		NewRelic.withApplicationToken(
				"AAc7c172dbf5c033a5830953ce5e4fe10b75a4e595"
				).start(getEimApplication());
	}

	/**
	 * 
	 * 初始化.
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
		
		// 检测网络和版本
		if(checkInternet()) {

			// 校验SD卡
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
			
			// 如果是第一次运行 App，将可用的 SD 卡路径保存在配置中
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
					// 如果本次运行发现 SD 卡路径和之前记录的不同，判断之前的路径是否可用，如果不可用，退出 App
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
			String oldPath = AppConfigManager.getInstance().getDataRootPath();
			
			/* 如果是第一次运行 App，将可用的 SD 卡路径保存在配置中 */
			if (oldPath == null) {
				Log.d(TAG, "save sdcard path: " + Constant.SDCARD_ROOT_PATH);
				mLoginCfg.setRootPath(Constant.SDCARD_ROOT_PATH);
				AppConfigManager.getInstance().saveLoginConfig(mLoginCfg);
			}
			else {
				if(oldPath.equals(Constant.SDCARD_ROOT_PATH)) {
					Log.d(TAG, "sdcard path " + Constant.SDCARD_ROOT_PATH + " has been saved.");
				}
				else {
					Log.d(TAG, "sdcard path " + Constant.SDCARD_ROOT_PATH + " has changed, the saved path is " + oldPath);
					/* 如果本次运行发现 SD 卡路径和之前记录的不同，判断之前的路径是否可用，如果不可用，退出 App */
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
		errTxt.setText("没有可用的网络连接");
		leftBtn.setText("退出");
		leftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eimApplication.exit();
			}
		});	
		
		rightBtn.setText("设置");
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
		errTxt.setText("没有可用的存储卡");
		leftBtn.setText("退出");
		leftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eimApplication.exit();
			}
		});	
		
		rightBtn.setText("设置");
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
		errTxt.setText("原存储卡不在位，发现其他可用的存储卡");
		leftBtn.setText("退出");
		leftBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eimApplication.exit();
			}
		});	
		
		rightBtn.setText("使用新存储卡");
		rightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoginCfg.setRootPath(path);
				AppConfigManager.getInstance().saveLoginConfig(mLoginCfg);
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
		startConnService();
		
		bottomBackgroundImg.setVisibility(View.GONE);
		errorOperLayout.setVisibility(View.GONE);
		normalOperLayout.setVisibility(View.VISIBLE);
		bottomLayout.setVisibility(View.VISIBLE);

		// 如果已保存用户名和密码，自动登录
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
	
	/**
	 * 按键事件处理
	 */
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

	/**
	 * 2秒内联系按两次返回键退出APP
	 */
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            showToast("再按一次退出程序");
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
	
}
