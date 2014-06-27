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

import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.task.LoginTask;
import com.view.asim.task.SignUpTask;
import com.view.asim.util.StringUtil;
import com.view.asim.util.ValidateUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
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
import com.view.asim.R;

/**
 * 
 * �û�ע�����.
 * 
 * @author allen
 */
public class SignUpActivity extends ActivitySupport {
	
	private final static String TAG = "SignUpActivity";
	
	private String to = null;
	private User newUser = null;
	private ImageView loginImg = null;

	private LinearLayout mCellphoneStepLayout = null;
	private LinearLayout mUserInfoStepLayout = null;
	
	private Button mSignUpCfmBtn = null;
	private Button mSignUpCmpBtn = null;
	private EditText mCellphoneText = null;
	private EditText mNickNameText = null;
	private EditText mPasswordText = null;
	private RadioGroup mGenderRadioGrp = null;
	private RadioButton mMaleRadio = null;
	private RadioButton mFemaleRadio = null;
	
	private String mGender = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.signup);
		
		init();
	}
	
	protected void init() {
		getEimApplication().addActivity(this);

		loginImg = (ImageView) findViewById(R.id.loading_img);

		mCellphoneStepLayout = (LinearLayout) findViewById(R.id.signup_cellphone_layout);
		mUserInfoStepLayout = (LinearLayout) findViewById(R.id.signup_userinfo_layout);

		mSignUpCfmBtn = (Button) findViewById(R.id.signup_cfm_btn);
		mSignUpCmpBtn = (Button) findViewById(R.id.signup_cmp_btn);

		mCellphoneText = (EditText) findViewById(R.id.signup_cellphone_input);
		
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
					mCellphoneStepLayout.setVisibility(View.GONE);
					mUserInfoStepLayout.setVisibility(View.VISIBLE);
					//new QueryUserExistThread(mCellphoneText.getText().toString()).start();
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
					newUser.setJID(StringUtil.getJidByName(username, mLoginCfg.getXmppServiceName()));
					newUser.setGender(mGender);
					newUser.setNickName(nickname);
					
					// �ȼ�¼�¸������Ŀǰ״̬,��¼�ɹ���ű���
					mLoginCfg.setUsername(username);
					mLoginCfg.setPassword(password);

					Log.d(TAG, "start signup task");
					SignUpTask task = new SignUpTask(SignUpActivity.this,
							mLoginCfg);
					task.initUser(newUser);
					task.execute();
				}
			}
		});

	}
	
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what) {
        	case 0:
    			showToast("ע��ɹ�");

            	// ע��ɹ���ֱ�ӵ�¼
    			LoginTask task = new LoginTask(SignUpActivity.this,
    					mLoginCfg, (AnimationDrawable) loginImg.getBackground());
    			task.initUser(newUser);
    			task.execute();
            	
                super.handleMessage(msg);
        		break;
        		
        	case 1:
    			showToast("���ֻ�������ע�����ţ���ֱ�ӵ�¼������ע����������");
				mUserInfoStepLayout.setVisibility(View.GONE);
    			mCellphoneStepLayout.setVisibility(View.VISIBLE);
        			
        		break;
        	}

        }
    };
    
    public void notifySignUpSucc() {
    	handler.sendEmptyMessage(0);
    }
    
    public void notifyUserIsExist() {
    	handler.sendEmptyMessage(1);
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
		return ValidateUtil.isMobileNumber(mCellphoneText);
	}
	
	/**
	 * 
	 * ע��У�飨�ǳƺ����룩.
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2014-4-8
	 */
	private boolean checkUserInfo() {
		boolean checked = false;
		checked = (!ValidateUtil.isEmpty(mNickNameText, "�ǳ�") && !ValidateUtil
				.isEmpty(mPasswordText, "����"));
		return checked;
	}

}
