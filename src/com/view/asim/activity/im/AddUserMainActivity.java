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
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.task.LoginTask;
import com.view.asim.task.SearchUserTask;
import com.view.asim.task.SignUpTask;
import com.view.asim.util.StringUtil;
import com.view.asim.util.ValidateUtil;

import android.content.Context;
import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.view.asim.R;

/**
 * 
 * 用户注册界面.
 * 
 * @author allen
 */
public class AddUserMainActivity extends ActivitySupport {
	
	private final static String TAG = "AddUserMainActivity";
	
	private String to = null;
	
	private Button mSearchCfmBtn = null;
	private EditText mSearchText = null;
	private TextView mBackTxtBtn;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.add_friends_main);
		init();
	}
	
	protected void init() {
		getEimApplication().addActivity(this);

		mBackTxtBtn = (TextView) findViewById(R.id.title_back_btn);
		mBackTxtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mSearchCfmBtn = (Button) findViewById(R.id.search_cfm_btn);
		mSearchText = (EditText) findViewById(R.id.search_user_input);
		
		mSearchCfmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkData()) {
					String input = mSearchText.getText().toString();
					if (input.equals(StringUtil.getCellphoneByName(ContacterManager.userMe.getName()))) {
						showToast("搜索自己，太无聊啦 :)");
						mSearchText.setText("");
					}
					else {
						SearchUserTask task = new SearchUserTask(AddUserMainActivity.this,
								mSearchText.getText().toString().trim());
						task.execute();
					}
				}

			}
		});
		
		findViewById(R.id.add_user_by_contacts_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, AddUserContactsActivity.class);
				startActivity(intent);
				finish();
			}
		});

	}
	
    
	/**
	 * 
	 * 登录校验.
	 * 
	 * @return
	 * @author allen
	 * @update 2014-4-8
	 */
	private boolean checkData() {
		boolean checked = false;
		checked = (!ValidateUtil.isEmpty(mSearchText, "搜索内容"));
		return checked;
	}

}
