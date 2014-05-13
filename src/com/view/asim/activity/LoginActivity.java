package com.view.asim.activity;


import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.task.LoginTask;
import com.view.asim.util.StringUtil;
import com.view.asim.util.ValidateUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.view.asim.R;

/**
 * 
 * 登录.
 * 
 * @author allen
 */
public class LoginActivity extends ActivitySupport {
	private EditText mUsernameText, mPasswdText;
	private Button mLoginCfmBtn = null;
	private ImageView loginImg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		init();
	}


	/**
	 * 
	 * 初始化.
	 * 
	 * @author xuweinan
	 * @update 2012-5-16 上午9:13:01
	 */
	protected void init() {
		getEimApplication().addActivity(this);

		mUsernameText = (EditText) findViewById(R.id.login_name_input);
		mPasswdText = (EditText) findViewById(R.id.login_password_input);
		mLoginCfmBtn = (Button) findViewById(R.id.login_cfm_btn);
		loginImg = (ImageView) findViewById(R.id.loading_img);

		// 初始化各组件的默认状态
		mUsernameText.setText(mLoginCfg.getUsername());
		mPasswdText.setText(mLoginCfg.getPassword());

		mLoginCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkCellphone() && checkData()) {
					String password = mPasswdText.getText().toString();
					String username = StringUtil.getNameByCellphone(mUsernameText.getText().toString());

					// 先记录下各组件的目前状态,登录成功后才保存
					mLoginCfg.setPassword(password);
					mLoginCfg.setUsername(username);

					loginImg.setVisibility(View.VISIBLE);
					LoginTask loginTask = new LoginTask(LoginActivity.this,
							mLoginCfg, (AnimationDrawable) loginImg.getBackground());
					loginTask.execute();
				}
			}
		});
	}

	/**
	 * 
	 * 登录校验.
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-5-16 上午9:12:37
	 */
	private boolean checkData() {
		boolean checked = false;
		checked = (!ValidateUtil.isEmpty(mUsernameText, "手机号码") && !ValidateUtil
				.isEmpty(mPasswdText, "密码"));
		return checked;
	}
	
	/**
	 * 
	 * 注册校验（手机号码）.
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2014-4-8
	 */
	private boolean checkCellphone() {
		return ValidateUtil.isMobileNumber(mUsernameText);
	}
	

	@Override
	public void onBackPressed() {
		isExit();
	}
}
