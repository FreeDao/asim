package com.view.asim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import com.csipsimple.api.SipConfigManager;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.csipsimple.utils.CustomDistribution;
import com.csipsimple.utils.PreferencesProviderWrapper;
import com.csipsimple.utils.PreferencesWrapper;
import com.view.asim.comm.Constant;
import com.view.asim.db.DBProvider;
import com.view.asim.activity.im.AContacterActivity;
import com.view.asim.activity.im.AddUserMainActivity;
import com.view.asim.activity.im.ChatActivity;
import com.view.asim.activity.im.GroupAddUserActivity;
import com.view.asim.activity.im.SettingsActivity;
import com.view.asim.activity.im.UserInfoActivity;
import com.view.asim.activity.im.UserInfoModActivity;
import com.view.asim.activity.im.UserNoticeActivity;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.CallLogManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.SMSVerifyManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.CallLogs;
import com.view.asim.model.ChatHisBean;
import com.view.asim.model.GroupUser;
import com.view.asim.model.CtrlMessage;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FaceConversionUtil;
import com.view.asim.util.FileUtil;
import com.view.asim.util.PinyinComparator;
import com.view.asim.util.StringUtil;
import com.view.asim.view.CallLogsAdapter;
import com.view.asim.view.ContactsAdapter;
import com.view.asim.view.LayoutChangeListener;
import com.view.asim.view.RecentChatAdapter;
import com.view.asim.view.ScrollLayout;
import com.view.asim.view.SideBar;
import com.view.asim.view.SideBar.*;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.CallLog.Calls;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.view.asim.R;

public class MainActivity extends AContacterActivity implements
		LayoutChangeListener, OnClickListener {
	private final static String TAG = "MainActivity";
	
	private File mTempCaptureAvatarImgFile;
	private LayoutInflater inflater;
	private ScrollLayout mLayout;
	private ImageView imageView;
	private View mTabMessage;
	private View mTabContacts;
	private View mTabCalllogs;
	private TextView mTabMsgTxt;
	private TextView mTabConTxt;
	private TextView mTabCallTxt;
	
	private ListView mContacterList = null;
	private ContactsAdapter mContactsAdapter = null;
	private static PinyinComparator mPinyinComparator;
	private SideBar mSideBar = null;

	View mMainMessageView = null;
	View mMainContactsView = null;
	View mMainCalllogsView = null;
	
	View mContactsHead = null;
	
	private ListView mMessageThreadList = null;
	private RecentChatAdapter mNoticeAdapter = null;

	private ListView mCalllogsListView = null;
	private CallLogsAdapter mCalllogsAdapter = null;

	private List<ChatHisBean> mInviteNotices = new ArrayList<ChatHisBean>();
	private List<User> mContacters = new ArrayList<User>();
	private List<CallLogs> mCalllogsList = new ArrayList<CallLogs>();
	
	private LinearLayout mNoChatMessageLayout = null;
	private LinearLayout mNoContactsLayout = null;
	private LinearLayout mNoCalllogsLayout = null;
	private TextView mMsgUnreadTxt;
	private TextView mNetworkFailedTxt;
	private TextView mAlphabeticText;

	private View mHeadTabDivideLineView = null;
	private View mTabHeadView = null;
	private View mTabBodyDivideLineView = null;
	private ImageView mMyAvatarImgBtn = null;
	private View mRemarkLayout = null;
	private View mLocationLayout = null;

	private PopupWindow mMyInfoPopupWindow = null;
	private View mPopupView = null;
	
	private ImageView mNewUserNotifyImg = null;
	private ImageView mUnreadNewUserImg = null;
	private ImageView mCallMissedImg = null;

	// 我的个人信息
	private ImageView mMyAvatarImg = null;
	private TextView mMyNicknameTxt = null;
	private TextView mMyRemarkTxt = null;
	private TextView mMyLocationTxt = null;
	private TextView mMyCellphoneTxt = null;
	private TextView mMyInfoSettingsTxt = null;
	private TextView mMyInfoGroupChatTxt = null;
	private TextView mMyInfoVoiceChatTxt = null;
	private TextView mMyInfoVideoChatTxt = null;
	
    private PreferencesProviderWrapper prefProviderWrapper;

		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(com.view.asim.R.layout.contacter_main);
		init();
		initSip();
	}
	
	private void clearOldSipAccount() {
		ArrayList<SipProfile> accounts = SipProfile.getAllProfiles(this, false);
		for(SipProfile acc : accounts) {
			Log.i(TAG, "clear sip account: " + acc.acc_id + ", " + acc.display_name);
			getContentResolver().delete(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, acc.id), null, null);
        }
	}
	
	private void addNewSipAccount() {
		SipProfile newAcc = buildAccount();
		Uri uri = getContentResolver().insert(SipProfile.ACCOUNT_URI, newAcc.getDbContentValues());
		newAcc.id = ContentUris.parseId(uri);

		ContacterManager.userMe.setSipAccountId(newAcc.id);
	}
	
	private SipProfile buildAccount() {
		SipProfile account = SipProfile.getProfileFromDbId(this, SipProfile.INVALID_ID, DBProvider.ACCOUNT_FULL_PROJECTION);
		String sipNum = StringUtil.getCellphoneByName(mLoginCfg.getUsername());
		String server = Constant.VOIP_SERVICE_HOST;
		
		account.display_name = mLoginCfg.getUsername();
		account.acc_id = "<sip:" + sipNum + "@" + server + ">";
		String regUri = "sip:" + server + ":" + Constant.VOIP_SERVICE_PORT;
		account.reg_uri = regUri;
		account.proxies = new String[] { regUri } ;

		account.realm = "*";
		account.username = sipNum;
		account.data = mLoginCfg.getPassword();

		account.scheme = SipProfile.CRED_SCHEME_DIGEST;
		account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
		account.transport = SipProfile.TRANSPORT_TCP;
		account.allow_via_rewrite = false;
		account.allow_contact_rewrite = false;
		account.mwi_enabled = false;
		
		if(account.use_rfc5626) {
            if(TextUtils.isEmpty(account.rfc5626_instance_id)) {
                String autoInstanceId = (UUID.randomUUID()).toString();
                account.rfc5626_instance_id = "<urn:uuid:"+autoInstanceId+">";
            }
        }
		return account;
	}
	
	private void initSip() {
        // 配置 SIP 用户
        clearOldSipAccount();
        addNewSipAccount();
        
	}
	
	private void resumeSip() {
        Log.d(TAG, "WE CAN NOW start SIP service");
        prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_BEEN_QUIT, false);
        
        startSipService();
	}
	
	// Service monitoring stuff
    private void startSipService() {
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(MainActivity.this.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(MainActivity.this, MainActivity.class));
                startService(serviceIntent);
                postStartSipService();
            };
        };
        t.start();

    }

    private void postStartSipService() {
        // If we have never set fast settings
        if (CustomDistribution.showFirstSettingScreen()) {
            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
            	
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                prefProviderWrapper.resetAllDefaultValues();
            }
        }

    }
	
	private void init() {

		getEimApplication().addActivity(this);
        FaceConversionUtil.getInstace().getFileText(this);
        prefProviderWrapper = new PreferencesProviderWrapper(this);

        // 主界面
		inflater = LayoutInflater.from(context);
		mLayout = (ScrollLayout) findViewById(R.id.scrolllayout);
		mLayout.addChangeListener(this);

		// 主界面三大子界面
		mMainMessageView = inflater.inflate(R.layout.main_message, null);
		mMainContactsView = inflater.inflate(R.layout.main_contacts, null);
		mMainCalllogsView = inflater.inflate(R.layout.main_calllogs, null);
		mLayout.addView(mMainMessageView);
		mLayout.addView(mMainContactsView);
		mLayout.addView(mMainCalllogsView);
		mLayout.setToScreen(0);
		
		// 主界面导航栏
		mTabMessage = findViewById(R.id.tab_message_layout);
		mTabContacts = findViewById(R.id.tab_contacts_layout);
		mTabCalllogs = findViewById(R.id.tab_calllogs_layout);
		mTabMsgTxt = (TextView)findViewById(R.id.tab_message);
		mTabConTxt = (TextView)findViewById(R.id.tab_contacts);
		mTabCallTxt = (TextView)findViewById(R.id.tab_calllogs);

		mMsgUnreadTxt = (TextView) findViewById(R.id.message_paopao_txt);
		mCallMissedImg = (ImageView) findViewById(R.id.calllogs_paopao_img);
		imageView = (ImageView) findViewById(R.id.top_bar_select);
		mTabHeadView = findViewById(R.id.tab_layout);
		mHeadTabDivideLineView = findViewById(R.id.head_body_divide_img);
		mTabBodyDivideLineView = findViewById(R.id.tab_body_divide_img);
		mNetworkFailedTxt = (TextView) findViewById(R.id.network_failed_txt);

		
		// 主界面顶部头像按钮
		mMyAvatarImgBtn = (ImageView) findViewById(R.id.my_avatar_btn);
		mMyAvatarImgBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupMyInfoWindow();
			}
		});

		// 个人信息弹出窗口
		mPopupView = getLayoutInflater().inflate(R.layout.my_info_popup_win, null);
		mMyAvatarImg = (ImageView) mPopupView.findViewById(R.id.user_avatar_img);
		mMyNicknameTxt = (TextView) mPopupView.findViewById(R.id.user_nickname_txt);
		mMyRemarkTxt = (TextView) mPopupView.findViewById(R.id.user_remark_txt);
		mMyLocationTxt = (TextView) mPopupView.findViewById(R.id.user_location_txt);
		mMyCellphoneTxt = (TextView) mPopupView.findViewById(R.id.user_cellphone_txt);
		mMyInfoSettingsTxt = (TextView) mPopupView.findViewById(R.id.start_settings_txt);
		mMyInfoGroupChatTxt = (TextView) mPopupView.findViewById(R.id.start_groupchat_txt);
		mMyInfoVoiceChatTxt = (TextView) mPopupView.findViewById(R.id.start_voicechat_txt);
		mMyInfoVideoChatTxt = (TextView) mPopupView.findViewById(R.id.start_videochat_txt);
		mRemarkLayout = mPopupView.findViewById(R.id.user_remark_layout);
		mLocationLayout = mPopupView.findViewById(R.id.user_location_layout);

		
		mMyInfoVideoChatTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
		mMyInfoSettingsTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, SettingsActivity.class);
				startActivity(intent);
			}
		});
		
		mMyInfoGroupChatTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, GroupAddUserActivity.class);
				startActivityForResult(intent, Constant.REQCODE_SELECT_USERS);
			}
		});
		
		mRemarkLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, UserInfoModActivity.class);
				intent.putExtra(UserInfoModActivity.MOD_TYPE_KEY, Constant.REQCODE_MOD_REMARK);
				intent.putExtra(UserInfoModActivity.REMARK, 
						ContacterManager.userMe.getRemark() == null ? "" : ContacterManager.userMe.getRemark());
				
				startActivityForResult(intent, Constant.REQCODE_MOD_REMARK);
			}
		});
		
		mLocationLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, UserInfoModActivity.class);
				intent.putExtra(UserInfoModActivity.MOD_TYPE_KEY, Constant.REQCODE_MOD_LOCATION);
				intent.putExtra(UserInfoModActivity.LOCATION, 
						ContacterManager.userMe.getLocation() == null ? "" : ContacterManager.userMe.getLocation());

				startActivityForResult(intent, Constant.REQCODE_MOD_LOCATION);
			}
		});
		
		mMyAvatarImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(MainActivity.this).
					setTitle("图片").
					setItems(new String[] { "拍摄", "从相册选择" }, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = null;
							if (which == 0) {
					        	// 激活相机  
					            intent = new Intent("android.media.action.IMAGE_CAPTURE");  
					            // 判断存储卡是否可以用，可用进行存储  
					            
					            String tmpImgfile = FileUtil.getUserTempPath() + FileUtil.genAvatarTempImageName();
					            
				                mTempCaptureAvatarImgFile = new File(tmpImgfile);  
				                // 从文件中创建uri  
				                Uri uri = Uri.fromFile(mTempCaptureAvatarImgFile);
				                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				                
					            // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA  
					            startActivityForResult(intent, Constant.REQCODE_MOD_AVATAR_BY_CAPTURE); 
							}
							else {
					        	
					        	// 激活系统图库，选择一张图片  
					            intent = new Intent(Intent.ACTION_PICK);  
					            intent.setType("image/*");  
					            // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY  
					            startActivityForResult(intent, Constant.REQCODE_MOD_AVATAR_BY_GALLERY);
							}
							
						}
					}).
					show();
			}
		});

		mMyNicknameTxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, UserInfoModActivity.class);
				intent.putExtra(UserInfoModActivity.MOD_TYPE_KEY, Constant.REQCODE_MOD_NICKNAME);
				intent.putExtra(UserInfoModActivity.NICKNAME, ContacterManager.userMe.getNickName());
				intent.putExtra(UserInfoModActivity.GENDER, 
						ContacterManager.userMe.getGender() == null ? "" : ContacterManager.userMe.getGender());

				startActivityForResult(intent, Constant.REQCODE_MOD_NICKNAME);
			}
		});
		
		
		// 联系人子界面的联系人列表
		mPinyinComparator = new PinyinComparator();
		mContacterList = (ListView) mMainContactsView.findViewById(R.id.contacts_list);
		
		mContactsHead = LayoutInflater.from(context).inflate(R.layout.contacts_header, null);
		mNewUserNotifyImg = (ImageView) mContactsHead.findViewById(R.id.user_new_notify_img);
		mUnreadNewUserImg = (ImageView) findViewById(R.id.contacts_paopao_img);

		mContactsHead.findViewById(R.id.user_notify_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, UserNoticeActivity.class);
				startActivity(intent);

			}
		});
		mContactsHead.findViewById(R.id.add_user_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, AddUserMainActivity.class);
				startActivity(intent);

			}
		});
		
		mNoContactsLayout = (LinearLayout) mContactsHead.findViewById(R.id.no_contacts_layout);

		mContacterList.addHeaderView(mContactsHead);
		mContactsAdapter = new ContactsAdapter(this, mContacters, ContactsAdapter.SHOW_MODE);
		mContacterList.setAdapter(mContactsAdapter);
		mContactsAdapter.setOnClickListener(contacterOnClickJ);

		// 联系人子界面右侧的字母表
		mSideBar = (SideBar) mMainContactsView.findViewById(R.id.sidebar_view);
		mAlphabeticText = (TextView) mMainContactsView.findViewById(R.id.alphabetic_txt);
		mSideBar.setTextView(mAlphabeticText);
		mSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				int position = mContactsAdapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					mContacterList.setSelection(position);
				}

			}
		});

		
		// 聊天会话子界面
		mMessageThreadList = (ListView) mMainMessageView.findViewById(R.id.message_thread_list);
		mNoChatMessageLayout = (LinearLayout) mMainMessageView.findViewById(R.id.no_messages_layout);

		mNoticeAdapter = new RecentChatAdapter(context, mInviteNotices);
		mMessageThreadList.setAdapter(mNoticeAdapter);
		mMessageThreadList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ChatHisBean bean = (ChatHisBean) view.findViewById(R.id.contact_name_txt).getTag();
				createChat(bean.getWith());
			}
		});
		
		mMessageThreadList.setOnCreateContextMenuListener(mMsgThreadsOnCreateContextMenuListener);  
		

		// 通话记录子界面
		mCalllogsListView = (ListView) mMainCalllogsView.findViewById(R.id.calllogs_list);
		mNoCalllogsLayout = (LinearLayout) mMainCalllogsView.findViewById(R.id.no_calllogs_layout);

		mCalllogsAdapter = new CallLogsAdapter(context, mCalllogsList);
		mCalllogsListView.setAdapter(mCalllogsAdapter);
		mCalllogsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CallLogs logs = (CallLogs) view.findViewById(R.id.contact_name_txt).getTag();
				showUserCallLogs(logs.getWith());
			}
		});
		mCalllogsListView.setOnCreateContextMenuListener(mCalllogsOnCreateContextMenuListener);  

		
		// 用户登录后处理离线消息
		Intent intent = new Intent(Constant.RECV_OFFLINE_MSG_ACTION);
		sendBroadcast(intent);
	}

	private final OnCreateContextMenuListener mMsgThreadsOnCreateContextMenuListener = new OnCreateContextMenuListener() {  
        @Override  
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;  
        	
			ChatHisBean bean = (ChatHisBean) mi.targetView.findViewById(R.id.contact_name_txt).getTag();
        	
        	menu.setHeaderTitle(ContacterManager.contacters.get(bean.getWith()).getNickName());
        	menu.add(0, 1, 0, "删除会话");
        }  
    };
    
	private final OnCreateContextMenuListener mCalllogsOnCreateContextMenuListener = new OnCreateContextMenuListener() {  
        @Override  
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;  

			CallLogs logs = (CallLogs) mi.targetView.findViewById(R.id.contact_name_txt).getTag();
        	
        	menu.setHeaderTitle(ContacterManager.contacters.get(logs.getWith()).getNickName());
        	menu.add(1, 1, 0, "删除通话记录");
        }  
    };
    
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
	        // 得到当前被选中的item信息  
	        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();  
	        
	        // 聊天记录的上下文菜单
        	if (item.getGroupId() == 0) {
            	ChatHisBean b = (ChatHisBean) menuInfo.targetView.findViewById(R.id.contact_name_txt).getTag();

        		switch(item.getItemId()) {  
		        case 1:  
					NoticeManager.getInstance().setCurrentChatUser(null);
					NoticeManager.getInstance().clearIMMessageNotify(ContacterManager.contacters.get(b.getWith()));
	
					// 删除数据库
					NoticeManager.getInstance()
							.delNoticeByWith(b.getWith());
					MessageManager.getInstance()
							.delChatHisByName(b.getWith());
					
					refreshMessageListView();
		        	break;  
	
		        default:  
		            return super.onContextItemSelected(item);  
		        }
        	} else 
        	// 通话记录的上下文菜单
        	if (item.getGroupId() == 1) {
        		CallLogs b = (CallLogs) menuInfo.targetView.findViewById(R.id.contact_name_txt).getTag();

        		switch(item.getItemId()) {  
		        case 1:  
		        	CallLogManager.getInstance().delCallLogsByName(StringUtil.getUserNameByJid(b.getWith()));
		        	refreshCalllogsListView();
		        	break;  
	
		        default:  
		            return super.onContextItemSelected(item);  
		        }
        	}
	        return true;
    }

    /* 
     * 剪切图片 
     */  
    private void crop(Uri uri) {  
        // 裁剪图片意图  
        Intent intent = new Intent("com.android.camera.action.CROP");  
        intent.setDataAndType(uri, "image/*");  
        intent.putExtra("crop", "true");  
        // 裁剪框的比例，1：1  
        intent.putExtra("aspectX", 1);  
        intent.putExtra("aspectY", 1);  
        // 裁剪后输出图片的尺寸大小  
        intent.putExtra("outputX", 250);  
        intent.putExtra("outputY", 250);  
  
        intent.putExtra("outputFormat", "JPEG");// 图片格式  
        intent.putExtra("noFaceDetection", true);// 取消人脸识别  
        intent.putExtra("return-data", true);  
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT  
        startActivityForResult(intent, Constant.REQCODE_MOD_AVATAR_CROP);  
    }  
	
	@Override
	protected void onResume() {
		super.onResume();
		
		resumeSip();

		refreshList();
		
		/*
		Intent intent = getIntent();
	    if (intent != null && intent.getAction() != null) {
			if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
		    	AUKeyManager.getInstance().setAUKeyStatus(AUKeyManager.ATTACHED);
				Toast.makeText(context, "ans key attached", Toast.LENGTH_SHORT).show();
	        } else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
		    	AUKeyManager.getInstance().setAUKeyStatus(AUKeyManager.DETACHED);
				Toast.makeText(context, "ans key detached", Toast.LENGTH_SHORT).show();
	        }
	    }
	    */
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		MessageManager.getInstance().destroy();
		NoticeManager.getInstance().destroy();
		AUKeyManager.getInstance().destroy();
		CallLogManager.getInstance().destroy();
		
		if (mMyInfoPopupWindow != null)
			mMyInfoPopupWindow.dismiss();
	
		sipDisconnect(false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        moveTaskToBack(true);//true对任何Activity都适用
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch(requestCode){
	        case Constant.REQCODE_MOD_NICKNAME:
	        	String nick = data.getStringExtra(UserInfoModActivity.NICKNAME);
	        	String gender = data.getStringExtra(UserInfoModActivity.GENDER);
	        	ContacterManager.userMe.setNickName(nick);
	        	if (!gender.equals("")) {
	        		ContacterManager.userMe.setGender(gender);
	        	}
	        	Log.d(TAG, "mod nick:" + ContacterManager.userMe);

	        	break;
	        case Constant.REQCODE_MOD_LOCATION:
	        	String loc = data.getStringExtra(UserInfoModActivity.LOCATION);
	        	if (!loc.equals("")) {
	        		ContacterManager.userMe.setLocation(loc);
	        	}
	        	Log.d(TAG, "mod location:" + ContacterManager.userMe);

	        	
	        	break;
	        case Constant.REQCODE_MOD_REMARK:
	        	String remark = data.getStringExtra(UserInfoModActivity.REMARK);
	        	if (!remark.equals("")) {
	        		ContacterManager.userMe.setRemark(remark);
	        	}
	        	Log.d(TAG, "mod remark:" + ContacterManager.userMe);
	        	break;
	        	
	        case Constant.REQCODE_MOD_AVATAR_BY_GALLERY:
	        	// 从相册返回的数据  
	            if (data != null) {  
	                // 得到图片的全路径  
	                Uri uri = data.getData();  
	                crop(uri);  
	            }  
	            break;
	        case Constant.REQCODE_MOD_AVATAR_BY_CAPTURE:
                crop(Uri.fromFile(mTempCaptureAvatarImgFile));  
	            break;	
	        case Constant.REQCODE_MOD_AVATAR_CROP:
	        	// 从剪切图片返回的数据  
	            if (data != null) {  
	                Bitmap bitmap = data.getParcelableExtra("data");  
	                ContacterManager.userMe.setHeadImg(bitmap);  
	                refreshMyInfoView();
	            }  
	            try {  
	                // 将临时文件删除  
	            	if (mTempCaptureAvatarImgFile != null) 
	            		mTempCaptureAvatarImgFile.delete();  
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }
	            break;
	            
	        case Constant.REQCODE_SELECT_USERS:
                ArrayList<User> selectUsers = (ArrayList<User>)data.getSerializableExtra(User.userListKey);  
                if (selectUsers.size() == 0) {
                	return;
                } else if (selectUsers.size() == 1) {
                	createChat(selectUsers.get(0).getJID());
                	return;
                	
                } else {
                	/*
                	// 生成新的群聊组
                	GroupUser grp = ContacterManager.createGroupUserByMember(selectUsers);
                	ContacterManager.groupUsers.remove(grp.getName());
                	ContacterManager.groupUsers.put(grp.getName(), grp);
                	ContacterManager.userMe.addGroup(grp);
                	createChat(grp.getName());

                	Intent intent = new Intent(Constant.GROUP_INVITE_ACTION);
            		intent.putExtra(Constant.GROUP_ACTION_KEY_INFO, grp);
            		sendBroadcast(intent);
            		*/
                	showToast("该功能下一版本发布，敬请期待:)");
                }
			}
			updateMyUserInfo();
		}
    }

	
	// 个人信息服务器更新
	public void updateMyUserInfo() {
		new UserInfoUpdateThread().start();
	}

	// 用户个人信息更新线程
	private class UserInfoUpdateThread extends Thread {
		
		@Override
		public void run() {
			Log.d(TAG, "UserInfoUpdateThread");
			XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
			ContacterManager.saveUserVCard(conn, ContacterManager.userMe);
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("update");
			conn.sendPacket(presence);

			/*
			Collection<RosterEntry> rosters = conn.getRoster()
					.getEntries();
			for (RosterEntry rosterEntry : rosters) {
				Log.d(TAG, "presence updated to " + rosterEntry.getUser());
				presence.setTo(rosterEntry.getUser());
				conn.sendPacket(presence);
			}
			*/
		}
	}
	
	// 个人信息界面更新
	public void refreshMyInfoView() {
		if (ContacterManager.userMe.getNickName() == null) {
			mMyNicknameTxt.setVisibility(View.GONE);
		} else {
			mMyNicknameTxt.setText(ContacterManager.userMe.getNickName());
		}
		
		Drawable leftIcon = null;
		Drawable rightIcon = null;
		if (ContacterManager.userMe.getGender() == null) {
			mMyNicknameTxt.setCompoundDrawables(null, null, null, null);
			
		} else {
			Drawable maleIcon = getResources().getDrawable(R.drawable.business_card_male_icon);
			maleIcon.setBounds(0, 0, maleIcon.getMinimumWidth(), maleIcon.getMinimumHeight());
			Drawable femaleIcon = getResources().getDrawable(R.drawable.business_card_female_icon);
			femaleIcon.setBounds(0, 0, femaleIcon.getMinimumWidth(), femaleIcon.getMinimumHeight());
			
			if (ContacterManager.userMe.getGender().equals(User.MALE)) {
				rightIcon = maleIcon;
			}
			else {
				rightIcon = femaleIcon;
			}
		}
		

		if (ContacterManager.userMe.getSecurity()!= null && ContacterManager.userMe.getSecurity().equals(AUKeyManager.ATTACHED)) {
			leftIcon = context.getResources().getDrawable(R.drawable.notificationbar_icon_logo_normal);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
		}
		
		mMyNicknameTxt.setCompoundDrawables(leftIcon, null, rightIcon, null);
		
		mMyRemarkTxt.setText(ContacterManager.userMe.getRemark());
		ViewTreeObserver vto = mMyRemarkTxt.getViewTreeObserver();
	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	            ViewTreeObserver obs = mMyRemarkTxt.getViewTreeObserver();
	            obs.removeGlobalOnLayoutListener(this);
	            if(mMyRemarkTxt.getLineCount() > 2){
	                Log.d("","Line["+mMyRemarkTxt.getLineCount()+"]"+mMyRemarkTxt.getText());
	                int lineEndIndex = mMyRemarkTxt.getLayout().getLineEnd(1);
	                String text = mMyRemarkTxt.getText().subSequence(0, lineEndIndex-3)+"...";
	                mMyRemarkTxt.setText(text);
	                Log.d("","NewText:"+text);
	            }

	        }
	    });
		
		
		mMyLocationTxt.setText(ContacterManager.userMe.getLocation());
		
		String s = StringUtil.getCellphoneByName(ContacterManager.userMe.getName());
		if (ContacterManager.userMe.getContactName() != null) {
			s += "(" + ContacterManager.userMe.getContactName() + ")";
		}
		mMyCellphoneTxt.setText(s);
		
		if (ContacterManager.userMe.getHeadImg() != null) {
			mMyAvatarImg.setImageBitmap(ContacterManager.userMe.getHeadImg());
			mMyAvatarImgBtn.setImageBitmap(ContacterManager.userMe.getHeadImg());
		} else {
			if (ContacterManager.userMe.getGender() == null) {
				mMyAvatarImg.setImageResource(R.drawable.default_avatar_male);
				mMyAvatarImgBtn.setImageResource(R.drawable.default_avatar_male);
				
			} else {
				if (ContacterManager.userMe.getGender().equals(User.MALE)) {
					mMyAvatarImg.setImageResource(R.drawable.default_avatar_male);
					mMyAvatarImgBtn.setImageResource(R.drawable.default_avatar_male);
					
				}
				else {
					mMyAvatarImg.setImageResource(R.drawable.default_avatar_female);
					mMyAvatarImgBtn.setImageResource(R.drawable.default_avatar_female);

				}
			}
		}
		
	}

	// 用户个人信息弹出窗口
	private void popupMyInfoWindow() {

		if (mMyInfoPopupWindow == null) {
			int winHeight = mTabHeadView.getHeight() +
					mTabBodyDivideLineView.getHeight() + 
					mLayout.getHeight();
			
			mMyInfoPopupWindow = new PopupWindow(mPopupView,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT, winHeight, true);

			mMyInfoPopupWindow.setTouchable(true);
			mMyInfoPopupWindow.setOutsideTouchable(true);
			mMyInfoPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources()));
			mMyInfoPopupWindow.showAsDropDown(mHeadTabDivideLineView);
		} else if (mMyInfoPopupWindow != null && mMyInfoPopupWindow.isShowing()) {
			mMyInfoPopupWindow.dismiss();
		} else if (mMyInfoPopupWindow != null && !mMyInfoPopupWindow.isShowing()) {
			mMyInfoPopupWindow.showAsDropDown(mHeadTabDivideLineView);
		}
	}

	

	/**
	 * 刷新整个界面
	 */
	@Override
	protected void refreshList() {
		
		// 更新网络状态界面
		refreshConnStatusView();
		
		// 更新标题栏界面
		refreshTitleView();

		// 更新聊天会话界面
		refreshMessageListView();
		
		// 更新联系人界面
		refreshContactListView();
		
		// 更新通话记录界面
		refreshCalllogsListView();
		
		// 更新个人信息界面
		refreshMyInfoView();

	}
	
	protected void refreshConnStatusView() {
		if (XmppConnectionManager.getInstance().getConnection().isConnected()) {
			mNetworkFailedTxt.setVisibility(View.GONE);
		} else {
			mNetworkFailedTxt.setVisibility(View.VISIBLE);
		}
	}
	
	protected void refreshTitleView() {
		View titleBar = findViewById(R.id.main_head);
		TextView title = (TextView) findViewById(R.id.ivTitleName);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			title.setTextColor(getResources().getColor(R.color.white));
			//aukeyOnlineImg.setVisibility(View.VISIBLE);
		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			title.setTextColor(getResources().getColor(R.color.darkgray));
			//aukeyOnlineImg.setVisibility(View.GONE);
		}
			
	}
	
	// 刷新聊天会话列表子界面
	protected void refreshMessageListView() {

		mInviteNotices = MessageManager.getInstance()
				.getRecentContactsWithLastMsg();
		for(ChatHisBean c: mInviteNotices) {
			Log.i(TAG, "chat his bean: " + c.getWith() + ", content: " + c.getContent());
		}
		
		Collections.sort(mInviteNotices);
		mNoticeAdapter.refreshList(mInviteNotices);
		
		/**
		 * 有新消息进来的气泡设置
		 */
		setMessagePaoPao();
		
		if(mInviteNotices.size() == 0) {
			mMessageThreadList.setVisibility(View.GONE);
			mNoChatMessageLayout.setVisibility(View.VISIBLE);
		}
		else {
			mNoChatMessageLayout.setVisibility(View.GONE);
			mMessageThreadList.setVisibility(View.VISIBLE);
		}
	}
	
	// 刷新通话记录列表子界面
	protected void refreshCalllogsListView() {

		mCalllogsList = CallLogManager.getInstance().getRecentCallLogs();
		for(CallLogs c: mCalllogsList) {
			Log.i(TAG, "chat logs: " + c.getWith() + ", time: " + c.getTime());
		}

		Collections.sort(mCalllogsList);
		mCalllogsAdapter.refreshList(mCalllogsList);
		
		/**
		 * 有新消息进来的气泡设置
		 */
		//setCalllogPaoPao();
		
		if(mCalllogsList.size() == 0) {
			mCalllogsListView.setVisibility(View.GONE);
			mNoCalllogsLayout.setVisibility(View.VISIBLE);
		}
		else {
			mNoCalllogsLayout.setVisibility(View.GONE);
			mCalllogsListView.setVisibility(View.VISIBLE);
		}
	}
	
	// 刷新聊天会话列表子界面
	protected void refreshMessageListView(List<ChatHisBean> list) {

		Collections.sort(list);
		mNoticeAdapter.refreshList(list);
		
		/**
		 * 有新消息进来的气泡设置
		 */
		setMessagePaoPao();
		
		if(mInviteNotices.size() == 0) {
			mMessageThreadList.setVisibility(View.GONE);
			mNoChatMessageLayout.setVisibility(View.VISIBLE);
		}
		else {
			mNoChatMessageLayout.setVisibility(View.GONE);
			mMessageThreadList.setVisibility(View.VISIBLE);
		}
	}
	
	// 刷新联系人列表子界面
	protected void refreshContactListView() {
		
		mContacters = ContacterManager.getContacterList();
		Collections.sort(mContacters, mPinyinComparator);
		mContactsAdapter.refreshList(mContacters);
		
		if (mContacters.size() == 0) {
			mNoContactsLayout.setVisibility(View.VISIBLE);
		} else {
			mNoContactsLayout.setVisibility(View.GONE);
		}
		
		int unreadNewUserCount = NoticeManager.getInstance().getUnreadNoticeCount();
		if (unreadNewUserCount > 0) {
			mNewUserNotifyImg.setVisibility(View.VISIBLE);
			mUnreadNewUserImg.setVisibility(View.VISIBLE);
		}
		else {
			mNewUserNotifyImg.setVisibility(View.GONE);
			mUnreadNewUserImg.setVisibility(View.GONE);
		}
	}
	
	// 刷新联系人列表子界面
	protected void refreshContactListView(List<User> list) {
		
		Collections.sort(list, mPinyinComparator);
		mContactsAdapter.refreshList(list);
		
		if (mContacters.size() == 0) {
			mNoContactsLayout.setVisibility(View.VISIBLE);
		} else {
			mNoContactsLayout.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 有新消息进来
	 */
	@Override
	protected void msgReceive(IMMessage msg) {
		Log.d(TAG, "msgReceive: " + msg);
		refreshMessageListView();
	}
	
	/**
	 * 联系人点击处理(显示该好友的个人信息)
	 */
	private OnClickListener contacterOnClickJ = new OnClickListener() {

		@Override
		public void onClick(View v) {
			User user = (User)v.findViewById(R.id.contact_name_txt).getTag();
			
			Intent intent = new Intent();
			intent.putExtra(User.userKey, user);
			intent.putExtra(UserInfoActivity.modeKey, UserInfoActivity.FRIEND_MODE);
			intent.setClass(MainActivity.this, UserInfoActivity.class);
			startActivity(intent);
		}
	};


	@Override
	protected void addUserReceive(User user) {
		if (user == null)
			return;
		
		Toast.makeText(
				context,
				"密友 " + 
				((user.getNickName() == null) ? user.getName() : user.getNickName()) +
				" 已添加", Toast.LENGTH_SHORT).show();
		refreshList();
	}

	@Override
	protected void deleteUserReceive(User user) {
		if (user == null)
			return;
		
		Toast.makeText(
				context,
				"密友 " + 
				((user.getNickName() == null) ? user.getName() : user.getNickName()) +
				" 已删除", Toast.LENGTH_SHORT).show();
		refreshList();
	}

	@Override
	protected void changePresenceReceive(User user) {
		refreshList();
	}

	@Override
	protected void updateUserReceive(User user) {
		refreshList();
	}

	@Override
	protected void subscripUserReceive(Notice notice) {
		refreshList();
	}
	
	// 子界面之间滑动回调处理
	@Override
	public void doChange(int lastIndex, int currentIndex) {
		if (lastIndex != currentIndex) {
			TranslateAnimation animation = null;
			int delta = ((LinearLayout) mTabMessage).getWidth();
			
			switch (currentIndex) {
				case 0:
					if (lastIndex == 1) {
						animation = new TranslateAnimation(delta, 0, 0, 0);
					} else if (lastIndex == 2) {
						animation = new TranslateAnimation(delta * 2, 0, 0, 0);
					}
					mTabMsgTxt.setTextColor(getResources().getColor(R.color.green));
					mTabConTxt.setTextColor(getResources().getColor(R.color.darkgray));
					mTabCallTxt.setTextColor(getResources().getColor(R.color.darkgray));
					
					break;
				case 1:
					if (lastIndex == 0) {
						animation = new TranslateAnimation(0, delta, 0, 0);
					} else if (lastIndex == 2) {
						animation = new TranslateAnimation(delta * 2, delta, 0, 0);
					}
					mTabMsgTxt.setTextColor(getResources().getColor(R.color.darkgray));
					mTabConTxt.setTextColor(getResources().getColor(R.color.green));
					mTabCallTxt.setTextColor(getResources().getColor(R.color.darkgray));					
					break;
				case 2:
					if (lastIndex == 1) {
						animation = new TranslateAnimation(delta, delta * 2, 0, 0);
					} else if (lastIndex == 0) {
						animation = new TranslateAnimation(0, delta * 2, 0, 0);
					}
					mTabMsgTxt.setTextColor(getResources().getColor(R.color.darkgray));
					mTabConTxt.setTextColor(getResources().getColor(R.color.darkgray));
					mTabCallTxt.setTextColor(getResources().getColor(R.color.green));					
					break;
			}
			animation.setDuration(300);
			animation.setFillAfter(true);
			imageView.startAnimation(animation);
		}
	}

	// 导航栏点击处理
	@Override
	public void onClick(View v) {

		if (v == mTabMessage) {
			mLayout.snapToScreen(0);

		} else if (v == mTabContacts) {
			mLayout.snapToScreen(1);

		} else if (v == mTabCalllogs) {
			mLayout.snapToScreen(2);

		}
	}


	 // 通知气泡更新
	private void setMessagePaoPao() {
		if (null != mInviteNotices && mInviteNotices.size() > 0) {
			int paoCount = 0;
			for (ChatHisBean c : mInviteNotices) {
				int countx = c.getUnreadCount();
				paoCount += countx;
			}
			if (paoCount == 0) {
				mMsgUnreadTxt.setVisibility(View.GONE);
				return;
			}
			Log.d(TAG, "total unread message " + paoCount);
			mMsgUnreadTxt.setText(paoCount + "");
			mMsgUnreadTxt.setVisibility(View.VISIBLE);
		} else {
			mMsgUnreadTxt.setVisibility(View.GONE);
		}
	}

	private void setCalllogPaoPao() {
		if (null != mCalllogsList && mCalllogsList.size() > 0) {
			mCallMissedImg.setVisibility(View.VISIBLE);
		} else {
			mCallMissedImg.setVisibility(View.GONE);
		}
	}
	
	
	// 延迟刷新界面
	public void delayRefresh() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				refreshList();

			}
		});
	}

	// 网络状态变化通知处理
	@Override
	protected void handReConnect(String status) {
		if (status.equals(XmppConnectionManager.CONNECTED) && XmppConnectionManager.getInstance().getConnection().isConnected()) {
			mNetworkFailedTxt.setVisibility(View.GONE);
		} else {
			mNetworkFailedTxt.setVisibility(View.VISIBLE);
		}
	}

}