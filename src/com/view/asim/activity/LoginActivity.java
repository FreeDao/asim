package com.view.asim.activity;


import org.jivesoftware.smack.SmackAndroid;

import com.view.asim.comm.ApplicationContext;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.task.LoginTask;
import com.view.asim.utils.StringUtil;
import com.view.asim.utils.ValidateUtil;

import com.view.asim.view.ClearEditText;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
 * ��¼.
 * 
 * @author allen
 */
public class LoginActivity extends ActivitySupport {
	private ClearEditText mUsernameText, mPasswdText;
	private Button mLoginCfmBtn = null;
	private ImageView loginImg = null;
    private long mExitTime = 0;   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		init();
	}


	/**
	 * 
	 * ��ʼ��.
	 * 
	 * @author xuweinan
	 */
	protected void init() {
		getEimApplication().addActivity(this);

		mUsernameText = (ClearEditText) findViewById(R.id.login_name_input);
		mPasswdText = (ClearEditText) findViewById(R.id.login_password_input);
		mLoginCfmBtn = (Button) findViewById(R.id.login_cfm_btn);
		loginImg = (ImageView) findViewById(R.id.loading_img);

		// ��ʼ���������Ĭ��״̬
		mUsernameText.setText(AppConfigManager.getInstance().getUsername());
		mPasswdText.setText(AppConfigManager.getInstance().getPassword());

		mLoginCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkCellphone() && checkData()) {
					String password = mPasswdText.getText().toString().trim();
					String username = StringUtil.getNameByCellphone(mUsernameText.getText().toString().trim());

					// �ȼ�¼�¸������Ŀǰ״̬,��¼�ɹ���ű���
					AppConfigManager.getInstance().setPassword(password);
					AppConfigManager.getInstance().setUsername(username);

					loginImg.setVisibility(View.VISIBLE);
					LoginTask loginTask = new LoginTask(LoginActivity.this, 
							(AnimationDrawable) loginImg.getBackground());
					loginTask.execute();
				}
			}
		});
	}
	
	@Override
	public boolean isLoginActivity() {
		// TODO Auto-generated method stub
		return true;
	}


	/**
	 * 
	 * ��¼У��.
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-5-16 ����9:12:37
	 */
	private boolean checkData() {
		boolean checked = false;
		checked = (!ValidateUtil.isEmpty(mUsernameText, getResources().getString(R.string.mobile_number)) && !ValidateUtil
				.isEmpty(mPasswdText,  getResources().getString(R.string.password)));
		return checked;
	}
	
	/**
	 * 
	 * ע��У�飨�ֻ����룩.
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
	
	/**
	 * Handle operation of pressing key
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
	 * Exit APP by clicking back key in 2 seconds continuously.
	 */
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            showToast( getResources().getString(R.string.exit_app_click_again));
            mExitTime = System.currentTimeMillis();
        } else {
        	getEimApplication().exit();
        }
    }
}
