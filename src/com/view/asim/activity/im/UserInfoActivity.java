package com.view.asim.activity.im;


import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.sip.api.ISipService;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.CallLogManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.sip.api.SipManager;
import com.view.asim.sip.api.SipProfileState;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.StringUtil;

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
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 用户资料查看.
 * 
 * @author xuweinan
 */
public class UserInfoActivity extends ActivitySupport {
	public static final String TAG = "UserInfoActivity";
	public static final String modeKey = "show_mode";
	
	public static final int FRIEND_MODE = 0;
	public static final int STRANDER_MODE = 1;
	
	private int mShowType = 0;
	
	private User mUser = null;
	private TextView mBackTxtBtn;
	private ImageView mAvatarImg;
	private TextView mNicknameTxt;
	private TextView mNameTxt;
	private TextView mRemarkTxt;
	private TextView mLocationTxt;
	private TextView mCellphoneTxt;

	private LinearLayout mFriendOperLayout;
	private Button mAddUserBtn;
	private Button mChatBtn;
	private Button mVoiceBtn;
	private ImageButton mRmvUserBtn;
    protected ISipService service = null;
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

		setContentView(R.layout.user_info);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);

		mUser = (User)getIntent().getParcelableExtra(User.userKey);
		mShowType = getIntent().getIntExtra(modeKey, 0);

		Log.d(TAG, "user:" + mUser + ", mode: " + mShowType);
		
		mBackTxtBtn = (TextView) findViewById(R.id.title_back_btn);
		mBackTxtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mAvatarImg = (ImageView) findViewById(R.id.user_avatar_img);
		mNicknameTxt = (TextView) findViewById(R.id.user_nickname_txt);
		mRemarkTxt = (TextView) findViewById(R.id.user_remark_txt);
		mLocationTxt = (TextView) findViewById(R.id.user_location_txt);
		mCellphoneTxt = (TextView) findViewById(R.id.user_cellphone_txt);

		mFriendOperLayout = (LinearLayout) findViewById(R.id.friend_oper_layout);
		mAddUserBtn = (Button) findViewById(R.id.adduser_btn);
		mChatBtn = (Button) findViewById(R.id.chat_btn);
		mVoiceBtn = (Button) findViewById(R.id.voice_btn);
		mRmvUserBtn = (ImageButton) findViewById(R.id.rmv_user_btn);
		
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

		mAddUserBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NoticeManager noticeManager = NoticeManager
						.getInstance();
				
				List<Notice> notices = null;
				
				notices = noticeManager.getNoticeByWith(mUser.getJID());
				
				if (notices == null || notices.size() == 0 ) {
					// 全新的添加好友请求
					Log.d(TAG, "add friend request for new user " + mUser.getJID());

					try {
						ContacterManager.createSubscriber(mUser.getJID(), mUser.getNickName(), null);
					} catch (XMPPException xe) {
						xe.printStackTrace();
						return;
					}
					
					Notice notice = new Notice();
					notice.setReadStatus(Notice.READ);
					notice.setWith(mUser.getJID());
					notice.setStatus(Notice.STATUS_WAIT_FOR_ACCEPT);
					notice.setDispStatus(Notice.DISPLAY);
					notice.setTime(DateUtil.getCurDateStr());
					notice.setContent(mUser.getNickName());
					
					if (mUser.getHeadImg() != null) {
						notice.setAvatar(mUser.getHeadImg());
					} else {
						if (mUser.getGender() == null) {
							notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_male));
							
						} else {
							if (mUser.getGender().equals(User.MALE)) {
								notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_male));
							}
							else {
								notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_female));
							}
						}
					}
					
					NoticeManager.getInstance().saveNotice(notice);
				}
				else if (notices.size() == 1) {
					Notice n = notices.get(0);

					// 如果该好友已经主动添加过我，我此时又尝试添加他，则直接同意他之前的请求
					if(n.getStatus().equals(Notice.STATUS_ADD_REQUEST)) {
						Log.d(TAG, "you want to add " + mUser.getJID() + 
								" to friend list, but its request sent to you.");
						
						ContacterManager.sendSubscribe(Presence.Type.subscribed, mUser.getJID());
						ContacterManager.sendSubscribe(Presence.Type.subscribe, mUser.getJID());

						noticeManager.updateStatusById(n.getId(), Notice.STATUS_COMPLETE);
						noticeManager.updateDispStatusById(n.getId(), Notice.DISPLAY);

						
						new ContacterUpdateThread(mUser.getJID()).start();
					}
					// 该好友已经在好友列表里，又搜索他去添加好友，不应该出现这种场景，在之前搜索环节已经过滤
					else if (n.getStatus().equals(Notice.STATUS_COMPLETE)) {
						Log.e(TAG, "the user " + mUser.getJID() + " has been your friend.");
					}
					// 之前已经添加过该好友，正在等待其同意，此时又搜索到他尝试重复添加
					else {
						Log.e(TAG, "you have waited for the user " + mUser.getJID() + " confirm, do not try to add it again.");
						noticeManager.updateTimeById(n.getId(), DateUtil.getCurDateStr());
					}
					
				}
				else {
					// 同一个好友不应该有重复的添加请求
					Log.e(TAG, "the notice count from user " + mUser.getJID() + " is more than one!");
				}
				
				finish();
			}
		});

		mRmvUserBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeleteDialog();
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
		
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);
	}
	
    @Override 
    public void onResume() {
    	super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);
		filter.addAction(Constant.ROSTER_PRESENCE_CHANGED);
		filter.addAction(Constant.ROSTER_UPDATED);

		registerReceiver(receiver, filter);
		refreshViewOnAUKeyStatusChange();
		refreshUserSecurityStatus();
    }
	
    @Override
    public void onPause() {
    	super.onPause();
		unregisterReceiver(receiver);
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
			
			setUserInfoView();
			refreshViewOnAUKeyStatusChange();
			refreshUserSecurityStatus();
		}
    };
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}
	
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
								CallLogManager.getInstance()
										.delCallLogsByName(mUser.getJID());
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
	
	private void refreshViewOnAUKeyStatusChange() {
		View titleBar = findViewById(R.id.main_head);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			mBackTxtBtn.setTextColor(getResources().getColor(R.color.white));
			mBackTxtBtn.setBackgroundResource(R.drawable.title_clickable_background_black);
			mRmvUserBtn.setBackgroundResource(R.drawable.title_clickable_background_black);
		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			mBackTxtBtn.setTextColor(getResources().getColor(R.color.darkgray));
			mBackTxtBtn.setBackgroundResource(R.drawable.title_clickable_background);
			mRmvUserBtn.setBackgroundResource(R.drawable.title_clickable_background);
		}
			
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
		
		mRemarkTxt.setText(mUser.getRemark());
		ViewTreeObserver vto = mRemarkTxt.getViewTreeObserver();
	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	            ViewTreeObserver obs = mRemarkTxt.getViewTreeObserver();
	            obs.removeGlobalOnLayoutListener(this);
	            if(mRemarkTxt.getLineCount() > 2){
	                Log.d("","Line["+mRemarkTxt.getLineCount()+"]"+mRemarkTxt.getText());
	                int lineEndIndex = mRemarkTxt.getLayout().getLineEnd(1);
	                String text = mRemarkTxt.getText().subSequence(0, lineEndIndex-3)+"...";
	                mRemarkTxt.setText(text);
	                Log.d("","NewText:"+text);
	            }

	        }
	    });
		
		
		mLocationTxt.setText(mUser.getLocation());
		
		String s = StringUtil.getCellphoneByName(mUser.getName());
		if (mUser.getContactName() != null) {
			s += " (" + mUser.getContactName() + ")";
		}
		mCellphoneTxt.setText(s);
		
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
		
		if(mShowType == FRIEND_MODE) {
			mFriendOperLayout.setVisibility(View.VISIBLE);
			mAddUserBtn.setVisibility(View.GONE);
		}
		else {
			mFriendOperLayout.setVisibility(View.GONE);
			mAddUserBtn.setVisibility(View.VISIBLE);
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
