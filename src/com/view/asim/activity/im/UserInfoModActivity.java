package com.view.asim.activity.im;



import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.task.LoginTask;
import com.view.asim.task.SignUpTask;
import com.view.asim.utils.StringUtil;
import com.view.asim.utils.ValidateUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 用户信息修改界面.
 * 
 * @author xuweinan
 */
public class UserInfoModActivity extends ActivitySupport {
	
	private final static String TAG = "UserInfoModActivity";
	public final static String MOD_TYPE_KEY = "mod_type";
	public final static String REMARK = "remark";
	public final static String LOCATION = "location";
	public final static String NICKNAME = "nickname";
	public final static String GENDER = "gender";

	
	private int mMode = 0;
	
	private String mOldNickname = null;
	private String mOldLocation = null;
	private String mOldRemark = null;
	private String mOldGender = null;
	
	private LinearLayout mModNickLayout = null;
	private LinearLayout mModLocLayout = null;
	private LinearLayout mModRemarkLayout = null;
	
	private TextView mTitleTxt = null;
	
	private Button mRemarkCfmBtn = null;
	private Button mLocCfmBtn = null;
	private Button mNickCfmBtn = null;
	
	private EditText mLocationTxt = null;
	private EditText mNickNameTxt = null;
	private EditText mRemarkTxt = null;
	
	private RadioGroup mGenderRadioGrp = null;
	private RadioButton mMaleRadio = null;
	private RadioButton mFemaleRadio = null;
	
	private String mGender = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.user_info_modify);
		
		init();
	}
	
	protected void init() {
		getEimApplication().addActivity(this);

		mTitleTxt = (TextView) findViewById(R.id.title_back_btn);
		
		mModNickLayout = (LinearLayout) findViewById(R.id.mod_nick_layout);
		mModLocLayout = (LinearLayout) findViewById(R.id.mod_location_layout);
		mModRemarkLayout = (LinearLayout) findViewById(R.id.mod_remark_layout);

		mRemarkCfmBtn = (Button) findViewById(R.id.remark_cfm_btn);
		mLocCfmBtn = (Button) findViewById(R.id.location_cfm_btn);
		mNickCfmBtn = (Button) findViewById(R.id.nickname_cfm_btn);

		mLocationTxt = (EditText) findViewById(R.id.location_input);
		mNickNameTxt = (EditText) findViewById(R.id.nickname_input);
		mRemarkTxt = (EditText) findViewById(R.id.remark_input);
		
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
	    
	    mRemarkCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(REMARK, mRemarkTxt.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
			}
		});
		
	    mLocCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(LOCATION, mLocationTxt.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
			}
		});
	    
	    mNickCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkNickname()) {
	                Intent intent = new Intent();
	                intent.putExtra(NICKNAME, mNickNameTxt.getText().toString());
	                intent.putExtra(GENDER, mGender);
	                
	                setResult(RESULT_OK, intent);
	                finish();
				}
			}
		});
	    
	    initViews();

	}

	private void initViews() {
		mMode = getIntent().getIntExtra(MOD_TYPE_KEY, 0);
		if (mMode == Constant.REQCODE_MOD_REMARK) {
			mOldRemark = getIntent().getStringExtra(REMARK);
			mRemarkTxt.setText(mOldRemark);
			mTitleTxt.setText(getResources().getString(R.string.my_info_title_remark));
			mTitleTxt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
	                Intent intent = new Intent();
	                intent.putExtra(REMARK, mRemarkTxt.getText().toString());
	                setResult(RESULT_OK, intent);
	                finish();
				}
			});
			
			mModRemarkLayout.setVisibility(View.VISIBLE);
			mModLocLayout.setVisibility(View.GONE);
			mModNickLayout.setVisibility(View.GONE);
			
		} else if (mMode == Constant.REQCODE_MOD_LOCATION) {
			mOldLocation = getIntent().getStringExtra(LOCATION);
			mLocationTxt.setText(mOldLocation);
			mTitleTxt.setText(getResources().getString(R.string.my_info_title_loc));
			mTitleTxt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
	                Intent intent = new Intent();
	                intent.putExtra(LOCATION, mLocationTxt.getText().toString());
	                setResult(RESULT_OK, intent);
	                finish();
				}
			});
			
			mModRemarkLayout.setVisibility(View.GONE);
			mModLocLayout.setVisibility(View.VISIBLE);
			mModNickLayout.setVisibility(View.GONE);

		} else {
			mOldNickname = getIntent().getStringExtra(NICKNAME);
			mOldGender = getIntent().getStringExtra(GENDER);
			mTitleTxt.setText(getResources().getString(R.string.my_info_title_nick));
			mTitleTxt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
	                Intent intent = new Intent();
	                intent.putExtra(NICKNAME, mNickNameTxt.getText().toString());
	                intent.putExtra(GENDER, mGender);
	                setResult(RESULT_OK, intent);
	                finish();
				}
			});
			
			mNickNameTxt.setText(mOldNickname);
			
			if(mOldGender.equals(User.MALE)) {
				mGenderRadioGrp.check(mMaleRadio.getId());
			} else if (mOldGender.equals(User.FEMALE)) {
				mGenderRadioGrp.check(mFemaleRadio.getId());
			}
			
			mModRemarkLayout.setVisibility(View.GONE);
			mModLocLayout.setVisibility(View.GONE);
			mModNickLayout.setVisibility(View.VISIBLE);

		}
	}
	
	/*
	@Override
	public void onBackPressed() {
        Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
	}
	*/

	/**
	 * 
	 * 校验（昵称）.
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2014-4-8
	 */
	private boolean checkNickname() {
		boolean checked = false;
		checked = !ValidateUtil.isEmpty(mNickNameTxt, "昵称");
		return checked;
	}
	
	@Override 
    public void onResume() {
    	super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);

		registerReceiver(receiver, filter);
		refreshViewOnAUKeyStatusChange();
    }
	
    @Override
    public void onPause() {
    	super.onPause();
		unregisterReceiver(receiver);
    }
	
    private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Constant.AUKEY_STATUS_UPDATE.equals(action)) {
				
				refreshViewOnAUKeyStatusChange();
			}
		}
    };
    
	private void refreshViewOnAUKeyStatusChange() {
		View titleBar = findViewById(R.id.main_head);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			mTitleTxt.setTextColor(getResources().getColor(R.color.white));
			mTitleTxt.setBackgroundResource(R.drawable.title_clickable_background_black);
		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			mTitleTxt.setTextColor(getResources().getColor(R.color.darkgray));
			mTitleTxt.setBackgroundResource(R.drawable.title_clickable_background);
		}
			
	}

}
