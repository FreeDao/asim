package com.view.asim.activity;


import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;

import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.SMSVerifyManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.task.LoginTask;
import com.view.asim.task.SignUpTask;
import com.view.asim.utils.StringUtil;
import com.view.asim.utils.ValidateUtil;
import com.view.asim.worker.BaseHandler;
import com.view.asim.worker.CommonResultListener;
import com.view.asim.worker.ExpiryTimerListener;
import com.view.asim.worker.SmsCodeSendHandler;
import com.view.asim.worker.TimerHandler;
import com.view.asim.worker.Worker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.view.asim.R;

/**
 * 
 * 锟矫伙拷注锟斤拷锟斤拷锟�.
 * 
 * @author allen
 */
public class SignUpActivity extends ActivitySupport {
	private boolean showVerificationLayout = false;
	private final static String TAG = "SignUpActivity";
	
	private final static int SIGNUP_CONFLICT = -1;
	private final static int SIGNUP_SUCC = 0;
	private final static int SEND_SMSCODE_COMPLETE = 1;
	private final static int SEND_SMSCODE_FAILED = 2;
	private final static int RESEND_SMSCODE_OK = 3;
	private final static int REFRESH_SMSCODE_TIMER = 4;
	
	private final static int SMS_CODE_RESEND_DURATION = 60;
	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟截硷拷锟剿筹拷锟斤拷锟叫讹拷时锟斤拷
    private long mExitTime = 0;   
	private User newUser = null;
	private ImageView loginImg = null;

	private LinearLayout mCellphoneStepLayout = null;
	private LinearLayout mCodeVerifyStepLayout = null;
	private LinearLayout mUserInfoStepLayout = null;
	
	private Button mSignUpCfmBtn = null;
	private Button mResendBtn = null;
	private Button mVerifyBtn = null;
	private Button mSignUpCmpBtn = null;
	private EditText mCellphoneText = null;
	private EditText mVerifyCodeText = null;
	private EditText mNickNameText = null;
	private EditText mPasswordText = null;
	private TextView mSmsSendTipsTxt = null;
	private RadioGroup mGenderRadioGrp = null;
	private RadioButton mMaleRadio = null;
	private RadioButton mFemaleRadio = null;
	
	private String mGender = "";
	
	private Worker mSmsCodeSendWorker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.signup);
		
		init();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(showVerificationLayout){
			notifySmsCodeSent();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSmsCodeSendWorker.destroy();
		showVerificationLayout = false;
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("showVerificationLayout", showVerificationLayout);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		showVerificationLayout = savedInstanceState.getBoolean("showVerificationLayout");
		Log.i(TAG, TAG +"onRestoreInstanceState"+" showVerificationLayout: "+showVerificationLayout);
	}
	
	protected void init() {
		getEimApplication().addActivity(this);

		loginImg = (ImageView) findViewById(R.id.loading_img);

		mCellphoneStepLayout = (LinearLayout) findViewById(R.id.signup_cellphone_layout);
		mCodeVerifyStepLayout = (LinearLayout) findViewById(R.id.signup_verifycode_layout);
		mUserInfoStepLayout = (LinearLayout) findViewById(R.id.signup_userinfo_layout);

		mSignUpCfmBtn = (Button) findViewById(R.id.signup_cfm_btn);
		mSignUpCmpBtn = (Button) findViewById(R.id.signup_cmp_btn);
		
		mResendBtn = (Button) findViewById(R.id.resend_btn);
		mVerifyBtn = (Button) findViewById(R.id.verify_btn);

		mCellphoneText = (EditText) findViewById(R.id.signup_cellphone_input);
		mVerifyCodeText = (EditText) findViewById(R.id.verify_code_input);
		mSmsSendTipsTxt = (TextView) findViewById(R.id.verify_code_tips_txt);

		mNickNameText = (EditText) findViewById(R.id.signup_name_input);
		mNickNameText.setRawInputType(InputType.TYPE_CLASS_TEXT); 
		
		mPasswordText = (EditText) findViewById(R.id.signup_password_input);
		mPasswordText.setRawInputType(InputType.TYPE_CLASS_TEXT); 
		
		mGenderRadioGrp = (RadioGroup) findViewById(R.id.gender_radio_grp);
	    mMaleRadio = (RadioButton) findViewById(R.id.gender_radio_male);
	    mFemaleRadio = (RadioButton) findViewById(R.id.gender_radio_female); 
	    
	    mGenderRadioGrp.setOnCheckedChangeListener(new 
    		RadioGroup.OnCheckedChangeListener() { 
				@Override 
				public void onCheckedChanged(RadioGroup group, int checkedId) { 
					if (checkedId == mMaleRadio.getId()) {
						mGender = User.MALE;
					}
					else if (checkedId == mFemaleRadio.getId()) {
						mGender = User.FEMALE;
					}
				}
    		}
	    ); 
	    
	    mSignUpCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(checkCellphone()) {
					showSendVerificationCodeDialog(mCellphoneText.getText().toString().trim());
				}
				
			}
		});
	    
	    mResendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(checkCellphone()) {
					sendSmsVerificationCode(mCellphoneText.getText().toString().trim());
				}
				
			}
		});
	    
	    mVerifyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "verify: " + mCellphoneText.getText().toString().trim() + ", " +
						mVerifyCodeText.getText().toString().trim());
				if(checkVerificationCode()) {
					if (SMSVerifyManager.getInstance().verification(mCellphoneText.getText().toString().trim(), 
							mVerifyCodeText.getText().toString().trim())) {
						mCellphoneStepLayout.setVisibility(View.GONE);
						mCodeVerifyStepLayout.setVisibility(View.GONE);
						mUserInfoStepLayout.setVisibility(View.VISIBLE);
					}
					else {
		    			showToast(getResources().getString(R.string.varified_password_error));
					}
				}
				
			}
		});
	    
		
	    mSignUpCmpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkUserInfo()) {
					String nickname = mNickNameText.getText().toString().trim();
					String password = mPasswordText.getText().toString().trim();
					String username = StringUtil.getNameByCellphone(mCellphoneText.getText().toString().trim());
					
					newUser = new User();
					newUser.setName(username);
					//newUser.setJID(StringUtil.getJidByName(username, mLoginCfg.getXmppServiceName()));
					newUser.setGender(mGender);
					newUser.setNickName(nickname);
					
					AppConfigManager.getInstance().setUsername(username);
					AppConfigManager.getInstance().setPassword(password);
					
					Log.d(TAG, "start signup task");
					SignUpTask task = new SignUpTask(SignUpActivity.this);
					task.initUser(newUser);
					task.execute();
				}
			}
		});
	    
	    mSmsCodeSendWorker = new Worker();
	    mSmsCodeSendWorker.initilize("SMS Code Sender");

	}
	
	private void showSendVerificationCodeDialog(final String cellphone) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.send_sms_code_tips))
				.setMessage(cellphone)
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								sendSmsVerificationCode(cellphone);
							}
						})
				.setNegativeButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what) {
        	case SIGNUP_SUCC:
    			showToast(getResources().getString(R.string.register_success));

            	// 注锟斤拷晒锟斤拷锟街憋拷拥锟铰�
    			LoginTask task = new LoginTask(SignUpActivity.this, (AnimationDrawable) loginImg.getBackground());
    			task.initUser(newUser);
    			task.execute();
            	
                super.handleMessage(msg);
        		break;
        		
        	case SIGNUP_CONFLICT:
    			showToast(getResources().getString(R.string.phone_number_had_register));
				mUserInfoStepLayout.setVisibility(View.GONE);
    			mCellphoneStepLayout.setVisibility(View.VISIBLE);
        			
        		break;
        		
        	case SEND_SMSCODE_COMPLETE:
				showToast(getResources().getString(R.string.sms_verification_code_send_success));
				showVerificationLayout(mCellphoneText.getText().toString().trim());
				break;
				
        	case SEND_SMSCODE_FAILED:
        		showToast(getResources().getString(R.string.sms_verification_code_send_failure));
        		break;
				
        	case RESEND_SMSCODE_OK:
				allowResendSmsCode();
				break;
				
        	case REFRESH_SMSCODE_TIMER:
            	mResendBtn.setText(msg.arg1 + getResources().getString(R.string.x_seconds_send_again));
        	}

        }
    };
    
    public void sendSmsVerificationCode(String cellphone) {
		BaseHandler smsSendHandler = new SmsCodeSendHandler(cellphone, new CommonResultListener() {
			@Override
			public void onResult(boolean result) {
				if(result) {
					notifySmsCodeSent();
				}
				else {
					//showToast("锟斤拷锟斤拷锟斤拷证锟诫发锟斤拷失锟杰ｏ拷锟斤拷锟皆等猴拷锟斤拷锟斤拷");
					notifySmsCodeSentFailed();
				}
			}
			
		});
		mSmsCodeSendWorker.addHandler(smsSendHandler);

    }
    
    private void showVerificationLayout(String cellphone) {
    	showVerificationLayout = true;
		mCellphoneStepLayout.setVisibility(View.GONE);
		mUserInfoStepLayout.setVisibility(View.GONE);

		mSmsSendTipsTxt.setText(getResources().getString(R.string.sms_verification_code_had_send_to) + cellphone);
		mResendBtn.setBackgroundResource(R.drawable.green_button_frame_pressed);
		mResendBtn.setClickable(false);
		BaseHandler timerHandler = new TimerHandler(SMS_CODE_RESEND_DURATION, new ExpiryTimerListener() {
			@Override
			public void onTick(int sec) {
				notifyRefreshResendTimer(sec);
			}

			@Override
			public void onEnd() {
				notifyCanResendSmsCode();
			}

			@Override
			public void onCancel() {
				
			}
		});
		mSmsCodeSendWorker.addHandler(timerHandler);

		mCodeVerifyStepLayout.setVisibility(View.VISIBLE);
    }
    
    private void allowResendSmsCode() {
    	mResendBtn.setText(getResources().getString(R.string.resend_btn));
		mResendBtn.setBackgroundResource(R.drawable.green_button);
		mResendBtn.setClickable(true);

    }
    
    public void notifySignUpSucc() {
    	handler.sendEmptyMessage(SIGNUP_SUCC);
    }
    
    public void notifyUserIsExist() {
    	handler.sendEmptyMessage(SIGNUP_CONFLICT);
    }
    
    public void notifySmsCodeSent() {
    	handler.sendEmptyMessage(SEND_SMSCODE_COMPLETE);
    }
    
    public void notifySmsCodeSentFailed() {
    	handler.sendEmptyMessage(SEND_SMSCODE_FAILED);
    }
    
    public void notifyCanResendSmsCode() {
    	handler.sendEmptyMessage(RESEND_SMSCODE_OK);
    }
    
    public void notifyRefreshResendTimer(int sec) {
    	handler.sendMessage(handler.obtainMessage(REFRESH_SMSCODE_TIMER, sec, 0));
    }
    
	/**
	 * 
	 * 注锟斤拷校锟介（锟街伙拷锟斤拷锟诫）.
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2014-4-8
	 */
	private boolean checkCellphone() {
		return ValidateUtil.isMobileNumber(mCellphoneText);
	}
	
	/**
	 * 
	 * 注锟斤拷校锟介（锟斤拷锟斤拷锟斤拷证锟诫）.
	 * 
	 * @return
	 * @author xuweinan
	 */
	private boolean checkVerificationCode() {
		return !ValidateUtil.isEmpty(mVerifyCodeText, getResources().getString(R.string.verification_code));
	}
	
	/**
	 * 
	 * 注锟斤拷校锟介（锟角称猴拷锟斤拷锟诫）.
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2014-4-8
	 */
	private boolean checkUserInfo() {
		boolean checked = false;
		checked = (!ValidateUtil.isEmpty(mNickNameText,getResources().getString(R.string.nickname)) && !ValidateUtil
				.isEmpty(mPasswordText, getResources().getString(R.string.password)));
		return checked;
	}
	
	/**
	 * 锟斤拷锟斤拷锟铰硷拷锟斤拷锟斤拷
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
	 * 2锟斤拷锟斤拷锟斤拷系锟斤拷锟斤拷锟轿凤拷锟截硷拷锟剿筹拷APP
	 */
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            showToast( getResources().getString(R.string.exit_app_click_again));
            mExitTime = System.currentTimeMillis();
        } else {
            /*finish();
            System.exit(0);
            */
        	getEimApplication().exit();
        }
    }
	@Override
	public boolean isSignUpActivity() {
		// TODO Auto-generated method stub
		return true;
	}

}
