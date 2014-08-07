package com.view.asim.activity.sip;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.sip.api.ISipService;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.im.ChatActivity;
import com.view.asim.activity.im.UserAvatarActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.CallLogManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.CallLogs;
import com.view.asim.model.ChatHisBean;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.SingleCallLog;
import com.view.asim.model.User;
import com.view.asim.sip.api.SipManager;
import com.view.asim.sip.api.SipProfileState;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.CallLogAdapter;
import com.view.asim.view.CallLogsAdapter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.view.asim.R;

/**
 * 
 * 用户通话记录查看.
 * 
 * @author xuweinan
 */
public class UserCallLogsActivity extends ActivitySupport {
	public static final String TAG = "UserCallLogsActivity";
	
	private User mUser = null;
	private TextView mBackTxtBtn;
	private ImageView mAvatarImg;
	private TextView mNicknameTxt;
	private ListView mCalllogListView;
	private List<SingleCallLog> mCalllogList = new ArrayList<SingleCallLog>();
	private CallLogAdapter mCalllogsAdapter;

	private Button mChatBtn;
	private Button mVoiceBtn;
    protected ISipService service;
    protected boolean mServiceConnected = false;

    protected ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
        	Log.i(TAG, "connect sip service OK");
            service = ISipService.Stub.asInterface(arg1);
            mServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.user_calllogs_info);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);

		mUser = (User)getIntent().getParcelableExtra(User.userKey);
		Log.i(TAG, "show call logs view from user:" + mUser);

		mBackTxtBtn = (TextView) findViewById(R.id.title_back_btn);
		mBackTxtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mAvatarImg = (ImageView) findViewById(R.id.user_avatar_img);
		mNicknameTxt = (TextView) findViewById(R.id.user_nickname_txt);
		mChatBtn = (Button) findViewById(R.id.chat_btn);
		mVoiceBtn = (Button) findViewById(R.id.voice_btn);
		
		mChatBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createChat(mUser.getJID());
			}
		});
		
		mVoiceBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				makeCall(mUser.getName());
			}
		});

		
		mAvatarImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(User.userKey, mUser);
				intent.setClass(context, UserAvatarActivity.class);
				startActivity(intent);
			}
		});
		
		setUserInfoView();
		
		mCalllogListView = (ListView) findViewById(R.id.calllogs_list);

		mCalllogsAdapter = new CallLogAdapter(context, mCalllogList);
		mCalllogListView.setAdapter(mCalllogsAdapter);
		mCalllogListView.setOnCreateContextMenuListener(mCalllogsOnCreateContextMenuListener);  
		
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);
        
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);
		filter.addAction(Constant.ROSTER_PRESENCE_CHANGED);
		filter.addAction(Constant.ROSTER_UPDATED);

		registerReceiver(receiver, filter);
	}
	
	private final OnCreateContextMenuListener mCalllogsOnCreateContextMenuListener = new OnCreateContextMenuListener() {  
        @Override  
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;  

			SingleCallLog log = (SingleCallLog) mi.targetView.findViewById(R.id.call_info).getTag();
        	
        	menu.setHeaderTitle(ContacterManager.contacters.get(log.getWith()).getNickName());
        	menu.add(1, 1, 0, "删除通话记录");
        }  
    };
    
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
	        // 得到当前被选中的item信息  
	        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();  
	        
        	if (item.getGroupId() == 1) {
        		SingleCallLog b = (SingleCallLog) menuInfo.targetView.findViewById(R.id.call_info).getTag();

        		switch(item.getItemId()) {  
		        case 1:  
		        	CallLogManager.getInstance().delCallLogById(b.getId());
		        	refreshCalllogListView();
		        	break;  
	
		        default:  
		            return super.onContextItemSelected(item);  
		        }
        	}
	        return true;
    }
    
    @Override 
    public void onResume() {
    	super.onResume();
    	refreshCalllogListView();
    	refreshUserSecurityStatus();
    }
	
    @Override
    public void onPause() {
    	super.onPause();
    }
	
    private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(Constant.ROSTER_PRESENCE_CHANGED) || action.equals(Constant.ROSTER_UPDATED)) {
				User u = intent.getParcelableExtra(User.userKey);
				if (u.getName().equals(mUser.getName())) {
					mUser = u;
					Log.i(TAG, "update user info: " + u.getName());
				}
				else {
					Log.i(TAG, "update other user info: " + u.getName());
				}
			}
	    	refreshCalllogListView();
	    	refreshUserSecurityStatus();
		}
    };
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		unbindService(connection);
	}
	
    private void refreshUserSecurityStatus() {
    	Drawable leftIcon = null;
    	
    	Drawable[] icons = mNicknameTxt.getCompoundDrawables();
		if (mUser.getSecurity()!= null && mUser.getSecurity().equals(AUKeyManager.ATTACHED)) {
			leftIcon = context.getResources().getDrawable(R.drawable.notificationbar_icon_logo_normal);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
		}
		
		mNicknameTxt.setCompoundDrawables(leftIcon, null, icons[2], null);
    }
	
	// 刷新通话记录列表子界面
	protected void refreshCalllogListView() {
		if (CallLogManager.getInstance() == null) {
			Log.w(TAG, "CallLogManager.getInstance() null");
			return;
		}
		
		mCalllogList.clear();
		mCalllogList = CallLogManager.getInstance().getCalllogsByName(mUser.getName(), 1, 500);
		Collections.sort(mCalllogList);
		mCalllogsAdapter.refreshList(mCalllogList);
		
		refreshViewOnAUKeyStatusChange();
	}
	
	private void refreshViewOnAUKeyStatusChange() {
		View titleBar = findViewById(R.id.main_head);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			mBackTxtBtn.setTextColor(getResources().getColor(R.color.white));
			mBackTxtBtn.setBackgroundResource(R.drawable.title_clickable_background_black);
		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			mBackTxtBtn.setTextColor(getResources().getColor(R.color.darkgray));
			mBackTxtBtn.setBackgroundResource(R.drawable.title_clickable_background);
		}
			
	}

	
	/**
	 * 创建一个一对一聊天
	 * 
	 * @param user
	 */
	protected void createChat(String with) {
		if (ContacterManager.contacters.containsKey(with)) {
			Intent intent = new Intent(context, ChatActivity.class);
			intent.putExtra(IMMessage.PROP_CHATTYPE, IMMessage.SINGLE);
			intent.putExtra(User.userKey, ContacterManager.contacters.get(with));
			startActivity(intent);
		}
		else if (ContacterManager.groupUsers.containsKey(with)) {
			Intent intent = new Intent(context, ChatActivity.class);
			intent.putExtra(IMMessage.PROP_CHATTYPE, IMMessage.GROUP);
			intent.putExtra(User.userKey, ContacterManager.groupUsers.get(with));
			startActivity(intent);
		}
		else {
			Log.e(TAG, "create chat with unknow name " + with);
		}
	}

	protected void showDeleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.delete_user_confim))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								try {
									ContacterManager.removeSubscriber(mUser.getJID());
								} catch (XMPPException e) {
									Log.e(TAG, "", e);
								}

								NoticeManager.getInstance().setCurrentChatUser(null);
								NoticeManager.getInstance().clearIMMessageNotify(mUser);

								// 删除数据库
								NoticeManager.getInstance()
										.delNoticeByWith(mUser.getJID());
								MessageManager.getInstance()
										.delChatHisByName(mUser.getJID());
								finish();

							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}
	
	protected void setUserInfoView() {
		if (mUser.getNickName() == null) {
			mNicknameTxt.setVisibility(View.GONE);
		} else {
			mNicknameTxt.setText(mUser.getNickName());
		}
		
		if (mUser.getGender() == null) {
			mNicknameTxt.setCompoundDrawables(null, null, null, null);
			
		} else {
			Drawable maleIcon = getResources().getDrawable(R.drawable.business_card_male_icon);
			maleIcon.setBounds(0, 0, maleIcon.getMinimumWidth(), maleIcon.getMinimumHeight());
			Drawable femaleIcon = getResources().getDrawable(R.drawable.business_card_female_icon);
			femaleIcon.setBounds(0, 0, femaleIcon.getMinimumWidth(), femaleIcon.getMinimumHeight());
			if (mUser.getGender().equals(User.MALE)) {
				
				mNicknameTxt.setCompoundDrawables(null, null, maleIcon, null);
			}
			else {
				mNicknameTxt.setCompoundDrawables(null, null, femaleIcon, null);
			}
		}
		
		if (mUser.getHeadImg() != null) {
			mAvatarImg.setImageBitmap(mUser.getHeadImg());
		} else {
			if (mUser.getGender() == null) {
				mAvatarImg.setImageResource(R.drawable.default_avatar_male);
				
			} else {
				if (mUser.getGender().equals(User.MALE)) {
					mAvatarImg.setImageResource(R.drawable.default_avatar_male);
				}
				else {
					mAvatarImg.setImageResource(R.drawable.default_avatar_female);
				}
			}
		}
				
	}
	
	private void makeCall(String name) {
		if (!mServiceConnected || service == null) {
    		showToast("网络正忙，请稍后拨打语音电话。");
    		return;
		}
		
        try {
        	SipProfileState state = service.getSipProfileState((int)ContacterManager.userMe.getSipAccountId());
        	if (state == null || !state.isValidForCall()) {
        		showToast("网络状态异常，无法进行语音呼叫。");
        		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
        		Log.w(TAG, "network status may be unavailable, cannot make call. (XMPP connection: " + 
        			conn + ", status: " + (conn != null ? conn.isConnected() : null) + 
        			", auth: " + (conn != null ? conn.isAuthenticated() : null) +
        			", sip status: " + (state != null ? state.isValidForCall() : null) + ")");
        		return;
        	}
        	Log.i(TAG, "my sip state: " + state.isValidForCall());
			service.makeCall(StringUtil.getCellphoneByName(name), (int) ContacterManager.userMe.getSipAccountId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
