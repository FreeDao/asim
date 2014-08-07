package com.view.asim.activity.im;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.LoginActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.task.SearchContactsTask;
import com.view.asim.task.SearchUserTask;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.ContactUsersAdapter;
import com.view.asim.view.NoticeAdapter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
 * 从通讯录添加密友界面
 * 
 * @author xuweinan
 */
public class AddUserContactsActivity extends ActivitySupport {
	private TextView mBackTxt = null;
	private ListView mFriendsList = null;
	private ContactUsersAdapter mUsersAdapter = null;
	private List<User> mContactUsers = new ArrayList<User>();
	private View noContactersLayout = null;
	private SearchContactsTask mTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_contact_friends);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);
		noContactersLayout = (LinearLayout) findViewById(R.id.no_other_contacters_layout);

		mBackTxt = (TextView) findViewById(R.id.title_back_btn);
		mBackTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mFriendsList = (ListView) findViewById(R.id.friends_list);
		mUsersAdapter = new ContactUsersAdapter(context, mContactUsers, addUserClick);
		mFriendsList.setAdapter(mUsersAdapter);
		//mFriendsList.setOnItemClickListener(inviteListClick);
		
		mTask = new SearchContactsTask(AddUserContactsActivity.this);
		mTask.execute();

	}

//	@Override
//	public void onBackPressed() {
//		if (mTask != null) {
//			mTask.stopProgress();
//		}
//		super.onBackPressed();
//	}
	
	private OnClickListener addUserClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			User user = (User)(v.getTag());
			try {
				ContacterManager.createSubscriber(user.getJID(), user.getNickName(), null);
			} catch (XMPPException xe) {
				xe.printStackTrace();
			}
			mContactUsers.remove(user);
			refresh();
		}
	};
	
	public void setContactUsers(List<User> contactUsers) {
		this.mContactUsers = contactUsers;
	}

	public void refresh() {
		
		if(mContactUsers.size() > 0) {
			mUsersAdapter.setUserList(mContactUsers);
			mUsersAdapter.notifyDataSetChanged();
			noContactersLayout.setVisibility(View.GONE);
			mFriendsList.setVisibility(View.VISIBLE);
		}
		else {
			noContactersLayout.setVisibility(View.VISIBLE);
			mFriendsList.setVisibility(View.GONE);
		}
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
