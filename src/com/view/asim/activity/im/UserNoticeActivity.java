package com.view.asim.activity.im;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.LoginActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.NoticeAdapter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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
 * 锟斤拷锟斤拷锟斤拷锟酵ㄖ�锟斤拷息锟斤拷示锟斤拷锟斤拷
 * 
 * @author xuweinan
 */
public class UserNoticeActivity extends ActivitySupport {
	protected static final String TAG = "UserNoticeActivity";
	private LinearLayout noNoticeLayout = null;
	private TextView mBackTxt = null;
	private ImageButton mClearBtn = null;
	private ListView noticeList = null;
	private NoticeAdapter noticeAdapter = null;
	private List<Notice> inviteNotices = new ArrayList<Notice>();
	private ContacterReceiver receiver = null;
	private NoticeManager noticeManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_notice);
		init();
	}

	@Override
	protected void onPause() {
		// 卸锟截广播锟斤拷锟斤拷锟斤拷
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();

		// 注锟斤拷悴ワ拷锟斤拷锟斤拷锟�
		IntentFilter filter = new IntentFilter();
		// 锟斤拷锟斤拷锟斤拷锟斤拷
		filter.addAction(Constant.ROSTER_SUBSCRIPTION);
		filter.addAction(Constant.ACTION_SYS_MSG);
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);
		
		registerReceiver(receiver, filter);
		NoticeManager.getInstance().clearRosterMessageNotify();
		refresh();
	}

	private void init() {
		getEimApplication().addActivity(this);

		noNoticeLayout = (LinearLayout) findViewById(R.id.no_notices_layout);
		
		mBackTxt = (TextView) findViewById(R.id.title_back_btn);
		mBackTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mClearBtn = (ImageButton) findViewById(R.id.clear_notify_btn);
		mClearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NoticeManager.getInstance().updateAllDispStatus(Notice.HIDE);
				refresh();
			}
		});
		
		receiver = new ContacterReceiver();
		noticeList = (ListView) findViewById(R.id.my_notice_list);
		noticeManager = NoticeManager.getInstance();
		noticeAdapter = new NoticeAdapter(context, inviteNotices, acceptClick);
		noticeList.setAdapter(noticeAdapter);

	}

	private class ContacterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//Notice notice = (Notice) intent.getParcelableExtra(Notice.noticeKey);
			//inviteNotices.add(notice);
			refresh();
		}
	}
	
	private OnClickListener acceptClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 锟斤拷锟斤拷锟斤拷锟斤拷
			if(v.getId() == R.id.accept_btn){
				Notice n = (Notice)v.getTag();
				Log.d(TAG, "accept the request of user " + n.getWith());
	
				ContacterManager.sendSubscribe(Presence.Type.subscribed, n.getWith());
				ContacterManager.sendSubscribe(Presence.Type.subscribe, n.getWith());
	
				NoticeManager noticeManager = NoticeManager
						.getInstance();
				noticeManager.updateStatusById(n.getId(), Notice.STATUS_COMPLETE);
				
				new ContacterUpdateThread(n.getWith()).start();
	
				refresh();		
			}
			//��ョ��璧����
			else if(v.getId() == R.id.new_user_name_txt||v.getId() == R.id.new_user_head_img){
				Notice n = (Notice)v.getTag();
				String jid = n.getWith();
				User user = ContacterManager.getUserInfoByJid(jid);		
				Intent intent = new Intent();
				if(n.getStatus().equals(Notice.STATUS_COMPLETE)) {
					intent.putExtra(User.userKey, ContacterManager.contacters.get(user.getJID()));
					intent.putExtra(UserInfoActivity.modeKey, UserInfoActivity.FRIEND_MODE);
				} else {
					intent.putExtra(User.userKey, user);
					intent.putExtra(UserInfoActivity.modeKey, UserInfoActivity.STRANDER_MODE);
				}
				
				intent.setClass(UserNoticeActivity.this, UserInfoActivity.class);
				startActivity(intent);				
				Log.i(TAG,"Notice :"+n.getContent());
			}
		}
	};

	private class ContacterUpdateThread extends Thread {
		private String user = null;
		
		public ContacterUpdateThread(String jid) {
			user = jid;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "ContacterUpdateThread");
			ContacterManager.loadAndUpdateContacter(user);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // resultCode为锟截达拷锟侥憋拷锟�
		case 1:
			refresh();
		default:
			break;
		}
	}

	private void refresh() {
		Log.d(TAG, "get all notices from db start on " + DateUtil.getCurDateStr());
		inviteNotices = noticeManager.getAllNoticeListByDispStatus(Notice.DISPLAY);
		Log.d(TAG, "get all notices from db end on " + DateUtil.getCurDateStr());

		updateReadStatus(inviteNotices);
		
		if(inviteNotices.size() > 0) {
			noticeAdapter.setNoticeList(inviteNotices);
			noticeAdapter.notifyDataSetChanged();
			noNoticeLayout.setVisibility(View.GONE);
			noticeList.setVisibility(View.VISIBLE);
		}
		else {
			noNoticeLayout.setVisibility(View.VISIBLE);
			noticeList.setVisibility(View.GONE);
		}
		
		refreshViewOnAUKeyStatusChange();
	}
	
	private void updateReadStatus(List<Notice> notices) {
		for (Notice n: notices) {
			if (n.getReadStatus().equals(Notice.UNREAD)) {
				NoticeManager.getInstance().updateReadStatusById(n.getId(), Notice.READ);
			}
		}
	}
	
	private void refreshViewOnAUKeyStatusChange() {
		View titleBar = findViewById(R.id.main_head);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			mBackTxt.setTextColor(getResources().getColor(R.color.white));
			mBackTxt.setBackgroundResource(R.drawable.title_clickable_background_black);
			mClearBtn.setBackgroundResource(R.drawable.title_clickable_background_black);
		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			mBackTxt.setTextColor(getResources().getColor(R.color.darkgray));
			mBackTxt.setBackgroundResource(R.drawable.title_clickable_background);
			mClearBtn.setBackgroundResource(R.drawable.title_clickable_background);
		}
			
	}
}
